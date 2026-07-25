package com.hydrogen.padzero

import android.content.Context
import com.hydrogen.padzero.data.LocalLibraryRepository
import com.hydrogen.padzero.data.SettingsRepository
import com.hydrogen.padzero.player.PlaybackEngine

object AppGraph {
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var libraryRepository: LocalLibraryRepository
        private set
    lateinit var playbackEngine: PlaybackEngine
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        settingsRepository = SettingsRepository(appContext)
        libraryRepository = LocalLibraryRepository(appContext)
        playbackEngine = PlaybackEngine(appContext)
    }
}
