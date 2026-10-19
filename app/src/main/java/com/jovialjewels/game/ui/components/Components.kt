package com.jovialjewels.game.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.jovialjewels.game.audio.Sfx
import com.jovialjewels.game.audio.SoundManager
import com.jovialjewels.game.ui.Assets
import com.jovialjewels.game.ui.theme.JJColors
import kotlin.math.roundToInt

/** SoundManager for UI clicks; null in previews/tests. */
val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }

val PlateTextShadow = Shadow(color = Color(0xE60A0218), offset = Offset(0f, 3f), blurRadius = 7f)

private val PanelShape = RoundedCornerShape(28.dp)

private fun plateLabelStyle(base: TextStyle): TextStyle =
    base.copy(color = Color.White, fontWeight = FontWeight.Bold, shadow = PlateTextShadow)

/** Full-screen stage background with a soft vignette for text readability. */
@Composable
fun ScreenBackground(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(JJColors.Amethyst)) {
        Image(
            painter = painterResource(Assets.bgMain),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x8C0A0218),
                        0.35f to Color(0x260A0218),
                        1f to Color(0x990A0218),
                    ),
                ),
        )
        content()
    }
}

/**
 * Draws a framed plate image "9-slice" style so ornate corners never stretch:
 * - capY == 0: horizontal 3-slice (left cap · stretched middle · right cap), scaled to the height.
 * - capY > 0: full 9-slice (corners kept, edges and center stretched).
 * Caps are given as fractions of the SOURCE image height.
 */
@Composable
fun NinePlate(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    capX: Float = 0.5f,
    capY: Float = 0f,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
) {
    val image: ImageBitmap = ImageBitmap.imageResource(res)
    Canvas(modifier) {
        val sw = image.width
        val sh = image.height
        val dw = size.width.roundToInt()
        val dh = size.height.roundToInt()
        if (sw <= 4 || sh <= 4 || dw <= 0 || dh <= 0) return@Canvas
        val sx = (sh * capX).toInt().coerceIn(1, sw / 2 - 1)
        val sy = (sh * capY).toInt().coerceIn(0, sh / 2 - 1)
        val scale = if (sy == 0) dh / sh.toFloat() else minOf(dw / sw.toFloat(), dh / sh.toFloat())
        val dx = (sx * scale).roundToInt().coerceAtMost(dw / 2)
        val dy = (sy * scale).roundToInt().coerceAtMost(dh / 2)
        val srcX = intArrayOf(0, sx, sw - sx, sw)
        val dstX = intArrayOf(0, dx, dw - dx, dw)
        val srcY = if (sy == 0) intArrayOf(0, sh) else intArrayOf(0, sy, sh - sy, sh)
        val dstY = if (sy == 0) intArrayOf(0, dh) else intArrayOf(0, dy, dh - dy, dh)
        for (i in 0 until 3) {
            for (j in 0 until srcY.size - 1) {
                val srcW = srcX[i + 1] - srcX[i]
                val srcH = srcY[j + 1] - srcY[j]
                val dstW = dstX[i + 1] - dstX[i]
                val dstH = dstY[j + 1] - dstY[j]
                if (srcW > 0 && srcH > 0 && dstW > 0 && dstH > 0) {
                    drawImage(
                        image = image,
                        srcOffset = IntOffset(srcX[i], srcY[j]),
                        srcSize = IntSize(srcW, srcH),
                        dstOffset = IntOffset(dstX[i], dstY[j]),
                        dstSize = IntSize(dstW, dstH),
                        alpha = alpha,
                        colorFilter = colorFilter,
                        filterQuality = FilterQuality.High,
                    )
                }
            }
        }
    }
}

/**
 * Plate button: the jeweled plate (3-slice, corners never distorted) with a WHITE bold label
 * drawn on top. Height grows with font scale; min 56dp touch target.
 */
@Composable
fun PlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 58.dp,
) {
    val sound = LocalSoundManager.current
    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = text) {
                sound?.play(Sfx.CLICK)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        NinePlate(
            res = Assets.btnPlate,
            modifier = Modifier.matchParentSize(),
            capX = 0.5f,
            alpha = if (enabled) 1f else 0.45f,
        )
        Text(
            text = text,
            style = plateLabelStyle(MaterialTheme.typography.titleMedium),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 14.dp),
        )
    }
}

