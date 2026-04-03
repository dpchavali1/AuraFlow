package com.auraflow.garden.ui.theme

import androidx.compose.ui.graphics.Color

// ── AuraFlow Brand Palette ──────────────────────────────────────────────────
val CosmicViolet  = Color(0xFF6C63FF)   // primary brand, UI chrome accents
val AuraTeal      = Color(0xFF00B4D8)   // secondary, energy states
val LumaGold      = Color(0xFFFFB703)   // tertiary, stars and rewards
val DeepSpace     = Color(0xFF0A0A1A)   // deepest screen background
val IslandSurface = Color(0xFF1A1A2E)   // card/surface layer
val PressureRed   = Color(0xFFE63946)   // error, fail states
val BloomGreen    = Color(0xFF06D6A0)   // success states

// ── Background layering (3-level depth) ─────────────────────────────────────
val Void          = Color(0xFF06061A)   // canvas background, deepest
val NightSurface  = Color(0xFF22223A)   // card surface layer 2, modal chrome, top bars
val TwilightRim   = Color(0xFF2E2E4A)   // card borders, dividers, track backgrounds

// ── Text hierarchy ───────────────────────────────────────────────────────────
val StarWhite     = Color(0xFFE8E8F0)   // primary text on dark surfaces
val MoonGray      = Color(0xFFAAAAAC)   // secondary text, captions
val DuskGray      = Color(0xFF66667A)   // tertiary text, disabled, placeholder

// ── Semantic states ──────────────────────────────────────────────────────────
val SuccessGreen  = BloomGreen          // level won
val WarningAmber  = LumaGold            // low energy warning
val ErrorRed      = PressureRed         // fail state
val InfoViolet    = Color(0xFF8B5CF6)   // tutorial hints, narrator accent

// ── Glow layers (used with Brush, NOT as solid backgrounds) ─────────────────
val GlowViolet    = Color(0x338B5CF6)   // 20% alpha violet glow halo
val GlowTeal      = Color(0x3300B4D8)   // 20% alpha teal glow halo
val GlowGold      = Color(0x33FFB703)   // 20% alpha gold glow halo

// ── Overlay scrims ───────────────────────────────────────────────────────────
val ModalScrim    = Color(0xCC06061A)   // 80% opaque, dialog/result overlay
val BannerScrim   = Color(0x990A0A18)   // 60% opaque, world banner gradient tail

// ── Legacy aliases (kept for compatibility) ──────────────────────────────────
val DarkBackground  = DeepSpace
val DarkSurface     = IslandSurface
val DarkOnSurface   = StarWhite
val DarkOnBackground = StarWhite
