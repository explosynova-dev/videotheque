package com.perso.videotheque.domain

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

enum class SortOrder(val label: String) {
    NEWEST("Plus récentes"),
    OLDEST("Plus anciennes"),
    SHORTEST("Durée la plus courte"),
    LONGEST("Durée la plus longue"),
    ALPHABETICAL("Alphabétique"),
}

/** ANY : au moins un des tags sélectionnés (par défaut). ALL : tous les tags. */
enum class TagMatchMode { ANY, ALL }

/**
 * Plage de durée en minutes. Les bornes extrêmes sont ouvertes :
 * 5 = "5 min ou moins", 25 = "25 min ou plus", pour ne jamais masquer une vidéo
 * plus courte ou plus longue quand le curseur est en butée.
 */
data class DurationRange(val minMinutes: Int = MIN, val maxMinutes: Int = MAX) {
    val isActive get() = minMinutes > MIN || maxMinutes < MAX

    fun matches(seconds: Int?): Boolean {
        if (!isActive) return true
        if (seconds == null) return false
        if (minMinutes > MIN && seconds < minMinutes * 60) return false
        if (maxMinutes < MAX && seconds > maxMinutes * 60) return false
        return true
    }

    companion object {
        const val MIN = 5
        const val MAX = 25
    }
}

data class VideoQuery(
    val text: String = "",
    val duration: DurationRange = DurationRange(),
    val tagIds: Set<Long> = emptySet(),
    val tagMatchMode: TagMatchMode = TagMatchMode.ANY,
    val sort: SortOrder = SortOrder.NEWEST,
) {
    val hasFilters get() = text.isNotBlank() || duration.isActive || tagIds.isNotEmpty()
}

fun List<Video>.applyQuery(query: VideoQuery): List<Video> {
    val needle = query.text.normalized()
    return filter { video ->
        query.duration.matches(video.durationSeconds) &&
            video.matchesTags(query.tagIds, query.tagMatchMode) &&
            (needle.isEmpty() || video.searchableText().contains(needle))
    }.sortedWith(query.sort.comparator())
}

private fun Video.matchesTags(ids: Set<Long>, mode: TagMatchMode): Boolean {
    if (ids.isEmpty()) return true
    val own = tags.mapTo(HashSet()) { it.id }
    return when (mode) {
        TagMatchMode.ANY -> ids.any { it in own }
        TagMatchMode.ALL -> own.containsAll(ids)
    }
}

private fun Video.searchableText() =
    listOfNotNull(title, channelName, url).plus(tags.map { it.name }).joinToString(" ").normalized()

private fun String.normalized() =
    Normalizer.normalize(trim().lowercase(Locale.FRENCH), Normalizer.Form.NFD)
        .replace(Regex("""\p{Mn}+"""), "")

private val collator = Collator.getInstance(Locale.FRENCH).apply { strength = Collator.PRIMARY }

private fun SortOrder.comparator(): Comparator<Video> = when (this) {
    SortOrder.NEWEST -> compareByDescending { it.createdAt }
    SortOrder.OLDEST -> compareBy { it.createdAt }
    // Les durées inconnues restent toujours en fin de liste.
    SortOrder.SHORTEST -> compareBy<Video, Int?>(nullsLast()) { it.durationSeconds }
    SortOrder.LONGEST -> compareBy<Video, Int?>(nullsLast(reverseOrder())) { it.durationSeconds }
    SortOrder.ALPHABETICAL -> Comparator { a, b -> collator.compare(a.title ?: a.url, b.title ?: b.url) }
}
