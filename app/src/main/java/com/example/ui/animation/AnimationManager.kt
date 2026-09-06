package com.example.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Material 3 Expressive Motion Specifications & Animation Manager.
 * Defines standard M3 easing curves, duration tokens, spring physics, and
 * reusable transition patterns for screen navigation and component state changes.
 */
object AnimationManager {

    // --- Material 3 Duration Tokens ---
    const val DurationShort1 = 50
    const val DurationShort2 = 100
    const val DurationShort3 = 150
    const val DurationShort4 = 200
    const val DurationMedium1 = 250
    const val DurationMedium2 = 300
    const val DurationMedium3 = 350
    const val DurationMedium4 = 400
    const val DurationLong1 = 450
    const val DurationLong2 = 500
    const val DurationLong3 = 550
    const val DurationLong4 = 600
    const val DurationExtraLong1 = 700

    // --- Material 3 Expressive Easing Curves ---
    // Emphasized Decelerate: Used for incoming elements to arrive swiftly and settle gracefully.
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    // Emphasized Accelerate: Used for outgoing elements to depart quickly and decisively.
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Emphasized: Symmetrical curve for state changes within the same container.
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    // Standard curves for subtle secondary UI transitions.
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardDecelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val StandardAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

    // --- Material 3 Expressive Spring Physics ---
    fun <T> expressiveBouncySpring(
        dampingRatio: Float = Spring.DampingRatioLowBouncy,
        stiffness: Float = Spring.StiffnessMediumLow
    ): AnimationSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

    fun <T> expressiveSpatialSpring(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): AnimationSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

    fun <T> expressiveSmoothSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): AnimationSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

    fun <T> snappySpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = Spring.StiffnessHigh
    ): AnimationSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

    // --- Standard Navigation Transition Patterns ---

    /**
     * Material 3 Fade Through Transition:
     * Recommended pattern for top-level navigation bar destinations (e.g. Home <-> Routines <-> Settings).
     * The outgoing destination fades out and scales down slightly, while the incoming
     * destination fades in and scales up from 94% to 100% using EmphasizedDecelerate easing.
     */
    fun materialFadeThrough(
        durationMillis: Int = DurationMedium2
    ): ContentTransform {
        val outgoingDuration = (durationMillis * 0.35f).toInt()
        val incomingDuration = durationMillis

        val enter = fadeIn(
            animationSpec = tween(
                durationMillis = incomingDuration,
                easing = EmphasizedDecelerate
            )
        ) + scaleIn(
            initialScale = 0.94f,
            transformOrigin = TransformOrigin(0.5f, 0.5f),
            animationSpec = tween(
                durationMillis = incomingDuration,
                easing = EmphasizedDecelerate
            )
        )

        val exit = fadeOut(
            animationSpec = tween(
                durationMillis = outgoingDuration,
                easing = EmphasizedAccelerate
            )
        ) + scaleOut(
            targetScale = 0.96f,
            transformOrigin = TransformOrigin(0.5f, 0.5f),
            animationSpec = tween(
                durationMillis = outgoingDuration,
                easing = EmphasizedAccelerate
            )
        )

        return enter togetherWith exit
    }

    /**
     * Material 3 Shared Axis Transition (Horizontal / X-Axis):
     * Recommended pattern for lateral or sequential transitions (e.g. stepping between adjacent tabs).
     * [forward] indicates moving to a higher index (slides from right to left).
     */
    fun materialSharedAxisX(
        forward: Boolean,
        slideOffsetFraction: Float = 0.20f,
        durationMillis: Int = DurationMedium3
    ): ContentTransform {
        val enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> if (forward) (fullWidth * slideOffsetFraction).toInt() else (-fullWidth * slideOffsetFraction).toInt() },
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedDecelerate)
        )

        val exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (forward) (-fullWidth * slideOffsetFraction).toInt() else (fullWidth * slideOffsetFraction).toInt() },
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(durationMillis = (durationMillis * 0.6f).toInt(), easing = EmphasizedAccelerate)
        )

        return enter togetherWith exit
    }

    /**
     * Material 3 Shared Axis Transition (Vertical / Y-Axis):
     * Recommended pattern for upward drill-ins, modal expansion, or inspector reveals.
     */
    fun materialSharedAxisY(
        forward: Boolean,
        slideOffsetFraction: Float = 0.15f,
        durationMillis: Int = DurationMedium3
    ): ContentTransform {
        val enter = slideInVertically(
            initialOffsetY = { fullHeight -> if (forward) (fullHeight * slideOffsetFraction).toInt() else (-fullHeight * slideOffsetFraction).toInt() },
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedDecelerate)
        )

        val exit = slideOutVertically(
            targetOffsetY = { fullHeight -> if (forward) (-fullHeight * slideOffsetFraction).toInt() else (fullHeight * slideOffsetFraction).toInt() },
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(durationMillis = (durationMillis * 0.6f).toInt(), easing = EmphasizedAccelerate)
        )

        return enter togetherWith exit
    }

    /**
     * Material 3 Shared Axis Transition (Scale / Z-Axis):
     * Recommended pattern for search activation, detail zoom, or focus state.
     */
    fun materialSharedAxisZ(
        forward: Boolean,
        durationMillis: Int = DurationMedium3
    ): ContentTransform {
        val enter = scaleIn(
            initialScale = if (forward) 0.88f else 1.10f,
            transformOrigin = TransformOrigin(0.5f, 0.5f),
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis, easing = EmphasizedDecelerate)
        )

        val exit = scaleOut(
            targetScale = if (forward) 1.08f else 0.90f,
            transformOrigin = TransformOrigin(0.5f, 0.5f),
            animationSpec = tween(durationMillis = (durationMillis * 0.5f).toInt(), easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(durationMillis = (durationMillis * 0.5f).toInt(), easing = EmphasizedAccelerate)
        )

        return enter togetherWith exit
    }

    // --- Component Enter/Exit Transitions ---

    fun componentEnterTransition(
        durationMillis: Int = DurationMedium2
    ): EnterTransition = fadeIn(
        animationSpec = tween(durationMillis, easing = EmphasizedDecelerate)
    ) + expandVertically(
        animationSpec = tween(durationMillis, easing = EmphasizedDecelerate),
        expandFrom = Alignment.Top
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(durationMillis, easing = EmphasizedDecelerate)
    )

    fun componentExitTransition(
        durationMillis: Int = DurationShort4
    ): ExitTransition = fadeOut(
        animationSpec = tween(durationMillis, easing = EmphasizedAccelerate)
    ) + shrinkVertically(
        animationSpec = tween(durationMillis, easing = EmphasizedAccelerate),
        shrinkTowards = Alignment.Top
    ) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(durationMillis, easing = EmphasizedAccelerate)
    )
}

