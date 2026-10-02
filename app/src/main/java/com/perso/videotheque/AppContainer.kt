package com.perso.videotheque

import android.app.Application
import android.content.Context
import com.perso.videotheque.data.local.AppDatabase
import com.perso.videotheque.data.metadata.MetadataFetcher
import com.perso.videotheque.data.metadata.YouTubeMetadataFetcher
import com.perso.videotheque.data.repository.VideoRepository

/** Dépendances partagées de l'application (injection manuelle, volontairement simple). */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)
    val repository = VideoRepository(database.videoDao(), database.tagDao())
    val metadataFetcher: MetadataFetcher = YouTubeMetadataFetcher()
}

class VideothequeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
