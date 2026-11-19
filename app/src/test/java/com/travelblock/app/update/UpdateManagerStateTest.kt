package com.travelblock.app.update

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateManagerStateTest {
    @Test
    fun downloadUrlTargetsLatestReleaseApk() {
        assertEquals(
            "https://github.com/WxboySuper/TravelBlock/releases/download/latest/TravelBlock.apk",
            UpdateManager.DownloadUrl,
        )
    }
}
