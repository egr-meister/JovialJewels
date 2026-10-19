package com.jovialjewels.game.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.jovialjewels.game.audio.Sfx
import com.jovialjewels.game.logic.GameLogic
import com.jovialjewels.game.logic.QuizContent
import com.jovialjewels.game.ui.Assets
import com.jovialjewels.game.ui.components.GemPanel
import com.jovialjewels.game.ui.components.JesterParade
import com.jovialjewels.game.ui.components.LocalSoundManager
import com.jovialjewels.game.ui.components.LockGlyph
import com.jovialjewels.game.ui.components.MenuButton
import com.jovialjewels.game.ui.components.PlateButton
import com.jovialjewels.game.ui.components.PlateTextShadow
import com.jovialjewels.game.ui.components.ScreenBackground
import com.jovialjewels.game.ui.components.ScreenFrame
import com.jovialjewels.game.ui.theme.JJColors
import com.jovialjewels.game.ui.viewmodel.ProgressUiState

/**
 * Public privacy policy URL shown from Settings. Put the URL of YOUR hosted policy here
 * (Google Play requires one). While blank, only the in-app privacy summary is shown.
 */
const val PRIVACY_POLICY_URL = ""

@Composable
fun LoadingScreen() {
    ScreenBackground {
        CircularProgressIndicator(
            color = JJColors.Topaz,
            modifier = Modifier.align(Alignment.Center).semantics { contentDescription = "Loading" },
        )
    }
}

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onQuiz: () -> Unit,
    onPairs: () -> Unit,
    onResults: () -> Unit,
    onSettings: () -> Unit,
    onRules: () -> Unit,
) {
    ScreenFrame(title = "Menu", onBack = onBack) {
        Image(
            painter = painterResource(Assets.logo),
            contentDescription = "Jovial Jewels",
            modifier = Modifier.size(width = 150.dp, height = 140.dp),
        )
        Spacer(Modifier.height(12.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MenuButton(text = "Quiz", onClick = onQuiz)
            MenuButton(text = "Matching Pairs", onClick = onPairs)
            MenuButton(text = "Results", onClick = onResults)
            MenuButton(text = "Settings", onClick = onSettings)
            MenuButton(text = "Game Rules", onClick = onRules)
        }
        JesterParade(Modifier.weight(1f).padding(top = 8.dp), mascots = listOf(Assets.mascotA, Assets.mascotB))
    }
}

@Composable
private fun StarRow(stars: Int, total: Int = 3) {
    Text(
        text = "★".repeat(stars) + "☆".repeat((total - stars).coerceAtLeast(0)),
        style = MaterialTheme.typography.titleMedium,
        color = JJColors.Topaz,
    )
}

@Composable
fun QuizCategoryScreen(
    state: ProgressUiState,
    onBack: () -> Unit,
    onCategory: (String) -> Unit,
) {
    val sound = LocalSoundManager.current
    ScreenFrame(title = "Quiz", onBack = onBack) {
        Text(
            text = "Circles, Balls & Spheres",
            style = MaterialTheme.typography.titleLarge.copy(shadow = PlateTextShadow),
            color = JJColors.Pearl,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Choose your difficulty",
            style = MaterialTheme.typography.bodyLarge,
            color = JJColors.Lavender,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuizContent.categories.forEach { category ->
                val best = state.quizBestFor(category.id)
                val bestText = if (best == null) "Not played yet" else "Best: $best/${category.questions.size}"
                val shape = RoundedCornerShape(24.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 92.dp)
                        .clip(shape)
                        .background(Brush.horizontalGradient(listOf(JJColors.Plum, JJColors.NightPlum)))
                        .border(2.dp, JJColors.Topaz, shape)
                        .clickable(role = Role.Button, onClickLabel = "Start ${category.title} quiz") {
                            sound?.play(Sfx.CLICK)
                            onCategory(category.id)
                        }
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${category.title}, ${category.stars} of 3 stars. ${category.subtitle}. $bestText"
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Assets.difficultyEmblem(category.id)),
                        contentDescription = null,
                        modifier = Modifier.size(68.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category.title, style = MaterialTheme.typography.titleLarge, color = JJColors.Pearl)
                        StarRow(category.stars)
                        Text(category.subtitle, style = MaterialTheme.typography.bodyMedium, color = JJColors.Lavender)
                        Text(bestText, style = MaterialTheme.typography.titleMedium, color = JJColors.Topaz)
                    }
                }
            }
        }
        JesterParade(Modifier.weight(1f).padding(top = 8.dp), mascots = listOf(Assets.mascotC))
    }
}

