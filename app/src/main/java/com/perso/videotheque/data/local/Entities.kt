package com.perso.videotheque.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String?,
    val thumbnailUrl: String?,
    val channelName: String?,
    val durationSeconds: Int?,
    val isAvailable: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "video_tags",
    primaryKeys = ["videoId", "tagId"],
    indices = [Index("tagId")],
    foreignKeys = [
        ForeignKey(VideoEntity::class, ["id"], ["videoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TagEntity::class, ["id"], ["tagId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class VideoTagCrossRef(val videoId: Long, val tagId: Long)

data class VideoWithTags(
    @Embedded val video: VideoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(VideoTagCrossRef::class, parentColumn = "videoId", entityColumn = "tagId"),
    )
    val tags: List<TagEntity>,
)
