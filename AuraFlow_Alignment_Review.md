# AURA FLOW — Alignment Review & Improvement Recommendations
## Comparing Game Script v2.0 ↔ Claude Code Build Prompt

---

## 1. GAPS BETWEEN THE TWO DOCUMENTS

### 1.1 Missing from Build Prompt: Color-Blind Palette Definitions
The guardrails mention "support color-blind palettes for all node colors" but no phase actually defines what those alternate palettes are. The game relies heavily on color-matching — this is a core accessibility issue.

**Fix — Add to Phase 2 (Data Models):**
```kotlin
// In NodeColor.kt, add color-blind-safe alternates
enum class NodeColor(
    val hex: String,
    val deuteranopiaHex: String,   // red-green deficiency
    val protanopiaHex: String,     // red deficiency  
    val tritanopiaHex: String,     // blue-yellow deficiency
    val displayName: String,
    val shapeHint: NodeShape,      // CIRCLE, SQUARE, TRIANGLE, DIAMOND, HEXAGON, STAR, CROSS, PENTAGON
)
```
Shape hints are critical — players with severe color vision deficiency need a non-color distinguishing feature. Each NodeColor should map to a unique shape that renders as a subtle icon inside the node circle.

**Also add to Phase 3 (Rendering):**
- Setting toggle: "Color-blind mode" with sub-options (Deuteranopia, Protanopia, Tritanopia)
- When enabled, nodes render with alternate hex values AND shape icons inside them
- Link trails also shift to alternate palette

### 1.2 Missing from Build Prompt: Localization Architecture
Phase 20 mentions Hindi and Telugu for v1.1, but no phase sets up the localization infrastructure. If strings are hardcoded in English across 20 phases, retrofitting localization is painful.

**Fix — Add to Phase 1 guardrails:**
```
- ALL user-facing strings (narrator lines, Luma words, UI labels, tooltips, 
  achievement descriptions) must use Android string resources (strings.xml).
- Never hardcode English text in Composables or ViewModels.
- Narrator and Luma dialogue: store in a separate strings_narrative.xml 
  so translators can work on game text independently from UI text.
- Use ICU MessageFormat for plurals and gendered text (future Hindi/Telugu support).
```

### 1.3 Missing from Script: World 4 (Sky Nebula) Detail
The script gives World 4 only a brief entry — no boss stage script, no mechanic descriptions, no stage milestones (like 110, 120, 150). Worlds 1-3 each have detailed stage breakdowns.

**Fix — Add to Script Section 4:**
- Stage 110: "Rotation Tutorial" — first stage requiring 3D rotation to see hidden node pairs
- Stage 125: "The Mirror Layer" — nodes exist on two planes that mirror each other
- Stage 150: "The Constellation" — all 8 colors present, nodes form recognizable star patterns
- Stage 200: World 4 Boss: "The Origin Point" — where all connections in Orizon began. Every link must be drawn across at least 2 layers. Narrator: "This is not the end. This is where it all started."

### 1.4 Missing from Build Prompt: Onboarding Conversion Funnel
The build prompt tracks analytics events but doesn't define a conversion funnel. For a free-to-premium game, understanding where players drop off is the most important data.

**Fix — Add to Phase 13 or a new section in Guardrails:**
```
Key Funnel Events (track in Firebase):
1. af_app_opened (first launch)
2. af_tutorial_stage_1_complete
3. af_tutorial_stage_5_complete  
4. af_world_1_boss_complete (stage 20)
5. af_world_2_entered (stage 21)
6. af_world_2_boss_complete (stage 50) — THIS IS THE CONVERSION GATE
7. af_wardens_pass_screen_shown
8. af_wardens_pass_purchased
9. af_first_cosmetic_purchased
10. af_day_7_retained

Target benchmarks:
- Step 1→2: >90% (if below, tutorial is confusing)
- Step 2→3: >70% (if below, early stages too hard or boring)
- Step 4→5: >60% (if below, World 1 boss is blocking)
- Step 6→7: >85% (if below, conversion screen isn't showing)
- Step 7→8: >5% (if below, pricing or value prop issue)
```

---

## 2. IMPROVEMENTS TO THE BUILD PROMPT

### 2.1 Add a Phase 0: Level Design Tooling
Before building the game, you need a way to create levels efficiently. Hand-crafting 50 JSON level files is tedious and error-prone.

