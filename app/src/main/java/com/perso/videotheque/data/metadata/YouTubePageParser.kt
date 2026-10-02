package com.perso.videotheque.data.metadata

import com.perso.videotheque.domain.DurationFormat

/** Extrait les informations utiles du HTML d'une page "watch" YouTube. */
object YouTubePageParser {

    data class PageInfo(
        val title: String?,
        val channelName: String?,
        val durationSeconds: Int?,
        val isPlayable: Boolean,
    )

    private val lengthSeconds = Regex(""""lengthSeconds":"(\d+)"""")
    private val isoDuration = Regex("""itemprop="duration" content="(PT[^"]+)"""")
    private val approxDurationMs = Regex(""""approxDurationMs":"(\d+)"""")
    private val metaTitle = Regex("""<meta name="title" content="([^"]*)"""")
    private val ownerChannel = Regex(""""ownerChannelName":"((?:[^"\\]|\\.)*)"""")
    private val unplayable = Regex(""""playabilityStatus":\{"status":"(ERROR|LOGIN_REQUIRED|UNPLAYABLE)"""")

    fun parse(html: String): PageInfo {
        // On cherche après "videoDetails" pour ne pas lire la durée d'une vidéo suggérée.
        val details = html.indexOf("\"videoDetails\":").takeIf { it >= 0 }?.let { html.substring(it) }
        val duration = details?.let { lengthSeconds.find(it)?.groupValues?.get(1)?.toIntOrNull() }
            ?: isoDuration.find(html)?.groupValues?.get(1)?.let(DurationFormat::parseIso8601)
            ?: approxDurationMs.find(html)?.groupValues?.get(1)?.toLongOrNull()?.let { (it / 1000).toInt() }

        val title = metaTitle.find(html)?.groupValues?.get(1)?.let(::unescapeHtml)?.takeIf { it.isNotBlank() }
        val channel = ownerChannel.find(html)?.groupValues?.get(1)?.let(::unescapeJson)?.takeIf { it.isNotBlank() }

        return PageInfo(title, channel, duration?.takeIf { it > 0 }, isPlayable = !unplayable.containsMatchIn(html))
    }

    private fun unescapeHtml(s: String) = s
        .replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<")
        .replace("&gt;", ">").replace("&amp;", "&")

    private fun unescapeJson(s: String) =
        Regex("""\\u([0-9a-fA-F]{4})|\\(.)""").replace(s) { m ->
            val hex = m.groupValues[1]
            if (hex.isNotEmpty()) hex.toInt(16).toChar().toString()
            else when (val c = m.groupValues[2]) {
                "n" -> "\n"
                "t" -> "\t"
                else -> c
            }
        }
}
