package com.jovialjewels.game.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jovialjewels.game.logic.GameLogic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Single DataStore instance for the whole process. A corrupted file is replaced with empty prefs. */
val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jovial_jewels_data",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

/** The only local data source. Stores [GameData] as a single serialized JSON string. */
class GameRepository(private val dataStore: DataStore<Preferences>) {

    val data: Flow<GameData> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Log.w(TAG, "Reading game data failed, using defaults", e)
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { prefs -> GameSerializer.decode(prefs[KEY_GAME_DATA]) }
        .distinctUntilChanged()

    suspend fun saveQuizBest(categoryId: String, score: Int) = update { data ->
        val safe = score.coerceIn(0, GameLogic.QUIZ_QUESTIONS_PER_CATEGORY)
        val current = data.quizBest[categoryId] ?: 0
        if (safe > current) data.copy(quizBest = data.quizBest + (categoryId to safe)) else data
    }

    suspend fun savePairsBest(level: Int, matchedPairs: Int) = update { data ->
        val config = GameLogic.levelConfig(level) ?: return@update data
        val safe = matchedPairs.coerceIn(0, config.pairs)
        val key = level.toString()
        val current = data.pairsBest[key] ?: 0
        if (safe > current) data.copy(pairsBest = data.pairsBest + (key to safe)) else data
    }

    suspend fun setSoundEnabled(enabled: Boolean) = update { data ->
        data.copy(settings = data.settings.copy(soundEnabled = enabled))
    }

    /** Clears all progress (quiz bests, pairs bests, level unlocks). The sound preference is kept. */
    suspend fun resetAll() = update { data ->
        GameData(settings = data.settings)
    }

    private suspend fun update(transform: (GameData) -> GameData) {
        try {
            dataStore.edit { prefs ->
                val current = GameSerializer.decode(prefs[KEY_GAME_DATA])
                prefs[KEY_GAME_DATA] = GameSerializer.encode(transform(current))
            }
        } catch (e: IOException) {
            Log.w(TAG, "Writing game data failed; the game keeps running", e)
        }
    }

    private companion object {
        const val TAG = "GameRepository"
        val KEY_GAME_DATA = stringPreferencesKey("game_data_json")
    }
}
