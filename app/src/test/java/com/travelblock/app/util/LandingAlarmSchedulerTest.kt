package com.travelblock.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LandingAlarmSchedulerTest {
    @Test
    fun exactAlarmUsedBeforeAndroid12() {
        assertTrue(shouldUseExactAlarm(sdkInt = 30, exactAlarmPermissionGranted = false))
    }

    @Test
    fun exactAlarmUsedOnAndroid12WhenPermissionGranted() {
        assertTrue(shouldUseExactAlarm(sdkInt = 31, exactAlarmPermissionGranted = true))
    }

    @Test
    fun inexactAlarmUsedOnAndroid12WhenPermissionMissing() {
        assertFalse(shouldUseExactAlarm(sdkInt = 31, exactAlarmPermissionGranted = false))
    }
}
