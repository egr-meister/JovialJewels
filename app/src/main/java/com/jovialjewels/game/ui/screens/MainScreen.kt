package com.jovialjewels.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jovialjewels.game.ui.Assets
import com.jovialjewels.game.ui.components.FillColumn
import com.jovialjewels.game.ui.components.MenuButton
import com.jovialjewels.game.ui.components.PlateTextShadow
import com.jovialjewels.game.ui.components.ScreenBackground
import com.jovialjewels.game.ui.theme.JJColors
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Home screen — "Royal Jester Stage" (not a dashboard):
 * the jester-mask logo floats inside a slowly turning carousel of jewels, and the three-jester
 * troupe performs on the theater stage of the background, with the juggler in the spotlight.
 * The ornate Start plate sits at the front of the stage.
 */
@Composable
fun MainScreen(onStart: () -> Unit) {
    ScreenBackground {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            val h = maxHeight
            val orbitSize: Dp = minOf(maxWidth * 0.92f, h * 0.42f, 440.dp)
            val stageHeight: Dp = minOf(h * 0.30f, 300.dp)
            FillColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                JewelCarousel(size = orbitSize)
                Spacer(Modifier.weight(1f))
                JesterStage(height = stageHeight)
                Spacer(Modifier.height(14.dp))
                MenuButton(text = "Start", onClick = onStart, widthFraction = 0.72f)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Match jewels · Answer riddles · Fully offline",
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = PlateTextShadow),
                    color = JJColors.Lavender,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun JewelCarousel(size: Dp) {
    val transition = rememberInfiniteTransition(label = "carousel")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 30_000, easing = LinearEasing)),
        label = "carouselAngle",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1_900), RepeatMode.Reverse),
        label = "logoPulse",
    )
    val density = LocalDensity.current
    val radiusX = with(density) { (size * 0.43f).toPx() }
    val radiusY = with(density) { (size * 0.40f).toPx() }
    val gemSize = size * 0.17f
    val orbit = listOf(0, 1, 2, 3, 4, 5, 8, 9)
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        // soft spotlight behind the logo
        Box(
            Modifier
                .size(size * 0.8f)
                .clip(RoundedCornerShape(50))
                .background(Brush.radialGradient(listOf(Color(0x66FFC93C), Color(0x00FFC93C)))),
        )
        orbit.forEachIndexed { i, item ->
            val step = 360f / orbit.size
            Image(
                painter = painterResource(Assets.item(item)),
                contentDescription = null,
                modifier = Modifier
                    .size(gemSize)
                    .offset {
                        val rad = Math.toRadians((angle + i * step).toDouble())
                        IntOffset((radiusX * cos(rad)).roundToInt(), (radiusY * sin(rad)).roundToInt())
                    }
                    .graphicsLayer {
                        // gems at the "back" of the ring are smaller and dimmer
                        val depth = ((sin(Math.toRadians((angle + i * step).toDouble())) + 1.0) / 2.0).toFloat()
                        val s = 0.75f + 0.35f * depth
                        scaleX = s
                        scaleY = s
                        alpha = 0.65f + 0.35f * depth
                    },
            )
        }
        Image(
            painter = painterResource(Assets.logo),
            contentDescription = "Jovial Jewels",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.66f)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                },
        )
    }
}

@Composable
private fun JesterStage(height: Dp) {
    val transition = rememberInfiniteTransition(label = "stage")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_300), RepeatMode.Reverse),
        label = "stageBob",
    )
    Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.BottomCenter) {
        // spotlight on the juggler
        Box(
            Modifier
                .fillMaxHeight()
                .width(height * 0.9f)
                .background(Brush.verticalGradient(listOf(Color(0x00FFF6E8), Color(0x33FFF6E8)))),
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Image(
                painter = painterResource(Assets.mascotA),
                contentDescription = "A jester in purple and gold, waving",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxHeight(0.82f)
                    .width(height * 0.48f)
                    .graphicsLayer { translationY = -bob * 4f * density },
            )
            Image(
                painter = painterResource(Assets.mascotC),
                contentDescription = "A jester juggling three jewels",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(height * 0.58f)
                    .graphicsLayer { translationY = -(1f - bob) * 8f * density },
            )
            Image(
                painter = painterResource(Assets.mascotB),
                contentDescription = "A jester taking a bow",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxHeight(0.82f)
                    .width(height * 0.52f)
                    .graphicsLayer { translationY = -bob * 4f * density },
            )
        }
    }
}
