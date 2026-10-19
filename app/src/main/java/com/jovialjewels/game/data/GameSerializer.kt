package com.jovialjewels.game.data

import kotlinx.serialization.json.Json

/**
 * Context-free JSON encode/decode for [GameData] so it can be unit-tested on the JVM.
 * Decoding NEVER throws: null, blank, corrupted or wrongly-typed JSON yields defaults.
 */
object GameSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    fun encode(data: GameData): String = json.encodeToString(GameData.serializer(), data)

    fun decode(raw: String?): GameData {
        if (raw.isNullOrBlank()) return GameData()
        return try {
            sanitize(json.decodeFromString(GameData.serializer(), raw))
        } catch (e: Exception) {
            GameData()
        }
    }

    /** Drops impossible values (negative scores) that hand-edited or legacy JSON could contain. */
    private fun sanitize(data: GameData): GameData = data.copy(
        quizBest = data.quizBest.filterValues { it >= 0 },
        pairsBest = data.pairsBest.filterValues { it >= 0 },
    )
}
