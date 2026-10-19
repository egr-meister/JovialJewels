package com.jovialjewels.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.jovialjewels.game.ui.components.LocalSoundManager
import com.jovialjewels.game.ui.navigation.AppNavHost
import com.jovialjewels.game.ui.theme.JovialJewelsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as JovialJewelsApp
        setContent {
            JovialJewelsTheme {
                CompositionLocalProvider(LocalSoundManager provides app.soundManager) {
                    AppNavHost(
                        repository = app.repository,
                        soundManager = app.soundManager,
                        onExit = { finish() },
                    )
                }
            }
        }
    }
}
