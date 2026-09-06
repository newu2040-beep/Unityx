package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.core.design.adaptive.LocalCompactMode
import com.example.core.design.adaptive.ProvideAdaptiveLayout
import com.example.core.design.theme.DarkElevatedSurfaceTokens
import com.example.core.design.theme.LightElevatedSurfaceTokens
import com.example.core.design.theme.LocalElevatedSurfaces

private val UnityXDarkColorScheme = darkColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = PolishPrimary,
    onSecondary = Color.White,
    secondaryContainer = PolishSurfaceVariant,
    onSecondaryContainer = PolishTextPrimary,
    tertiary = PolishEmerald,
    onTertiary = Color.White,
    background = PolishBackground,
    onBackground = PolishTextPrimary,
    surface = PolishSurface,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = PolishTextSecondary,
    outline = PolishBorder,
    error = AlertCoral,
    onError = Color.White
)

private val UnityXLightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = PolishPrimary,
    onSecondary = Color.White,
    secondaryContainer = PolishSurfaceVariant,
    onSecondaryContainer = PolishTextPrimary,
    tertiary = PolishEmerald,
    onTertiary = Color.White,
    background = PolishBackground,
    onBackground = PolishTextPrimary,
    surface = PolishSurface,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = PolishTextSecondary,
    outline = PolishBorder,
    error = AlertCoral,
    onError = Color.White
)

@Composable
fun UnityXTheme(
    darkTheme: Boolean = false, // Professional Polish clean light theme
    dynamicColor: Boolean = false, // Keep consistent brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> UnityXDarkColorScheme
        else -> UnityXLightColorScheme
    }

    val elevatedTokens = if (darkTheme) DarkElevatedSurfaceTokens else LightElevatedSurfaceTokens

    ProvideAdaptiveLayout {
        val isCompact = LocalCompactMode.current
        val resolvedTypography = if (isCompact) CompactTypography else Typography

        CompositionLocalProvider(
            LocalElevatedSurfaces provides elevatedTokens
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = resolvedTypography,
                content = content
            )
        }
    }
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    UnityXTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

