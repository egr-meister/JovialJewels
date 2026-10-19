package com.jovialjewels.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val JJColorScheme = darkColorScheme(
    primary = JJColors.Ruby,
    onPrimary = Color.White,
    secondary = JJColors.Sapphire,
    onSecondary = Color.White,
    tertiary = JJColors.Topaz,
    onTertiary = JJColors.NightPlum,
    background = JJColors.Amethyst,
    onBackground = JJColors.Pearl,
    surface = JJColors.Plum,
    onSurface = JJColors.Pearl,
    surfaceVariant = JJColors.NightPlum,
    onSurfaceVariant = JJColors.Lavender,
    error = JJColors.Garnet,
    onError = Color.White,
    outline = JJColors.Topaz,
)

/** Material 3 typography on the system sans-serif family (no bundled fonts). All sizes in sp. */
private val JJTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 1.2.sp),
)

@Composable
fun JovialJewelsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JJColorScheme,
        typography = JJTypography,
        content = content,
    )
}
