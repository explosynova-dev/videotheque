package com.perso.videotheque.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VideoDao {

    @Transaction
    @Query("SELECT * FROM videos")
    abstract fun observeAll(): Flow<List<VideoWithTags>>

    @Transaction
    @Query("SELECT * FROM videos WHERE id = :id")
    abstract suspend fun getById(id: Long): VideoWithTags?

    @Insert
    protected abstract suspend fun insert(video: VideoEntity): Long

    @Update
    protected abstract suspend fun update(video: VideoEntity)

    @Query("DELETE FROM video_tags WHERE videoId = :videoId")
    protected abstract suspend fun clearTags(videoId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertTags(refs: List<VideoTagCrossRef>)

    @Query("DELETE FROM videos WHERE id = :id")
    abstract suspend fun delete(id: Long)

    /** Crée (id = 0) ou met à jour la vidéo et remplace ses tags, en une transaction. */
    @Transaction
    open suspend fun save(video: VideoEntity, tagIds: Collection<Long>): Long {
        val id = if (video.id == 0L) insert(video) else video.id.also { update(video) }
        clearTags(id)
        insertTags(tagIds.map { VideoTagCrossRef(id, it) })
        return id
    }
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY id")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Insert
    suspend fun insert(tag: TagEntity): Long
}
