package com.example.core.design.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.design.theme.LocalElevatedSurfaces
import com.example.core.design.theme.elevatedSurfaceColor

enum class ElevatedLevel(val elevation: Dp) {
    Level0(0.dp),
    Level1(1.dp),
    Level2(3.dp),
    Level3(6.dp),
    Level4(8.dp),
    Level5(12.dp)
}

/**
 * Material 3 Elevated Expressive Card
 * Uses dynamic surface tokens and subtle border contrast for elevated components.
 */
@Composable
fun ElevatedExpressiveCard(
    modifier: Modifier = Modifier,
    level: ElevatedLevel = ElevatedLevel.Level1,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val surfaceTokens = LocalElevatedSurfaces.current
    val resolvedContainerColor = containerColor ?: MaterialTheme.colorScheme.elevatedSurfaceColor(
        elevation = level.elevation,
        tokens = surfaceTokens
    )

    val resolvedBorder = border ?: BorderStroke(1.dp, surfaceTokens.outlineBorder)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = resolvedContainerColor,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = level.elevation,
                pressedElevation = (level.elevation + 2.dp)
            ),
            border = resolvedBorder
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = resolvedContainerColor,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = level.elevation),
            border = resolvedBorder
        ) {
            content()
        }
    }
}

/**
 * Pure Surface implementation with dynamic token elevation
 */
@Composable
fun ElevatedExpressiveSurface(
    modifier: Modifier = Modifier,
    level: ElevatedLevel = ElevatedLevel.Level1,
    shape: Shape = RoundedCornerShape(14.dp),
    border: BorderStroke? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceTokens = LocalElevatedSurfaces.current
    val surfaceColor = MaterialTheme.colorScheme.elevatedSurfaceColor(
        elevation = level.elevation,
        tokens = surfaceTokens
    )
    val finalBorder = border ?: BorderStroke(1.dp, surfaceTokens.outlineBorder)

    Box(
        modifier = modifier
            .shadow(
                elevation = level.elevation,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(surfaceColor)
            .border(finalBorder, shape)
    ) {
        content()
    }
}
