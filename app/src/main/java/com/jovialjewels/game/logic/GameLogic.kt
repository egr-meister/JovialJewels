package com.jovialjewels.game.logic

import kotlin.random.Random

/** One Matching Pairs level: how many pairs, grid columns and seconds on the clock. */
data class LevelConfig(
    val level: Int,
    val pairs: Int,
    val columns: Int,
    val seconds: Int,
)

/** Pure, context-free game rules. No Android types — fully unit-testable. */
object GameLogic {

    const val ITEM_COUNT = 12
    const val QUIZ_QUESTIONS_PER_CATEGORY = 10

    val levels: List<LevelConfig> = listOf(
        LevelConfig(level = 1, pairs = 2, columns = 2, seconds = 60),
        LevelConfig(level = 2, pairs = 3, columns = 2, seconds = 70),
        LevelConfig(level = 3, pairs = 4, columns = 2, seconds = 70),
        LevelConfig(level = 4, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 5, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 6, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 7, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 8, pairs = 10, columns = 4, seconds = 110),
        LevelConfig(level = 9, pairs = 12, columns = 4, seconds = 120),
    )

    val levelCount: Int get() = levels.size

    fun levelConfig(level: Int): LevelConfig? = levels.firstOrNull { it.level == level }

    /**
     * Builds a shuffled deck of item indices (0 until [ITEM_COUNT]) where `pairs` distinct
     * items appear exactly twice. `pairs` is clamped to 1..[ITEM_COUNT].
     */
    fun buildDeck(pairs: Int, random: Random = Random.Default): List<Int> {
        val safePairs = pairs.coerceIn(1, ITEM_COUNT)
        val chosen = (0 until ITEM_COUNT).shuffled(random).take(safePairs)
        return (chosen + chosen).shuffled(random)
    }

    fun isMatch(firstItem: Int, secondItem: Int): Boolean = firstItem == secondItem

    fun bestPairs(level: Int, pairsBest: Map<String, Int>): Int =
        (pairsBest[level.toString()] ?: 0).coerceAtLeast(0)

    /** A level is cleared once all of its pairs have been matched in one run. */
    fun isLevelCleared(level: Int, pairsBest: Map<String, Int>): Boolean {
        val config = levelConfig(level) ?: return false
        return bestPairs(level, pairsBest) >= config.pairs
    }

    /** Level 1 is always unlocked; every other level unlocks after the previous one is cleared. */
    fun isLevelUnlocked(level: Int, pairsBest: Map<String, Int>): Boolean = when {
        levelConfig(level) == null -> false
        level == levels.first().level -> true
        else -> isLevelCleared(level - 1, pairsBest)
    }

    fun completedLevelCount(pairsBest: Map<String, Int>): Int =
        levels.count { isLevelCleared(it.level, pairsBest) }

    fun nextLevel(level: Int): Int? = levelConfig(level + 1)?.level

    fun formatSeconds(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val minutes = safe / 60
        val seconds = safe % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}
