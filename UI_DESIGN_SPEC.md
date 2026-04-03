# AuraFlow: The Kinetic Garden — UI Design Specification
# Version 1.0 | Dark-first, WCAG AA, Compose Multiplatform

---

## SCOPE AND CONVENTIONS

This document specifies the complete visual upgrade for AuraFlow's UI chrome. It covers
everything that is NOT the game canvas (node bubbles, link strokes, energy dots). The
game canvas rendering defined in GameCanvas.kt is out of scope and must not be altered.

All measurements are in dp unless labeled sp or px.
All colors are given as ARGB hex literals for direct use in Kotlin:
  Color(0xFFRRGGBB) — fully opaque
  Color(0xCCRRGGBB) — partially transparent (CC = 80% alpha)

Corner radii use RoundedCornerShape(Xdp).
All interactive targets must be at minimum 48x48dp (WCAG 2.5.5 touch target).
All text must meet WCAG AA contrast (4.5:1 normal text, 3:1 large text ≥18sp/14sp bold).

---

## 1. COLOR SYSTEM

### 1.1 Foundation — Existing Brand Colors (keep as-is in Color.kt)

These are already defined and must not be renamed. The spec below extends them.

  CosmicViolet  #6C63FF  — primary brand, UI chrome accents
  AuraTeal      #00B4D8  — secondary, energy states
  LumaGold      #FFB703  — tertiary, stars and rewards
  DeepSpace     #0A0A1A  — deepest background layer
  IslandSurface #1A1A2E  — card/surface layer
  PressureRed   #E63946  — error, fail states
  BloomGreen    #06D6A0  — success states

### 1.2 Extended Palette — New Tokens (add to Color.kt)

These fill gaps left by the existing palette.

```
// Background layering (3-level depth system)
val Void          = Color(0xFF06061A)   // Canvas background, deepest
val DeepSpace     = Color(0xFF0A0A1A)   // Screen background (existing)
val IslandSurface = Color(0xFF1A1A2E)   // Card surface layer 1 (existing)
val NightSurface  = Color(0xFF22223A)   // Card surface layer 2, modal chrome
val TwilightRim   = Color(0xFF2E2E4A)   // Card border, dividers, subtle strokes

// On-color text and icons
val StarWhite     = Color(0xFFE8E8F0)   // Primary text on dark surfaces (existing as DarkOnSurface)
val MoonGray      = Color(0xFFAAAAAC)   // Secondary text, captions
val DuskGray      = Color(0xFF66667A)   // Tertiary text, disabled, placeholder

// Semantic states
val SuccessGreen  = Color(0xFF06D6A0)   // Same as BloomGreen — level won
val WarningAmber  = Color(0xFFFFB703)   // Same as LumaGold — low energy warning
val ErrorRed      = Color(0xFFE63946)   // Same as PressureRed — fail state
val InfoViolet    = Color(0xFF8B5CF6)   // Tutorial hints, narrator speaker label

// Glow layers (used with graphicsLayer { alpha } or Brush, NOT as solid backgrounds)
val GlowViolet    = Color(0x338B5CF6)   // 20% alpha, violet glow halo
val GlowTeal      = Color(0x3300B4D8)   // 20% alpha, teal glow halo
val GlowGold      = Color(0x33FFB703)   // 20% alpha, gold glow halo

// Overlay scrims
val ModalScrim    = Color(0xCC06061A)   // 80% opaque, dialog background
val CardScrim     = Color(0xDD0A0A18)   // 87% opaque, result overlay (current ResultOverlay)
val BannerScrim   = Color(0x990A0A18)   // 60% opaque, world banner gradient tail
```

### 1.3 Material3 Color Scheme Mapping

The darkColorScheme in AuraFlowTheme.kt maps to these values. No changes needed to
the existing mapping — this spec adds CompositionLocals for world-specific accents
(Section 7) on top of the base scheme.

```
primary             → CosmicViolet  #6C63FF
onPrimary           → StarWhite     #E8E8F0
primaryContainer    → Color(0xFF2A2456)   // deep violet container for stage cards
onPrimaryContainer  → StarWhite     #E8E8F0
secondary           → AuraTeal      #00B4D8
onSecondary         → DeepSpace     #0A0A1A
secondaryContainer  → Color(0xFF0D2E3A)   // deep teal container for Zen button
onSecondaryContainer → StarWhite    #E8E8F0
tertiary            → LumaGold      #FFB703
surface             → IslandSurface #1A1A2E
onSurface           → StarWhite     #E8E8F0
surfaceVariant      → NightSurface  #22223A
onSurfaceVariant    → MoonGray      #AAAAAC
outline             → TwilightRim   #2E2E4A
background          → DeepSpace     #0A0A1A
onBackground        → StarWhite     #E8E8F0
error               → ErrorRed      #E63946
onError             → StarWhite
```

### 1.4 Per-World Accent Colors

Each world overrides a small set of UI chrome tokens via a CompositionLocal called
`LocalWorldTheme`. The data class and provider are defined in Section 7.

| World              | Accent Primary | Accent Secondary | Surface Tint | Banner Gradient Start |
|--------------------|----------------|------------------|--------------|-----------------------|
| WHISPERING_MEADOW  | #06D6A0        | #4CAF50          | #0D1F15      | #071A10               |
| CRYSTAL_CAVERNS    | #00B4D8        | #7ECEF0          | #0A1A22      | #071218               |
| FLOATING_ISLES     | #8B5CF6        | #C4B5FD          | #180F2E      | #100A20               |
| DEEP_SEA           | #0077B6        | #48CAE4          | #050F1A      | #030810               |
| GLITCH_CITY        | #FF6B6B        | #FFB703          | #1F0F0F      | #1A0808               |
| CELESTIAL_SUMMIT   | #FFB703        | #F0F0F5          | #1A1608      | #120F04               |

---

## 2. HOME SCREEN REDESIGN

The current HomeScreen.kt has a flat Column layout with a plain text header, no world
visual identity, and a three-column grid of identically-sized boxes. The redesign gives
the screen a world-aware hero section and a richer grid.

### 2.1 Screen Layout Structure

```
Screen (fillMaxSize, background = DeepSpace)
  ├── StatusBarSpacer (windowInsetsPadding top)
  ├── WorldHeroBanner (height = 220dp)
  ├── ProgressStrip (height = 48dp)
  ├── StageGrid (weight = 1f, LazyVerticalGrid)
  └── BottomActionBar (height = 72dp + navigationBarInsets)
```

