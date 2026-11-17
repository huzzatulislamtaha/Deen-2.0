package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.core.i18n.AppLanguage
import com.example.domain.model.DeenFontConfig
import com.example.domain.model.ThemeMode

private val ColorPaletteSurfaceHighLight = androidx.compose.ui.graphics.Color(0xFFEBE3D3)

private val DeenLightColorScheme = lightColorScheme(
    primary = DeenEmerald,
    onPrimary = DeenWhiteCard,
    primaryContainer = DeenEmeraldContainer,
    onPrimaryContainer = DeenOnEmeraldContainer,
    secondary = DeenGold,
    onSecondary = DeenWhiteCard,
    secondaryContainer = DeenGoldSoft,
    onSecondaryContainer = DeenOnGoldContainer,
    tertiary = DeenEmeraldDark,
    onTertiary = DeenWhiteCard,
    background = DeenCreamBg,
    onBackground = DeenInk,
    surface = DeenOffWhiteSurface,
    onSurface = DeenInk,
    surfaceVariant = DeenWhiteCard,
    onSurfaceVariant = DeenInkMuted,
    surfaceContainerLowest = DeenWhiteCard,
    surfaceContainerLow = DeenCreamBg,
    surfaceContainer = DeenOffWhiteSurface,
    surfaceContainerHigh = ColorPaletteSurfaceHighLight,
    outline = DeenOutlineLight,
    error = DeenErrorLight,
    onError = DeenWhiteCard,
    errorContainer = DeenErrorContainerLight
)

private val DeenDarkColorScheme = darkColorScheme(
    primary = DeenEmeraldBright,
    onPrimary = DeenOnEmeraldDark,
    primaryContainer = DeenEmeraldContainerDark,
    onPrimaryContainer = DeenOnEmeraldContainerDark,
    secondary = DeenGoldBright,
    onSecondary = DeenOnGoldContainer,
    secondaryContainer = DeenGoldContainerDark,
    onSecondaryContainer = DeenOnGoldContainerDark,
    tertiary = DeenEmeraldBright,
    onTertiary = DeenOnEmeraldDark,
    background = DeenDarkBg,
    onBackground = DeenIvoryText,
    surface = DeenDarkSurface,
    onSurface = DeenIvoryText,
    surfaceVariant = DeenDarkCard,
    onSurfaceVariant = DeenMutedTextDark,
    surfaceContainerLowest = DeenDarkBg,
    surfaceContainerLow = DeenDarkSurface,
    surfaceContainer = DeenDarkCard,
    surfaceContainerHigh = DeenDarkSurfaceHigh,
    outline = DeenOutlineDark,
    error = DeenErrorDark,
    onError = DeenErrorContainerDark,
    errorContainer = DeenErrorContainerDark
)

val DeenShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DeenTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontConfig: DeenFontConfig = DeenFontConfig(),
    language: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DeenDarkColorScheme else DeenLightColorScheme
    val typography = remember(fontConfig, language) {
        buildDeenTypography(fontConfig = fontConfig, language = language)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalDeenFontConfig provides fontConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = DeenShapes,
            content = content
        )
    }
}
