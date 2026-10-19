package com.jovialjewels.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jovialjewels.game.audio.SoundManager
import com.jovialjewels.game.data.GameRepository
import com.jovialjewels.game.logic.GameLogic
import com.jovialjewels.game.logic.QuizContent
import com.jovialjewels.game.ui.components.FallbackScreen
import com.jovialjewels.game.ui.screens.LevelsScreen
import com.jovialjewels.game.ui.screens.LoadingScreen
import com.jovialjewels.game.ui.screens.MainScreen
import com.jovialjewels.game.ui.screens.MenuScreen
import com.jovialjewels.game.ui.screens.PairsResultsScreen
import com.jovialjewels.game.ui.screens.PairsScreen
import com.jovialjewels.game.ui.screens.QuizCategoryScreen
import com.jovialjewels.game.ui.screens.QuizResultsScreen
import com.jovialjewels.game.ui.screens.QuizScreen
import com.jovialjewels.game.ui.screens.RulesScreen
import com.jovialjewels.game.ui.screens.SettingsScreen
import com.jovialjewels.game.ui.screens.SummaryScreen
import com.jovialjewels.game.ui.viewmodel.PairsViewModel
import com.jovialjewels.game.ui.viewmodel.ProgressViewModel
import com.jovialjewels.game.ui.viewmodel.QuizViewModel

/** Decodes typed args without ever throwing (bad/missing args → null → fallback screen). */
inline fun <reified T : Any> NavBackStackEntry.safeRoute(): T? =
    try {
        toRoute<T>()
    } catch (e: Exception) {
        null
    }

/** Back that ignores double taps during transitions and never pops the last entry. */
fun NavController.safeBack(onExit: () -> Unit) {
    val current = currentBackStackEntry ?: return onExit()
    if (current.lifecycle.currentState != Lifecycle.State.RESUMED) return
    if (previousBackStackEntry != null) popBackStack() else onExit()
}

fun NavController.goToMenu() {
    navigate(MenuRoute) {
        popUpTo<MainRoute> { inclusive = false }
        launchSingleTop = true
    }
}

private fun NavController.goToQuizCategories() {
    navigate(QuizCategoriesRoute) {
        popUpTo<MenuRoute> { inclusive = false }
        launchSingleTop = true
    }
}

