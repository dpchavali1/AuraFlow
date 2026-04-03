package com.auraflow.garden.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColorScheme = darkColorScheme(
    primary             = CosmicViolet,
    onPrimary           = StarWhite,
    primaryContainer    = Color(0xFF2A2456),
    onPrimaryContainer  = StarWhite,
    secondary           = AuraTeal,
    onSecondary         = DeepSpace,
    secondaryContainer  = Color(0xFF0D2E3A),
    onSecondaryContainer = StarWhite,
    tertiary            = LumaGold,
    onTertiary          = DeepSpace,
    background          = DeepSpace,
    onBackground        = StarWhite,
    surface             = IslandSurface,
    onSurface           = StarWhite,
    surfaceVariant      = NightSurface,
    onSurfaceVariant    = MoonGray,
    outline             = TwilightRim,
    error               = PressureRed,
    onError             = StarWhite,
)

private val lightColorScheme = lightColorScheme(
    primary    = CosmicViolet,
    onPrimary  = Color.White,
    secondary  = AuraTeal,
    onSecondary = Color.White,
    tertiary   = LumaGold,
    onTertiary = DeepSpace,
    background = Color(0xFFF5F5FF),
    onBackground = DeepSpace,
    surface    = Color(0xFFFFFFFF),
    onSurface  = DeepSpace,
    error      = PressureRed,
    onError    = Color.White,
)

@Composable
fun AuraFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraFlowTypography,
        content = content,
    )
}
