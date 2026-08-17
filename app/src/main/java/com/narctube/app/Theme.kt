package com.narctube.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8B5CF6),
    secondary = androidx.compose.ui.graphics.Color(0xFF22D3EE),
    tertiary = androidx.compose.ui.graphics.Color(0xFFf59e0b)
)

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7C3AED),
    secondary = androidx.compose.ui.graphics.Color(0xFF0EA5E9),
    tertiary = androidx.compose.ui.graphics.Color(0xFFF59E0B)
)

@Composable
fun NarcTubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
