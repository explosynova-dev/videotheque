package com.perso.videotheque.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoQueryTest {

    private val yogaDoux = Tag(1, "Yoga doux", 0)
    private val yin = Tag(2, "Yin yoga", 0)
    private val tuto = Tag(3, "Tuto", 0)

    private fun video(id: Long, minutes: Int?, vararg tags: Tag, title: String = "Vidéo $id") = Video(
        id = id, url = "https://www.youtube.com/watch?v=aaaaaaaaaa$id", title = title, thumbnailUrl = null,
        channelName = null, durationSeconds = minutes?.let { it * 60 }, isAvailable = true,
        tags = tags.toList(), createdAt = id, updatedAt = id,
    )

    private val videos = listOf(
        video(1, 8, yogaDoux),
        video(2, 12, yogaDoux, yin),
        video(3, 18, yin),
        video(4, 22, tuto),
        video(5, 100, yogaDoux),
        video(6, null, yogaDoux),
        video(7, 3, tuto),
    )

    private fun ids(q: VideoQuery) = videos.applyQuery(q).map { it.id }.sorted()

    @Test fun noFilterShowsEverything() {
        assertEquals((1L..7L).toList(), ids(VideoQuery()))
    }

    @Test fun durationRangeIsInclusive() {
        assertEquals(listOf(2L, 3L), ids(VideoQuery(duration = DurationRange(10, 20))))
        assertEquals(listOf(2L), ids(VideoQuery(duration = DurationRange(12, 12))))
    }

    @Test fun durationEndsAreOpen() {
        // Max en butée (90) : inclut aussi les vidéos de plus de 1 h 30.
        assertEquals(listOf(4L, 5L), ids(VideoQuery(duration = DurationRange(20, 90))))
        // Min en butée (5) : inclut aussi les vidéos de moins de 5 min.
        assertEquals(listOf(1L, 7L), ids(VideoQuery(duration = DurationRange(5, 10))))
    }

    @Test fun unknownDurationExcludedOnlyWhenFiltering() {
        assertEquals(true, 6L in ids(VideoQuery()))
        assertEquals(false, 6L in ids(VideoQuery(duration = DurationRange(5, 24))))
    }

    @Test fun tagsMatchAnyByDefault() {
        assertEquals(listOf(1L, 2L, 3L, 5L, 6L), ids(VideoQuery(tagIds = setOf(1, 2))))
    }

    @Test fun tagsMatchAllMode() {
        assertEquals(listOf(2L), ids(VideoQuery(tagIds = setOf(1, 2), tagMatchMode = TagMatchMode.ALL)))
    }

    @Test fun durationAndTagCombined() {
        assertEquals(listOf(2L), ids(VideoQuery(duration = DurationRange(10, 20), tagIds = setOf(1))))
    }

    @Test fun textSearchIgnoresCaseAndAccents() {
        val list = listOf(video(1, 10, title = "Séance énergisante"), video(2, 10, yin, title = "Autre"))
        assertEquals(listOf(1L), list.applyQuery(VideoQuery(text = "ENERGIS")).map { it.id })
        assertEquals(listOf(2L), list.applyQuery(VideoQuery(text = "yin")).map { it.id })
    }

    @Test fun sortOrders() {
        fun sorted(s: SortOrder) = videos.applyQuery(VideoQuery(sort = s)).map { it.id }
        assertEquals(listOf(7L, 6L, 5L, 4L, 3L, 2L, 1L), sorted(SortOrder.NEWEST))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L), sorted(SortOrder.OLDEST))
        assertEquals(listOf(7L, 1L, 2L, 3L, 4L, 5L, 6L), sorted(SortOrder.SHORTEST))
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L, 7L, 6L), sorted(SortOrder.LONGEST))
        val alpha = listOf(video(1, 1, title = "zèbre"), video(2, 1, title = "Écureuil"), video(3, 1, title = "abeille"))
        assertEquals(listOf(3L, 2L, 1L), alpha.applyQuery(VideoQuery(sort = SortOrder.ALPHABETICAL)).map { it.id })
    }
}