**New Phase 0 Prompt:**
```
Build a simple desktop/web level editor for AuraFlow that runs as 
a standalone HTML file (single-file, no backend needed).

Features:
- Canvas grid where you click to place nodes
- Color selector for node types
- Click two same-color nodes to define a pair
- Drag nodes to reposition
- Set energy budget (auto-calculated from optimal path + margin %)
- Toggle: pressure node, moving node, boss constraints
- Export to JSON matching the Level schema from Phase 2
- Import existing JSON to edit
- "Test solve" mode: try solving your own level in the editor
- Batch export: save all levels for a world as a single JSON file

This is an internal tool, not shipped with the app.
```

This would save you 10+ hours of manual JSON editing and make level balancing much faster.

### 2.2 Strengthen Offline Resilience in Phase 14
The guardrails mention offline handling, but the billing phase doesn't address what happens when:
- Player buys Warden's Pass but Google Play acknowledgment fails (network drop)
- Player is on a flight and opens the app — should locked worlds show "Purchase pending" or just unlock optimistically?

**Fix — Add to Phase 14 prompt:**
```
Offline purchase handling:
- On successful launchBillingFlow(), immediately set a local flag: 
  pendingWardenPass = true in DataStore (not Room, for speed)
- If acknowledgePurchase() succeeds: set hasWardenPass = true, clear pending flag
- If acknowledgePurchase() fails (network): keep pending flag, retry on next app launch
- While pendingWardenPass == true: unlock worlds 3-4 locally (optimistic unlock)
- On next successful queryPurchasesAsync(): reconcile pending state
- NEVER show "purchase failed" if Google Play returned a purchase token — 
  the purchase went through, only acknowledgment failed
```

### 2.3 Add Error Copy to Script
The script defines narrator/Luma lines for gameplay events but doesn't cover error states that players will encounter:

**Add to Script — Error State Dialogue:**
```
Network error (Daily Challenge):
  Narrator: "The wind carries no signal today. The daily island will wait."
  Luma: "...Later."

Purchase error:
  Narrator: "The garden's gate is slow to open. Try once more."
  Luma: "...Wait."

Ad failed to load:
  Luma: "...Oh."
  (No narrator — keep it brief, no drama around ads)

App crash recovery (on relaunch after crash):
  Luma: "...Still here."
  (Same as idle-long, reuses the warm reconnection feeling)
```

### 2.4 Add Data Migration Strategy
The build prompt doesn't address what happens when you ship v1.1 with new Room schema changes, new level packs, or new achievement definitions.

**Fix — Add to Phase 2 guardrails:**
```
Room Migration Strategy:
- Export Room schemas (exportSchema = true in @Database annotation)
- Store schema JSONs in schemas/ directory (committed to git)
- For every schema change, write a Migration object (never use fallbackToDestructiveMigration)
- Test migrations with MigrationTestHelper in instrumentation tests
- Version 1: MVP schema
- Version 2: add seasonal pack tables, lore fragment tracking
- Version 3: community blueprint sync fields
```

### 2.5 Crash & ANR Budget
The guardrails set performance targets but don't set quality targets for crashes.

**Fix — Add to guardrails:**
```
Quality targets:
- Crash-free rate: >99.5% (Google Play vitals threshold for "bad" is 1.09%)
- ANR rate: <0.47% (Google Play vitals threshold)
- Startup crash rate: <0.1%
- Use Firebase Crashlytics non-fatal logging for: ad load failures, 
  billing errors, JSON parse errors, audio initialization failures
```

---

## 3. IMPROVEMENTS TO THE GAME SCRIPT

### 3.1 Add Seasonal Pack Script Samples
The script mentions seasonal packs in the monetization section but doesn't include any narrative content for them. Seasonal stages need their own tone.

**Add to Script — Seasonal World Scripts:**

**Diwali Lights (10 stages):**
World Entry: "Tonight, the garden celebrates. Every node is a diya 
waiting to be lit. Draw the connections — and let Orizon glow."
Luma: "Bright!"
Boss (Stage 10): "The Rangoli" — nodes arranged in a mandala pattern. 
All links must be drawn without intersections to complete the design.
On clear: "The rangoli is complete. The garden has never been more beautiful."

