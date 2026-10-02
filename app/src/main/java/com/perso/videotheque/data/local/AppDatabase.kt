package com.perso.videotheque.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.perso.videotheque.domain.BodyKeywords

@Database(
    entities = [VideoEntity::class, TagEntity::class, VideoTagCrossRef::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun tagDao(): TagDao

    companion object {
        val DEFAULT_TAGS = listOf(
            "Yoga sportif", "Yoga dynamique", "Yoga doux", "Yin yoga", "Yin yoga profond",
            "Tuto", "Wingfoil", "KiteSurf", "Astuce", "Musique",
        ) + BodyKeywords.ALL + "Autres"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "videotheque.db")
                .addCallback(object : Callback() {
                    // Tags de départ : ajoutés une seule fois, à la création de la base,
                    // pour qu'un tag supprimé depuis l'app ne réapparaisse pas.
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        val now = System.currentTimeMillis()
                        DEFAULT_TAGS.forEach { name ->
                            db.insert("tags", SQLiteDatabase.CONFLICT_IGNORE, ContentValues().apply {
                                put("name", name)
                                put("createdAt", now)
                            })
                        }
                    }
                })
                .build()
    }
}
