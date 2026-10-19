package com.jovialjewels.game.ui

import androidx.annotation.DrawableRes
import com.jovialjewels.game.R

/**
 * Single access point for every image. Files live in res/drawable-nodpi with FIXED names:
 *   bg_main.jpg, logo, mascot_a / mascot_b / mascot_c, btn_menu (ornate plate), btn_plate,
 *   panel_frame, plate_round (level bubbles), icon_back, icon_pause,
 *   item_01 … item_12 (match icons), deco_* (decorative props).
 * Replace a file with the same name to change the art — no code change needed
 * (plate aspect ratios are read from the images at runtime).
 */
object Assets {
    @DrawableRes val bgMain: Int = R.drawable.bg_main
    @DrawableRes val logo: Int = R.drawable.logo

    /** Bright jester in purple & gold. */
    @DrawableRes val mascotA: Int = R.drawable.mascot_a

    /** Bowing purple jester. */
    @DrawableRes val mascotB: Int = R.drawable.mascot_b

    /** Juggling jester with three gems. */
    @DrawableRes val mascotC: Int = R.drawable.mascot_c

    @DrawableRes val btnMenu: Int = R.drawable.btn_menu
    @DrawableRes val btnPlate: Int = R.drawable.btn_plate
    @DrawableRes val panelFrame: Int = R.drawable.panel_frame
    @DrawableRes val plateRound: Int = R.drawable.plate_round
    @DrawableRes val iconBack: Int = R.drawable.icon_back
    @DrawableRes val iconPause: Int = R.drawable.icon_pause

    @DrawableRes val decoScepter: Int = R.drawable.deco_scepter
    @DrawableRes val decoLute: Int = R.drawable.deco_lute
    @DrawableRes val decoShoes: Int = R.drawable.deco_shoes
    @DrawableRes val decoHourglass: Int = R.drawable.deco_hourglass
    @DrawableRes val decoChest: Int = R.drawable.deco_chest
    @DrawableRes val decoFan: Int = R.drawable.deco_fan

    val items: List<Int> = listOf(
        R.drawable.item_01, R.drawable.item_02, R.drawable.item_03, R.drawable.item_04,
        R.drawable.item_05, R.drawable.item_06, R.drawable.item_07, R.drawable.item_08,
        R.drawable.item_09, R.drawable.item_10, R.drawable.item_11, R.drawable.item_12,
    )

    /** Text alternatives for the 12 match icons (each has a distinct silhouette, not just a color). */
    val itemNames: List<String> = listOf(
        "Ruby", "Sapphire", "Emerald", "Amethyst",
        "Star brooch", "Golden crown", "Jester mask", "Jester cap",
        "Golden bell", "Jeweled goblet", "Jester key", "Carnival drum",
    )

    /** Main mascots, in stage order. */
    val mascots: List<Int> = listOf(mascotA, mascotC, mascotB)

    @DrawableRes
    fun item(index: Int): Int = items[index.mod(items.size)]

    fun itemName(index: Int): String = itemNames[index.mod(itemNames.size)]

    /** Emblem for a quiz difficulty id (falls back to the scepter). */
    @DrawableRes
    fun difficultyEmblem(categoryId: String): Int = when (categoryId) {
        "easy" -> decoFan
        "medium" -> decoLute
        else -> decoScepter
    }
}
