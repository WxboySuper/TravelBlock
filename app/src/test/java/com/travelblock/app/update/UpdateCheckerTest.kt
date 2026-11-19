package com.travelblock.app.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {
    @Test fun newerBuildIsReturned() {
        assertEquals(12, UpdateChecker.parse(releaseJson(12), 11)?.build)
    }

    @Test fun olderAndEqualBuildsAreIgnored() {
        assertNull(UpdateChecker.parse(releaseJson(11), 11))
        assertNull(UpdateChecker.parse(releaseJson(10), 11))
    }

    @Test fun missingFirstLineAndAssetAreIgnored() {
        assertNull(UpdateChecker.parse(releaseJson(12, body = "Release details"), 11))
        assertNull(UpdateChecker.parse(releaseJson(12, asset = false), 11))
    }

    @Test fun malformedJsonIsIgnored() {
        assertNull(UpdateChecker.parse("{bad json", 11))
    }

    @Test fun throttleWaitsFifteenMinutes() {
        assertFalse(UpdateChecker.shouldCheck(1_000L, 1_000L + 899_999L))
        assertTrue(UpdateChecker.shouldCheck(1_000L, 1_000L + 900_000L))
    }

    private fun releaseJson(build: Int, body: String = "build=$build sha=abc123 published=2026-11-18T12:00:00Z", asset: Boolean = true) =
        """{"body":"$body","html_url":"https://github.com/WxboySuper/TravelBlock/releases/tag/latest","assets":${if (asset) "[{\"name\":\"TravelBlock.apk\",\"browser_download_url\":\"https://example.com/app.apk\",\"size\":42}]" else "[]"}}"""
}
