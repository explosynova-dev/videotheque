package com.perso.videotheque.data.metadata

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubePageParserTest {

    @Test fun readsDurationFromVideoDetails() {
        val html = """
            <meta name="title" content="Yoga doux &amp; respiration">
            {"lengthSeconds":"60"}
            var ytInitialPlayerResponse = {"playabilityStatus":{"status":"OK"},
            "videoDetails":{"videoId":"abc","title":"x","lengthSeconds":"1125","author":"y"},
            "microformat":{"ownerChannelName":"Studio été"}}
        """.trimIndent()
        val info = YouTubePageParser.parse(html)
        assertEquals(1125, info.durationSeconds)
        assertEquals("Yoga doux & respiration", info.title)
        assertEquals("Studio été", info.channelName)
        assertTrue(info.isPlayable)
    }

    @Test fun fallsBackToIsoDuration() {
        val info = YouTubePageParser.parse("""<meta itemprop="duration" content="PT12M34S">""")
        assertEquals(754, info.durationSeconds)
    }

    @Test fun detectsUnavailableVideo() {
        val info = YouTubePageParser.parse("""{"playabilityStatus":{"status":"ERROR","reason":"Video unavailable"}}""")
        assertFalse(info.isPlayable)
        assertNull(info.durationSeconds)
    }
}
