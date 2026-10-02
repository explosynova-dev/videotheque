package com.perso.videotheque.data.metadata

import com.perso.videotheque.domain.VideoLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Sans clé API :
 *  - oEmbed YouTube pour le titre, la chaîne et la miniature ;
 *  - la page de la vidéo pour la durée réelle.
 * Chaque source peut échouer indépendamment : on garde ce qui a été obtenu.
 */
class YouTubeMetadataFetcher : MetadataFetcher {

    private sealed interface OEmbed {
        data class Found(val title: String?, val author: String?, val thumbnail: String?) : OEmbed
        data object NotFound : OEmbed
    }

    override suspend fun fetch(url: String): VideoMetadata? {
        val id = VideoLinks.youTubeId(url) ?: return null
        val watchUrl = "https://www.youtube.com/watch?v=$id"
        return withContext(Dispatchers.IO) {
            coroutineScope {
                val oEmbed = async { runCatching { fetchOEmbed(watchUrl) }.getOrNull() }
                val page = async { runCatching { fetchPage(watchUrl) }.getOrNull() }
                combine(id, oEmbed.await(), page.await())
            }
        }
    }

    private fun combine(id: String, oEmbed: OEmbed?, page: YouTubePageParser.PageInfo?): VideoMetadata? {
        if (oEmbed == null && page == null) return null
        val found = oEmbed as? OEmbed.Found
        val unavailable = page?.isPlayable == false ||
            (oEmbed == OEmbed.NotFound && page?.durationSeconds == null)
        return VideoMetadata(
            title = found?.title ?: page?.title,
            channelName = found?.author ?: page?.channelName,
            thumbnailUrl = found?.thumbnail ?: VideoLinks.youTubeThumbnail(id),
            durationSeconds = page?.durationSeconds,
            isAvailable = !unavailable,
        )
    }

    private fun fetchOEmbed(watchUrl: String): OEmbed {
        val endpoint = "https://www.youtube.com/oembed?format=json&url=" + URLEncoder.encode(watchUrl, "UTF-8")
        val (code, body) = get(endpoint)
        if (code in 400..404) return OEmbed.NotFound
        val json = JSONObject(body ?: error("HTTP $code"))
        return OEmbed.Found(
            title = json.optString("title").ifBlank { null },
            author = json.optString("author_name").ifBlank { null },
            thumbnail = json.optString("thumbnail_url").ifBlank { null },
        )
    }

    private fun fetchPage(watchUrl: String): YouTubePageParser.PageInfo {
        val (code, body) = get(watchUrl)
        return YouTubePageParser.parse(body ?: error("HTTP $code"))
    }

    private fun get(url: String): Pair<Int, String?> {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8")
            // Évite la page de consentement aux cookies servie en Europe.
            connection.setRequestProperty("Cookie", "CONSENT=YES+cb; SOCS=CAI")
            val code = connection.responseCode
            val body = if (code in 200..299) connection.inputStream.bufferedReader().use { it.readText() } else null
            code to body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0 Safari/537.36"
    }
}
