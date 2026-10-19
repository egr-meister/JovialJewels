package com.jovialjewels.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jovialjewels.game.logic.GameLogic
import com.jovialjewels.game.logic.LevelConfig
import com.jovialjewels.game.ui.Assets
import com.jovialjewels.game.ui.components.FillColumn
import com.jovialjewels.game.ui.components.GemModal
import com.jovialjewels.game.ui.components.GemPanel
import com.jovialjewels.game.ui.components.GemTopBar
import com.jovialjewels.game.ui.components.InfoChip
import com.jovialjewels.game.ui.components.JesterParade
import com.jovialjewels.game.ui.components.PanelActions
import com.jovialjewels.game.ui.components.PlateButton
import com.jovialjewels.game.ui.components.PlateTextShadow
import com.jovialjewels.game.ui.components.ScreenBackground
import com.jovialjewels.game.ui.theme.JJColors
import com.jovialjewels.game.ui.viewmodel.PairCard
import com.jovialjewels.game.ui.viewmodel.PairsOutcome
import com.jovialjewels.game.ui.viewmodel.PairsViewModel
import kotlin.math.ceil

/** Card proportions: between a playing card (0.62) and almost square (0.9). */
private const val MIN_CARD_RATIO = 0.62f
private const val MAX_CARD_RATIO = 0.9f

/**
 * Matching Pairs board. The whole grid always fits the screen — NO scrolling: card size is
 * computed from the space left under the HUD, the column count and the row count.
 */
