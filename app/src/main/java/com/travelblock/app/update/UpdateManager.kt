package com.travelblock.app.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.travelblock.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

private val Context.updateStore by preferencesDataStore("app_updates")

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class Available(val info: UpdateInfo) : UpdateUiState
    data class Downloading(val ratio: Float) : UpdateUiState
    data object Installing : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

data class DownloadProgress(val received: Long, val expected: Long)

class UpdateManager private constructor(context: Context) {
    private val app = context.applicationContext
    private val mutableState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = mutableState.asStateFlow()
    private val mutableLastCheck = MutableStateFlow(0L)
    val lastCheckMillis: StateFlow<Long> = mutableLastCheck.asStateFlow()
    private var pendingInfo: UpdateInfo? = null
    private var downloadedFile: File? = null

    init { InstallStatusReceiver.manager = this }

    companion object {
        @Volatile private var shared: UpdateManager? = null
        private const val ACTION_INSTALL_STATUS = "com.travelblock.app.UPDATE_INSTALL_STATUS"
        val LastCheckKey = longPreferencesKey("last_check")
        val DismissedBuildKey = intPreferencesKey("dismissed_build")

        fun get(context: Context): UpdateManager = shared ?: synchronized(this) {
            shared ?: UpdateManager(context.applicationContext).also { shared = it }
        }
    }

    suspend fun resumePendingInstall() {
        pendingInfo?.let { install(it) }
    }

    suspend fun check(manual: Boolean = false) {
        if (!manual && BuildConfig.BUILD_NUMBER == 0) return
        mutableState.value = UpdateUiState.Checking
        val checkedAt = System.currentTimeMillis()
        app.updateStore.edit { it[LastCheckKey] = checkedAt }
        mutableLastCheck.value = checkedAt
        runCatching { UpdateChecker.check(BuildConfig.BUILD_NUMBER) }
            .onSuccess { info ->
                val dismissed = app.updateStore.data.firstValue(DismissedBuildKey, 0)
                mutableState.value = when {
                    info == null -> UpdateUiState.UpToDate
                    !manual && info.build == dismissed -> UpdateUiState.Idle
                    else -> UpdateUiState.Available(info)
                }
            }
            .onFailure { error ->
                Log.w("TravelBlockUpdate", "Update check failed", error)
                if (manual) mutableState.value = UpdateUiState.Error("Couldn't check for updates")
                else mutableState.value = UpdateUiState.Idle
            }
    }

    suspend fun checkOnForeground() {
        val last = app.updateStore.data.firstValue(LastCheckKey, 0L)
        if (UpdateChecker.shouldCheck(last, System.currentTimeMillis())) check()
    }

    suspend fun dismiss(build: Int) {
        app.updateStore.edit { it[DismissedBuildKey] = build }
        if ((mutableState.value as? UpdateUiState.Available)?.info?.build == build) mutableState.value = UpdateUiState.Idle
    }

    suspend fun visibleInfo(): UpdateInfo? {
        val info = (mutableState.value as? UpdateUiState.Available)?.info ?: return null
        val dismissed = app.updateStore.data.firstValue(DismissedBuildKey, 0)
        return info.takeIf { it.build != dismissed }
    }

    suspend fun install(info: UpdateInfo) {
        pendingInfo = info
        if (Build.VERSION.SDK_INT >= 26 && !app.packageManager.canRequestPackageInstalls()) {
            mutableState.value = UpdateUiState.Error("Allow installs, then tap Update again")
            app.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${app.packageName}" )).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        runCatching {
            download(info).collect { progress ->
                val ratio = if (progress.expected > 0) progress.received.toFloat() / progress.expected else 0f
                mutableState.value = UpdateUiState.Downloading(ratio)
            }
            installPackage(requireNotNull(downloadedFile))
        }.onFailure { error ->
            Log.w("TravelBlockUpdate", "Update install failed", error)
            openReleasePage(info)
            mutableState.value = UpdateUiState.Error("Update couldn't install. Opening release page.")
        }
    }

    fun download(info: UpdateInfo): Flow<DownloadProgress> = flow {
        val directory = File(app.cacheDir, "updates").apply { mkdirs() }
        directory.listFiles()?.forEach(File::delete)
        val file = File(directory, "TravelBlock-${info.build}.apk")
        val connection = URL(info.apkUrl).openConnection().apply { connectTimeout = 5_000; readTimeout = 5_000 }
        try {
            connection.getInputStream().use { input -> file.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var received = 0L
                val expected = info.sizeBytes.takeIf { it > 0 } ?: connection.contentLengthLong
                emit(DownloadProgress(0, expected))
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    received += count
                    emit(DownloadProgress(received, expected))
                }
                if (info.sizeBytes > 0 && received != info.sizeBytes) error("APK size mismatch")
            } }
            downloadedFile = file
        } catch (error: Throwable) { file.delete(); throw error }
    }.flowOn(Dispatchers.IO)

    private fun installPackage(file: File) {
        val installer = app.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (Build.VERSION.SDK_INT >= 31) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            file.inputStream().use { input -> session.openWrite("TravelBlock.apk", 0, file.length()).use { output -> input.copyTo(output); session.fsync(output) } }
            val intent = Intent(app, InstallStatusReceiver::class.java).setAction(ACTION_INSTALL_STATUS)
            val callback = PendingIntent.getBroadcast(app, sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            mutableState.value = UpdateUiState.Installing
            session.commit(callback.intentSender)
        }
    }

    fun handleInstallStatus(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmation = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java) else @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_INTENT)
                confirmation?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let(context::startActivity)
            }
            PackageInstaller.STATUS_SUCCESS -> mutableState.value = UpdateUiState.Idle
            else -> pendingInfo?.let { info ->
                openReleasePage(info)
                mutableState.value = UpdateUiState.Error("Update was cancelled or failed. Opening release page.")
            }
        }
    }

    private fun openReleasePage(info: UpdateInfo) {
        app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.pageUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

}

private suspend fun <T> kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences>.firstValue(
    key: androidx.datastore.preferences.core.Preferences.Key<T>, default: T,
): T = first()[key] ?: default