private val GreyscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun LevelsScreen(
    state: ProgressUiState,
    onBack: () -> Unit,
    onLevel: (Int) -> Unit,
    onRules: () -> Unit,
) {
    val sound = LocalSoundManager.current
    ScreenFrame(title = "Matching Pairs", onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(Assets.decoChest), contentDescription = null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Cleared ${state.pairsCleared}/${GameLogic.levelCount}",
                style = MaterialTheme.typography.titleLarge.copy(shadow = PlateTextShadow),
                color = JJColors.Topaz,
            )
        }
        Spacer(Modifier.height(10.dp))
        GameLogic.levels.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { cfg ->
                    val unlocked = state.isUnlocked(cfg.level)
                    val cleared = state.isCleared(cfg.level)
                    val best = state.bestPairs(cfg.level)
                    val status = when {
                        cleared -> "Cleared"
                        unlocked -> "Unlocked"
                        else -> "Locked"
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = unlocked, role = Role.Button, onClickLabel = "Play level ${cfg.level}") {
                                sound?.play(Sfx.CLICK)
                                onLevel(cfg.level)
                            }
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Level ${cfg.level}, $status, best $best of ${cfg.pairs} pairs"
                                stateDescription = status
                            }
                            .padding(4.dp),
                    ) {
                        Box(Modifier.size(86.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(Assets.plateRound),
                                contentDescription = null,
                                modifier = Modifier.size(86.dp),
                                colorFilter = if (unlocked) null else GreyscaleFilter,
                                alpha = if (unlocked) 1f else 0.75f,
                            )
                            if (unlocked) {
                                Text(
                                    text = cfg.level.toString(),
                                    style = MaterialTheme.typography.headlineMedium.copy(shadow = PlateTextShadow),
                                    color = Color.White,
                                )
                            } else {
                                LockGlyph(Modifier.size(32.dp))
                            }
                        }
                        Text(
                            text = when {
                                cleared -> "★ $best/${cfg.pairs}"
                                unlocked -> "$best/${cfg.pairs}"
                                else -> "Locked"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(shadow = PlateTextShadow),
                            color = if (cleared) JJColors.Topaz else if (unlocked) JJColors.Pearl else JJColors.Lavender,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        PlateButton(text = "Game Rules", onClick = onRules, modifier = Modifier.fillMaxWidth(0.72f))
        JesterParade(Modifier.weight(1f).padding(top = 8.dp), mascots = listOf(Assets.mascotB, Assets.mascotA))
    }
}

@Composable
fun SummaryScreen(state: ProgressUiState, onBack: () -> Unit) {
    ScreenFrame(title = "Results", onBack = onBack) {
        Image(painter = painterResource(Assets.decoChest), contentDescription = "Treasure chest", modifier = Modifier.size(110.dp))
        GemPanel(title = "Quiz Best") {
            QuizContent.categories.forEach { category ->
                val best = state.quizBestFor(category.id)
                SummaryRow(label = "${category.title}  " + "★".repeat(category.stars), value = if (best == null) "—" else "$best/${category.questions.size}")
            }
        }
        Spacer(Modifier.height(14.dp))
        GemPanel(title = "Matching Pairs") {
            SummaryRow(label = "Levels cleared", value = "${state.pairsCleared}/${GameLogic.levelCount}")
            GameLogic.levels.forEach { cfg ->
                val best = state.bestPairs(cfg.level)
                val mark = if (state.isCleared(cfg.level)) "★ " else ""
                SummaryRow(label = "Level ${cfg.level}", value = "$mark$best/${cfg.pairs}")
            }
        }
        JesterParade(Modifier.weight(1f).padding(top = 8.dp), mascots = listOf(Assets.mascotC))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp)
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = JJColors.Pearl, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = JJColors.Topaz)
    }
}

