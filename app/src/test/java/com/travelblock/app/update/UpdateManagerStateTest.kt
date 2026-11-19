package com.travelblock.app.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerStateTest {
    @Test fun persistedInstallerMarkerSurvivesProcessRestart() {
        assertTrue(shouldRefreshAfterInstallerReturn(true, UpdateUiState.Idle))
    }

    @Test fun installingStateResolvesWhenInstallerReturns() {
        assertTrue(shouldRefreshAfterInstallerReturn(false, UpdateUiState.Installing))
    }

    @Test fun ordinaryForegroundDoesNotUseInstallerRecovery() {
        assertFalse(shouldRefreshAfterInstallerReturn(false, UpdateUiState.Available(updateInfo)))
    }

    private companion object {
        val updateInfo = UpdateInfo(12, "abc", "https://example.com/app.apk", 42, "https://example.com/release", "2026-11-18")
    }
}
