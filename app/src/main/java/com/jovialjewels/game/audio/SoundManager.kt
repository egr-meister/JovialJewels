package com.jovialjewels.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import com.jovialjewels.game.R
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

enum class Sfx(@RawRes val resId: Int) {
    CLICK(R.raw.sfx_click),
    CORRECT(R.raw.sfx_correct),
    WRONG(R.raw.sfx_wrong),
    MATCH(R.raw.sfx_match),
    WIN(R.raw.sfx_win),
    LOSE(R.raw.sfx_lose),
}

/**
 * Short sound effects through SoundPool. Created once in the Application.
 * Replace res/raw/sfx_*.wav (or .ogg with the same names) to change the sounds.
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds = HashMap<Sfx, Int>()
    private val loadedIds: MutableSet<Int> = Collections.newSetFromMap(ConcurrentHashMap())

    @Volatile
    private var enabled: Boolean = true

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedIds.add(sampleId)
        }
        val appContext = context.applicationContext
        Sfx.entries.forEach { sfx ->
            try {
                soundIds[sfx] = soundPool.load(appContext, sfx.resId, 1)
            } catch (e: Exception) {
                Log.w(TAG, "Could not load $sfx", e)
            }
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun isEnabled(): Boolean = enabled

    fun play(sfx: Sfx) {
        if (!enabled) return
        val id = soundIds[sfx] ?: return
        if (id !in loadedIds) return
        soundPool.play(id, VOLUME, VOLUME, 1, 0, 1f)
    }

    private companion object {
        const val TAG = "SoundManager"
        const val MAX_STREAMS = 6
        const val VOLUME = 0.8f
    }
}
