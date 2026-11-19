package com.travelblock.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

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
    private val downloader by lazy { ApkDownloader(app) }

    companion object {
        @Volatile private var shared: UpdateManager? = null
        val LastCheckKey = longPreferencesKey("last_check")
        val DismissedBuildKey = intPreferencesKey("dismissed_build")
        private val InstallerOpenedKey = booleanPreferencesKey("installer_opened")

        fun get(context: Context): UpdateManager = shared ?: synchronized(this) {
            shared ?: UpdateManager(context.applicationContext).also { shared = it }
        }
    }

    suspend fun onForeground() {
        if (consumeInstallerReturn()) {
            pendingInfo = null
            downloader.deleteDownloadedApks()
            mutableState.value = UpdateUiState.Idle
            check(manual = true)
            return
        }
        val pending = pendingInfo
        if (pending != null && app.packageManager.canRequestPackageInstalls()) {
            install(pending)
            return
        }
        checkOnForeground()
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
                if (manual && error is MissingApkAssetException) {
                    openReleasePage(error.pageUrl)
                    mutableState.value = UpdateUiState.Error("Release APK is missing. Opening release page.")
                } else if (manual) mutableState.value = UpdateUiState.Error("Couldn't check for updates")
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
            openUnknownSourcesSettings(info)
            return
        }
        runCatching {
            download(info).collect { progress ->
                val ratio = if (progress.expected > 0) progress.received.toFloat() / progress.expected else 0f
                mutableState.value = UpdateUiState.Downloading(ratio)
            }
            openInstaller(requireNotNull(downloader.file))
        }.onFailure { error ->
            showInstallFailure(info, error)
        }
    }

    fun download(info: UpdateInfo): Flow<DownloadProgress> = downloader.download(info).onEach { progress ->
        val ratio = if (progress.expected > 0) progress.received.toFloat() / progress.expected else 0f
        mutableState.value = UpdateUiState.Downloading(ratio)
    }

    private suspend fun openInstaller(file: java.io.File) {
        val contentUri = FileProvider.getUriForFile(app, "${app.packageName}.updates", file)
        app.updateStore.edit { it[InstallerOpenedKey] = true }
        mutableState.value = UpdateUiState.Installing
        app.startActivity(createApkInstallIntent(contentUri))
        pendingInfo = null
    }

    private suspend fun openUnknownSourcesSettings(info: UpdateInfo) {
        runCatching {
            mutableState.value = UpdateUiState.Error("Allow installs, then tap Update again")
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${app.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            app.startActivity(intent)
        }.onFailure { error -> showInstallFailure(info, error) }
    }

    private suspend fun consumeInstallerReturn(): Boolean {
        val opened = app.updateStore.data.firstValue(InstallerOpenedKey, false)
        if (opened) app.updateStore.edit { it.remove(InstallerOpenedKey) }
        return shouldRefreshAfterInstallerReturn(opened, mutableState.value)
    }

    private suspend fun showInstallFailure(info: UpdateInfo, error: Throwable) {
        Log.w("TravelBlockUpdate", "Update install failed", error)
        app.updateStore.edit { it.remove(InstallerOpenedKey) }
        runCatching { openReleasePage(info.pageUrl) }
        mutableState.value = UpdateUiState.Error("Update couldn't install. Opening release page.")
    }

    private fun openReleasePage(url: String) {
        app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

internal fun createApkInstallIntent(contentUri: Uri): Intent = Intent(Intent.ACTION_VIEW)
    .setDataAndType(contentUri, "application/vnd.android.package-archive")
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

internal fun shouldRefreshAfterInstallerReturn(
    installerWasOpened: Boolean,
    state: UpdateUiState,
): Boolean = installerWasOpened || state == UpdateUiState.Installing

private suspend fun <T> kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences>.firstValue(
    key: androidx.datastore.preferences.core.Preferences.Key<T>, default: T,
): T = first()[key] ?: default