private fun NavController.goToLevels() {
    navigate(LevelsRoute) {
        popUpTo<MenuRoute> { inclusive = false }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost(
    repository: GameRepository,
    soundManager: SoundManager,
    onExit: () -> Unit,
) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.safeBack(onExit) }

    NavHost(navController = nav, startDestination = MainRoute) {

        composable<MainRoute> {
            MainScreen(onStart = { nav.navigate(MenuRoute) { launchSingleTop = true } })
        }

        composable<MenuRoute> {
            MenuScreen(
                onBack = back,
                onQuiz = { nav.navigate(QuizCategoriesRoute) { launchSingleTop = true } },
                onPairs = { nav.navigate(LevelsRoute) { launchSingleTop = true } },
                onResults = { nav.navigate(SummaryRoute) { launchSingleTop = true } },
                onSettings = { nav.navigate(SettingsRoute) { launchSingleTop = true } },
                onRules = { nav.navigate(RulesRoute) { launchSingleTop = true } },
            )
        }

        composable<QuizCategoriesRoute> {
            val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
            val state by vm.state.collectAsStateWithLifecycle()
            QuizCategoryScreen(
                state = state,
                onBack = back,
                onCategory = { id -> nav.navigate(QuizRoute(id)) { launchSingleTop = true } },
            )
        }

        composable<QuizRoute> { entry ->
            val category = QuizContent.category(entry.safeRoute<QuizRoute>()?.categoryId)
            if (category == null) {
                FallbackScreen(message = "This quiz category is not available.", onBack = back)
            } else {
                val vm: QuizViewModel = viewModel(
                    key = "quiz_${category.id}",
                    factory = QuizViewModel.factory(category, repository, soundManager),
                )
                QuizScreen(
                    viewModel = vm,
                    onBack = back,
                    onMenu = { nav.goToMenu() },
                    onFinished = { score ->
                        nav.navigate(QuizResultsRoute(category.id, score)) {
                            popUpTo<QuizRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable<QuizResultsRoute> { entry ->
            val route = entry.safeRoute<QuizResultsRoute>()
            val category = QuizContent.category(route?.categoryId)
            val score = route?.score ?: -1
            if (category == null || score !in 0..category.questions.size) {
                FallbackScreen(message = "These quiz results are no longer available.", onBack = back)
            } else {
                val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
                val state by vm.state.collectAsStateWithLifecycle()
                QuizResultsScreen(
                    category = category,
                    score = score,
                    best = state.quizBestFor(category.id),
                    onContinue = { nav.goToQuizCategories() },
                    onRestart = {
                        nav.navigate(QuizRoute(category.id)) {
                            popUpTo<QuizResultsRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onMenu = { nav.goToMenu() },
                )
            }
        }

        composable<LevelsRoute> {
            val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
            val state by vm.state.collectAsStateWithLifecycle()
            LevelsScreen(
                state = state,
                onBack = back,
                onLevel = { level -> nav.navigate(PairsRoute(level)) { launchSingleTop = true } },
                onRules = { nav.navigate(RulesRoute) { launchSingleTop = true } },
            )
        }

        composable<PairsRoute> { entry ->
            val config = GameLogic.levelConfig(entry.safeRoute<PairsRoute>()?.level ?: 0)
            val progressVm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
            val progress by progressVm.state.collectAsStateWithLifecycle()
            when {
                config == null -> FallbackScreen(message = "This level does not exist.", onBack = back)
                !progress.loaded -> LoadingScreen()
                !progress.isUnlocked(config.level) && !progress.isCleared(config.level) ->
                    FallbackScreen(message = "Level ${config.level} is still locked. Clear the previous level first.", onBack = back)
                else -> {
                    val vm: PairsViewModel = viewModel(
                        key = "pairs_${config.level}",
                        factory = PairsViewModel.factory(config, repository, soundManager),
                    )
                    PairsScreen(
                        viewModel = vm,
                        onBack = back,
                        onMenu = { nav.goToMenu() },
                        onFinished = { won, matched ->
                            nav.navigate(PairsResultsRoute(config.level, matched, won)) {
                                popUpTo<PairsRoute> { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }

        composable<PairsResultsRoute> { entry ->
            val route = entry.safeRoute<PairsResultsRoute>()
            val config = GameLogic.levelConfig(route?.level ?: 0)
            val matched = route?.matched ?: -1
            if (route == null || config == null || matched !in 0..config.pairs) {
                FallbackScreen(message = "These level results are no longer available.", onBack = back)
            } else {
                val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
                val state by vm.state.collectAsStateWithLifecycle()
                val next = GameLogic.nextLevel(config.level)
                val canGoNext = route.won && next != null && state.isUnlocked(next)
                PairsResultsScreen(
                    config = config,
                    matched = matched,
                    won = route.won,
                    best = state.bestPairs(config.level),
                    hasNextLevel = canGoNext,
                    onContinue = {
                        if (canGoNext && next != null) {
                            nav.navigate(PairsRoute(next)) {
                                popUpTo<PairsResultsRoute> { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            nav.goToLevels()
                        }
                    },
                    onRestart = {
                        nav.navigate(PairsRoute(config.level)) {
                            popUpTo<PairsResultsRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onLevels = { nav.goToLevels() },
                    onMenu = { nav.goToMenu() },
                )
            }
        }

        composable<SummaryRoute> {
            val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
            val state by vm.state.collectAsStateWithLifecycle()
            SummaryScreen(state = state, onBack = back)
        }

        composable<SettingsRoute> {
            val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repository))
            val state by vm.state.collectAsStateWithLifecycle()
            SettingsScreen(
                state = state,
                onBack = back,
                onSoundChange = { enabled ->
                    soundManager.setEnabled(enabled)
                    vm.setSoundEnabled(enabled)
                },
                onReset = vm::resetAll,
            )
        }

        composable<RulesRoute> {
            RulesScreen(onBack = back)
        }
    }
}
