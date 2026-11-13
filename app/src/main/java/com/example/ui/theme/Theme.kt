package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.example.core.i18n.AppLanguage

private val InstrumentPanelLightColorScheme = lightColorScheme(
    primary = FahsBrand,
    onPrimary = Color.White,
    primaryContainer = FahsBrandSoft,
    onPrimaryContainer = FahsInk,
    secondary = FahsInk,
    onSecondary = Color.White,
    secondaryContainer = FahsPaper,
    onSecondaryContainer = FahsInk,
    tertiary = FahsMutedText,
    onTertiary = Color.White,
    background = FahsPaper,
    onBackground = FahsInk,
    surface = FahsSurface,
    onSurface = FahsInk,
    surfaceVariant = Color(0xFFECEFED),
    onSurfaceVariant = FahsMutedText,
    outline = FahsHairline
)

private val InstrumentPanelDarkColorScheme = darkColorScheme(
    primary = FahsBrandDark,
    onPrimary = Color.White,
    primaryContainer = FahsInkElevated,
    onPrimaryContainer = FahsTextPrimaryDark,
    secondary = FahsProgressArcNeutral,
    onSecondary = FahsInk,
    secondaryContainer = FahsSurfaceDark,
    onSecondaryContainer = FahsTextPrimaryDark,
    tertiary = FahsTextMutedDark,
    onTertiary = FahsInk,
    background = FahsPaperDark,
    onBackground = FahsTextPrimaryDark,
    surface = FahsSurfaceDark,
    onSurface = FahsTextPrimaryDark,
    surfaceVariant = FahsInkElevated,
    onSurfaceVariant = FahsTextMutedDark,
    outline = FahsHairlineDark
)

@Composable
fun FahsTheme(
    language: AppLanguage = AppLanguage.FR,
    darkTheme: Boolean = isSystemInDarkTheme(),
    isDarkInstrumentScreen: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InstrumentPanelDarkColorScheme else InstrumentPanelLightColorScheme
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = getTypography(language)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                // Dark status bar icons on light Paper screens; white status bar icons on dark Ink scan screen or in Dark Mode
                controller.isAppearanceLightStatusBars = !(darkTheme || isDarkInstrumentScreen)
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
