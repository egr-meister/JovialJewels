package com.jovialjewels.game.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jovialjewels.game.logic.QuizCategory
import com.jovialjewels.game.ui.Assets
import com.jovialjewels.game.ui.components.AutoSizeText
import com.jovialjewels.game.ui.components.FillColumn
import com.jovialjewels.game.ui.components.GemModal
import com.jovialjewels.game.ui.components.GemPanel
import com.jovialjewels.game.ui.components.GemProgressBar
import com.jovialjewels.game.ui.components.GemTopBar
import com.jovialjewels.game.ui.components.JesterParade
import com.jovialjewels.game.ui.components.NinePlate
import com.jovialjewels.game.ui.components.PanelActions
import com.jovialjewels.game.ui.components.PlateTextShadow
import com.jovialjewels.game.ui.components.ScreenBackground
import com.jovialjewels.game.ui.theme.JJColors
import com.jovialjewels.game.ui.viewmodel.QuizOption
import com.jovialjewels.game.ui.viewmodel.QuizViewModel

/**
 * One-screen quiz layout (no scrolling on normal phones and font sizes):
 * top bar → progress → framed question card → 4 answer plates → the jester troupe fills
 * whatever space is left at the bottom.
 */
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onFinished: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finishedScore) {
        state.finishedScore?.let(onFinished)
    }
    // Back closes the pause overlay first; otherwise standard back navigation.
    BackHandler(enabled = state.paused) { viewModel.resume() }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
                .then(if (state.paused) Modifier.clearAndSetSemantics { } else Modifier),
        ) {
            GemTopBar(title = state.categoryTitle, onBack = onBack, onPause = viewModel::pause)
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val cardHeight = (maxHeight * 0.25f).coerceIn(120.dp, 210.dp)
                FillColumn(Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        GemProgressBar(
                            fraction = state.progressFraction,
                            description = "Question ${state.counterText}",
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = state.counterText,
                            style = MaterialTheme.typography.titleLarge.copy(shadow = PlateTextShadow),
                            color = JJColors.Topaz,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    QuestionCard(text = state.question, height = cardHeight)
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        state.options.forEachIndexed { index, option ->
                            AnswerButton(
                                option = option,
                                reveal = state.answered,
                                selected = state.selectedIndex == index,
                                enabled = !state.answered && !state.paused,
                                onClick = { viewModel.answer(index) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Score: ${state.score}",
                        style = MaterialTheme.typography.titleMedium.copy(shadow = PlateTextShadow),
                        color = JJColors.Lavender,
                    )
                    // The troupe cheers from the empty space under the answers.
                    JesterParade(Modifier.weight(1f).padding(top = 4.dp), maxJesterHeight = 230.dp, minHeight = 80.dp)
                }
            }
        }

        if (state.paused) {
            GemModal(title = "Paused") {
                Image(painter = painterResource(Assets.decoHourglass), contentDescription = null, modifier = Modifier.size(72.dp))
                Text("Take a breather — your question waits for you.", color = JJColors.Pearl, textAlign = TextAlign.Center)
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
private fun QuestionCard(text: String, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center,
    ) {
        NinePlate(res = Assets.panelFrame, modifier = Modifier.matchParentSize(), capX = 0.27f, capY = 0.27f)
        AutoSizeText(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(shadow = PlateTextShadow),
            color = JJColors.Pearl,
            minFontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 24.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

private enum class AnswerLook { NORMAL, CORRECT, WRONG, DIMMED }

@Composable
private fun AnswerButton(
    option: QuizOption,
    reveal: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val look = when {
        !reveal -> AnswerLook.NORMAL
        option.isCorrect -> AnswerLook.CORRECT
        selected -> AnswerLook.WRONG
        else -> AnswerLook.DIMMED
    }
    val shape = RoundedCornerShape(18.dp)
    val marker = when (look) {
        AnswerLook.CORRECT -> "✓  "
        AnswerLook.WRONG -> "✗  "
        else -> ""
    }
    val spoken = when (look) {
        AnswerLook.CORRECT -> if (selected) "Your answer, correct" else "Correct answer"
        AnswerLook.WRONG -> "Your answer, wrong"
        else -> ""
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = if (spoken.isEmpty()) option.text else "${option.text}. $spoken"
            },
        contentAlignment = Alignment.Center,
    ) {
        when (look) {
            AnswerLook.NORMAL, AnswerLook.DIMMED -> NinePlate(
                res = Assets.btnPlate,
                modifier = Modifier.matchParentSize(),
                capX = 0.5f,
                alpha = if (look == AnswerLook.DIMMED) 0.5f else 1f,
            )
            AnswerLook.CORRECT -> Box(
                Modifier
                    .matchParentSize()
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .background(JJColors.Emerald, shape)
                    .border(3.dp, JJColors.Topaz, shape),
            )
            AnswerLook.WRONG -> Box(
                Modifier
                    .matchParentSize()
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .background(JJColors.Garnet, shape)
                    .border(3.dp, JJColors.Topaz, shape),
            )
        }
        Text(
            text = marker + option.text,
            style = MaterialTheme.typography.titleMedium.copy(shadow = PlateTextShadow),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 12.dp),
        )
    }
}

@Composable
fun QuizResultsScreen(
    category: QuizCategory,
    score: Int,
    best: Int?,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
) {
    BackHandler { onContinue() }
    val total = category.questions.size
    val good = score >= total / 2
    val message = when {
        score == total -> "Flawless! The whole court applauds."
        score >= total * 7 / 10 -> "Brilliant! Almost a perfect circle."
        good -> "Nice roll — keep juggling!"
        else -> "Every jester drops a ball. Try again!"
    }
    ScreenBackground {
        FillColumn(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(if (good) Assets.decoChest else Assets.decoHourglass),
                contentDescription = if (good) "Treasure chest of jewels" else "Hourglass",
                modifier = Modifier.size(120.dp),
            )
            Spacer(Modifier.height(6.dp))
            GemPanel(title = "${category.title} Quiz") {
                Text(
                    text = "Score: $score/$total",
                    style = MaterialTheme.typography.displaySmall.copy(shadow = PlateTextShadow),
                    color = JJColors.Pearl,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Text(message, style = MaterialTheme.typography.bodyLarge, color = JJColors.Lavender, textAlign = TextAlign.Center)
                Text(
                    text = "Best: ${best ?: score}/$total",
                    style = MaterialTheme.typography.titleMedium,
                    color = JJColors.Topaz,
                )
                PanelActions(onContinue = onContinue, onRestart = onRestart, onMenu = onMenu)
            }
            JesterParade(
                Modifier.weight(1f).padding(top = 8.dp),
                mascots = if (good) listOf(Assets.mascotA, Assets.mascotC) else listOf(Assets.mascotB),
            )
        }
    }
}
