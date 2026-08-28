package com.idealista.challenge.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IdealistaGreen = Color(0xFF00E176)
private val IdealistaGreenDark = Color(0xFF00B860)
private val OnIdealistaGreen = Color(0xFF0A2E1C)
private val ScreenBackground = Color(0xFFF7F7F9)
private val SurfaceCard = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)

// Mirrors themes.xml/colors.xml (the app's brand palette) so Compose screens
// don't silently fall back to Material3's default baseline colors, which
// previously left the detail screen looking un-branded next to the XML list
// screen.
private val IdealistaColorScheme = lightColorScheme(
    primary = IdealistaGreenDark,
    onPrimary = OnIdealistaGreen,
    secondary = IdealistaGreen,
    onSecondary = OnIdealistaGreen,
    background = ScreenBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = ScreenBackground,
    onSurfaceVariant = TextSecondary,
)

@Composable
fun IdealistaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IdealistaColorScheme,
        typography = Typography(),
        content = content,
    )
}
