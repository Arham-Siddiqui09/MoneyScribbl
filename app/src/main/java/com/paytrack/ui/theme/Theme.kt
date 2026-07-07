package com.paytrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = IndigoPrimary,
    onPrimary            = Color.White,
    primaryContainer     = IndigoContainer,
    onPrimaryContainer   = IndigoDark,
    secondary            = VioletAccent,
    onSecondary          = Color.White,
    secondaryContainer   = VioletLight,
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary             = IncomeGreen,
    onTertiary           = Color.White,
    tertiaryContainer    = IncomeGreenBg,
    onTertiaryContainer  = IncomeGreenDark,
    background           = AppBackground,
    onBackground         = AppTextPrimary,
    surface              = AppSurface,
    onSurface            = AppTextPrimary,
    surfaceVariant       = AppSurfaceMuted,
    onSurfaceVariant     = AppTextSecondary,
    outline              = AppBorder,
    outlineVariant       = AppSurfaceVariant,
    error                = ExpenseRed,
    onError              = Color.White,
    errorContainer       = ExpenseRedBg,
    onErrorContainer     = ExpenseRedDark,
    inverseSurface       = AppTextPrimary,
    inverseOnSurface     = AppSurface,
    inversePrimary       = IndigoLight
)

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF818CF8), // Lighter indigo for dark mode
    onPrimary            = Color(0xFF1E1B4B),
    primaryContainer     = IndigoDark,
    onPrimaryContainer   = IndigoLight,
    secondary            = Color(0xFFA78BFA), // Lighter violet for dark
    onSecondary          = Color(0xFF2E1065),
    secondaryContainer   = Color(0xFF4C1D95),
    onSecondaryContainer = VioletLight,
    tertiary             = Color(0xFF34D399), // Lighter emerald
    onTertiary           = Color(0xFF022C22),
    tertiaryContainer    = IncomeGreenDark,
    onTertiaryContainer  = IncomeGreenBg,
    background           = AppBackgroundDark,
    onBackground         = AppTextPrimaryDark,
    surface              = AppSurfaceDark,
    onSurface            = AppTextPrimaryDark,
    surfaceVariant       = AppSurfaceMutedDark,
    onSurfaceVariant     = AppTextSecondaryDark,
    outline              = AppBorderDark,
    outlineVariant       = AppSurfaceVariantDark,
    error                = Color(0xFFFB7185), // Lighter rose for dark
    onError              = Color(0xFF4C0519),
    errorContainer       = Color(0xFF881337),
    onErrorContainer     = Color(0xFFFFE4E6),
    inverseSurface       = AppTextPrimaryDark,
    inverseOnSurface     = AppSurfaceDark,
    inversePrimary       = IndigoPrimary
)

@Composable
fun PayTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
