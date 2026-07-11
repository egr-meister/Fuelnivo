package com.fuelnivo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FuelAmber,
    onPrimary = TankBlack,
    primaryContainer = PaleFuelPanel,
    onPrimaryContainer = DeepGraphite,
    secondary = DeepAmber,
    onSecondary = SurfaceWhite,
    tertiary = InformationColor,
    onTertiary = SurfaceWhite,
    background = AppBackground,
    onBackground = DarkText,
    surface = SurfaceWhite,
    onSurface = DarkText,
    surfaceVariant = PaleFuelPanel,
    onSurfaceVariant = SteelGray,
    outline = DividerGray,
    outlineVariant = LightSteel,
    error = ErrorColor,
    onError = SurfaceWhite
)

private val DarkColors = darkColorScheme(
    primary = FuelAmber,
    onPrimary = TankBlack,
    primaryContainer = DeepGraphite,
    onPrimaryContainer = WarmYellow,
    secondary = WarmYellow,
    onSecondary = TankBlack,
    tertiary = InformationColor,
    onTertiary = SurfaceWhite,
    background = TankBlack,
    onBackground = AppBackground,
    surface = DeepGraphite,
    onSurface = AppBackground,
    surfaceVariant = SteelGray,
    onSurfaceVariant = LightSteel,
    outline = SteelGray,
    outlineVariant = SteelGray,
    error = ErrorColor,
    onError = SurfaceWhite
)

@Composable
fun FuelnivoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = FuelnivoTypography,
        content = content
    )
}
