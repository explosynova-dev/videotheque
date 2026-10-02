package com.perso.videotheque.domain

import java.net.URI

object VideoLinks {

    private val urlInText = Regex("""https?://\S+""")
    private val youTubeId = Regex("""^[A-Za-z0-9_-]{11}$""")

    /** Extrait le premier lien d'un texte partagé (ex. "Titre https://youtu.be/abc"). */
    fun extractUrl(text: String): String? =
        urlInText.find(text)?.value?.trimEnd('.', ',', ')', ']', '"', '\'')

    /** Lien http(s) avec un hôte : sinon "Ce lien ne semble pas être valide". */
    fun isValidUrl(url: String): Boolean = runCatching {
        val uri = URI(url.trim())
        (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank() && uri.host.contains('.')
    }.getOrDefault(false)

    /** Identifiant YouTube (11 caractères) pour watch, youtu.be, shorts, embed, live. */
    fun youTubeId(url: String): String? {
        val uri = runCatching { URI(url.trim()) }.getOrNull() ?: return null
        val host = uri.host?.lowercase()?.removePrefix("www.")?.removePrefix("m.")?.removePrefix("music.") ?: return null
        val segments = uri.path.orEmpty().split('/').filter { it.isNotEmpty() }
        val id = when (host) {
            "youtu.be" -> segments.firstOrNull()
            "youtube.com", "youtube-nocookie.com" -> when (segments.firstOrNull()) {
                "watch" -> queryParam(uri.rawQuery, "v")
                "shorts", "embed", "live", "v" -> segments.getOrNull(1)
                else -> null
            }
            else -> null
        }
        return id?.takeIf { youTubeId.matches(it) }
    }

    /** Lien YouTube normalisé, ou le lien tel quel pour les autres sites. */
    fun normalize(url: String): String =
        youTubeId(url)?.let { "https://www.youtube.com/watch?v=$it" } ?: url.trim()

    fun youTubeThumbnail(id: String) = "https://i.ytimg.com/vi/$id/hqdefault.jpg"

    private fun queryParam(query: String?, name: String): String? =
        query?.split('&')?.map { it.split('=', limit = 2) }
            ?.firstOrNull { it[0] == name }?.getOrNull(1)
}