@Composable
fun SettingsScreen(
    state: ProgressUiState,
    onBack: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current
    var showReset by rememberSaveable { mutableStateOf(false) }
    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    var resetDone by rememberSaveable { mutableStateOf(false) }

    ScreenFrame(title = "Settings", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        GemPanel {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .toggleable(value = state.soundEnabled, role = Role.Switch, onValueChange = onSoundChange)
                    .semantics { stateDescription = if (state.soundEnabled) "Sound on" else "Sound off" }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painter = painterResource(Assets.item(8)), contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sound effects", style = MaterialTheme.typography.titleLarge, color = JJColors.Pearl)
                    Text(
                        text = if (state.soundEnabled) "On" else "Off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JJColors.Lavender,
                    )
                }
                Switch(
                    checked = state.soundEnabled,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JJColors.Pearl,
                        checkedTrackColor = JJColors.Emerald,
                        uncheckedThumbColor = JJColors.Lavender,
                        uncheckedTrackColor = JJColors.NightPlum,
                        uncheckedBorderColor = JJColors.Lavender,
                    ),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PlateButton(text = "Privacy Policy", onClick = { showPrivacy = true }, modifier = Modifier.fillMaxWidth(0.8f))
            PlateButton(text = "Rate the Game", onClick = { openStorePage(context) }, modifier = Modifier.fillMaxWidth(0.8f))
            PlateButton(text = "Reset Progress", onClick = { showReset = true }, modifier = Modifier.fillMaxWidth(0.8f))
        }
        if (resetDone) {
            Spacer(Modifier.height(12.dp))
            Text("Progress has been reset.", style = MaterialTheme.typography.bodyLarge, color = JJColors.Topaz)
        }
        JesterParade(Modifier.weight(1f).padding(top = 8.dp), mascots = listOf(Assets.mascotA))
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            containerColor = JJColors.Plum,
            titleContentColor = JJColors.Topaz,
            textContentColor = JJColors.Pearl,
            title = { Text("Reset all progress?") },
            text = { Text("Best quiz scores, matched pairs and unlocked levels will be erased. Your sound setting is kept. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset()
                        resetDone = true
                        showReset = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("Reset", color = JJColors.Topaz) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Cancel", color = JJColors.Pearl)
                }
            },
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            containerColor = JJColors.Plum,
            titleContentColor = JJColors.Topaz,
            textContentColor = JJColors.Pearl,
            title = { Text("Privacy") },
            text = {
                Text(
                    "Jovial Jewels works fully offline. It does not collect, share or send any personal data, " +
                        "has no accounts, ads, analytics or tracking. Your scores and the sound setting are stored " +
                        "only on this device and are removed when you reset progress or uninstall the game.",
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Close", color = JJColors.Pearl)
                }
            },
            dismissButton = if (PRIVACY_POLICY_URL.isNotBlank()) {
                {
                    TextButton(
                        onClick = {
                            openUrl(context, PRIVACY_POLICY_URL)
                            showPrivacy = false
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text("Full policy", color = JJColors.Topaz) }
                }
            } else {
                null
            },
        )
    }
}

private fun openUrl(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: ActivityNotFoundException) {
    false
} catch (e: SecurityException) {
    false
}

private fun openStorePage(context: Context) {
    val pkg = context.packageName
    if (!openUrl(context, "market://details?id=$pkg")) {
        openUrl(context, "https://play.google.com/store/apps/details?id=$pkg")
    }
}

@Composable
fun RulesScreen(onBack: () -> Unit) {
    ScreenFrame(title = "Game Rules", onBack = onBack) {
        Spacer(Modifier.height(4.dp))
        GemPanel(title = "Quiz") {
            Image(painter = painterResource(Assets.decoFan), contentDescription = null, modifier = Modifier.size(64.dp))
            RulesText(
                "• Three difficulty levels — Easy, Medium and Hard — with 10 questions each about circles, balls and spheres.\n" +
                    "• Every question has 4 answers and exactly one is right.\n" +
                    "• The correct answer turns green with a ✓, a wrong pick turns red with a ✗.\n" +
                    "• The next question appears automatically. Your best score per level is saved.",
            )
        }
        Spacer(Modifier.height(14.dp))
        GemPanel(title = "Matching Pairs") {
            Image(painter = painterResource(Assets.decoHourglass), contentDescription = null, modifier = Modifier.size(64.dp))
            RulesText(
                "• Flip two cards. Matching treasures stay open; different ones flip back.\n" +
                    "• Match every pair before the hourglass runs out to see \"Well Done\".\n" +
                    "• If time runs out, it's \"Out of Time\" — try again!\n" +
                    "• Clearing a level unlocks the next one. Level 1 is always open.\n" +
                    "• Pause any time; the timer stops while paused.",
            )
        }
        Spacer(Modifier.height(14.dp))
        PlateButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(0.72f))
        JesterParade(Modifier.weight(1f).padding(top = 8.dp), mascots = listOf(Assets.mascotC))
    }
}

@Composable
private fun RulesText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = JJColors.Pearl,
        modifier = Modifier.fillMaxWidth(),
    )
}