### 2.2 WorldHeroBanner Component

The banner is a Box that fills width and is 220dp tall.

Background treatment:
- A full-bleed Box with a vertical Brush.linearGradient:
    colors = listOf(worldTheme.bannerGradientStart, Color.Transparent)
    startY = 0f, endY = Float.POSITIVE_INFINITY
- Behind the gradient sits an optional decorative Canvas layer drawn at 15% alpha that
  renders slowly drifting particle dots in the world accent color (use the existing
  ParticleSystem infrastructure). This layer is skip-rendered when
  WindowSizeClass == COMPACT and Reduce Motion is on.

Overlaid content (inside the Box, aligned to BottomStart):
- World icon glyph: a 48x48dp Icon or Canvas-drawn symbol (see Section 7 for per-world
  symbols). Placed at padding(start=20dp, bottom=64dp).
- World name: headlineLarge (32sp, SemiBold). Color = worldTheme.accentPrimary.
  padding(start=20dp, bottom=40dp).
- Subtitle: "Stage range X–Y". bodySmall (12sp). Color = MoonGray.
  padding(start=20dp, bottom=24dp).

Top-right icon row (aligned to TopEnd, padding=8dp):
- Store icon: IconButton 48x48dp. Icon tint = StarWhite. Uses Icons.Filled.ShoppingCart.
- Settings icon: IconButton 48x48dp. Icon tint = StarWhite. Uses Icons.Filled.Settings.

App title treatment: REMOVE the "Aura Flow / The Kinetic Garden" text header from its
current Row. The world banner fully replaces it. The app name appears only on the store
listing and the settings screen title.

World selector: A horizontally scrolling row of 6 world indicator pills sits at the
BOTTOM of the banner, inside the banner box (aligned BottomCenter, padding bottom=8dp).
Each pill is 10dp tall, 28dp wide when selected, 10dp wide when not selected, corner
radius 5dp. Selected pill color = worldTheme.accentPrimary. Unselected = TwilightRim.
Animate width changes with animateDpAsState (spring, stiffness=400f). This row acts as
both a pager indicator and a tap-to-jump navigation control.

### 2.3 ProgressStrip Component

A full-width Row at height=48dp, padding horizontal=20dp.

Left side: "${uiState.clearedCount} / ${uiState.totalStages} stages cleared"
  Style: labelLarge (14sp, Medium). Color = StarWhite.

Right side: a thin horizontal progress bar, 120dp wide, 6dp tall, corner radius 3dp.
  Track color: TwilightRim.
  Fill color: Brush.horizontalGradient(worldTheme.accentSecondary, worldTheme.accentPrimary).
  Fraction = clearedCount.toFloat() / totalStages.toFloat()
  Animate fraction with animateFloatAsState (tween 600ms, FastOutSlowIn easing).

Accessibility: semantics contentDescription = "World progress: ${clearedCount} of
${totalStages} stages cleared".

### 2.4 StageGrid and StageCard Redesign

Grid spec:
  columns = GridCells.Fixed(3)
  horizontalArrangement = Arrangement.spacedBy(10dp)
  verticalArrangement = Arrangement.spacedBy(10dp)
  contentPadding = PaddingValues(horizontal=16dp, vertical=12dp)

StageCard dimensions:
  Size: fillMaxWidth with aspectRatio(1f) — fills the cell rather than fixed 100dp.
  Corner radius: 16dp (up from 12dp).
  Minimum touch target: the entire card surface is clickable.

Locked card appearance:
  Background: NightSurface #22223A
  Alpha: 0.45f on the entire Box
  Center content: Lock icon (Icons.Filled.Lock), size=20dp, tint=DuskGray
  No star row shown.
  Border: 1dp stroke, color=TwilightRim

Unlocked, zero-star card (attempted but incomplete):
  Background: Brush.radialGradient(
    center = Offset(0.5f * width, 0f),   // top-center
    radius = width * 1.2f,
    colors = listOf(worldTheme.accentPrimary.copy(alpha=0.15f), IslandSurface)
  )
  Border: 1dp, color = worldTheme.accentPrimary.copy(alpha=0.35f)

Cleared card (bestStars >= 1):
  Background: same radialGradient as above but alpha 0.25f at center
  Border: 1dp, color = worldTheme.accentPrimary.copy(alpha=0.60f)
  On hover/pressed: scale 0.96f with graphicsLayer, spring stiffness=600f

Stage number label:
  Text: "${stageProgress.stageId}"
  Style: titleLarge (22sp, Normal). Color = StarWhite.
  Position: align Center, offset slightly upward by padding(bottom=14dp) from center.

Star row:
  3 icons, size=14dp each (up from 12dp), spacing 2dp horizontal.
  Filled star tint: LumaGold #FFB703
  Empty star tint: DuskGray #66667A (use Icons.Outlined.StarBorder)
  Position: align BottomCenter, padding(bottom=8dp) inside the card.

NEW — "Personal best" micro-label on 3-star cleared cards:
  Text: "PERFECT". labelSmall (11sp, Medium). Color = LumaGold.
  Shown above the star row only when bestStars == 3.

Skeleton loading state (replaces plain "Loading..." text):
  Show 15 StageCard-sized Box composables in the grid.
  Each has background = TwilightRim, corner radius = 16dp.
  Animate alpha between 0.4f and 0.8f with infiniteRepeatable tween 900ms Reverse.
  Each card delays its animation by (index * 40ms) for a staggered shimmer effect.
  Use Modifier.animateItemPlacement() when transitioning from skeleton to real data.

### 2.5 BottomActionBar

A Row pinned to the screen bottom, height=72dp, with windowInsetsPadding(bottom).
Background: NightSurface with a vertical gradient overlay at top edge:
  Brush.verticalGradient(Color.Transparent, NightSurface, endY=24f, startY=0f)
  (creates a fade-up shadow that separates the bar from the grid)

Zen Mode button:
  Occupies the full width minus padding horizontal=16dp.
  Height: 48dp. Corner radius: 24dp (full pill shape).
  Background: Brush.linearGradient(
    colors = listOf(worldTheme.accentSecondary.copy(alpha=0.25f), worldTheme.accentPrimary.copy(alpha=0.25f))
  )
  Border: 1dp, color = worldTheme.accentPrimary.copy(alpha=0.60f)
  Left icon: a custom "leaf" or "infinity" glyph from the existing icon set, 20dp, tint=worldTheme.accentPrimary
  Label: "Zen Mode". labelLarge (14sp, Medium). Color=worldTheme.accentPrimary.
  Right icon: Icons.AutoMirrored.Filled.ArrowForward, 16dp, tint=worldTheme.accentPrimary.copy(alpha=0.7f)

