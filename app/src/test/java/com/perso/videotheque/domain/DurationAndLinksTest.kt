package com.perso.videotheque.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DurationAndLinksTest {

    @Test fun formatsDuration() {
        assertEquals("18 min 45 s", DurationFormat.format(1125))
        assertEquals("08 min 20 s", DurationFormat.format(500))
        assertEquals("00 min 45 s", DurationFormat.format(45))
        assertEquals("1 h 02 min 05 s", DurationFormat.format(3725))
    }

    @Test fun parsesManualDuration() {
        assertEquals(1125, DurationFormat.parseInput("18:45"))
        assertEquals(720, DurationFormat.parseInput("12"))
        assertEquals(3725, DurationFormat.parseInput("1:02:05"))
        assertNull(DurationFormat.parseInput("12:75"))
        assertNull(DurationFormat.parseInput("abc"))
        assertNull(DurationFormat.parseInput(""))
        assertEquals("18:45", DurationFormat.toInput(1125))
        assertEquals("1:02:05", DurationFormat.toInput(3725))
    }

    @Test fun parsesIsoDuration() {
        assertEquals(1125, DurationFormat.parseIso8601("PT18M45S"))
        assertEquals(3600, DurationFormat.parseIso8601("PT1H"))
        assertNull(DurationFormat.parseIso8601("PT"))
    }

    @Test fun extractsYouTubeIds() {
        val id = "dQw4w9WgXcQ"
        listOf(
            "https://www.youtube.com/watch?v=$id",
            "https://youtube.com/watch?feature=share&v=$id&t=10",
            "https://m.youtube.com/watch?v=$id",
            "https://youtu.be/$id?si=AbCdEf",
            "https://www.youtube.com/shorts/$id",
            "https://www.youtube.com/embed/$id",
            "https://www.youtube.com/live/$id?feature=share",
            "https://music.youtube.com/watch?v=$id",
        ).forEach { assertEquals(it, id, VideoLinks.youTubeId(it)) }
        assertNull(VideoLinks.youTubeId("https://vimeo.com/123456"))
        assertNull(VideoLinks.youTubeId("https://www.youtube.com/watch?v=short"))
        assertEquals("https://www.youtube.com/watch?v=$id", VideoLinks.normalize("https://youtu.be/$id?si=x"))
    }

    @Test fun extractsUrlFromSharedText() {
        assertEquals("https://youtu.be/dQw4w9WgXcQ?si=x", VideoLinks.extractUrl("Regarde ça : https://youtu.be/dQw4w9WgXcQ?si=x"))
        assertNull(VideoLinks.extractUrl("pas de lien"))
    }

    @Test fun validatesUrls() {
        assertTrue(VideoLinks.isValidUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(VideoLinks.isValidUrl("http://vimeo.com/1"))
        assertFalse(VideoLinks.isValidUrl("youtube"))
        assertFalse(VideoLinks.isValidUrl("ftp://site.com/x"))
        assertFalse(VideoLinks.isValidUrl("https://localhost"))
        assertFalse(VideoLinks.isValidUrl("https://exa mple.com"))
    }
}
