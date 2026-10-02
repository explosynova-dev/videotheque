package com.perso.videotheque.data.repository

import com.perso.videotheque.data.local.TagDao
import com.perso.videotheque.data.local.TagEntity
import com.perso.videotheque.data.local.VideoDao
import com.perso.videotheque.data.local.VideoEntity
import com.perso.videotheque.data.local.VideoWithTags
import com.perso.videotheque.domain.Tag
import com.perso.videotheque.domain.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Point d'accès unique aux vidéos et aux tags enregistrés. */
class VideoRepository(
    private val videoDao: VideoDao,
    private val tagDao: TagDao,
) {
    val videos: Flow<List<Video>> = videoDao.observeAll().map { list -> list.map { it.toDomain() } }

    val tags: Flow<List<Tag>> = tagDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getVideo(id: Long): Video? = videoDao.getById(id)?.toDomain()

    suspend fun save(video: Video): Long {
        val entity = VideoEntity(
            id = video.id,
            url = video.url,
            title = video.title,
            thumbnailUrl = video.thumbnailUrl,
            channelName = video.channelName,
            durationSeconds = video.durationSeconds,
            isAvailable = video.isAvailable,
            createdAt = video.createdAt,
            updatedAt = video.updatedAt,
        )
        return videoDao.save(entity, video.tags.map { it.id })
    }

    suspend fun delete(id: Long) = videoDao.delete(id)

    /** Crée le tag, ou renvoie l'existant si le nom existe déjà (sans tenir compte de la casse). */
    suspend fun createTag(name: String): Tag {
        val clean = name.trim().replace(Regex("""\s+"""), " ")
        tagDao.findByName(clean)?.let { return it.toDomain() }
        val entity = TagEntity(name = clean, createdAt = System.currentTimeMillis())
        return entity.copy(id = tagDao.insert(entity)).toDomain()
    }

    suspend fun findTag(name: String): Tag? = tagDao.findByName(name)?.toDomain()

    /** Supprime le tag et le retire des vidéos, sans supprimer de vidéo. */
    suspend fun deleteTag(id: Long) = tagDao.delete(id)
}

private fun TagEntity.toDomain() = Tag(id, name, createdAt)

private fun VideoWithTags.toDomain() = Video(
    id = video.id,
    url = video.url,
    title = video.title,
    thumbnailUrl = video.thumbnailUrl,
    channelName = video.channelName,
    durationSeconds = video.durationSeconds,
    isAvailable = video.isAvailable,
    tags = tags.sortedBy { it.id }.map { it.toDomain() },
    createdAt = video.createdAt,
    updatedAt = video.updatedAt,
)
