package com.auraflow.garden.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.auraflow.garden.data.model.WorldType

data class WorldTheme(
    val accentPrimary: Color,
    val accentSecondary: Color,
    val surfaceTint: Color,
    val bannerGradientStart: Color,
)

val worldThemeFor: Map<WorldType, WorldTheme> = mapOf(
    WorldType.WHISPERING_MEADOW  to WorldTheme(Color(0xFF06D6A0), Color(0xFF4CAF50), Color(0xFF0D1F15), Color(0xFF071A10)),
    WorldType.CRYSTAL_CAVERNS    to WorldTheme(Color(0xFF00B4D8), Color(0xFF7ECEF0), Color(0xFF0A1A22), Color(0xFF071218)),
    WorldType.FLOATING_ISLES     to WorldTheme(Color(0xFF8B5CF6), Color(0xFFC4B5FD), Color(0xFF180F2E), Color(0xFF100A20)),
    WorldType.DEEP_SEA           to WorldTheme(Color(0xFF0077B6), Color(0xFF48CAE4), Color(0xFF050F1A), Color(0xFF030810)),
    WorldType.GLITCH_CITY        to WorldTheme(Color(0xFFFF6B6B), Color(0xFFFFB703), Color(0xFF1F0F0F), Color(0xFF1A0808)),
    WorldType.CELESTIAL_SUMMIT   to WorldTheme(Color(0xFFFFB703), Color(0xFFF0F0F5), Color(0xFF1A1608), Color(0xFF120F04)),
)

val LocalWorldTheme = compositionLocalOf {
    worldThemeFor[WorldType.WHISPERING_MEADOW]!!
}
