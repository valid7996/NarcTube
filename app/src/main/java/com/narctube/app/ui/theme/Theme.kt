package com.narctube.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = NarcViolet,
    onPrimary = NarcOnSurfaceDark,
    secondary = NarcCrimson,
    onSecondary = NarcOnSurfaceDark,
    background = NarcBackgroundDark,
    onBackground = NarcOnSurfaceDark,
    surface = NarcSurfaceDark,
    onSurface = NarcOnSurfaceDark,
    surfaceVariant = NarcSurfaceVariantDark,
    onSurfaceVariant = NarcOnSurfaceMutedDark,
    error = StatusError
)

private val LightColors = lightColorScheme(
    primary = NarcVioletDark,
    onPrimary = NarcSurfaceLight,
    secondary = NarcCrimson,
    onSecondary = NarcSurfaceLight,
    background = NarcBackgroundLight,
    onBackground = NarcOnSurfaceLight,
    surface = NarcSurfaceLight,
    onSurface = NarcOnSurfaceLight,
    surfaceVariant = NarcSurfaceVariantLight,
    onSurfaceVariant = NarcOnSurfaceMutedLight,
    error = StatusError
)

@Composable
fun NarcTubeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NarcTubeTypography,
        content = content
    )
}