/**
 * Ornate menu button. Width ~70% of the screen; the height follows the plate image's own
 * aspect ratio (read at runtime), so the frame is never distorted.
 */
@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.7f,
) {
    val sound = LocalSoundManager.current
    val painter = painterResource(Assets.btnMenu)
    val intrinsic = painter.intrinsicSize
    val ratio = if (intrinsic.width.isFinite() && intrinsic.height.isFinite() && intrinsic.width > 0f && intrinsic.height > 0f) {
        intrinsic.width / intrinsic.height
    } else {
        4f
    }
    Box(
        modifier = modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth(widthFraction)
            .aspectRatio(ratio)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClickLabel = text) {
                sound?.play(Sfx.CLICK)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            text = text,
            style = plateLabelStyle(MaterialTheme.typography.titleLarge),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 44.dp),
        )
    }
}

/** Round jeweled icon button (back / pause). 54dp target ≥ 48dp. */
@Composable
fun IconPlateButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
) {
    val sound = LocalSoundManager.current
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.Button, onClickLabel = contentDescription) {
                sound?.play(Sfx.CLICK)
                onClick()
            },
    )
}

/** Top bar: back on the left, gold title in the middle, optional pause on the right. */
@Composable
fun GemTopBar(title: String, onBack: (() -> Unit)?, onPause: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconPlateButton(icon = Assets.iconBack, contentDescription = "Back", onClick = onBack)
        } else {
            Spacer(Modifier.width(54.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(shadow = PlateTextShadow),
            color = JJColors.Topaz,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { heading() },
        )
        if (onPause != null) {
            IconPlateButton(icon = Assets.iconPause, contentDescription = "Pause", onClick = onPause)
        } else {
            Spacer(Modifier.width(54.dp))
        }
    }
}

/**
 * Standard screen: background, safe insets, top bar and a content column that is at least
 * as tall as the screen. Children may use `Modifier.weight(...)` to fill leftover space
 * (e.g. the jester parade); if content is taller than the screen (huge font scale) it scrolls.
 */
@Composable
fun ScreenFrame(
    title: String,
    onBack: (() -> Unit)?,
    onPause: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp),
        ) {
            GemTopBar(title = title, onBack = onBack, onPause = onPause)
            FillColumn(Modifier.weight(1f).fillMaxWidth(), content = content)
        }
    }
}

/** Column at least as tall as its viewport (so weights work) that scrolls when content overflows. */
@Composable
fun FillColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier) {
        val viewport = maxHeight
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(min = viewport),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = verticalArrangement,
                content = content,
            )
        }
    }
}

/**
 * The jester troupe standing at the bottom of the available space. Shows as many jesters as
 * fit, sized to the space it gets; hides itself when there is too little room. Decorative.
 */
@Composable
fun JesterParade(
    modifier: Modifier = Modifier,
    mascots: List<Int> = Assets.mascots,
    maxJesterHeight: Dp = 260.dp,
    minHeight: Dp = 84.dp,
    animate: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "parade")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_500), RepeatMode.Reverse),
        label = "paradeBob",
    )
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        if (maxHeight < minHeight || mascots.isEmpty()) return@BoxWithConstraints
        val h = minOf(maxHeight, maxJesterHeight)
        val w = h * 0.62f
        val count = minOf(mascots.size, (maxWidth / (w * 0.85f)).toInt().coerceAtLeast(1))
        val shown = if (count >= mascots.size) mascots else mascots.take(count)
        Row(
            modifier = Modifier.fillMaxWidth().height(h),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            shown.forEachIndexed { i, res ->
                val phase = if (i % 2 == 0) bob else 1f - bob
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .width(w)
                        .fillMaxHeight(if (i == shown.size / 2 && shown.size == 3) 1f else 0.9f)
                        .graphicsLayer { translationY = if (animate) -phase * 5f * density else 0f },
                )
            }
        }
    }
}

