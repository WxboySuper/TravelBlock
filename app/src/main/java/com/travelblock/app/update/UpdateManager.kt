package com.travelblock.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.travelblock.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

private val Context.updateStore by preferencesDataStore("app_updates")

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class Available(val info: UpdateInfo) : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}
class UpdateManager private constructor(context: Context) {
    private val app = context.applicationContext
    private val mutableState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = mutableState.asStateFlow()
    private val mutableLastCheck = MutableStateFlow(0L)
    val lastCheckMillis: StateFlow<Long> = mutableLastCheck.asStateFlow()

    companion object {
        @Volatile private var shared: UpdateManager? = null
        const val DownloadUrl = "https://github.com/WxboySuper/TravelBlock/releases/download/latest/TravelBlock.apk"
        val LastCheckKey = longPreferencesKey("last_check")
        val DismissedBuildKey = intPreferencesKey("dismissed_build")

        fun get(context: Context): UpdateManager = shared ?: synchronized(this) {
            shared ?: UpdateManager(context.applicationContext).also { shared = it }
        }
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

    fun openDownload() {
        app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DownloadUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun openReleasePage(url: String) {
        app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

}

private suspend fun <T> kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences>.firstValue(
    key: androidx.datastore.preferences.core.Preferences.Key<T>, default: T,
): T = first()[key] ?: default
