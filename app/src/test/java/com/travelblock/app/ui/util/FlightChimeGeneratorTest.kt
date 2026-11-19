package com.travelblock.app.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightChimeGeneratorTest {
    @Test
    fun generatedCuesHaveExpectedLengthAndFadedEdges() {
        val samples = FlightChimeGenerator.generate(FlightAudioCue.FlightStarted)

        assertEquals((FlightChimeGenerator.SampleRate * 0.72).toInt(), samples.size)
        assertTrue(kotlin.math.abs(samples.first().toInt()) <= 1)
        assertTrue(kotlin.math.abs(samples.last().toInt()) <= 1)
    }

    @Test
    fun generatedCuesStayWithinPcmRangeWithoutClipping() {
        FlightAudioCue.entries.forEach { cue ->
            val samples = FlightChimeGenerator.generate(cue)
            assertTrue(samples.isNotEmpty())
            assertTrue(samples.maxOf { kotlin.math.abs(it.toInt()) } < Short.MAX_VALUE)
        }
    }

    @Test
    fun announcementTextMatchesFlightStages() {
        assertEquals("Now boarding flight TB443 to Tulsa at gate L4", FlightAnnouncements.text(FlightAudioCue.GateCleared))
        assertEquals("Cabin crew, prepare for takeoff", FlightAnnouncements.text(FlightAudioCue.FlightStarted))
        assertEquals("Cabin crew, prepare for landing", FlightAnnouncements.text(FlightAudioCue.PrepareForLanding))
        assertEquals("Welcome to Tulsa. Local time is 4:53 PM.", FlightAnnouncements.text(FlightAudioCue.FlightArrived))
    }
}