/**
 * Text that shrinks its font (down to [minFontSize]) until it fits its bounds — used inside
 * fixed-size framed cards so long questions and large font scales never get clipped.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    minFontSize: TextUnit = 12.sp,
) {
    var scale by remember(text, style) { mutableFloatStateOf(1f) }
    var ready by remember(text, style) { mutableStateOf(false) }
    val base = if (style.fontSize.isSpecified) style.fontSize else 18.sp
    val lineHeight = if (style.lineHeight.isSpecified) style.lineHeight * scale else TextUnit.Unspecified
    Text(
        text = text,
        color = color,
        style = style.copy(fontSize = base * scale, lineHeight = lineHeight),
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.drawWithContent { if (ready) drawContent() },
        onTextLayout = { result ->
            if (result.hasVisualOverflow && (base.value * scale) > minFontSize.value) {
                scale *= 0.9f
            } else {
                ready = true
            }
        },
    )
}

/**
 * Modal panel DRAWN IN COMPOSE: rounded rectangle, plum gradient fill, gold border.
 * Grows with its content — no stretched frame image.
 */
@Composable
fun GemPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Brush.verticalGradient(listOf(JJColors.Plum, JJColors.NightPlum)))
            .border(3.dp, Brush.verticalGradient(listOf(JJColors.Topaz, Color(0xFFB8860B), JJColors.Topaz)), PanelShape)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(shadow = PlateTextShadow),
                color = JJColors.Topaz,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
        }
        content()
    }
}

/** Full-screen scrim that blocks touches and centers a [GemPanel]. */
@Composable
fun GemModal(title: String, content: @Composable ColumnScope.() -> Unit) {
    val blocker = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JJColors.Scrim)
            .clickable(interactionSource = blocker, indication = null, onClick = {})
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        FillColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            GemPanel(title = title, content = content)
        }
    }
}

/** The three standard overlay actions (pause / results). */
@Composable
fun PanelActions(
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    continueLabel: String = "Continue",
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PlateButton(text = continueLabel, onClick = onContinue, modifier = Modifier.fillMaxWidth(0.92f))
        PlateButton(text = "Restart", onClick = onRestart, modifier = Modifier.fillMaxWidth(0.92f))
        PlateButton(text = "Menu", onClick = onMenu, modifier = Modifier.fillMaxWidth(0.92f))
    }
}

/** Thin rounded progress bar drawn in Compose. */
@Composable
fun GemProgressBar(
    fraction: Float,
    description: String,
    modifier: Modifier = Modifier,
    fill: Color = JJColors.Sapphire,
) {
    val f = fraction.coerceIn(0f, 1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .semantics { contentDescription = description },
    ) {
        val r = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(color = JJColors.NightPlum, cornerRadius = r)
        if (f > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(fill, JJColors.Topaz)),
                size = Size(size.width * f, size.height),
                cornerRadius = r,
            )
        }
        drawRoundRect(color = JJColors.Topaz.copy(alpha = 0.8f), cornerRadius = r, style = Stroke(width = 2f))
    }
}

/** Rounded HUD chip (timer / pairs counter) with an optional leading icon. */
@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = JJColors.Topaz,
    @DrawableRes icon: Int? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(JJColors.NightPlum.copy(alpha = 0.88f))
            .border(2.dp, accent, RoundedCornerShape(50))
            .heightIn(min = 40.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = accent)
    }
}

/** Friendly fallback for missing/invalid navigation arguments or unavailable content. */
@Composable
fun FallbackScreen(message: String, onBack: () -> Unit) {
    ScreenFrame(title = "Oops!", onBack = onBack) {
        Spacer(Modifier.height(24.dp))
        GemPanel(title = "Nothing to show here") {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = JJColors.Pearl,
                textAlign = TextAlign.Center,
            )
            PlateButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(0.9f))
        }
        JesterParade(Modifier.weight(1f), mascots = listOf(Assets.mascotB))
    }
}

/** Simple padlock drawn in Compose (no icon library). */
@Composable
fun LockGlyph(modifier: Modifier = Modifier, color: Color = JJColors.Topaz) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bodyTop = h * 0.45f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.27f, h * 0.08f),
            size = Size(w * 0.46f, h * 0.7f),
            style = Stroke(width = w * 0.11f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, bodyTop),
            size = Size(w * 0.7f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
        )
        drawCircle(color = JJColors.NightPlum, radius = w * 0.07f, center = Offset(w / 2, bodyTop + h * 0.22f))
    }
}