Pressed state: scale 0.96f, spring stiffness=500f.
Focus indicator: 2dp outline, color=worldTheme.accentPrimary, offset=2dp.

---

## 3. GAME SCREEN REDESIGN

### 3.1 Top Bar

Current: a flat Row with back button, title text, score text, undo/reset icon buttons,
and LumaComposable. Everything shares a single invisible background.

Redesign:
  Background: NightSurface at 90% opacity (Color(0xE622223A)).
  Bottom border: 1dp, color = TwilightRim.
  Height: 56dp total row. windowInsetsPadding(top).

Back button:
  Left edge, padding(start=4dp).
  Icon: Icons.AutoMirrored.Filled.ArrowBack, tint=StarWhite, size=24dp.
  Touch target: 48x48dp.

Stage title (center slot, weight=1f):
  Text: level.title or "Stage $stageId".
  Style: titleMedium (16sp, Medium). Color = StarWhite.
  maxLines=1, overflow=Ellipsis.

Score chip (right of title):
  A small pill-shaped container, height=28dp, horizontal padding=10dp.
  Background: CosmicViolet.copy(alpha=0.20f).
  Border: 1dp, color=CosmicViolet.copy(alpha=0.50f). Corner radius=14dp.
  Text: "${state.score}". labelLarge (14sp, Medium). Color=CosmicViolet.
  Animates score increments: when score changes, run a brief scale pulse 1.0f→1.18f→1.0f
  using animateFloatAsState with spring (dampingRatio=0.4f, stiffness=800f).

Undo button:
  Icon: Icons.AutoMirrored.Filled.Undo, size=22dp.
  Tint when enabled: StarWhite. Tint when disabled: DuskGray.
  Touch target: 44x44dp.

Reset button:
  Icon: Icons.Filled.Refresh, size=22dp.
  Same enabled/disabled tinting as undo.

Luma position:
  Stays rightmost in the top bar per the current implementation (avoids canvas overlap).
  LumaComposable receives padding(end=8dp).
  Keep current LumaComposable visual — this spec does not change the Luma character art.

### 3.2 Energy Bar

Current: 10dp tall, full width, plain. Visually tiny relative to screen importance.

Redesign:
  Container: Row, fillMaxWidth, height=24dp, padding(horizontal=16dp, vertical=4dp).
  The actual bar track sits inside this container at height=6dp (down from 10dp) and
  takes weight=1f with a lightning bolt icon at start and a label at end.

Left icon: a lightning bolt SVG or ImageVector, size=12dp, tint = current barColor.
  Placed with padding(end=6dp). (Use a simple vector drawable — specify this as a
  custom icon to be added to the res/drawable set.)

