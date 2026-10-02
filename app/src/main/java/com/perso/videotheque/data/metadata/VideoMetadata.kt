package com.perso.videotheque.data.metadata

data class VideoMetadata(
    val title: String?,
    val channelName: String?,
    val thumbnailUrl: String?,
    val durationSeconds: Int?,
    /** false : la vidéo semble supprimée ou privée. */
    val isAvailable: Boolean = true,
)

interface MetadataFetcher {
    /** null si rien n'a pu être récupéré (pas de réseau, site non pris en charge…). */
    suspend fun fetch(url: String): VideoMetadata?
}
