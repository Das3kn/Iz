package com.das3kn.iz.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BeehivePrimaryDark,
    secondary = BeehiveSecondary,
    tertiary = BeehiveAccent,
    background = BeehiveBackgroundDark,
    surface = BeehiveSurfaceDark,
    onPrimary = BeehiveOnPrimary,
    onSecondary = BeehiveOnSecondary,
    onBackground = BeehiveOnBackground,
    onSurface = BeehiveOnSurface,
    onSurfaceVariant = BeehiveOnSurfaceVariant,
    error = BeehiveError,
    onError = BeehiveOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = BeehivePrimary,
    secondary = BeehiveSecondary,
    tertiary = BeehiveAccent,
    background = BeehiveBackground,
    surface = BeehiveSurface,
    onPrimary = BeehiveOnPrimary,
    onSecondary = BeehiveOnSecondary,
    onBackground = BeehiveOnBackground,
    onSurface = BeehiveOnSurface,
    onSurfaceVariant = BeehiveOnSurfaceVariant,
    error = BeehiveError,
    onError = BeehiveOnPrimary
)

@Composable
fun IzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}