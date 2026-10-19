package com.jovialjewels.game

import android.app.Application
import com.jovialjewels.game.audio.SoundManager
import com.jovialjewels.game.data.GameRepository
import com.jovialjewels.game.data.gameDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Creates the single repository and sound manager (no DI framework). */
class JovialJewelsApp : Application() {

    lateinit var repository: GameRepository
        private set

    lateinit var soundManager: SoundManager
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        repository = GameRepository(gameDataStore)
        soundManager = SoundManager(this)

        // Sync the persisted sound toggle into SoundManager at startup and live afterwards.
        appScope.launch {
            repository.data
                .map { it.settings.soundEnabled }
                .distinctUntilChanged()
                .collect { soundManager.setEnabled(it) }
        }
    }
}
