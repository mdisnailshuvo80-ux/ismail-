package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CapCutColorScheme = darkColorScheme(
    primary = CapCutCyan,
    onPrimary = Color.Black,
    primaryContainer = CapCutSurfaceHighlight,
    onPrimaryContainer = CapCutCyan,
    secondary = CapCutPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A102A),
    onSecondaryContainer = CapCutPink,
    tertiary = CapCutPurple,
    onTertiary = Color.White,
    background = CapCutDarkBackground,
    onBackground = CapCutTextPrimary,
    surface = CapCutSurface,
    onSurface = CapCutTextPrimary,
    surfaceVariant = CapCutSurfaceVariant,
    onSurfaceVariant = CapCutTextSecondary,
    outline = CapCutBorder,
    error = CapCutRedPlayhead,
    onError = Color.White
)

@Composable
fun IsmailCapCutTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CapCutColorScheme,
        typography = Typography,
        content = content
    )
}
