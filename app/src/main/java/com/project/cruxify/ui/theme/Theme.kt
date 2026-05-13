package com.project.cruxify.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CruxifyColorScheme = darkColorScheme(
    primary            = CruxPurple,
    onPrimary          = Color.White,
    primaryContainer   = CruxPurpleMuted,
    onPrimaryContainer = Color.White,

    secondary          = CruxPurpleLight,
    onSecondary        = CruxBackground,
    secondaryContainer = CruxSurfaceHigh,
    onSecondaryContainer = CruxTextPrimary,

    background         = CruxBackground,
    onBackground       = CruxTextPrimary,

    surface            = CruxSurface,
    onSurface          = CruxTextPrimary,
    surfaceVariant     = CruxSurfaceCard,
    onSurfaceVariant   = CruxTextSecondary,

    error              = CruxError,
    onError            = Color.White,
    errorContainer     = CruxErrorBg,
    onErrorContainer   = CruxError,

    outline            = CruxBorder,
    outlineVariant     = CruxBorderLight,
)

@Composable
fun CruxifyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CruxifyColorScheme,
        typography  = Typography,
        content     = content
    )
}