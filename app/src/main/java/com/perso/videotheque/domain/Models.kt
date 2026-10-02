package com.perso.videotheque.domain

data class Tag(
    val id: Long,
    val name: String,
    val createdAt: Long,
)

data class Video(
    val id: Long,
    val url: String,
    val title: String?,
    val thumbnailUrl: String?,
    val channelName: String?,
    /** Durée réelle en secondes, null si inconnue. */
    val durationSeconds: Int?,
    /** false si YouTube indique que la vidéo n'existe plus / est privée. */
    val isAvailable: Boolean,
    val tags: List<Tag>,
    val createdAt: Long,
    val updatedAt: Long,
)
