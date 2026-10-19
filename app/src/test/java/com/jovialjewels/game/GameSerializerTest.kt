package com.jovialjewels.game

import com.jovialjewels.game.data.GameData
import com.jovialjewels.game.data.GameSerializer
import com.jovialjewels.game.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSerializerTest {

    private val defaults = GameData()

    @Test
    fun nullEmptyAndBlankDecodeToDefaults() {
        assertEquals(defaults, GameSerializer.decode(null))
        assertEquals(defaults, GameSerializer.decode(""))
        assertEquals(defaults, GameSerializer.decode("   \n"))
    }

    @Test
    fun corruptedJsonDecodesToDefaults() {
        listOf("{", "not json", "{\"quizBest\":", "[1,2,3]", "null", "42", "\"text\"", "{\"quizBest\":\"oops\"}")
            .forEach { assertEquals("input: $it", defaults, GameSerializer.decode(it)) }
    }

    @Test
    fun emptyObjectUsesAllDefaults() {
        val data = GameSerializer.decode("{}")
        assertEquals(defaults, data)
        assertTrue(data.settings.soundEnabled)
    }

    @Test
    fun unknownKeysAreIgnored() {
        val json = """{"quizBest":{"gem_lore":7},"futureFeature":{"x":1},"settings":{"soundEnabled":false,"vibration":true}}"""
        val data = GameSerializer.decode(json)
        assertEquals(7, data.quizBest["gem_lore"])
        assertFalse(data.settings.soundEnabled)
    }

    @Test
    fun missingFieldsFallBackToDefaults() {
        val data = GameSerializer.decode("""{"pairsBest":{"1":2}}""")
        assertEquals(mapOf("1" to 2), data.pairsBest)
        assertTrue(data.quizBest.isEmpty())
        assertEquals(Settings(), data.settings)
    }

    @Test
    fun nullValuesForNonNullFieldsFallBackToDefaults() {
        val data = GameSerializer.decode("""{"quizBest":null,"settings":null}""")
        assertEquals(defaults, data)
    }

    @Test
    fun negativeScoresAreDropped() {
        val data = GameSerializer.decode("""{"quizBest":{"a":-1,"b":3},"pairsBest":{"1":-9}}""")
        assertEquals(mapOf("b" to 3), data.quizBest)
        assertTrue(data.pairsBest.isEmpty())
    }

    @Test
    fun roundTripKeepsEverything() {
        val data = GameData(
            quizBest = mapOf("gem_lore" to 10, "color_sparkle" to 4),
            pairsBest = mapOf("1" to 2, "6" to 5),
            settings = Settings(soundEnabled = false),
        )
        assertEquals(data, GameSerializer.decode(GameSerializer.encode(data)))
    }
}
