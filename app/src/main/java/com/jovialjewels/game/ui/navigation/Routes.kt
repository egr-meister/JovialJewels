package com.jovialjewels.game.ui.navigation

import kotlinx.serialization.Serializable

/*
 * Type-safe Navigation Compose routes. Every argument has a default so a route can always be
 * decoded; screens validate the values and show a friendly fallback for anything invalid.
 */

@Serializable
data object MainRoute

@Serializable
data object MenuRoute

@Serializable
data object QuizCategoriesRoute

@Serializable
data class QuizRoute(val categoryId: String = "")

@Serializable
data class QuizResultsRoute(val categoryId: String = "", val score: Int = -1)

@Serializable
data object LevelsRoute

@Serializable
data class PairsRoute(val level: Int = 0)

@Serializable
data class PairsResultsRoute(val level: Int = 0, val matched: Int = -1, val won: Boolean = false)

@Serializable
data object SummaryRoute

@Serializable
data object SettingsRoute

@Serializable
data object RulesRoute