@Composable
fun PairsScreen(
    viewModel: PairsViewModel,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onFinished: (won: Boolean, matched: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.outcome) {
        val outcome = state.outcome ?: return@LaunchedEffect
        onFinished(outcome == PairsOutcome.WIN, state.matchedPairs)
    }
    BackHandler(enabled = state.paused) { viewModel.resume() }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
                .then(if (state.paused) Modifier.clearAndSetSemantics { } else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GemTopBar(title = "Level ${state.level}", onBack = onBack, onPause = viewModel::pause)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                InfoChip(
                    text = GameLogic.formatSeconds(state.secondsLeft),
                    icon = Assets.decoHourglass,
                    accent = if (state.lowTime) JJColors.Garnet else Color(0xFF6FA8FF),
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "Time left ${state.secondsLeft} seconds"
                    },
                )
                InfoChip(
                    text = "Pairs ${state.matchedPairs}/${state.totalPairs}",
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Spacer(Modifier.height(12.dp))
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                val columns = state.columns.coerceAtLeast(1)
                val rows = ceil(state.cards.size / columns.toFloat()).toInt().coerceAtLeast(1)
                val spacing = if (rows >= 5) 8.dp else 12.dp
                val cellW = (maxWidth - spacing * (columns - 1)) / columns
                val cellH = (maxHeight - spacing * (rows - 1)) / rows
                var cardW = cellW
                var cardH = cellH
                if (cardW / cardH > MAX_CARD_RATIO) cardW = cardH * MAX_CARD_RATIO
                if (cardW / cardH < MIN_CARD_RATIO) cardH = cardW / MIN_CARD_RATIO
                cardW = cardW.coerceAtLeast(48.dp)
                cardH = cardH.coerceAtLeast(48.dp)
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                    state.cards.chunked(columns).forEach { rowCards ->
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            rowCards.forEach { card ->
                                FlipCard(
                                    card = card,
                                    width = cardW,
                                    height = cardH,
                                    enabled = !state.paused && !state.inputLocked && state.outcome == null,
                                    onClick = { viewModel.flip(card.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.paused) {
            GemModal(title = "Paused") {
                Image(painter = painterResource(Assets.decoHourglass), contentDescription = null, modifier = Modifier.size(72.dp))
                Text("The hourglass is frozen. Ready when you are!", color = JJColors.Pearl, textAlign = TextAlign.Center)
                PanelActions(
                    onContinue = viewModel::resume,
                    onRestart = viewModel::restart,
                    onMenu = onMenu,
                )
            }
        }
    }
}

@Composable
private fun FlipCard(card: PairCard, width: Dp, height: Dp, enabled: Boolean, onClick: () -> Unit) {
    val open = card.faceUp || card.matched
    val rotation by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "cardFlip",
    )
    val corner = min(width, height) * 0.1f
    val shape = RoundedCornerShape(corner.coerceIn(8.dp, 18.dp))
    val description = when {
        card.matched -> "${Assets.itemName(card.item)}, matched"
        card.faceUp -> "${Assets.itemName(card.item)}, open"
        else -> "Hidden card ${card.id + 1}"
    }
    val border = if (min(width, height) < 80.dp) 2.dp else 3.dp
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clip(shape)
            .clickable(enabled = enabled && !open, role = Role.Button, onClickLabel = "Flip card") { onClick() }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (rotation <= 90f) {
            // Card back: royal-blue velvet with the gold jester logo.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(JJColors.Sapphire, JJColors.RoyalDeep)), shape)
                    .border(border, JJColors.Topaz, shape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Assets.logo),
                    contentDescription = null,
                    modifier = Modifier.size(min(width, height) * 0.6f),
                )
            }
        } else {
            // Card face (counter-rotated so the art is not mirrored).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF6A2FB0), JJColors.Plum, JJColors.NightPlum)),
                        shape,
                    )
                    .border(if (card.matched) border + 1.dp else border, if (card.matched) JJColors.Emerald else JJColors.Topaz, shape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Assets.item(card.item)),
                    contentDescription = null,
                    modifier = Modifier.size(min(width, height) * 0.82f),
                )
                if (card.matched) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size((min(width, height) * 0.24f).coerceIn(18.dp, 30.dp))
                            .background(JJColors.Emerald, RoundedCornerShape(50))
                            .border(1.5.dp, Color.White, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun PairsResultsScreen(
    config: LevelConfig,
    matched: Int,
    won: Boolean,
    best: Int,
    hasNextLevel: Boolean,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onLevels: () -> Unit,
    onMenu: () -> Unit,
) {
    BackHandler { onLevels() }
    ScreenBackground {
        FillColumn(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(if (won) Assets.decoChest else Assets.decoHourglass),
                contentDescription = if (won) "Treasure chest of jewels" else "Empty hourglass",
                modifier = Modifier.size(120.dp),
            )
            Spacer(Modifier.height(6.dp))
            GemPanel(title = if (won) "Well Done" else "Out of Time") {
                Text(
                    text = "Level ${config.level}",
                    style = MaterialTheme.typography.titleLarge,
                    color = JJColors.Lavender,
                )
                Text(
                    text = "Pairs: $matched/${config.pairs}",
                    style = MaterialTheme.typography.displaySmall.copy(shadow = PlateTextShadow),
                    color = JJColors.Pearl,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Text(
                    text = when {
                        won && hasNextLevel -> "Level ${config.level + 1} is now unlocked!"
                        won -> "You cleared every level. Dazzling!"
                        else -> "So close! Give it another sparkle."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = JJColors.Lavender,
                    textAlign = TextAlign.Center,
                )
                Text(text = "Best: $best/${config.pairs}", style = MaterialTheme.typography.titleMedium, color = JJColors.Topaz)
                PanelActions(onContinue = onContinue, onRestart = onRestart, onMenu = onMenu)
                if (won && hasNextLevel) {
                    PlateButton(text = "All Levels", onClick = onLevels, modifier = Modifier.fillMaxWidth(0.92f))
                }
            }
            JesterParade(
                Modifier.weight(1f).padding(top = 8.dp),
                mascots = if (won) listOf(Assets.mascotA, Assets.mascotC, Assets.mascotB) else listOf(Assets.mascotB),
            )
        }
    }
}