**Monsoon Garden (10 stages):**
World Entry: "The rain has come. The nodes are slippery — they drift 
slightly after being placed. But water gives life. The garden needs this."
Luma: "Wet." (amused glow)
Mechanic twist: nodes have slight position drift after being linked 
(visual only, doesn't affect gameplay logic — just aesthetic)

### 3.2 Add Warden Name & Identity
The script references "player's Warden name" in the sharing section but never defines when/how the player chooses this name.

**Add to Script — Identity:**
- On first launch, after tutorial Stage 1 clear, a gentle prompt appears:
  "The garden would like to remember you. What shall it call you?"
  - Text input, 2-20 characters, default: "Warden"
  - This name appears on: shared clips, community blueprints, daily leaderboard
  - Can be changed in Settings anytime
  - Luma: "Welcome, [name]." (only time Luma says the player's name)

### 3.3 Add "Between Worlds" Transition Script
When transitioning from one world to the next, there's no script for the journey between them. This is a missed emotional beat.

**Add to Script:**
Between Meadow → Glitch City:
  Narrator: "The gentle fields end here. Ahead — something sharper. 
  Something that tried to improve on nature and learned that nature 
  does not appreciate being improved upon."
  Luma: (flickering nervously) "...Different."

Between Glitch City → Deep Sea (Warden's Pass gate):
  Narrator: "You have healed what was broken above. But below the 
  islands, in the weight of forgotten water, something else waits. 
  Something older. Are you ready to go deeper?"
  Luma: "...Deep." (slow, heavy pulse)

Between Deep Sea → Sky Nebula:
  Narrator: "You have touched the bottom. Now — the top. The sky 
  has never been restored. No Warden has ever gone this high. 
  The nodes up there don't just need connection. They need someone 
  who understands that some things can only be seen from a distance."
  Luma: "...High." (excited, rising glow)

---

## 4. ARCHITECTURE RECOMMENDATIONS

### 4.1 Consider a core/ Module
Right now everything is in a single app module. For a project this size, extract a `core` module early:

```
:core — models, constants, enums, telemetry keys, util extensions
:app  — everything else (UI, engine, billing, etc.)
```

This keeps the shared constants truly shared and prevents circular dependencies as the project grows. It also speeds up incremental builds since `core` rarely changes after Phase 2.

### 4.2 Feature Flags for Premium Content
Instead of checking `hasWardenPass` in 15 different places, create a FeatureGate:

```kotlin
@Singleton
class FeatureGate @Inject constructor(
    private val playerRepo: PlayerRepository
) {
    fun canAccess(feature: Feature): Flow<Boolean> = when (feature) {
        Feature.DEEP_SEA -> playerRepo.hasWardenPass()
        Feature.SKY_NEBULA -> playerRepo.hasWardenPass()
        Feature.ZEN_MODE -> playerRepo.hasWardenPass()
        Feature.COMMUNITY_BLUEPRINTS -> playerRepo.hasWardenPass()
        Feature.DAILY_CHALLENGE -> flowOf(true) // always free
        Feature.SEASONAL_PACK(id) -> playerRepo.hasSeasonalPack(id)
    }
}
```

This centralizes all entitlement logic and makes it trivial to add new premium features later.

### 4.3 Analytics as a Cross-Cutting Concern
Rather than sprinkling `FirebaseAnalytics.logEvent()` calls throughout the codebase, create an `AnalyticsTracker` interface:

```kotlin
interface AnalyticsTracker {
    fun trackStageStarted(stageId: Int, worldId: String)
    fun trackStageCompleted(result: StageResult)
    fun trackStageFailed(stageId: Int, reason: FailReason)
    fun trackPurchase(productId: String, price: String)
    fun trackFunnelStep(step: FunnelStep)
    // etc.
}

class FirebaseAnalyticsTracker @Inject constructor(...) : AnalyticsTracker
class DebugAnalyticsTracker : AnalyticsTracker // logs to Logcat in debug
```

This makes analytics testable, swappable, and prevents Firebase imports from leaking across the entire codebase.

---

## 5. PRIORITY ACTION ITEMS

| # | Action | Where | Impact |
|---|--------|-------|--------|
| 1 | Add color-blind palette + shape hints to NodeColor | Script + Build Phase 2, 3 | Critical accessibility |
| 2 | Enforce string resources from Phase 1 (localization prep) | Build Phase 1 guardrails | Saves 10+ hrs at localization time |
| 3 | Build Phase 0 level editor (HTML tool) | New phase before Phase 1 | Saves 10+ hrs of JSON editing |
| 4 | Add conversion funnel events | Build Phase 13 | Essential for revenue optimization |
| 5 | Write World 4 stage scripts (110, 125, 150, 200 boss) | Script Section 4 | Completes the narrative arc |
| 6 | Add between-worlds transition scripts | Script new section | Emotional polish |
| 7 | Add error state dialogue | Script new section | Covers real-world edge cases |
| 8 | Define Room migration strategy | Build Phase 2 | Prevents data loss on updates |
| 9 | Add crash/ANR quality targets | Build guardrails | Play Store vitals compliance |
| 10 | Add Warden name identity flow | Script + Build Phase 11 | Enables sharing & community features |
