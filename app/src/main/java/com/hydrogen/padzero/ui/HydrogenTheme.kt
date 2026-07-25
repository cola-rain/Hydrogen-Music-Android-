package com.hydrogen.padzero.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.hydrogen.padzero.AppGraph
import com.hydrogen.padzero.data.ThemeMode

private val HydrogenLight = lightColorScheme(
    primary = Color(0xFF1696D2),
    secondary = Color(0xFF6B8796),
    tertiary = Color(0xFF3B8EA5),
    background = Color(0xFFF7FBFD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6F0F4),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF11212B),
    onSurface = Color(0xFF102028),
)

private val HydrogenDark = darkColorScheme(
    primary = Color(0xFF6FD0FF),
    secondary = Color(0xFFA5C8D5),
    tertiary = Color(0xFF7AD7E7),
    background = Color(0xFF0B1216),
    surface = Color(0xFF10181D),
    surfaceVariant = Color(0xFF1B2A33),
    onPrimary = Color(0xFF00141E),
    onSecondary = Color(0xFF06121A),
    onBackground = Color(0xFFD7E6ED),
    onSurface = Color(0xFFD7E6ED),
)

@Composable
fun HydrogenTheme(content: @Composable () -> Unit) {
    val settings by AppGraph.settingsRepository.state.collectAsState()
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) HydrogenDark else HydrogenLight,
        content = content,
    )
}
