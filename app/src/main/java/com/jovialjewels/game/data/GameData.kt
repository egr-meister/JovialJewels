package com.jovialjewels.game.data

import kotlinx.serialization.Serializable

/**
 * Everything the game persists, stored as ONE JSON string in DataStore Preferences.
 * Every field has a default so older/newer JSON always decodes (forward compatibility).
 *
 * quizBest:  category id -> best score (0..10)
 * pairsBest: level number as string ("1".."9") -> best matched pairs in that level
 */
@Serializable
data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val settings: Settings = Settings(),
)

@Serializable
data class Settings(
    val soundEnabled: Boolean = true,
)
