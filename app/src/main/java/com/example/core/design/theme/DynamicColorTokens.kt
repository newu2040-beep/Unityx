package com.example.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Dynamic Surface Elevation Tokens
 * Defines 6 elevation surface layers (0 to 5) with tonal blending over base surfaces.
 */
@Immutable
data class ElevatedSurfaceTokens(
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val outlineBorder: Color,
    val outlineSubtle: Color
)

val LightElevatedSurfaceTokens = ElevatedSurfaceTokens(
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FC),
    surfaceContainer = Color(0xFFF1F3F9),
    surfaceContainerHigh = Color(0xFFEBEFF6),
    surfaceContainerHighest = Color(0xFFE2E7F0),
    surfaceDim = Color(0xFFDCDFE7),
    surfaceBright = Color(0xFFFFFFFF),
    outlineBorder = Color(0xFFE1E2EC),
    outlineSubtle = Color(0xFFE8EAED).copy(alpha = 0.7f)
)

val DarkElevatedSurfaceTokens = ElevatedSurfaceTokens(
    surfaceContainerLowest = Color(0xFF0F1118),
    surfaceContainerLow = Color(0xFF161922),
    surfaceContainer = Color(0xFF1D212D),
    surfaceContainerHigh = Color(0xFF262B3A),
    surfaceContainerHighest = Color(0xFF303649),
    surfaceDim = Color(0xFF111319),
    surfaceBright = Color(0xFF383F54),
    outlineBorder = Color(0xFF2C3242),
    outlineSubtle = Color(0xFF252A38)
)

val LocalElevatedSurfaces = staticCompositionLocalOf { LightElevatedSurfaceTokens }

/**
 * Resolves elevated surface container color dynamically according to Material 3 elevation level (0..5)
 */
fun ColorScheme.elevatedSurfaceColor(
    elevation: Dp,
    tokens: ElevatedSurfaceTokens
): Color {
    return when {
        elevation <= 0.dp -> tokens.surfaceContainerLowest
        elevation <= 1.dp -> tokens.surfaceContainerLow
        elevation <= 3.dp -> tokens.surfaceContainer
        elevation <= 6.dp -> tokens.surfaceContainerHigh
        else -> tokens.surfaceContainerHighest
    }
}

/**
 * Helper to compute tonal alpha overlay for custom elevations
 */
fun surfaceColorAtElevation(
    baseColor: Color,
    tintColor: Color,
    elevation: Dp
): Color {
    if (elevation <= 0.dp) return baseColor
    val alpha = ((4.5f * kotlin.math.ln(elevation.value + 1)) + 2f) / 100f
    return tintColor.copy(alpha = alpha.coerceIn(0f, 1f)).compositeOver(baseColor)
}