/**
 * Screen Transition Types for Material 3 Screen Navigation.
 */
enum class ScreenTransitionType {
    FadeThrough,
    SharedAxisX,
    SharedAxisY,
    SharedAxisZ
}

/**
 * Reusable transition composable implementing Material 3 Expressive motion
 * for screen navigation backstack and tab switching.
 */
@Composable
fun <T> ExpressiveScreenTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    transitionType: ScreenTransitionType = ScreenTransitionType.FadeThrough,
    isForward: Boolean = true,
    content: @Composable AnimatedVisibilityScope.(targetState: T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            when (transitionType) {
                ScreenTransitionType.FadeThrough -> AnimationManager.materialFadeThrough()
                ScreenTransitionType.SharedAxisX -> AnimationManager.materialSharedAxisX(forward = isForward)
                ScreenTransitionType.SharedAxisY -> AnimationManager.materialSharedAxisY(forward = isForward)
                ScreenTransitionType.SharedAxisZ -> AnimationManager.materialSharedAxisZ(forward = isForward)
            }
        },
        label = "expressive_screen_transition",
        content = content
    )
}

/**
 * Reusable AnimatedVisibility composable pre-configured with Material 3 Expressive curves.
 */
@Composable
fun ExpressiveAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = AnimationManager.componentEnterTransition(),
    exit: ExitTransition = AnimationManager.componentExitTransition(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}

/**
 * Modifier for tactile press bounce feedback implementing Material 3 spring physics.
 * Gives cards, buttons, and action items a subtle physical compression when touched.
 */
@Composable
fun Modifier.expressiveBounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = AnimationManager.expressiveBouncySpring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expressive_bounce_scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Modifier that scales a component on press without handling the click event directly.
 */
@Composable
fun Modifier.expressivePressScale(
    scaleDown: Float = 0.97f
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = AnimationManager.expressiveBouncySpring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expressive_press_scale"
    )

    return this.scale(scale)
}

/**
 * Modifier that applies Material 3 Expressive spring physics when a component changes size.
 */
fun Modifier.expressiveAnimateContentSize(): Modifier = this.animateContentSize(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
)

/**
 * Staggered animated appearance container for list items (cards, rows, chips).
 * Animates into view with a slight slide-up and fade based on the item index.
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMillis: Long = 35L,
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val yOffsetAnim = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        delay(index * baseDelayMillis)
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = AnimationManager.DurationMedium2,
                easing = AnimationManager.EmphasizedDecelerate
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(index * baseDelayMillis)
        yOffsetAnim.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, yOffsetAnim.value.dp.roundToPx()) }
            .alpha(alphaAnim.value)
    ) {
        content()
    }
}

/**
 * Expressive organic breathing pulse effect for active voice recording,
 * listening states, or running automations.
 */
@Composable
fun ExpressivePulsingRing(
    isPulsing: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    minScale: Float = 1.0f,
    maxScale: Float = 1.35f
) {
    if (!isPulsing) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .alpha(pulseAlpha)
            .graphicsLayer {
                clip = false
            }
    )
}
