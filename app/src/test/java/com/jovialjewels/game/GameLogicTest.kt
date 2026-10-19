package com.jovialjewels.game

import com.jovialjewels.game.logic.GameLogic
import com.jovialjewels.game.logic.QuizContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameLogicTest {

    @Test
    fun levelTableMatchesSpec() {
        val expected = listOf(
            Triple(2, 2, 60), Triple(3, 2, 70), Triple(4, 2, 70),
            Triple(6, 3, 80), Triple(6, 3, 80), Triple(8, 4, 100),
            Triple(8, 4, 100), Triple(10, 4, 110), Triple(12, 4, 120),
        )
        assertEquals(9, GameLogic.levelCount)
        GameLogic.levels.forEachIndexed { i, cfg ->
            assertEquals(i + 1, cfg.level)
            assertEquals(expected[i], Triple(cfg.pairs, cfg.columns, cfg.seconds))
        }
    }

    @Test
    fun deckHasEveryChosenItemExactlyTwice() {
        GameLogic.levels.forEach { cfg ->
            repeat(20) { seed ->
                val deck = GameLogic.buildDeck(cfg.pairs, Random(seed))
                assertEquals(cfg.pairs * 2, deck.size)
                val counts = deck.groupingBy { it }.eachCount()
                assertEquals(cfg.pairs, counts.size)
                assertTrue(counts.values.all { it == 2 })
                assertTrue(deck.all { it in 0 until GameLogic.ITEM_COUNT })
            }
        }
    }

    @Test
    fun deckClampsPairCount() {
        assertEquals(2, GameLogic.buildDeck(0).size)
        assertEquals(2, GameLogic.buildDeck(-5).size)
        assertEquals(GameLogic.ITEM_COUNT * 2, GameLogic.buildDeck(99).size)
    }

    @Test
    fun deckIsShuffledWithSeed() {
        val a = GameLogic.buildDeck(12, Random(1))
        val b = GameLogic.buildDeck(12, Random(1))
        assertEquals(a, b)
    }

    @Test
    fun levelOneAlwaysUnlocked() {
        assertTrue(GameLogic.isLevelUnlocked(1, emptyMap()))
        assertFalse(GameLogic.isLevelUnlocked(2, emptyMap()))
    }

    @Test
    fun levelUnlocksOnlyAfterPreviousCleared() {
        assertFalse(GameLogic.isLevelUnlocked(2, mapOf("1" to 1)))
        assertTrue(GameLogic.isLevelUnlocked(2, mapOf("1" to 2)))
        assertFalse(GameLogic.isLevelUnlocked(3, mapOf("1" to 2)))
        assertTrue(GameLogic.isLevelUnlocked(3, mapOf("1" to 2, "2" to 3)))
    }

    @Test
    fun invalidLevelsAreNeverUnlockedOrCleared() {
        assertFalse(GameLogic.isLevelUnlocked(0, emptyMap()))
        assertFalse(GameLogic.isLevelUnlocked(10, mapOf("9" to 12)))
        assertFalse(GameLogic.isLevelCleared(42, mapOf("42" to 100)))
        assertNull(GameLogic.levelConfig(-1))
        assertNull(GameLogic.nextLevel(9))
        assertEquals(2, GameLogic.nextLevel(1))
    }

    @Test
    fun completedCountCountsOnlyFullyClearedLevels() {
        assertEquals(0, GameLogic.completedLevelCount(emptyMap()))
        val best = mapOf("1" to 2, "2" to 3, "3" to 3, "4" to 6, "9" to 12, "junk" to 5)
        assertEquals(4, GameLogic.completedLevelCount(best))
        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertEquals(9, GameLogic.completedLevelCount(all))
    }

    @Test
    fun negativeBestIsTreatedAsZero() {
        assertEquals(0, GameLogic.bestPairs(1, mapOf("1" to -3)))
    }

    @Test
    fun formatSeconds() {
        assertEquals("1:00", GameLogic.formatSeconds(60))
        assertEquals("0:05", GameLogic.formatSeconds(5))
        assertEquals("0:00", GameLogic.formatSeconds(-1))
    }

    @Test
    fun quizContentHasThreeCategoriesOfTenWithFourDistinctOptions() {
        assertEquals(3, QuizContent.categories.size)
        assertEquals(3, QuizContent.categories.map { it.id }.toSet().size)
        QuizContent.categories.forEach { c ->
            assertEquals(GameLogic.QUIZ_QUESTIONS_PER_CATEGORY, c.questions.size)
            c.questions.forEach { q ->
                assertEquals(3, q.wrong.size)
                assertEquals(4, (q.wrong + q.correct).toSet().size)
            }
        }
        assertNull(QuizContent.category(null))
        assertNull(QuizContent.category(""))
        assertNull(QuizContent.category("nope"))
    }
}
