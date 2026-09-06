package com.example.core.design.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DisplayMode {
    Compact,   // Small display phones (< 360dp width or < 700dp height)
    Standard,  // Modern standard phones (360dp - 599dp)
    Expanded   // Tablets, foldables, or landscape displays (>= 600dp)
}

/**
 * Adaptive layout metrics specifically tuned to scale UI elements gracefully
 * across compact phones, standard displays, and foldables.
 */
@Immutable
data class AdaptiveDimensions(
    val isCompact: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.Standard,
    val horizontalPadding: Dp = 20.dp,
    val cardPadding: Dp = 18.dp,
    val itemSpacing: Dp = 12.dp,
    val sectionSpacing: Dp = 16.dp,
    val micButtonSize: Dp = 112.dp,
    val micGlowSize: Dp = 136.dp,
    val micIconSize: Dp = 48.dp,
    val bottomNavHeight: Dp = 76.dp,
    val typographyScale: Float = 1.0f,
    val screenWidthDp: Dp = 380.dp,
    val screenHeightDp: Dp = 800.dp
)

val CompactAdaptiveDimensions = AdaptiveDimensions(
    isCompact = true,
    displayMode = DisplayMode.Compact,
    horizontalPadding = 12.dp,
    cardPadding = 12.dp,
    itemSpacing = 8.dp,
    sectionSpacing = 10.dp,
    micButtonSize = 88.dp,
    micGlowSize = 112.dp,
    micIconSize = 36.dp,
    bottomNavHeight = 62.dp,
    typographyScale = 0.88f
)

val StandardAdaptiveDimensions = AdaptiveDimensions(
    isCompact = false,
    displayMode = DisplayMode.Standard,
    horizontalPadding = 20.dp,
    cardPadding = 18.dp,
    itemSpacing = 12.dp,
    sectionSpacing = 16.dp,
    micButtonSize = 112.dp,
    micGlowSize = 136.dp,
    micIconSize = 48.dp,
    bottomNavHeight = 76.dp,
    typographyScale = 1.0f
)

val ExpandedAdaptiveDimensions = AdaptiveDimensions(
    isCompact = false,
    displayMode = DisplayMode.Expanded,
    horizontalPadding = 32.dp,
    cardPadding = 22.dp,
    itemSpacing = 16.dp,
    sectionSpacing = 24.dp,
    micButtonSize = 128.dp,
    micGlowSize = 156.dp,
    micIconSize = 56.dp,
    bottomNavHeight = 84.dp,
    typographyScale = 1.05f
)

val LocalAdaptiveDimensions = staticCompositionLocalOf { StandardAdaptiveDimensions }
val LocalCompactMode = staticCompositionLocalOf { false }

/**
 * Adaptive container that measures incoming layout constraints
 * and provides responsive sizing metrics across all child Composables.
 */
@Composable
fun ProvideAdaptiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        val displayMode = when {
            width < 360.dp || height < 700.dp -> DisplayMode.Compact
            width >= 600.dp -> DisplayMode.Expanded
            else -> DisplayMode.Standard
        }

        val dimensions = when (displayMode) {
            DisplayMode.Compact -> CompactAdaptiveDimensions.copy(
                screenWidthDp = width,
                screenHeightDp = height
            )
            DisplayMode.Expanded -> ExpandedAdaptiveDimensions.copy(
                screenWidthDp = width,
                screenHeightDp = height
            )
            DisplayMode.Standard -> StandardAdaptiveDimensions.copy(
                screenWidthDp = width,
                screenHeightDp = height
            )
        }

        val isCompact = displayMode == DisplayMode.Compact

        CompositionLocalProvider(
            LocalAdaptiveDimensions provides dimensions,
            LocalCompactMode provides isCompact
        ) {
            content()
        }
    }
}

/**
 * Adaptive screen horizontal padding modifier
 */
fun Modifier.adaptiveScreenPadding(): Modifier = composed {
    val adaptive = LocalAdaptiveDimensions.current
    this.padding(horizontal = adaptive.horizontalPadding)
}

/**
 * Adaptive card internal padding modifier
 */
fun Modifier.adaptiveCardPadding(): Modifier = composed {
    val adaptive = LocalAdaptiveDimensions.current
    this.padding(adaptive.cardPadding)
}