Track background: RoundedCornerShape(3dp), color=TwilightRim.
Fill: RoundedCornerShape(3dp), Brush.horizontalGradient per existing logic:
  > 30% energy: listOf(#FFFFFF55, BloomGreen, BloomGreen.copy(alpha=0.75f))
  10–30% energy: listOf(#FFFFFF55, WarningAmber, WarningAmber.copy(alpha=0.75f))
  <= 10% energy: listOf(#FFFFFF55, ErrorRed, ErrorRed.copy(alpha=0.75f)) + pulse animation
  (Keep existing animateColorAsState and pulse logic.)

Right label: "${(fraction * 100).toInt()}%". labelSmall (11sp, Medium). Color = MoonGray.
  min-width=28dp so it doesn't jump layout. Use TextAlign.End.

Glow enhancement at <= 10%: add a Box behind the fill bar with Modifier.blur(6.dp) and
the fill color at 50% alpha to create a soft glow bleed. The blur modifier requires
API 31+ on Android; gate it with a platform expect function isBlurSupported().
On iOS: use graphicsLayer { renderEffect = BlurEffect(6f, 6f) } via the Skia backend.

### 3.3 Result Overlay — Win State

Current: a centered Box with a title, score text, and stacked buttons. Uses Color(0xEE0A0A18).

Win state redesign:
  Container Box: fillMaxWidth, padding horizontal=24dp.
  Background: ModalScrim (Color(0xCC06061A)).
  Corner radius: 24dp.
  Border: 1.5dp, color = SuccessGreen.copy(alpha=0.50f).
  Inner padding: 28dp.

Victory icon: a Canvas-drawn radiant star burst, diameter=56dp, rendered in LumaGold.
  4 long rays + 4 short rays, all emanating from center. Animate with infiniteRepeatable
  slow rotation (360°, tween 8000ms, LinearEasing). This adds visual celebration weight.
  Place above the title text, verticalArrangement spacing=16dp.

Title: "Level Complete!". headlineMedium (28sp, SemiBold). Color = SuccessGreen.
  Apply a Brush.linearGradient text fill: listOf(SuccessGreen, AuraTeal). Use the
  TextStyle.brush API available in Compose 1.3+.

Score row: Row, horizontalArrangement=Center, spacing=8dp.
  Left label: "SCORE". labelSmall (11sp, Medium). Color=MoonGray.
  Right value: "${state.score}". headlineSmall (24sp, SemiBold). Color=StarWhite.
  If score > 0, wrap the value in the score-pulse animation described in 3.1.

Star award row: 3 large star icons, size=36dp each, spacing=8dp.
  Stars animate in sequentially: star 1 after 300ms, star 2 after 500ms, star 3 after 700ms.
  Each animates scale 0f→1.2f→1.0f (spring, dampingRatio=0.5f, stiffness=300f).
  Filled star: LumaGold with Modifier.shadow(elevation=8dp, spotColor=LumaGold).
  Empty star: DuskGray.
  This requires passing starsAwarded (Int 0–3) into ResultOverlay.

Primary button ("Next Level"):
  Height=52dp, fillMaxWidth, corner=26dp (full pill).
  Background: Brush.linearGradient(CosmicViolet, AuraTeal).
  Label: labelLarge (14sp, Medium). Color=StarWhite.
  No border. Elevation: Modifier.shadow(8dp, spotColor=CosmicViolet, shape=RoundedCornerShape(26dp)).

Secondary button ("Replay"):
  Height=48dp, fillMaxWidth, corner=24dp.
  Background: Color.Transparent.
  Border: 1dp, color=TwilightRim.
  Label: labelLarge. Color=MoonGray.

Home button:
  Height=44dp, fillMaxWidth, corner=22dp.
  Background: Color.Transparent.
  Label: "Home". labelMedium (12sp). Color=DuskGray.
  No border.

Button spacing: verticalArrangement=Arrangement.spacedBy(8dp).

Dismiss behavior: tapping the scrim area outside the card (the full-screen backdrop)
does nothing on win — the player must choose an action.

### 3.4 Result Overlay — Fail State

Same container, corner, padding specs as win.
Background: ModalScrim.
Border: 1.5dp, color=ErrorRed.copy(alpha=0.50f).

Fail icon: a depleted energy bolt drawn in Canvas, diameter=48dp. Use DuskGray base
with a single broken/jagged crack drawn in ErrorRed. Static (no rotation animation).

Title: "Out of Energy". headlineMedium. Color=ErrorRed.
  Text brush gradient: listOf(ErrorRed, Color(0xFFFF8800)) (red→orange).

Score row: same layout as win. Only show if score > 0.

Ad offer row (ONLY shown in Daily Challenge mode, ONLY after fail):
  A full-width Row, height=56dp, corner=12dp.
  Background: Color(0xFF1A1400).
  Border: 1dp, color=LumaGold.copy(alpha=0.40f).
  Left: a 24dp icon (video play symbol), tint=LumaGold.
  Text: "Watch an ad to restore 50% energy". bodySmall (12sp). Color=MoonGray.
  Right: a small pill "WATCH" button, height=32dp, corner=16dp, background=LumaGold,
  text color=DeepSpace, labelSmall (11sp).
  This row must be hidden (gone, not invisible) outside Daily Challenge. Confirm
  with viewModel.state.value.isDailyChallengeMode before showing.

Primary button ("Try Again"):
  Same as win primary but Background: Brush.linearGradient(ErrorRed, Color(0xFFFF6B6B)).

Secondary: none in fail state.
Home button: same spec as win.

### 3.5 Tutorial Overlay

Current: a centered Box with a pulsing violet border, HINT label, body text, dismiss caption.

Redesign:
  Remove the full-width fillMaxWidth. Use wrapContentWidth() with max 320dp.
  Position: align=Alignment.Center (keep current positioning).
  Corner radius: 20dp (up from 16dp).
  Background: ModalScrim with a 12dp blur behind (same platform gate as energy bar).
  Outer glow: Modifier.shadow(elevation=16dp, spotColor=InfoViolet,
    shape=RoundedCornerShape(20dp)) — creates a violet halo on supporting platforms.
  Border: 1.5dp, color = InfoViolet, pulsing alpha (keep existing 0.6→1.0 animation).
  Inner padding: 20dp all sides.

Top label row: Row, verticalAlignment=CenterVertically, spacing=6dp.
  Left: a 6dp filled circle drawn via Canvas, color=InfoViolet, pulsing scale 1.0→1.4.
  Text "HINT": labelSmall (11sp, Medium). Color=InfoViolet. LetterSpacing=2.sp.

Divider: horizontal 1dp line, color=TwilightRim, padding(vertical=10dp).

Hint text: bodyLarge (16sp, Normal). Color=StarWhite. TextAlign.Center.
  maxLines=4. Soft-wrap enabled.

Dismiss row: aligned BottomCenter, padding(top=12dp).
  Text "Tap anywhere to dismiss". labelSmall (11sp). Color=DuskGray. TextAlign.Center.
  Animate text alpha 0.5→1.0 looping with tween 1200ms to draw eye to it.

### 3.6 Narrator Dialogue Overlay (DialogueOverlay)

Current: slides in from bottom, RoundedCornerShape(12dp), background=Color(0xCC1A1A2E).

Redesign (maintains slide-in animation):
  Corner radius: 16dp (top corners only — BottomStart=0, BottomEnd=0, TopStart=16, TopEnd=16)
    since it anchors to the screen bottom.
  Background: Color(0xEE0D0D22) — slightly darker than current.
  Top accent bar: a 3dp tall Box at the very top of the column, fillMaxWidth.
    Color = speaker accent:
      "warden" → CosmicViolet
      "luma"   → LumaGold
      else     → AuraTeal
    Corner radius on top bar: RoundedCornerShape(topStart=16dp, topEnd=16dp).

Speaker label: labelSmall (11sp, Medium). LetterSpacing=1.5.sp. Color=accent color above.
  Padding(start=16dp, top=12dp, bottom=4dp).

Message text: bodyLarge (16sp, Normal). Color=StarWhite.
  Padding(horizontal=16dp, bottom=16dp). maxLines=5.

Dismiss affordance: a 32x4dp pill centered at the very top of the overlay (above accent bar).
  Background: StarWhite.copy(alpha=0.3f). Corner radius=2dp. No label.
  This follows the iOS/Material bottom sheet dismiss handle convention.

---

## 4. TYPOGRAPHY SYSTEM

### 4.1 Font Selection

The existing AuraFlowTypography in Typography.kt uses the default Material3 system font
stack. This spec adds a custom font recommendation that must be bundled as assets.

Primary display font (headlines, world names, result titles):
  Recommended: "Outfit" (Google Fonts, OFL license). Variable weight 300–700.
  Fallback: system-ui, sans-serif.
  Usage: displaySmall through headlineLarge.

Body and UI font (all other text, labels, scores):
  Recommended: "DM Sans" (Google Fonts, OFL license). Variable weight 300–700.
  Fallback: system-ui, sans-serif.
  Usage: titleLarge through labelSmall.

Monospace (scores, counters when animating digits):
  Recommended: "JetBrains Mono" or "DM Mono" (Google Fonts, OFL license).
  Usage: score display in game top bar and result overlay ONLY. This ensures digit
  widths are stable during counter animations and prevents layout jank.

To integrate: add font files to shared/src/commonMain/resources/font/ and reference them
via FontFamily in Typography.kt. Use androidx.compose.ui.text.font.Font with the KMP
resource system from compose-multiplatform resources (composeResources).

### 4.2 Size Scale Reference

All sizes match the existing AuraFlowTypography definitions. This table clarifies
intended usage in the redesigned screens:

  displaySmall   36sp  — app title (settings screen only)
  headlineLarge  32sp  — world name in banner
  headlineMedium 28sp  — result overlay title (win/fail)
  headlineSmall  24sp  — score value in result overlay, stage number in stage card
  titleLarge     22sp  — stage number (large format card)
  titleMedium    16sp  — game screen top bar stage title
  titleSmall     14sp  — world subtitle in banner
  bodyLarge      16sp  — tutorial hint text, narrator message, body copy
  bodyMedium     14sp  — general body text
  bodySmall      12sp  — progress strip label, stage range subtitle
  labelLarge     14sp  — button labels, Zen Mode label, score chip text
  labelMedium    12sp  — secondary button labels, Home button
  labelSmall     11sp  — HINT label, speaker name, percentage readout

### 4.3 Letter Spacing and Case

  HINT label, speaker names, PERFECT badge: letterSpacing=1.5.sp, all-caps via
    text.uppercase() — do not use TextStyle(letterSpacing) for all-caps effects as
    this breaks accessibility. Use Kotlin string.uppercase() directly.
  All other labels and body text: no manual letter spacing (use defaults).

### 4.4 Line Height and Readability

  All existing lineHeight values in AuraFlowTypography are correct. Do not change them.
  For the narrator overlay (max 5 lines of bodyLarge), set lineHeight=26.sp to give
  slightly more breathing room than the default 24.sp.

---

## 5. COMPONENT LIBRARY

All components listed here are composable functions that should live in
shared/src/commonMain/kotlin/com/auraflow/garden/ui/components/.

### 5.1 AuraButton

Purpose: replaces all Button and OutlinedButton usages in result overlays and action bars.

```
Variants: PRIMARY, SECONDARY, GHOST, DANGER

PRIMARY:
  Background: Brush.linearGradient(CosmicViolet, AuraTeal) — horizontal
  Border: none
  Label color: StarWhite
  Height: 52dp
  Corner radius: 26dp (full pill)
  Shadow: Modifier.shadow(8dp, spotColor=CosmicViolet, shape=pill)

SECONDARY:
  Background: Color.Transparent
  Border: 1dp, TwilightRim
  Label color: MoonGray
  Height: 48dp
  Corner radius: 24dp

GHOST:
  Background: Color.Transparent
  Border: none
  Label color: DuskGray
  Height: 44dp

DANGER:
  Background: Brush.linearGradient(ErrorRed, Color(0xFFFF6B6B))
  Border: none
  Label color: StarWhite
  Height: 52dp
  Corner radius: 26dp
```

All variants:
  Typography: labelLarge (14sp, Medium) for PRIMARY/DANGER, labelMedium for SECONDARY/GHOST
  Leading/trailing icon slots: optional. Icon size=18dp, spacing from text=8dp.
  Pressed state: scale 0.96f, spring stiffness=500f, dampingRatio=0.8f.
  Disabled: alpha=0.38f, no press response.
  Focus indicator: 2dp outline in primary color, outlineOffset=2dp.
  Minimum touch target: 48dp height enforced via Modifier.defaultMinSize(minHeight=48.dp).
  Semantics: role=Role.Button. disabled state conveyed.

### 5.2 WorldAccentCard

Purpose: the base card surface used for stage cards, the Zen Mode button area, and
any future card-style UI. Replaces raw Box with .background().

```
Parameters:
  worldTheme: WorldTheme
  isSelected: Boolean = false
  elevation: Dp = 0.dp
  cornerRadius: Dp = 16.dp
  content: @Composable BoxScope.() -> Unit

Background: Brush.radialGradient(
  colors = listOf(
    worldTheme.accentPrimary.copy(alpha = if (isSelected) 0.25f else 0.12f),
    IslandSurface
  )
)
Border: 1dp, color = worldTheme.accentPrimary.copy(alpha = if (isSelected) 0.60f else 0.30f)
Shadow: shadow(elevation, spotColor=worldTheme.accentPrimary, shape=RoundedCornerShape(cornerRadius))
Pressed: scale 0.97f, spring stiffness=600f
```

### 5.3 StarRow

Purpose: consistent 3-star display across StageCard and ResultOverlay.

```
Parameters:
  stars: Int          // 0–3
  size: Dp = 14.dp
  spacing: Dp = 2.dp
  animateIn: Boolean = false   // true in ResultOverlay

When animateIn=true: each star delays by (index * 200ms), animates
  scale 0f→1.2f→1.0f (spring, dampingRatio=0.5f, stiffness=300f)
  + alpha 0f→1f (tween 150ms).

Filled star: Icons.Filled.Star, tint=LumaGold
Empty star: Icons.Outlined.StarBorder, tint=DuskGray

Semantics: contentDescription = "$stars out of 3 stars"
```

### 5.4 WorldProgressBar

Purpose: the horizontal progress strip bar and any other fill-bar UI.

```
Parameters:
  fraction: Float     // 0f–1f
  worldTheme: WorldTheme
  height: Dp = 6.dp
  width: Dp = 120.dp  // Dp.Unspecified uses fillMaxWidth

Track: RoundedCornerShape(height/2), background=TwilightRim
Fill: RoundedCornerShape(height/2),
  Brush.horizontalGradient(worldTheme.accentSecondary, worldTheme.accentPrimary)
  fillMaxWidth(animatedFraction)

Animation: animateFloatAsState(fraction, tween(600ms, FastOutSlowIn))
Semantics: contentDescription passed as parameter, defaults to "${(fraction*100).toInt()}% complete"
```

### 5.5 EnergyBadge

Purpose: inline score or count display with a pill container. Used in the top bar
score chip and potentially on stage cards for a "best score" display.

```
Parameters:
  value: String
  accentColor: Color = CosmicViolet
  textStyle: TextStyle = MaterialTheme.typography.labelLarge

Container: height=28dp, horizontal padding=10dp, corner=14dp
Background: accentColor.copy(alpha=0.20f)
Border: 1dp, accentColor.copy(alpha=0.50f)
Text color: accentColor

On value change: run scale 1f→1.18f→1f, spring(dampingRatio=0.4f, stiffness=800f)
```

### 5.6 SpeakerDialogueCard

Purpose: the narrator bottom sheet panel. Extracted from DialogueOverlay into a
standalone composable for reuse if additional narrator UI is needed.

```
Parameters:
  speakerName: String    // already resolved ("The Warden", "Luma", etc.)
  accentColor: Color     // caller resolves from speakerId
  message: String
  onDismiss: () -> Unit

Container: fillMaxWidth, cornerShape=TopStart(16dp)+TopEnd(16dp)
Background: Color(0xEE0D0D22)
Top accent stripe: 3dp height, accentColor, same top-corner rounding
Drag handle: 32x4dp pill, StarWhite.copy(alpha=0.3f), above stripe
Speaker label: labelSmall, uppercase, accentColor
Message text: bodyLarge, lineHeight=26.sp, StarWhite
```

### 5.7 ShimmerBox

Purpose: skeleton loading placeholder. Used in home screen stage grid and any future
list that loads asynchronously.

```
Parameters:
  modifier: Modifier
  cornerRadius: Dp = 16.dp

Background: TwilightRim
Animated alpha: infiniteRepeatable tween 900ms, Reverse, 0.35f→0.75f
No content inside — pure placeholder shape.
```

### 5.8 WorldPillIndicator

Purpose: the row of world-selector pills in the WorldHeroBanner.

```
Parameters:
  worlds: List<WorldType>
  selectedWorld: WorldType
  worldTheme: (WorldType) -> WorldTheme
  onSelectWorld: (WorldType) -> Unit

Layout: LazyRow or Row (6 items fits), spacing=6dp
Each pill: height=10dp, animatedWidth (10dp unselected, 28dp selected),
  corner=5dp. Selected: worldTheme(selectedWorld).accentPrimary. Unselected: TwilightRim.
Width animation: animateDpAsState(spring stiffness=400f, dampingRatio=0.8f)
Touch target: each pill padded to 44x44dp vertical touch area via Modifier.size(width, 44.dp)
Semantics: each pill has contentDescription = "${world.displayName}, ${if selected} selected else unselected"
```

---

## 6. ANIMATION GUIDELINES

All UI transitions (not game canvas) must respect the system Reduce Motion preference.
Gate all decorative animations behind:
  val reduceMotion = LocalAccessibilityManager.current?.isReduceMotionEnabled() ?: false

When reduceMotion is true: use instant or alpha-only transitions. Never skip
functional feedback (focus rings, press states) — only skip decorative motion.

### 6.1 Screen Transitions

Navigation between HomeScreen and GameScreen:
  Enter: fadeIn(tween(300ms)) + slideInVertically(initialOffsetY = { it / 8 }, tween(300ms))
  Exit:  fadeOut(tween(200ms)) + slideOutVertically(targetOffsetY = { -it / 8 }, tween(200ms))
  Easing: FastOutSlowIn for enter, LinearOutSlowIn for exit.
  Reduce Motion fallback: fadeIn(tween(150ms)) / fadeOut(tween(100ms)) only.

### 6.2 World Banner Transition (world switching)

When the selected world changes:
  The banner background gradient cross-fades over 400ms (tween, LinearEasing).
  The world name text fades out (150ms) then fades in (200ms) — total 350ms.
  The progress bar fill animates to the new world's fraction (600ms, FastOutSlowIn).
  Stage grid animates out with fadeOut(150ms) + slides up 24dp, then new grid fades in.
  Reduce Motion: use crossfade only, no slide offsets.

### 6.3 Stage Card Interactions

Tap press: scale 0.96f. spring(stiffness=600f, dampingRatio=0.8f). Duration ~120ms decay.
Tap release: scale 1.0f. Same spring.
Unlock animation (when a stage becomes unlocked after clearing prior):
  Scale 0f→1.1f→1.0f (spring stiffness=300f, dampingRatio=0.6f).
  Alpha 0f→1f (tween 200ms).
  Add a brief burst of 4 particles in worldTheme.accentPrimary radiating from card center.
  Particle radius: 3dp each, travel 32dp outward, fade alpha 1f→0f over 400ms.
  Reduce Motion: alpha only, no particles.

### 6.4 Result Overlay Entrance

Win overlay:
  Enter: scale 0.85f→1.02f→1.0f (spring stiffness=250f, dampingRatio=0.6f).
  + fade 0f→1f (tween 200ms).
  Backdrop scrim: fade 0f→1f tween 300ms.
  Delay between overlay appear and star animation start: 400ms.

Fail overlay:
  Enter: slideInVertically(initialOffsetY = { it / 3 }, tween 350ms, FastOutSlowIn).
  + fade 0f→1f.
  No star animation.

### 6.5 Score Counter Animation

When score value increments:
  Use an animatedContent block that scrolls digits vertically (slide-up for increase).
  Each digit transition: tween 150ms, FastOutSlowIn.
  Font: JetBrains Mono (monospace) to prevent width jitter.

### 6.6 Energy Bar Transitions

Color change (green→amber→red): animateColorAsState tween 400ms (existing, keep).
Pulse animation at <=10%: animateFloat 0.4f→1.0f tween 500ms Reverse (existing, keep).
Fraction decrease: animateFloatAsState tween 250ms LinearEasing — smooth drain.

### 6.7 Tutorial Overlay

Appear: fadeIn(tween 250ms).
Dismiss: fadeOut(tween 200ms) + scale 1.0f→0.95f (tween 200ms).
Border pulse: 0.6→1.0 tween 800ms LinearEasing Reverse (existing, keep).

### 6.8 Dialogue Overlay

Enter: slideInVertically(initialOffsetY = { it }, spring stiffness=400f). Keep existing.
Exit:  slideOutVertically(targetOffsetY = { it }, tween 200ms). Keep existing.

### 6.9 Timing Reference Table

| Interaction              | Duration  | Easing            | Reduce Motion |
|--------------------------|-----------|-------------------|---------------|
| Screen enter             | 300ms     | FastOutSlowIn     | 150ms fade    |
| Screen exit              | 200ms     | LinearOutSlowIn   | 100ms fade    |
| World banner crossfade   | 400ms     | Linear            | 400ms fade    |
| Stage card press         | ~120ms    | Spring 600/0.8    | Instant       |
| Stage unlock reveal      | 350ms     | Spring 300/0.6    | 200ms fade    |
| Result overlay enter     | 300ms     | Spring 250/0.6    | 150ms fade    |
| Star award per star      | 200ms stagger | Spring 300/0.5 | None shown   |
| Score increment digit    | 150ms     | FastOutSlowIn     | Instant swap  |
| Energy drain             | 250ms     | Linear            | Instant       |
| Tutorial appear          | 250ms     | Linear            | 150ms fade    |
| Dialogue slide in        | Spring    | Spring 400f       | 150ms fade    |

---

## 7. WORLD-SPECIFIC THEMING

### 7.1 WorldTheme Data Class

Add this to the ui/theme/ package:

```kotlin
// ui/theme/WorldTheme.kt
data class WorldTheme(
    val worldType: WorldType,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val surfaceTint: Color,
    val bannerGradientStart: Color,
    val bannerSymbol: String,       // Unicode glyph or named icon reference
    val energyBarColors: List<Color> // [full, warning, critical]
)

val LocalWorldTheme = staticCompositionLocalOf {
    WorldThemeDefaults.WHISPERING_MEADOW
}
```

### 7.2 WorldTheme Values Per World

WHISPERING_MEADOW — Soft botanical, bioluminescent flora
  accentPrimary:       #06D6A0  (BloomGreen)
  accentSecondary:     #4CAF50
  surfaceTint:         #0D1F15
  bannerGradientStart: #071A10
  bannerSymbol:        "leaf" (Icons.Filled or custom SVG)
  energyBarColors:     [#06D6A0, #4CAF50, #E63946]
  UI chrome flavor: soft glowing vine motifs as decorative dividers (1dp lines
    with a slight organic curve if custom drawn). Star trail particles are pale green.

CRYSTAL_CAVERNS — Cold crystalline, refractive blues
  accentPrimary:       #00B4D8  (AuraTeal)
  accentSecondary:     #7ECEF0
  surfaceTint:         #0A1A22
  bannerGradientStart: #071218
  bannerSymbol:        "diamond" or "crystal" (hexagonal facet shape)
  energyBarColors:     [#00B4D8, #7ECEF0, #E63946]
  UI chrome flavor: geometric hexagonal border motifs on cards. Stage card borders
    use a subtle faceted inner highlight (1dp, white 8% alpha on top-left edges only).
    Particle burst on unlock: ice-blue shards.

FLOATING_ISLES — Ethereal, purple-violet, dreamy
  accentPrimary:       #8B5CF6  (InfoViolet)
  accentSecondary:     #C4B5FD
  surfaceTint:         #180F2E
  bannerGradientStart: #100A20
  bannerSymbol:        "cloud" or floating rock SVG
  energyBarColors:     [#8B5CF6, #C4B5FD, #E63946]
  UI chrome flavor: card borders have a slight shadow bloom in violet. Particle burst
    uses slowly drifting large dots (radius 5dp) rather than fast sparks.

DEEP_SEA — Abyssal navy, bioluminescent teal
  accentPrimary:       #0077B6
  accentSecondary:     #48CAE4
  surfaceTint:         #050F1A
  bannerGradientStart: #030810
  bannerSymbol:        "water drop" or fish silhouette
  energyBarColors:     [#48CAE4, #0077B6, #E63946]
  UI chrome flavor: subtle animated ripple on the WorldHeroBanner (concentric
    circle waves emanating from bottom-center, very low opacity 8%, tween 2000ms loop).
    Card borders: deep teal. Result overlay border: #48CAE4 at 50%.
    Reduce Motion: disable ripple entirely.

GLITCH_CITY — Neon, corrupted, high contrast
  accentPrimary:       #FF6B6B  (Coral)
  accentSecondary:     #FFB703  (LumaGold)
  surfaceTint:         #1F0F0F
  bannerGradientStart: #1A0808
  bannerSymbol:        "glitch" or pixel/circuit icon
  energyBarColors:     [#FF6B6B, #FFB703, #E63946]
  UI chrome flavor: stage card borders occasionally (every 4–6 seconds) display a
    brief "glitch" animation: a 2-frame horizontal slice offset (4dp) for 80ms, then
    return. Use a random timer per card with LaunchedEffect. This is purely decorative
    and must be disabled with Reduce Motion. Result overlay in this world uses a scan-line
    effect: 1dp horizontal lines at 4dp spacing overlaid at 6% opacity.

CELESTIAL_SUMMIT — Radiant gold, cosmic white, peak clarity
  accentPrimary:       #FFB703  (LumaGold)
  accentSecondary:     #F0F0F5  (Pearl)
  surfaceTint:         #1A1608
  bannerGradientStart: #120F04
  bannerSymbol:        "star" or sun-burst
  energyBarColors:     [#FFB703, #F0F0F5, #E63946]
  UI chrome flavor: gold accent on ALL interactive elements. Stage cards that are
    3-star cleared get a persistent slow gold shimmer (Brush.linearGradient sweep
    animating from left to right over 2000ms loop). Win result overlay gets an
    enhanced star burst with 8 rays instead of 4. The energy bar fill in this world
    uses a gold-to-white gradient when above 50%.

### 7.3 Providing WorldTheme in the Composition

In NavGraph.kt or HomeScreen.kt, resolve the current WorldTheme from the viewModel
state and wrap the subtree:

```kotlin
val worldTheme = WorldThemeDefaults.forWorld(uiState.currentWorldType)
CompositionLocalProvider(LocalWorldTheme provides worldTheme) {
    // HomeScreen content or NavHost content
}
```

WorldThemeDefaults is a companion object with a forWorld(WorldType) function that
returns the appropriate WorldTheme instance. This is the single source of truth — no
hardcoded colors anywhere in the screen composables.

---

## 8. ACCESSIBILITY COMPLIANCE CHECKLIST

This section is implementation-facing. Every component built from this spec must pass:

Color Contrast:
  StarWhite (#E8E8F0) on DeepSpace (#0A0A1A): 17.5:1. PASS.
  StarWhite (#E8E8F0) on IslandSurface (#1A1A2E): 12.8:1. PASS.
  MoonGray (#AAAAAC) on DeepSpace (#0A0A1A): 8.1:1. PASS.
  DuskGray (#66667A) on DeepSpace (#0A0A1A): 3.9:1. PASS for large text (18sp+) only.
    Do NOT use DuskGray for body text below 18sp — use MoonGray minimum.
  LumaGold (#FFB703) on DeepSpace (#0A0A1A): 9.8:1. PASS.
  CosmicViolet (#6C63FF) on DeepSpace (#0A0A1A): 4.6:1. PASS for normal text.
  BloomGreen (#06D6A0) on DeepSpace (#0A0A1A): 8.3:1. PASS.
  ErrorRed (#E63946) on DeepSpace (#0A0A1A): 4.7:1. PASS.
  InfoViolet (#8B5CF6) on DeepSpace (#0A0A1A): 5.1:1. PASS.

Touch Targets:
  All IconButtons: minimum 48x48dp. Enforce via Modifier.size(48.dp) on the IconButton.
  Stage cards: full card surface clickable. At 3-column grid on 360dp screen each card
    is approximately 109dp — well above 44dp minimum.
  World pills: 44dp vertical touch area despite 10dp visual height.
  Pill buttons in action bar: 48dp height minimum.

Screen Reader:
  All Icon composables used decoratively: contentDescription=null.
  All Icon composables with semantic meaning: contentDescription in English.
  Stage cards: "Stage ${id}, ${if locked} locked ${else} ${stars} stars, ${if 3-star} perfect".
  WorldPillIndicator pills: "${world.displayName}, ${if selected} selected".
  EnergyBar: "${(fraction*100).toInt()} percent energy remaining".
  StarRow: "${stars} out of 3 stars".
  EnergyBadge: "Score: ${value}" or "Count: ${value}" — caller provides semantics.
  Progress strip: "World progress: ${cleared} of ${total} stages cleared".
  ResultOverlay: announce title and score on appear using Modifier.semantics { liveRegion = LiveRegionMode.Polite }.
  TutorialOverlay: "Tutorial hint: ${hint.text}. Tap to dismiss." — already present, keep.

Keyboard / D-pad Navigation:
  Stage grid: all cards reachable via focusRequester, tab order = grid reading order.
  Result overlay buttons: focus order = primary → secondary → home.
  Bottom action bar: Zen Mode button receives focus after grid.
  All IconButtons: focusable, show 2dp focus ring in accentPrimary.

Reduce Motion:
  Every decorative infiniteRepeatable animation is gated behind a reduceMotion check.
  Functional animations (press feedback, screen transitions) use duration-reduced
  variants rather than being removed entirely.

Dynamic Type / Font Scale:
  All text uses sp units (not dp) so OS font scaling applies.
  At 200% font scale verify: stage number in card fits without truncation (titleLarge
    22sp → 44sp effective — may require the card height to expand via wrapContentHeight
    rather than fixed aspectRatio).
  Tutorial overlay: maxLines=4 with ellipsis overflow. At 200% scale this is sufficient
    for all planned hint lengths (<120 characters).

---

## 9. DEVELOPER HANDOFF NOTES

### File Changes Summary

Files to MODIFY:
  shared/src/commonMain/kotlin/com/auraflow/garden/ui/theme/Color.kt
    — Add NightSurface, TwilightRim, StarWhite (rename from DarkOnSurface),
      MoonGray, DuskGray, InfoViolet, GlowViolet, GlowTeal, GlowGold,
      ModalScrim, CardScrim, BannerScrim.
    — Update primaryContainer and secondaryContainer values in AuraFlowTheme.kt.

  shared/src/commonMain/kotlin/com/auraflow/garden/ui/theme/AuraFlowTheme.kt
    — Update darkColorScheme primaryContainer and secondaryContainer.
    — Add CompositionLocalProvider(LocalWorldTheme) wrapping MaterialTheme content.

  shared/src/commonMain/kotlin/com/auraflow/garden/ui/theme/Typography.kt
    — Add font family declarations once font assets are bundled.
    — Adjust narrator bodyLarge lineHeight to 26.sp.

Files to CREATE (all in commonMain):
  ui/theme/WorldTheme.kt           — WorldTheme data class, LocalWorldTheme, WorldThemeDefaults
  ui/components/AuraButton.kt      — Section 5.1
  ui/components/WorldAccentCard.kt — Section 5.2
  ui/components/StarRow.kt         — Section 5.3
  ui/components/WorldProgressBar.kt — Section 5.4
  ui/components/EnergyBadge.kt     — Section 5.5
  ui/components/SpeakerDialogueCard.kt — Section 5.6
  ui/components/ShimmerBox.kt      — Section 5.7
  ui/components/WorldPillIndicator.kt — Section 5.8

Files to REFACTOR (screen-level, do not change game logic):
  ui/screens/home/HomeScreen.kt    — Full layout per Section 2
  ui/screens/game/GameScreen.kt    — Top bar, energy bar wrapper, result overlays per Section 3
  ui/screens/game/canvas/EnergyBar.kt — Add lightning icon, percentage label per Section 3.2
  ui/tutorial/TutorialOverlay.kt   — Layout and spec per Section 3.5
  ui/narrator/DialogueOverlay.kt   — Speaker accent strip, drag handle per Section 3.6

### Implementation Order Recommendation

1. WorldTheme.kt — all other files depend on it.
2. Color.kt additions — required by WorldTheme.
3. AuraFlowTheme.kt update — wire LocalWorldTheme.
4. Component library (5.1–5.8) — bottom-up, no screen dependencies.
5. EnergyBar.kt — low risk, isolated.
6. TutorialOverlay.kt — isolated composable.
7. DialogueOverlay.kt — isolated composable.
8. HomeScreen.kt — uses all components; test on both 360dp and 412dp width devices.
9. GameScreen.kt ResultOverlay — requires starsAwarded param added to GameUiState.
10. GameScreen.kt top bar — low risk after ResultOverlay is confirmed.

### Platform-Specific Notes

Blur (energy bar glow, tutorial overlay backdrop):
  Android: Modifier.blur(Xdp) requires API 31 (Android 12). Use an expect/actual
    isBlurAvailable() function. On API < 31 omit the blur layer silently.
  iOS: Compose Multiplatform on iOS renders via Skia which supports graphicsLayer
    renderEffect. The same Modifier.blur call works on iOS 15+ via CMP's Skia backend.
    No additional implementation needed for iOS.

Font assets:
  Place .ttf or variable .ttf files in:
    shared/src/commonMain/composeResources/font/
  Reference via: Font(resource = Res.font.outfit_variable)
  Ensure the composeResources block is configured in shared/build.gradle.kts.

Shimmer animation delay (staggered cards):
  Use LaunchedEffect(index) { delay(index * 40L) } before setting a local isVisible
  state. This is safe in commonMain with kotlinx-coroutines.

Score counter digit animation:
  Use AnimatedContent with ContentTransform(
    targetContentEnter = slideInVertically { -it } + fadeIn(),
    initialContentExit = slideOutVertically { it } + fadeOut()
  ) wrapping each Text("$score").

Glitch card effect (GLITCH_CITY world only):
  Use a LaunchedEffect that loops with delay(Random.nextLong(4000, 6000)) and
  briefly sets a local glitchOffset state to 4.dp then back to 0.dp after 80ms.
  Apply as Modifier.offset(x = glitchOffset) on the card content (not the card itself,
  to avoid clipping). Gate entirely on !reduceMotion.
