package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantLilac,
    onPrimary = ElegantPurpleDeep,
    primaryContainer = ElegantPurpleDark,
    onPrimaryContainer = ElegantLilacLight,
    secondary = ElegantLilacLight,
    onSecondary = ElegantPurpleDeep,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = ElegantLilac,
    tertiary = ElegantGreen,
    onTertiary = Color(0xFF13380B),
    tertiaryContainer = ElegantGreenContainer,
    onTertiaryContainer = ElegantGreen,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceElevated,
    surfaceContainerHighest = DarkSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineSubtle,
    error = ElegantRed,
    onError = Color(0xFF690005),
    errorContainer = ElegantRedContainer,
    onErrorContainer = ElegantRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Elegant Dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Elegant Dark Theme applied across the entire app
    val colorScheme = ElegantDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
