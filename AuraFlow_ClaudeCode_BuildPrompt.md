# AURA FLOW: THE KINETIC GARDEN
## Complete Claude Code Build Prompt — Start to Finish (Cross-Platform KMP Edition)

> **What this document is:** A comprehensive, step-by-step Claude Code prompt sequence to build the entire Aura Flow game from zero to Play Store AND App Store submission. Each phase is a self-contained prompt you can paste into Claude Code. Phases build on each other sequentially.
>
> **Tech Stack:** Kotlin 2.1.20, Compose Multiplatform 1.10.3, KMP (Kotlin Multiplatform), MVVM, Room 2.8.3 (KMP), Koin 4.1.1 DI, Compose Canvas (2D rendering), Ktor Client 3.4.2 (networking), Multiplatform Settings 1.3.0, Media3 (Android audio), AVFoundation (iOS audio), Core Haptics (iOS), Google Play Billing v6 (Android), StoreKit 2 (iOS), AdMob (both platforms), Firebase Analytics + Crashlytics (both platforms via GitLive SDK + expect/actual)
>
> **Target:** Android 8.0+ (API 26+) AND iOS 16.0+
>
> **Architecture:** KMP multi-module project — `shared/` module contains ~95% of code in `commonMain/`, with `expect/actual` for platform APIs. `androidApp/` and `iosApp/` are thin application shells. All game logic, UI, models, navigation, and persistence are shared.

---

## TABLE OF CONTENTS

1. [Phase 1: Project Scaffolding & Architecture](#phase-1) *(KMP scaffold, Koin, Navigation, Xcode project)*
2. [Phase 2: Core Data Models & Level Schema](#phase-2) *(Room 2.8.3 KMP, domain models, level JSON)*
3. [Phase 3: Game Engine — Node Rendering & Link Drawing](#phase-3) *(Compose Canvas, touch handling, iOS benchmark)*
4. [Phase 4: Game Logic — Energy, Scoring, Win/Fail States](#phase-4) *(pure shared logic)*
5. [Phase 5: Luma Companion System](#phase-5) *(shared Canvas rendering)*
6. [Phase 6: Haptic Feedback Engine](#phase-6) *(expect/actual: VibrationEffect + Core Haptics)*
7. [Phase 7: Narrator & Dialogue System](#phase-7) *(shared Compose UI)*
8. [Phase 8: World Progression & Stage Management](#phase-8) *(shared navigation + persistence)*
9. [Phase 9: Visual Effects — Bloom, Crescendo, Particle System](#phase-9) *(shared Canvas effects)*
10. [Phase 10: Audio — Generative Ambient Music & SFX](#phase-10) *(expect/actual: Media3 + AVFoundation)*
11. [Phase 11: Tutorial Flow (Stages 1-5)](#phase-11) *(shared logic + UI)*
12. [Phase 12: Dynamic Difficulty System](#phase-12) *(pure shared logic)*
13. [Phase 13: Engagement Systems — Streaks, Daily Challenge, Achievements](#phase-13) *(shared logic + expect/actual notifications)*
14. [Phase 14: Monetization — Warden's Pass, Cosmetics, IAP](#phase-14) *(expect/actual: Play Billing + StoreKit 2)*
15. [Phase 15: AdMob Rewarded Video Integration](#phase-15) *(expect/actual: AdMob Android/iOS + ATT)*
16. [Phase 16: Zen Mode & Community Blueprints](#phase-16) *(shared Canvas sandbox)*
17. [Phase 17: Shareable Crescendo Clips](#phase-17) *(expect/actual: MediaCodec + AVAssetWriter)*
18. [Phase 18: Backend — Daily Challenges & Leaderboard API](#phase-18) *(Ktor Client KMP + Spring Boot)*
19. [Phase 19: Polish — Animations, Transitions, Loading States](#phase-19) *(cross-platform edge cases)*
20. [Phase 20: Testing, Build Config & Store Prep](#phase-20) *(Play Store + App Store submission)*

> **Cross-platform structure:** Every phase uses `shared/commonMain/` for shared code, `shared/androidMain/` and `shared/iosMain/` for platform implementations via `expect/actual`, and the thin `androidApp/` and `iosApp/` shells. "Done when" checklists verify BOTH platforms.

---

## BUILD GUARDRAILS (READ BEFORE PROMPTING)

### Dependency Version Table (Cross-Platform)

These are the pinned versions for the entire project. Do not override unless a phase explicitly requires it.

| Dependency | Version | Gradle Artifact / Plugin ID | Platform |
|---|---|---|---|
| Kotlin | 2.1.20 | `org.jetbrains.kotlin.multiplatform` | All |
| Kotlin Serialization Plugin | 2.1.20 | `org.jetbrains.kotlin.plugin.serialization` | All |
| Compose Multiplatform | 1.10.3 | `org.jetbrains.compose` (Gradle plugin) | All |
| Compose Compiler | bundled with Kotlin 2.1.20 | `org.jetbrains.kotlin.plugin.compose` | All |
| AGP | 8.7.3 | `com.android.application` / `com.android.library` | Android |
| Gradle | 8.11.1 | `gradle-wrapper.properties` | Build tool |
| Koin BOM | 4.1.1 | `io.insert-koin:koin-bom` | All |
| Koin Core | 4.1.1 | `io.insert-koin:koin-core` | commonMain |
| Koin Android | 4.1.1 | `io.insert-koin:koin-android` | androidMain |
| Koin Compose | 4.1.1 | `io.insert-koin:koin-compose` | commonMain |
| Koin Compose Viewmodel | 4.1.1 | `io.insert-koin:koin-compose-viewmodel` | commonMain |
| Navigation Compose (KMP) | 2.9.2 | `org.jetbrains.androidx.navigation:navigation-compose` | commonMain |
| Lifecycle ViewModel Compose | 2.10.0 | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` | commonMain |
| Lifecycle Runtime Compose | 2.10.0 | `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` | commonMain |
| Room Runtime (KMP) | 2.8.3 | `androidx.room:room-runtime` | commonMain |
| Room Compiler (KSP) | 2.8.3 | `androidx.room:room-compiler` | KSP (all targets) |
| SQLite Bundled | 2.5.1 | `androidx.sqlite:sqlite-bundled` | commonMain |
| KSP | 2.1.20-1.0.32 | `com.google.devtools.ksp` | Build |
| Ktor Client Core | 3.4.2 | `io.ktor:ktor-client-core` | commonMain |
| Ktor Client OkHttp | 3.4.2 | `io.ktor:ktor-client-okhttp` | androidMain |
| Ktor Client Darwin | 3.4.2 | `io.ktor:ktor-client-darwin` | iosMain |
| Ktor Content Negotiation | 3.4.2 | `io.ktor:ktor-client-content-negotiation` | commonMain |
| Ktor Serialization JSON | 3.4.2 | `io.ktor:ktor-serialization-kotlinx-json` | commonMain |
| kotlinx-serialization-json | 1.9.0 | `org.jetbrains.kotlinx:kotlinx-serialization-json` | commonMain |
| kotlinx-coroutines | 1.10.2 | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | commonMain |
| kotlinx-datetime | 0.7.1 | `org.jetbrains.kotlinx:kotlinx-datetime` | commonMain |
| kotlinx-collections-immutable | 0.3.8 | `org.jetbrains.kotlinx:kotlinx-collections-immutable` | commonMain |
| Multiplatform Settings | 1.3.0 | `com.russhwolf:multiplatform-settings` | commonMain |
| Multiplatform Settings Coroutines | 1.3.0 | `com.russhwolf:multiplatform-settings-coroutines` | commonMain |
| Coil Compose | 3.4.0 | `io.coil-kt.coil3:coil-compose` | commonMain |
| Coil Network Ktor | 3.4.0 | `io.coil-kt.coil3:coil-network-ktor3` | commonMain |
| Firebase Kotlin SDK (GitLive) | 2.4.0 | `dev.gitlive:firebase-analytics` | commonMain |
| Google Play Billing | 6.2.1 | `com.android.billingclient:billing-ktx` | androidMain |
| Google Mobile Ads (Android) | 23.0.0 | `com.google.android.gms:play-services-ads` | androidMain |
| Media3 ExoPlayer | 1.3.1 | `androidx.media3:media3-exoplayer` | androidMain |
| LeakCanary | 2.14 | `com.squareup.leakcanary:leakcanary-android` | androidMain (debug) |

### Performance Targets

**Android (release on Pixel 5+):** 60fps gameplay; frame time p95 <16ms; cold start <2.0s; heap <220MB; battery <5%/15m session.

**iOS (release on iPhone 12+):** 60fps gameplay (Metal via Skiko); frame time p95 <16ms; cold start <2.5s; heap <180MB (iOS enforces stricter limits); battery <5%/15m session. ProMotion displays (120Hz on iPhone 13 Pro+) supported.

### Accessibility & Comfort
- Support color-blind palettes for all node colors; respect system font scale 0.85-1.3.
- Provide reduced-motion mode for particles/sky rotation (check `Settings.Global.ANIMATOR_DURATION_SCALE` on Android, `UIAccessibility.isReduceMotionEnabled` on iOS via expect/actual).
- All narrator text skippable; haptics toggle respected globally.
- iOS VoiceOver: Compose semantics tree maps to iOS accessibility tree automatically. Verify `contentDescription` values read correctly.
- iOS Dynamic Type: Compose Multiplatform respects system font scaling on iOS automatically.

### Telemetry Naming
- Prefix all analytics with `af_`; include `player_id`, `stage_id`, `world_id`, `session_id`; avoid PII; log consent state for ads/analytics.

### Offline/Poor Network
- Stages, assets, and audio must degrade gracefully; queue analytics; never hard-fail purchases -- provide local pending state on both platforms.

### Build Commands (Both Platforms)
- **Shared module:** `./gradlew :shared:build` (compiles for all targets)
- **Android debug:** `./gradlew :androidApp:assembleDebug`
- **Android release:** `./gradlew :androidApp:assembleRelease`
- **Android bundle:** `./gradlew :androidApp:bundleRelease`
- **Shared tests (all KMP targets):** `./gradlew :shared:allTests`  (runs Android JVM + iOS Simulator tests on macOS)
- **Android lint:** `./gradlew :androidApp:lintDebug`
- **iOS shared framework:** `./gradlew :shared:compileKotlinIosSimulatorArm64`
- **iOS app (Simulator):** `xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' build`
- **iOS archive:** `xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -configuration Release -archivePath build/iosApp.xcarchive archive`
- For backend phases, provide a curl-able mock endpoint and a sample JSON payload.

### Quality Targets
- Crash-free rate >99.5% on BOTH platforms.
- ANR rate <0.47% (Android); watchdog kill rate <0.1% (iOS).
- Startup crash rate <0.1% on BOTH platforms.
- Use Crashlytics non-fatal logging for ad load failures, billing errors, JSON parse errors, audio init failures.

### Firebase Consent (Both Platforms)
- **Android:** Disable Firebase Analytics auto-initialization. Collection enabled ONLY after user consent via Google UMP SDK. Add `<meta-data android:name="firebase_analytics_collection_enabled" android:value="false" />` to AndroidManifest.
- **iOS:** Disable auto-initialization in `GoogleService-Info.plist` (`FIREBASE_ANALYTICS_COLLECTION_ENABLED = NO`). Request ATT permission before enabling collection. Add `NSUserTrackingUsageDescription` to Info.plist.

### Room Migrations (KMP)
- Always use `exportSchema = true`. Never use `fallbackToDestructiveMigration()`. Write explicit Migration objects for every schema change. Test with MigrationTestHelper on Android. On iOS, verify migration by running Room on iOS simulator with pre-migration database.
- Room database builder instantiation differs per platform: androidMain uses `Room.databaseBuilder(context, ...)`, iosMain uses `Room.databaseBuilder<AuraFlowDatabase>(name = dbFilePath).setDriver(BundledSQLiteDriver()).build()`.

---

## SELF-GOVERNANCE PROTOCOL

Claude Code must treat each phase as a gated milestone. No phase begins until the previous phase is verified. No phase ends until it passes self-review.

### Before Starting Any Phase

1. **Read `BUILD_STATE.md` and `MISTAKES.md`.** Confirm which phase was last completed, what files exist, and what mistakes to watch for. Pay special attention to MISTAKES.md entries whose "Recurrence Risk" mentions the upcoming phase.

2. **Verify the previous phase's "Done when" checklist.** For every item, run the actual verification command. Do not rely on memory -- actually execute the check:
   ```bash
   ./gradlew :shared:build 2>&1 | tail -20
   ./gradlew :androidApp:assembleDebug 2>&1 | tail -20
   ./gradlew :shared:allTests 2>&1 | tail -20
   ```
   For iOS verification, confirm the shared framework compiles:
   ```bash
   ./gradlew :shared:compileKotlinIosSimulatorArm64 2>&1 | tail -20
   ```
   If any "Done when" item fails, stop. Fix it before proceeding.

3. **Check what already exists.** Before writing any new file:
   ```bash
   find shared/src -name "*FileName*" -o -name "*filename*"
   find androidApp/src -name "*FileName*"
   find iosApp -name "*FileName*"
   ```
   If a match exists, read it first. Extend or modify -- do not create a duplicate.

4. **Check dependencies.** Before adding any dependency, verify it is not already in `gradle/libs.versions.toml` or `shared/build.gradle.kts`. Before defining any constant, check `core/constants/` and `core/telemetry/`.

### After Completing Any Phase -- Self-Review Checklist

Run every item. Any failure is a blocking issue:

**Compilation & Build (Both Platforms):**
- [ ] `./gradlew :shared:build` passes with zero errors (compiles for all targets)
- [ ] `./gradlew :androidApp:assembleDebug` passes
- [ ] `./gradlew :androidApp:assembleRelease` passes (catches R8 issues early)
- [ ] `./gradlew :shared:compileKotlinIosSimulatorArm64` passes (iOS framework)
- [ ] All new Koin module definitions use correct scope (`single {}`, `factory {}`, `viewModel {}`)
- [ ] All Room entities listed in `@Database(entities = [...])`
- [ ] All new DAOs exposed via abstract functions in the Database class
- [ ] No platform-specific imports in `commonMain/` (no `android.*`, no `platform.*` UIKit)
- [ ] All `expect` declarations have matching `actual` implementations in BOTH `androidMain/` and `iosMain/`

**No Hardcoded Values:**
- [ ] Zero hardcoded user-facing English strings in Kotlin files -- use Compose Multiplatform string resources or constants
- [ ] All colors reference the theme or `NodeColor` enum -- no raw hex in composables
- [ ] Analytics event names use `af_` prefix and are defined in `core/telemetry/AnalyticsEvents.kt`
- [ ] API URLs, ad unit IDs, API keys come from build configuration, not hardcoded strings

**Test Gate:**
- [ ] `./gradlew :shared:allTests` -- ALL tests pass on all KMP targets, not just new ones
- [ ] Every new public function in `domain/` or `data/repository/` has at least one unit test in `commonTest/`

**Lint Gate:**
- [ ] `./gradlew :androidApp:lintDebug` -- no new errors introduced by this phase

**Accessibility:**
- [ ] All clickable composables have `contentDescription` or `semantics {}` blocks
- [ ] Text sizes use `sp` not `dp`
- [ ] Color-dependent info has a non-color alternative (shape, label, pattern)
- [ ] Haptic and audio feedback gated behind their toggle settings
- [ ] Particle/animation composables check reduced-motion setting (via expect/actual)

**Performance:**
- [ ] No object allocation (Paint, Path) inside `drawScope` -- all in `remember {}`
- [ ] No `collectAsState()` on high-frequency flows in composable scope
- [ ] No database or network calls on Main dispatcher
- [ ] Lists in LazyColumn/LazyRow have stable keys

**Diff Review:**
- [ ] `git diff --stat` -- did I modify files outside this phase's scope?
- [ ] Did I duplicate code that exists in a shared location?

**Update State:**
- [ ] `BUILD_STATE.md` updated with completion status, files created, notes
- [ ] Git checkpoint: `git commit -m "Phase N complete: [description]"` and `git tag phase-N-complete`

### On Any Error During a Phase

1. **Read the full error output.** Do not truncate stack traces.
2. **Check MISTAKES.md** for similar errors encountered before.
3. **Diagnose the root cause.** State it explicitly before attempting a fix.
4. **Fix the root cause**, not the symptom. If a test fails because of a missing dependency, add the dependency -- do not delete the test.
5. **Re-run the failing command** to confirm the fix works.
6. **Run the full test suite** to confirm the fix did not break something else.
7. **Log the error in `MISTAKES.md`** with: error, root cause, fix, prevention rule, recurrence risk.

### Hard Stops -- NEVER Proceed If:
- `./gradlew :shared:build` fails
- `./gradlew :androidApp:assembleDebug` fails
- `./gradlew :shared:compileKotlinIosSimulatorArm64` fails
- Any previously passing test now fails (regression)
- Lint reports new errors introduced by the current phase
- A "Done when" item from any previous phase is no longer true

---

## ERROR PREVENTION RULES

These are the specific, recurring mistakes in the Kotlin/Compose/Koin/Room/Coroutines/KMP stack. Read this list before starting each phase.

### Kotlin / Compose Canvas

1. **Canvas drawScope is NOT a Composable context.** Never call `@Composable` functions, `animateFloatAsState()`, or suspend functions inside `Canvas { drawScope -> ... }`. Pre-compute all animated values outside Canvas, read `.value` inside drawScope.

2. **Object allocation in drawScope kills frame rate.** Never create `Paint()`, `Path()`, `Offset()`, or `Color()` inside drawScope. With 200 particles at 60fps, this causes thousands of GC-triggering objects per second. Allocate in `remember {}` outside Canvas. Call `path.reset()` + `path.moveTo/lineTo` each frame. This is even more critical on iOS because Kotlin/Native's GC has different collection timing than Android's ART GC.

3. **Modifier ordering matters.** `.clickable().padding()` is different from `.padding().clickable()`. For 56dp touch targets on nodes: put `.pointerInput` before `.padding`.

4. **Recomposition storms from unstable GameState.** `GameState` has `List<Node>` and `List<Link>` -- Compose cannot infer stability for classes with `List` fields. Split observation into granular flows: `val energy: StateFlow<Float>`, `val nodes: StateFlow<List<Node>>`, etc. Use `@Stable` or `@Immutable` annotations, or use `kotlinx.collections.immutable`.

5. **LaunchedEffect key mismanagement.** Using a mutable object as key causes infinite restarts. Use stable, immutable keys (primitives, data class instances).

6. **Use `withFrameNanos {}` for animation loops, not `delay(16)`.** Frame callbacks sync with display refresh; `delay(16)` does not. Works identically on both platforms via Compose Multiplatform.

7. **Canvas touch coordinate mismatch.** Nodes use normalized 0-1 coordinates but touch events report pixels. Convert using Canvas measured size: `normalizedX = touchX / size.width`. Account for system insets by offsetting the Canvas, NOT by adding padding to it.

### Koin / Dependency Injection (replaces Hilt rules)

8. **Koin module declarations differ from Hilt.** Replace `@Module`/`@InstallIn` with `module { single { } }`, `module { factory { } }`, `module { viewModel { } }`. Replace `@Singleton` with `single { }`. Replace `@HiltViewModel` with `viewModel { }` in Koin module. Replace `hiltViewModel()` with `koinViewModel()` in composables.

9. **Koin initialization is explicit, not annotation-driven.** Call `startKoin { modules(...) }` in `Application.onCreate()` (Android) and in `MainViewController()` init (iOS). Missing initialization causes `NoBeanDefFoundException` at runtime.

10. **Platform-specific Koin modules.** Create separate modules in `androidMain/di/` and `iosMain/di/` for platform bindings (e.g., `PlatformContext`, `HapticEngine`, `AudioEngine`). Combine with shared modules at app startup: `startKoin { modules(sharedModule, platformModule) }`.

11. **Scoping in Koin.** `single { }` = app lifetime (like `@Singleton`). Do NOT use `single { }` for objects holding Activity/UIViewController context -- these leak. `BillingManager` and `AdManager` need Activity/UIViewController for purchase/ad flows -- use `factory { }` or Activity-scoped launchers. ViewModels communicate via `SharedFlow<Intent>`.

12. **Only call `koinViewModel()` at screen-level composables.** Never inside LazyColumn items, dialogs, or bottom sheets. Pass ViewModel state and callbacks DOWN as parameters.

### Room (KMP)

13. **Schema migration required for EVERY entity change.** Every column add/remove/modify requires: increment database version + write a `Migration` object. Never use `@AutoMigration` for anything other than adding nullable columns or new tables. Never use `fallbackToDestructiveMigration()`.

14. **Type converters needed for non-primitives.** Room cannot store `Offset`, `List<Offset>`, `Enum`, or `Color` directly. Create `@TypeConverter` for every non-primitive. For enums: use `.name` / `valueOf()`, never `.ordinal` (ordinal breaks if enum order changes).

15. **Do NOT store lists as comma-separated strings.** Create proper junction tables: `UnlockedAchievementEntity(playerId, achievementId)`. This enables indexed queries and relational integrity.

16. **Room version schedule:** Phase 2 = v1, Phase 11 = v2, Phase 12 = v3, Phase 13 = v4, Phase 16 = v5. Each bump requires Migration + MigrationTestHelper test.

17. **Room KMP database builder differs per platform.** In androidMain: `Room.databaseBuilder(context, AuraFlowDatabase::class.java, "auraflow.db")`. In iosMain: `Room.databaseBuilder<AuraFlowDatabase>(name = dbFilePath).setDriver(BundledSQLiteDriver()).build()`. Use `expect`/`actual` for the database builder factory. Never instantiate the database builder in `commonMain`.

18. **Room DAO return types must be `Flow` or `suspend`.** LiveData is JVM-only and not available in KMP. All DAOs return `Flow<T>` for reactive queries and use `suspend` for writes.

### Coroutines

19. **`viewModelScope` cancels on ViewModel clear.** For work that must survive (e.g., saving progress on exit), use `NonCancellable`.

20. **StateFlow vs SharedFlow.** `StateFlow` for state with a current value (GameState). `SharedFlow` for one-shot events (LinkDrawn, StageFailed). Never use SharedFlow for state -- late subscribers miss emissions. All SharedFlows for game events: `MutableSharedFlow<GameEvent>(replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)`.

21. **Coroutine lifecycle leaks.** Game loop coroutines: launch in `viewModelScope`. Frame-rate animations: use `LaunchedEffect` with `withFrameNanos`. Pressure node timers: `viewModelScope.launch { delay(...) }`. Never use `GlobalScope`, `Handler.postDelayed()`, or `Timer`.

### KMP-Specific Rules

22. **Never put platform-specific imports in commonMain.** No `android.*`, `java.*`, `platform.UIKit.*`, `platform.CoreHaptics.*` imports in `shared/src/commonMain/`. All platform access goes through `expect/actual` declarations. Compile errors from wrong imports may only surface when building a specific target.

23. **Replace `java.time` with `kotlinx-datetime`.** `java.time` is JVM-only. Use `kotlinx.datetime.Clock.System.now()`, `kotlinx.datetime.LocalDate`, `kotlinx.datetime.Instant`. Replace `System.currentTimeMillis()` with `Clock.System.now().toEpochMilliseconds()`.

24. **Replace `java.util.UUID` with `kotlin.uuid.Uuid`.** Kotlin 2.x provides `Uuid.random()` in the standard library. Available in commonMain.

25. **Ktor engine configuration is platform-specific.** OkHttp engine on Android (`io.ktor:ktor-client-okhttp`), Darwin engine on iOS (`io.ktor:ktor-client-darwin`). Use `expect fun createPlatformHttpEngine(): HttpClientEngine` with actual implementations in each platform source set.

26. **iOS Core Haptics: CHHapticEngine must be started before playing patterns.** Call `engine.startAndReturnError(null)` in `initialize()`. Handle `stoppedHandler` and `resetHandler` to restart the engine when iOS suspends and resumes it.

27. **iOS AVFoundation: AVAudioSession category must be set before playback.** Call `AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)` before any audio operations. Handle audio interruptions (phone calls) and route changes (headphone disconnect).

28. **iOS StoreKit 2: transactions are Swift async/await.** Bridge to Kotlin/Native via a thin Swift helper class exposed as an ObjC-compatible framework, or use Kotlin/Native ObjC interop directly.

29. **Compose Multiplatform on iOS: no Android-specific Modifier extensions.** Do not use `Modifier.systemBarsPadding()` from Accompanist or `Modifier.statusBarsPadding()` from AndroidX. Use `WindowInsets` from Compose Multiplatform, or handle safe areas via expect/actual.

### ProGuard / R8 (Android)

30. **Explicit keep rules required.** R8 strips Room entities, Kotlin serialization classes, Billing classes, Ktor classes, Koin classes, and enum members. Phase 20 must include complete proguard-rules.pro with `-keep` for all these. Test by running release instrumentation tests.

31. **Firebase Crashlytics mapping.** Enable `mappingFileUploadEnabled = true` in release buildType for deobfuscated crash reports. For iOS: upload dSYMs to Firebase for symbolicated crash reports.

### Cross-Phase Architecture

32. **Single game loop for all animation.** Create a `GameLoopManager` in Phase 3 that owns the single `withFrameNanos` callback. All animated systems (node pulsing, Luma, particles, effects) register update callbacks -- no independent LaunchedEffect frame loops.

33. **Canvas layer system.** Define `CanvasLayer` interface with ordered priority: BACKGROUND, NODES, LINKS, EFFECTS, COMPANION. All future phases register as layers. Prevents Luma/particles from invalidating the entire canvas on every frame.

34. **Scoring accepts modifiers from Phase 4.** The scoring function must accept `ScoringModifiers(dynamicDifficultyActive: Boolean, lumaBoostUsed: Boolean)` as placeholders. Phase 12 populates them without modifying Phase 4 code.

35. **Daily challenge seed must be deterministic and cross-platform.** Do NOT use `String.hashCode()` -- it is JVM-implementation-dependent and may differ on Kotlin/Native. Use `seed = year * 10000 + month * 100 + day` as integer. Document: this MUST match server-side (Phase 18).

36. **Energy cost in normalized coordinate space.** Always calculate as euclidean distance in 0.0-1.0 space, NOT pixel coordinates. Document in GameEngine.kt.

---

## REGRESSION TESTING MANDATE

Every phase must prove it did not break previous phases. This is non-negotiable.

### After Every Phase, Run:

```bash
# Shared module (all targets)
./gradlew :shared:build                              # Compiles for Android + iOS
./gradlew :shared:allTests                  # ALL shared unit tests (Android JVM + iOS Simulator on macOS)

# Android
./gradlew :androidApp:assembleDebug                  # Debug build
./gradlew :androidApp:assembleRelease                # Release build (catches R8 issues)
./gradlew :androidApp:lintDebug                      # Lint check
./gradlew :androidApp:connectedDebugAndroidTest      # Instrumentation tests (if they exist)

# iOS (run when Xcode project is set up)
./gradlew :shared:compileKotlinIosSimulatorArm64     # iOS framework compilation
# Xcode: build iosApp for iOS Simulator
```

### Phase-Specific Regression Checks

Beyond the full test suite, verify these previous-phase behaviors have NOT regressed:

| After Phase | Verify These Still Work |
|-------------|------------------------|
| 3 (Rendering) | Phase 1 navigation works; placeholder screens render on BOTH platforms |
| 4 (Logic) | Phase 3 canvas renders at 60fps; links draw correctly on BOTH platforms |
| 5 (Luma) | Phase 4 engine tests pass; energy calculations unchanged |
| 6 (Haptics) | Phase 5 Luma reactions fire; no duplicate event emissions |
| 7 (Narrator) | Luma and narrator text NEVER overlap simultaneously |
| 8 (World Map) | Phase 4 stage result persistence works; Room schema valid |
| 9 (VFX) | Phase 3 canvas frame time still <16ms p95 with effects on BOTH platforms |
| 10 (Audio) | Phase 6 haptics still fire; audio+haptics don't conflict on BOTH platforms |
| 11 (Tutorial) | Phases 5, 6, 7 systems all function during tutorial |
| 12 (Difficulty) | Phase 4 scoring unmodified; energy calculations unchanged |
| 13 (Engagement) | Phase 8 world progression intact; Room migrations valid |
| 14 (Billing) | Phase 8 lock/unlock works; no accidental free premium access on either platform |
| 15 (AdMob) | Phase 14 billing unaffected; ad SDK doesn't conflict with billing on either platform |
| 16 (Zen Mode) | Phase 3 canvas shared correctly; no state leaking into main game |
| 17 (Share) | Phase 9 effects capture correctly; no missing permissions on either platform |
| 18 (Backend) | Phase 13 engagement works offline; network failure doesn't crash; Ktor client works on BOTH platforms |
| 19 (Polish) | ALL previous phases functional after animation changes on BOTH platforms |
| 20 (Release) | Full Phase 20 QA checklist passes; `bundleRelease` succeeds; Xcode archive succeeds |

### iOS Simulator Smoke Test (After Every Phase Starting Phase 1)

After confirming Android builds, also verify:
```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```
If the iOS framework fails to compile, fix before proceeding. This catches commonMain code that accidentally uses JVM-only APIs.

### Memory Health Checkpoints

After Phases 4, 7, 10, 14, and 19: run an automated test that launches the app, plays 5 stages, navigates to world map, returns to gameplay, repeats 3x, force-GCs, and asserts:
- **Android:** heap < 150MB and retained objects < 50. Add LeakCanary as `debugImplementation` in Phase 1.
- **iOS:** heap < 120MB. Use `autoreleasepool {}` in tight loops within iosMain code. Monitor via Xcode Instruments.

### If a Regression Is Found

1. Do NOT continue the current phase.
2. Identify which change caused it: `git diff phase-(N-1)-complete..HEAD`
3. Fix the regression. Do NOT `@Ignore` the failing test.
4. Re-run the full test suite on BOTH platforms.
5. Log the regression in MISTAKES.md with root cause and fix.
6. If the fix requires modifying code from a previous phase, re-run that phase's "Done when" checks.

### Rollback Protocol (Nuclear Option)

If a phase introduces too many regressions for targeted fixes:
```bash
git log --oneline phase-(N-1)-complete..HEAD    # see what was added
git checkout phase-(N-1)-complete               # return to last known good
git checkout -b phase-N-retry                   # fresh branch for retry
```
Re-paste the phase prompt incorporating lessons from the failed attempt.

---

## MISTAKE LOG PROTOCOL

Maintain `MISTAKES.md` in the project root. This is Claude Code's persistent memory of errors.

### Entry Format

```markdown
### [PHASE-NN] Short error description
- **Date:** YYYY-MM-DD
- **Phase:** N -- [Phase Name]
- **Platform:** Android / iOS / Both / Shared
- **Error:** [Key lines of error message]
- **Root Cause:** [One sentence: WHY it happened]
- **Fix Applied:** [What changed, with file path]
- **Prevention Rule:** [One sentence rule to avoid this in future]
- **Recurrence Risk:** [Which future phases could hit the same issue]
```

### When to Write an Entry
- Build fails (`assembleDebug`, `assembleRelease`, or iOS framework compilation)
- A test fails
- A runtime crash occurs on either platform
- A lint error is introduced
- A regression is discovered from a previous phase
- A dependency version conflict occurs
- A fix requires more than one attempt
- An `expect` declaration is missing an `actual` on either platform
- A commonMain file accidentally imports platform-specific APIs

### When to Read This File
- Before starting any phase (scan all entries, focus on recurrence risks for upcoming phase)
- Before writing Canvas code (filter for Phases 3, 9, 19)
- Before modifying Room entities (filter for Phases 2, 8, 13)
- Before touching Koin modules (filter for DI-related entries)
- Before adding dependencies (filter for version conflict entries)
- Before writing expect/actual code (filter for KMP-related entries)
- Before writing iOS platform code (filter for iOS-specific entries)

---

## PHASE TRANSITION GATES (MANDATORY)

Before declaring any phase complete, execute this verification sequence. A phase is NOT complete until every gate passes.

### Gate 1: Compilation (Both Platforms)
```bash
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:assembleRelease
./gradlew :shared:compileKotlinIosSimulatorArm64
```

### Gate 2: Tests
```bash
./gradlew :shared:allTests                    # ALL shared tests
./gradlew :androidApp:connectedDebugAndroidTest        # If instrumentation tests exist
```

### Gate 3: Lint
```bash
./gradlew :androidApp:lintDebug                        # Zero new errors
```

### Gate 4: Dependencies
```bash
./gradlew :androidApp:dependencies --configuration debugRuntimeClasspath | grep FAILED
```
Must return empty. No unresolved conflicts.

### Gate 5: iOS Build Verification
```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```
If iosApp Xcode project exists, also build in Xcode for iOS Simulator.

### Gate 6: Regression Smoke
Re-run the PREVIOUS phase's "Done when" checks. Confirm they still pass on BOTH platforms.

### Gate 7: Git Checkpoint
```bash
git add -A
git commit -m "Phase N complete: [description]"
git tag phase-N-complete
```

### If Any Gate Fails:
Fix within the current phase. Re-run ALL gates from Gate 1 (fixes can introduce new problems). Do NOT proceed to the next phase.

---

## FAILURE MODE PREVENTION RULES

These prevent specific silent failures identified for this project's architecture:

### 1. Canvas Layer Conflict (Phases 3 + 5 + 9)
GameCanvas, Luma, and particles all draw on Canvas. Define a `CanvasLayer` system in Phase 3 with ordered priorities. All future rendering registers as a layer. Prevents full-canvas invalidation when only Luma's idle animation runs.

### 2. Accompanist is NOT Available in KMP
Do NOT add Accompanist -- it is Android-only and deprecated. Compose Multiplatform handles edge-to-edge display, system bars, and insets natively. On Android use `WindowCompat.setDecorFitsSystemWindows()`. On iOS use safe area insets from Compose Multiplatform. Use `expect/actual` for platform-specific window inset handling.

### 3. Haptic API Level Guards (Phase 6)
**Android:** `VibrationEffect.Composition` is API 30+ only. App targets API 26+. Wrap in `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)`. Fallback to `createOneShot()` / `createWaveform()`.
**iOS:** Check `CHHapticEngine.capabilitiesForHardware().supportsHaptics` before using Core Haptics. Fallback to `UIImpactFeedbackGenerator` / `UINotificationFeedbackGenerator` on older devices.

### 4. SoundPool + ExoPlayer Conflicts / AVFoundation Session (Phase 10)
**Android:** SoundPool has ~1MB limit per loaded sound. All SFX must be: mono, 44.1kHz, OGG, <500KB. Coordinate audio focus in ONE place (`AudioEngine`). Pre-load SoundPool during splash, not on first play.
**iOS:** Set `AVAudioSession` category before any playback. Handle interruptions (phone calls) and route changes. Use MP3/AAC format (OGG not natively supported on iOS) or convert at build time. Pre-load audio buffers during splash.

### 5. Shared Element Transitions on KMP (Phase 19)
Use Compose Multiplatform's navigation transition APIs. Do NOT use deprecated accompanist-navigation-animation. Verify transition behavior on BOTH platforms as animation timing may differ.

### 6. Billing Reconnection (Phase 14)
**Android:** BillingClient disconnects on backgrounding. Implement exponential backoff reconnection (1s, 2s, 4s, max 32s, 5 retries). Before any billing operation, check `billingClient.isReady`. Never expose BillingClient directly to ViewModels.
**iOS:** StoreKit 2 handles connection management automatically. Listen for `Transaction.updates` on app launch with a detached Task. Verify entitlements on foreground.

### 7. Activity/ViewController Reference Safety (Phases 14 + 15)
**Android:** BillingManager and AdManager need Activity for `launchBillingFlow()` / `RewardedAd.show()`. Do NOT inject into ViewModels. Create Activity-scoped launchers. ViewModels communicate via `SharedFlow<Intent>`.
**iOS:** AdManager needs UIViewController for `GADRewardedAd.present()`. Obtain via `UIApplication.shared.keyWindow?.rootViewController`. BillingManager (StoreKit 2) does not need a ViewController.

### 8. Gift Feature Requires Backend (Phase 14 -> 18)
"Gift a Garden" needs backend endpoints from Phase 18. Hide behind `FeatureFlags.GIFT_ENABLED = false` until Phase 18 is complete.

### 9. Procedural Level Solvability (Phase 2)
`LevelDesigner` must include `verifySolvability(level: Level): Boolean`. Brute-force check that all pairs can be connected within energy budget. Regenerate (up to 10 attempts) if unsolvable.

### 10. Clean Build Verification
After Phases 5, 10, 15, and 20:
```bash
# Android
rm -rf build/ shared/build/ androidApp/build/ && ./gradlew clean :androidApp:assembleDebug
# iOS (if applicable)
./gradlew :shared:compileKotlinIosSimulatorArm64
```
Ensures no reliance on stale caches.

### 11. ARCHITECTURE.md as Persistent Memory
After Phase 4, create `ARCHITECTURE.md` documenting: all public interfaces, the CanvasLayer system, event flow (emitters + consumers), DI graph (Koin module structure), expect/actual declarations and their implementations. Update after every phase that adds a system. Paste contents at START of each new Claude Code session for context.

### 12. Canvas Performance on iOS via Skiko/Metal (Phase 3)
Benchmark Canvas performance on iOS early. Skiko renders via Metal on iOS. If frame time exceeds 16ms with 200 particles on an iPhone 12, reduce particle budget for iOS via `expect val maxParticleCount: Int` (200 on Android, lower on iOS if needed). Test on real devices, not just simulators.

### 13. Kotlin/Native Memory Model Differences
Kotlin/Native uses a tracing GC (concurrent, non-freezing since 1.7.20). No special threading restrictions. However: use `autoreleasepool {}` in tight loops in iosMain to prevent ObjC object accumulation. Monitor heap on iOS -- reduce image cache size if needed.

### 14. iOS Background Execution Limits
Unlike Android Services, iOS strictly limits background execution. Save game state immediately on `UIApplication.willResignActiveNotification`. Do not rely on long-running background tasks for game state persistence. Use `beginBackgroundTask` only for critical saves.

### 15. App Store Review Rejection Risks
Common rejection reasons: missing ATT prompt before tracking, IAP not using StoreKit, external payment links, missing privacy labels, crashes on review device. Test on physical devices. Ensure all IAP works in sandbox. Complete privacy nutrition labels accurately. Allow 1-7 days for initial review.

## SHARED CONSTANTS & ENUMS
When a phase needs shared values (colors, haptic names, analytics keys), define them once in `core/constants/` or `core/telemetry/` in `shared/commonMain/` and have Claude import instead of re-declaring to reduce drift.

## TESTING REQUIREMENTS (PER-PHASE MINIMUMS)

Each phase must produce the specific tests listed. "Done when" checklists are necessary but not sufficient.

| Phase | Required Tests |
|-------|---------------|
| 1 | `NavGraphTest` -- all 5 routes reachable. `ThemeTest` -- brand colors resolve in dark mode. Both build variants pass. iOS framework compiles. |
| 2 | `LevelLoaderTest` -- parse sample JSON, assert nodes/colors/pairs. `LevelDesignerTest` -- generate 10 levels per world, all solvable. `RoomSchemaTest` -- insert/query PlayerProgress + UnlockedCosmeticEntity (commonTest). `NodeColorTest` -- unique hex/displayName/shape/cvdHex per value; no two values share NodeShape; symbol non-empty. `UnlockedCosmeticDaoTest` -- seed rows insert correctly; query by playerId + cosmeticType. `MigrationTestHelper` test in androidTest (not commonTest). |
| 3 | `IntersectionDetectorTest` -- 8+ cases. `EnergyCostTest` -- verify normalized coordinate math. Every composable has a `@Preview`. iOS Canvas benchmark: frame time <16ms p95 on iPhone 12 equivalent. |
| 4 | `GameEngineTest` -- 12+ cases: win, fail, perfect, crescendo, near-miss, resonance, pressure timeout, scoring. `StageResultDaoTest` -- insert/query. |
| 5 | `LumaReactionEngineTest` -- every GameEvent maps correctly. `IdleTimerTest` -- 25s/45s/90s thresholds. |
| 6 | `HapticEngineTest` -- every HapticType maps to valid pattern. Toggle=false suppresses all vibration. No crash without vibrator hardware. API 26 fallback path tested (Android). Core Haptics unavailable fallback tested (iOS). |
| 7 | `DialogueManagerTest` -- queue priority, no simultaneous display, failure line only on first failure. All strings from resources. |
| 8 | `StageUnlockTest` -- linear unlock, boss gates, Warden's Pass gates. `CheckpointTest` -- retry from failed stage, not checkpoint. |
| 9 | `ParticleEngineTest` -- 200 particles, 100 frames, dead particles culled, count never exceeds 200. Frame time benchmark on BOTH platforms. |
| 10 | `AudioEngineTest` -- mute suppresses all playback. Volume clamps 0-1. `AudioFocusTest` (Android) -- focus loss pauses/ducks correctly. `AudioSessionTest` (iOS) -- interruption handling works. |
| 11 | `TutorialStage1Test` -- automated: tap+drag all 3 pairs, assert StageCleared. `TutorialFlagsTest` -- no re-show when flags set. Stage 1 infinite energy. |
| 12 | `DifficultyManagerTest` -- 8+ cases: 0-1 fails (no change), 2 fails (+12%), 3 fails (Luma Boost), 5 fails (+20%), boss rules, reset on clear/leave. |
| 13 | `StreakManagerTest` -- today/yesterday/2-days logic, midnight edge case (using kotlinx-datetime). `DailyChallengeTest` -- deterministic seeds, difficulty scaling. `AchievementManagerTest` -- each condition fires once. |
| 14 | `BillingManagerTest` -- mock billing client: query, purchase, acknowledge, restore, pending recovery, reconnection (Android). StoreKit 2 sandbox test (iOS). FeatureGate gates all premium features. |
| 15 | `AdManagerTest` -- mock ad loading: preload, show, reward, timeout, skip. Ads ONLY in Daily Challenge after failure. Consent-denied disables loading. ATT prompt test (iOS). |
| 16 | `ZenModeViewModelTest` -- place/link/delete/clear/autosave. `BlueprintDaoTest` -- save/load, JSON round-trip. |
| 17 | `ShareManagerTest` -- overlay applied, PNG fallback on video failure, correct MIME type. Test on BOTH platforms. |
| 18 | Backend: MockMvc tests per endpoint. Client: mock Ktor engine -- success, 404, 429 (retry), 503 (backoff), offline fallback. Contract: daily challenge JSON parses to Level model. |
| 19 | Performance benchmark: cold start <2.0s (Android), <2.5s (iOS), bloom frame time p95 <16ms. Manual: back gesture, backgrounding, rotation lock (both platforms), iOS safe area verification. |
| 20 | Android: `bundleRelease` succeeds. ProGuard smoke test on release variant. `testReleaseUnitTest` passes. iOS: Xcode archive succeeds. TestFlight build uploads. Listing text fits character limits on BOTH stores. |

---

<a id="phase-1"></a>
## PHASE 1: Project Scaffolding & Architecture

#### Done when
- [ ] KMP project compiles with `shared/`, `androidApp/`, and `iosApp/` modules.
- [ ] `./gradlew :shared:build` succeeds (compiles for all targets).
- [ ] `./gradlew :androidApp:assembleDebug` produces an APK with NavGraph showing 5 placeholder screens.
- [ ] `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds.
- [ ] Xcode builds and runs iosApp on iOS Simulator showing same placeholder screens.
- [ ] Material 3 theme uses brand palette; dark mode is default.
- [ ] Koin DI initializes on BOTH platforms without errors.
- [ ] Navigation between all 5 screens works on BOTH platforms.
- [ ] All `expect` class stubs have matching `actual` stubs in both `androidMain/` and `iosMain/`.
- [ ] Baseline lint passes.

### Prompt for Claude Code:

```
Create a new KMP (Kotlin Multiplatform) + Compose Multiplatform project called "AuraFlow" with package name com.auraflow.garden targeting Android and iOS.

## Tech Stack (use these EXACT versions):
- Kotlin 2.1.20 with K2 compiler
- Compose Multiplatform 1.10.3 (JetBrains)
- AGP 8.7.3
- Gradle 8.11.1
- Koin 4.1.1 (DI framework — replaces Hilt)
- Room 2.8.3 KMP (configure but don't create schemas yet)
- Jetpack Navigation Compose for KMP: org.jetbrains.androidx.navigation:navigation-compose:2.9.2
- Lifecycle ViewModel Compose: org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0
- kotlinx-coroutines 1.10.2
- kotlinx-serialization-json 1.9.0
- kotlinx-datetime 0.7.1
- kotlinx-collections-immutable 0.3.8
- Multiplatform Settings 1.3.0
- Coil 3.4.0 (KMP image loading)
- Ktor 3.4.2 (configure but don't use yet)
- KSP 2.1.20-1.0.32

Android targets: minSdk 26, targetSdk 35, compileSdk 35  (Google Play requires API 35+ for new submissions in 2026)
iOS targets: iosX64, iosArm64, iosSimulatorArm64, deployment target 16.0

## Project Structure — create exactly this layout:

AuraFlow/
├── build.gradle.kts                    # Root: plugin declarations only
├── settings.gradle.kts                 # Module includes, version catalog ref
├── gradle/
│   └── libs.versions.toml              # ALL versions defined here
├── gradle.properties                   # KMP & Compose flags
│
├── shared/                             # KMP shared module (95% of code)
│   ├── build.gradle.kts                # KMP + Compose + Room + KSP config
│   └── src/
│       ├── commonMain/kotlin/com/auraflow/garden/
│       │   ├── di/SharedModule.kt              # Koin modules (shared DI graph)
│       │   ├── data/
│       │   │   ├── local/db/                   # Room DB (Phase 2)
│       │   │   ├── model/                      # Domain models (Phase 2)
│       │   │   ├── remote/                     # Ktor API (Phase 18)
│       │   │   └── repository/                 # Repository interfaces
│       │   ├── domain/
│       │   │   ├── engine/                     # GameEngine (Phase 3+)
│       │   │   ├── scoring/                    # Score calc (Phase 4)
│       │   │   └── difficulty/                 # DifficultyManager (Phase 12)
│       │   ├── ui/
│       │   │   ├── theme/
│       │   │   │   ├── AuraFlowTheme.kt
│       │   │   │   ├── Color.kt
│       │   │   │   └── Typography.kt
│       │   │   ├── screens/
│       │   │   │   ├── home/HomeScreen.kt
│       │   │   │   ├── game/GameScreen.kt
│       │   │   │   ├── zen/ZenScreen.kt
│       │   │   │   ├── store/StoreScreen.kt
│       │   │   │   └── settings/SettingsScreen.kt
│       │   │   ├── components/                 # Reusable composables
│       │   │   └── navigation/
│       │   │       ├── NavGraph.kt
│       │   │       └── Routes.kt
│       │   ├── companion/                      # Luma (Phase 5)
│       │   ├── effects/                        # Particles (Phase 9)
│       │   ├── engagement/                     # Streaks (Phase 13)
│       │   ├── sharing/                        # Share (Phase 17)
│       │   ├── platform/                       # expect declarations
│       │   │   └── PlatformContext.kt
│       │   ├── core/
│       │   │   ├── constants/
│       │   │   └── telemetry/AnalyticsEvents.kt
│       │   └── util/
│       │       └── App.kt                      # Root @Composable AuraFlowApp()
│       ├── commonTest/kotlin/com/auraflow/garden/
│       ├── androidMain/kotlin/com/auraflow/garden/
│       │   ├── di/AndroidModule.kt
│       │   └── platform/PlatformContext.android.kt
│       └── iosMain/kotlin/com/auraflow/garden/
│           ├── di/IosModule.kt
│           ├── platform/PlatformContext.ios.kt
│           └── MainViewController.kt
│
├── androidApp/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/auraflow/garden/
│       │   ├── AuraFlowApplication.kt
│       │   └── MainActivity.kt
│       └── res/values/strings.xml
│
└── iosApp/
    ├── iosApp.xcodeproj/
    ├── iosApp/
    │   ├── AuraFlowApp.swift
    │   ├── ContentView.swift
    │   ├── iOSAppDelegate.swift
    │   ├── Info.plist
    │   ├── Assets.xcassets/
    │   └── Entitlements/iosApp.entitlements
    └── Podfile

## Build Configuration Files

Create gradle/libs.versions.toml with ALL dependency versions from the guardrails table.
Create root build.gradle.kts declaring plugins with `apply false`.
Create settings.gradle.kts including shared and androidApp modules.
Create gradle.properties with KMP flags:
  kotlin.mpp.enableCInteropCommonization=true
  kotlin.mpp.applyDefaultHierarchyTemplate=true
  org.jetbrains.compose.experimental.uikit.enabled=true
  android.useAndroidX=true

Create shared/build.gradle.kts with:
  - KMP targets: androidTarget, iosX64, iosArm64, iosSimulatorArm64
  - CocoaPods integration block (REQUIRED — this replaces raw iOS framework config; Firebase+AdMob are native iOS pods):
    ```kotlin
    cocoapods {
        summary = "AuraFlow shared KMP module"
        homepage = "https://auraflow.app"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "shared"
            isStatic = false   // CocoaPods requires dynamic framework
        }
        pod("Firebase/Analytics") { version = "~> 11.0" }
        pod("Firebase/Crashlytics") { version = "~> 11.0" }
        pod("Google-Mobile-Ads-SDK") { version = "~> 11.0" }
    }
    ```
  - commonMain dependencies: Compose runtime/foundation/material3/ui/resources, Navigation Compose KMP, Lifecycle ViewModel/Runtime Compose, Koin core/compose/compose-viewmodel, Room runtime, SQLite bundled, Ktor core/content-negotiation/serialization, kotlinx-serialization/coroutines/datetime/collections-immutable, Multiplatform Settings, Coil compose/network-ktor, GitLive Firebase KMP wrappers (dev.gitlive:firebase-analytics:2.4.0, dev.gitlive:firebase-crashlytics:2.4.0)
  - androidMain dependencies: Ktor OkHttp, kotlinx-coroutines-android, Koin Android, Play Billing, AdMob, Media3 ExoPlayer
  - iosMain dependencies: Ktor Darwin
  - KSP for Room compiler on all targets
  - IMPORTANT: Because CocoaPods is used, the workspace file iosApp/iosApp.xcworkspace must be used for all Xcode builds — NOT iosApp.xcodeproj.

Create androidApp/build.gradle.kts with signing configs, build types (debug/release), and dependency on :shared.

## expect/actual PlatformContext

commonMain: expect class PlatformContext
androidMain: actual typealias PlatformContext = android.content.Context
iosMain: actual class PlatformContext (empty — iOS doesn't need Context equivalent)

## Koin DI Setup

shared/commonMain di/SharedModule.kt:
- Define initKoin() function callable from both platforms
- Empty shared module for now (populated in later phases)
- Accept platform-specific modules as parameter

androidMain di/AndroidModule.kt: platform module providing PlatformContext
iosMain di/IosModule.kt: platform module providing PlatformContext

## Navigation (Jetpack Navigation Compose KMP)

NavGraph.kt: Set up NavHost with 5 routes: Home, Game, Zen, Store, Settings
Routes.kt: Sealed class with route definitions
Each screen is a placeholder @Composable showing screen name and a back button.

## Theme (dark-mode-first)

Brand colors:
- Primary: #6C63FF (cosmic violet)
- Secondary: #00B4D8 (aura teal)
- Tertiary: #FFB703 (luma gold)
- Background: #0A0A1A (deep space)
- Surface: #1A1A2E (island surface)
- Error/Alert: #E63946 (pressure red)
- Success: #06D6A0 (bloom green)

Dark-mode-first with optional light mode. Use Material 3 typography with default system font.

## App Entry Points

App.kt (commonMain): Root AuraFlowApp() composable wrapping theme and NavGraph.
MainActivity.kt (Android): Single activity calling initKoin() then setContent { AuraFlowApp() }.
MainViewController.kt (iosMain): fun MainViewController() = ComposeUIViewController { AuraFlowApp() }
AuraFlowApp.swift (iOS): @main SwiftUI app hosting ComposeUIViewController.
ContentView.swift (iOS): UIViewControllerRepresentable wrapping MainViewController.
iOSAppDelegate.swift (iOS): AppDelegate calling initKoin().

## Constraints
- ALL UI code goes in commonMain — no platform-specific Compose code
- Use Material 3 (not Material2)
- Use Koin (NOT Hilt — Hilt is Android-only)
- Use Jetpack Navigation Compose KMP (NOT Voyager, NOT Android-only Navigation)
- Use Room 2.8.3 KMP (NOT SQLDelight)
- Do NOT add Accompanist (Android-only, deprecated)
- Alpine.js comes with Livewire — do NOT install separately (this is not relevant, ignore)
- Every file must have proper package declarations matching directory structure
- Add LeakCanary as debugImplementation in androidApp

## Build Verification
After creating all files, run:
1. ./gradlew :shared:build
2. ./gradlew :androidApp:assembleDebug
3. ./gradlew :shared:compileKotlinIosSimulatorArm64
All must succeed with zero errors.
```

---

<a id="phase-2"></a>
## PHASE 2: Core Data Models & Level Schema

#### Done when
- [ ] All domain models compile in `shared/commonMain/data/model/`.
- [ ] Room entities/DAOs compile; schema export updated. `@Database(entities = [PlayerProgress::class, StageResult::class, UnlockedCosmeticEntity::class, ...])` includes all entities.
- [ ] Database builder works on BOTH platforms (androidMain context-based, iosMain BundledSQLiteDriver).
- [ ] Sample level JSON validates against schema and loads in a repository test (commonTest).
- [ ] `NodeColorTest` passes: each enum value has a unique `shape`, unique `cvdHex`; no two share the same `NodeShape`; `symbol` is non-empty.
- [ ] Room MigrationTestHelper test exists in `androidApp/src/androidTest/` (NOT in commonTest — it requires Android instrumentation runtime).
- [ ] `./gradlew :shared:build` and `./gradlew :androidApp:assembleDebug` succeed.
- [ ] `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds.

### Prompt for Claude Code:

```
In the AuraFlow KMP project, create all core data models and persistence layer in shared/commonMain.

## Domain Models (shared/src/commonMain/kotlin/com/auraflow/garden/data/model/)

### Node.kt
data class Node(
    val id: String,
    val colorType: NodeColor,
    val position: Offset,        // x,y normalized 0-1 range (Compose Offset available in commonMain)
    val isPressureNode: Boolean = false,
    val pressureDurationMs: Long = 0,
    val movementPath: List<Offset>? = null,
    val movementSpeedDps: Float = 0f,
    val isLinked: Boolean = false,
    val pairedNodeId: String = "",
)

### NodeColor.kt
/**
 * Shape discriminator for accessibility (WCAG 1.4.1 + App Store guideline).
 * Every node renders BOTH its shape AND its color — color is NEVER the sole differentiator.
 * All 8 values must be visually distinct when rendered at 40dp.
 */
enum class NodeShape { CIRCLE, SQUARE, TRIANGLE, DIAMOND, HEXAGON, STAR, CROSS, PENTAGON }

/**
 * Node color with full color-vision-deficiency (CVD) palette support.
 *
 * hex:         Standard color for full-color vision.
 * displayName: Screen-reader label (used in contentDescription).
 * shape:       Mandatory shape rendered ON the node alongside color (WCAG 1.4.1).
 * cvdHex:      Deuteranopia/protanopia-safe alternative color (activated via Settings toggle).
 * symbol:      Single-char glyph rendered inside node for severe CVD users.
 *
 * RULE: No two NodeColor values may share the same NodeShape. Enforced in NodeColorTest.
 */
enum class NodeColor(
    val hex: String,
    val displayName: String,
    val shape: NodeShape,
    val cvdHex: String,
    val symbol: String,
) {
    VIOLET( "#8B5CF6", "Violet",  NodeShape.CIRCLE,   "#5599FF", "●"),
    TEAL(   "#00B4D8", "Teal",    NodeShape.SQUARE,   "#FFCC00", "■"),
    ROSE(   "#E63946", "Rose",    NodeShape.TRIANGLE, "#FF8800", "▲"),
    AMBER(  "#FFB703", "Amber",   NodeShape.DIAMOND,  "#FFFFFF", "◆"),
    CORAL(  "#FF6B6B", "Coral",   NodeShape.HEXAGON,  "#CC99FF", "⬡"),
    INDIGO( "#4338CA", "Indigo",  NodeShape.STAR,     "#0066CC", "★"),
    EMERALD("#06D6A0", "Emerald", NodeShape.CROSS,    "#99FF99", "✚"),
    PEARL(  "#F0F0F5", "Pearl",   NodeShape.PENTAGON, "#DDDDDD", "⬠"),
}

### Link.kt, Level.kt, WorldType.kt, GameState.kt
(Same structures as the game design spec — all in commonMain using Compose Offset and kotlinx-datetime types)

### PlayerProgress.kt — Room Entity
/**
 * Single-row player progress record (id=1 always).
 * Unlocked cosmetics are NOT stored here as comma-separated strings (Error Prevention Rule 15).
 * See UnlockedCosmeticEntity below for the proper junction table.
 */
@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val id: Int = 1,
    val highestStageCleared: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPlayedDate: String = "",  // ISO date string via kotlinx-datetime
    val hasWardenPass: Boolean = false,
    val totalPerfectClears: Int = 0,
    val totalCrescendos: Int = 0,
    val selectedAuraSkin: String = "default",
    val selectedLumaSkin: String = "firefly",
    val wardenName: String = "Warden",  // Player-visible Warden identity; used in Phase 7 dialogue and Phase 14 gift flow
)

/**
 * Junction table for unlocked cosmetics.
 * Replaces comma-separated unlockedAuraSkins / unlockedLumaSkins strings (Rule 15).
 * Enables indexed queries and relational integrity.
 *
 * cosmeticType: "aura_skin" | "luma_skin" | "seasonal_pack"
 * Seeded rows on first install: ("default","aura_skin") + ("firefly","luma_skin")
 */
@Entity(
    tableName = "unlocked_cosmetics",
    primaryKeys = ["playerId", "cosmeticId"],
)
data class UnlockedCosmeticEntity(
    val playerId: Int = 1,
    val cosmeticId: String,          // e.g. "default", "firefly", "aurora_pack"
    val cosmeticType: String,        // "aura_skin" | "luma_skin" | "seasonal_pack"
    val unlockedAtMs: Long = 0L,     // epoch millis via Clock.System.now().toEpochMilliseconds()
)

### StageResult.kt — Room Entity
(Same fields but use kotlinx.datetime.Clock.System.now().toEpochMilliseconds() instead of System.currentTimeMillis())

## Room Database (KMP)

Create AuraFlowDatabase in shared/commonMain with entities and DAOs.
All DAOs return Flow<T> for reactive queries and use suspend for writes (NO LiveData — it's JVM-only).

TypeConverters for: Offset lists, enums (using .name/valueOf, never .ordinal), kotlinx-datetime Instant.

Database builder factory via expect/actual:
- shared/commonMain: expect fun createDatabaseBuilder(): RoomDatabase.Builder<AuraFlowDatabase>
- shared/androidMain: actual fun using Room.databaseBuilder(context, ...)
- shared/iosMain: actual fun using Room.databaseBuilder<AuraFlowDatabase>(name).setDriver(BundledSQLiteDriver())

exportSchema = true. Room schema directory: shared/schemas/

## Level JSON & LevelLoader

Level JSON files in shared/src/commonMain/composeResources/files/levels/ (Compose Multiplatform resources).
Create LevelLoader using kotlinx-serialization to parse JSON.
Create LevelDesigner for procedural generation with verifySolvability().
Create LevelRepository and PlayerRepository with Koin registration.

## Build Verification
./gradlew :shared:build
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-3"></a>
## PHASE 3: Game Engine — Node Rendering & Link Drawing

#### Done when
- [ ] GameCanvas renders nodes/links at 60fps on Pixel 5 emulator AND iOS Simulator.
- [ ] Link drawing enforces color match + intersection detection.
- [ ] Energy cost derived from path length and shown in UI.
- [ ] Undo clears last link and updates energy.
- [ ] Canvas performance benchmark: <16ms p95 on BOTH platforms.
- [ ] `./gradlew :shared:build`, `./gradlew :androidApp:assembleDebug`, and iOS framework compile all succeed.

### Prompt for Claude Code:

```
Build the core game rendering engine for AuraFlow using Compose Multiplatform Canvas. ALL rendering code lives in shared/commonMain — zero platform-specific drawing code.

## GameCanvas Composable (shared/commonMain/.../ui/screens/game/)

Create a full-screen Compose Canvas that renders the game field. The Compose Canvas API is identical across Android and iOS via Compose Multiplatform.

### Node Rendering:
- Each node is a circle with a soft outer glow matching its NodeColor
- Unlinked nodes pulse gently (scale 1.0 → 1.05 → 1.0 over 2s, looping)
- Linked nodes stop pulsing and glow steadily
- Pressure nodes (Deep Sea) have bioluminescent glow that fades over pressureDurationMs. NO countdown numbers — the glow IS the timer. Below 25% glow, edges flicker.
- Moving nodes (Glitch City) interpolate along movementPath with 0.5s window flash.
- Node size: ~40dp. Touch target: 56dp (larger for fat-finger tolerance).

### Link Drawing:
- Touch node → scale-up (1.0 → 1.3) + glow intensification
- Drag: smooth bezier curve from source to finger position, node color at 60% alpha
- Release over valid target (same color, unlinked): link snaps with animation, flora sprouts along path
- Release over invalid target: fizzle animation, Luma: "No... no."
- Release over empty space: trail fades

### Energy Calculation:
- Energy cost = path length in normalized units x energyCostMultiplier
- Energy bar at top, depletes smoothly. < 30% turns amber, < 10% turns red and pulses.

### Intersection Detection:
- Line segment intersection between all drawn link paths
- Burst particle at intersection points
- For noIntersectionsRequired stages, intersections trigger fail state

### Undo System:
- Undo button removes last link, refunds energy, withers flora in reverse
- Shake gesture: expect class ShakeDetector in commonMain
  - Android actual: SensorManager TYPE_ACCELEROMETER
  - iOS actual: CMMotionManager accelerometer updates
  Clears all links with chalkboard-erase animation.

### GameViewModel (using Koin, NOT Hilt):
- Holds GameState as StateFlow
- Injected via koinViewModel() at screen level
- Emits game events as SharedFlow

### GameLoopManager:
- Single withFrameNanos callback for ALL animated systems
- CanvasLayer system with ordered priorities: BACKGROUND, NODES, LINKS, EFFECTS, COMPANION

### Performance:
- Pre-allocate Paint/Path in remember{} — NEVER inside drawScope
- Profile on BOTH platforms. Target: 60fps with effects active.
- On iOS: Skiko renders via Metal. Same Canvas operations, same performance rules.

### Accessibility — REQUIRED in Phase 3 (NOT deferred to Phase 19):

**Canvas semantics overlay (App Store critical risk without this):**
The Compose Canvas is opaque to VoiceOver (iOS) and TalkBack (Android) by default. Overlay semantics in Phase 3:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    Canvas(modifier = Modifier.fillMaxSize()) { /* game drawing */ }
    // Semantic overlay: one invisible Box per node with contentDescription
    nodes.forEach { node ->
        Box(
            modifier = Modifier
                .offset { IntOffset(/* node screen position */) }
                .size(56.dp)
                .semantics {
                    contentDescription = "${node.colorType.displayName} ${node.colorType.shape.name} node"
                    role = Role.Button
                    onClick(label = "Select node") { /* handle tap */ true }
                }
        )
    }
}
```

**Tap-to-connect alternative input mode (mandatory for App Store motor accessibility):**
- Tap source node → highlights it. Tap target node of same color → draws link.
- This replaces drag-only as the sole input method. Both modes must work simultaneously.
- Implement as `connectMode: Boolean` in GameViewModel (toggled in Settings).
- VoiceOver/Switch Control users can only interact via tap, not drag.

**iOS safe area insets — handle in Phase 3:**
The game canvas must not extend under the Dynamic Island or home indicator. Do NOT defer to Phase 19.
```kotlin
// In GameScreen.kt (commonMain):
val insets = WindowInsets.safeContent  // Compose Multiplatform — works on both platforms
Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(insets)) {
    GameCanvas(...)
}
```

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-4"></a>
## PHASE 4: Game Logic — Energy, Scoring, Win/Fail States

#### Done when
- [ ] GameEngine computes win/fail with unit tests for scoring, energy, resonance (all in commonTest).
- [ ] Fail/win transitions update GameState and persist StageResult via Room.
- [ ] Near-miss detection triggers retry CTA; analytics logged.
- [ ] All tests in `domain/engine` pass.
- [ ] `./gradlew :shared:build` and both platform builds succeed.

### Prompt for Claude Code:

```
Implement the complete game logic engine for AuraFlow. ALL code lives in shared/commonMain — pure Kotlin with zero platform dependencies.

## GameEngine (shared/commonMain/.../domain/engine/GameEngine.kt)

### Energy System:
- energyBudget per level. Cost per link = euclidean distance (normalized 0-1 space) x costMultiplier.
- costMultiplier: Meadow=1.0, Glitch City=1.2, Deep Sea=1.4, Sky Nebula=1.6
- Resonance Chain (3+ same-color in sequence): refund 15% energy on those links
- Energy=0 + unlinked pairs → StageFailed

### Win/Fail Conditions:
- All pairs linked → StageCleared
- Energy remaining >= 95% of theoretical minimum → Perfect
- Perfect + all resonance chains → Crescendo
- Pressure node timer expires → fail
- noIntersectionsRequired + intersection → fail

### Scoring:
- Base: 100 x links. Efficiency: (remaining/budget) x 500. Speed bonus. Resonance: 50 x chainCount.
- No-intersection bonus: 200. Perfect: 500. Crescendo: 1000.
- Stars: 1=cleared, 2=efficiency>50%, 3=perfect/crescendo
- ScoringModifiers(dynamicDifficultyActive, lumaBoostUsed) — placeholders for Phase 12

### Near-Miss Detection:
- StageFailed + only 1 unlinked pair → NearMiss event with energy deficit

### Stage Result Persistence:
- Save StageResult to Room. Update PlayerProgress. Use kotlinx-datetime for timestamps.

### Unit Tests (shared/commonTest):
- Energy calculation, win/fail edges, resonance chains, near-miss, intersection detection, scoring.

### Koin Registration:
Register GameEngine, ScoringEngine in shared Koin module.

### Build Verification:
./gradlew :shared:allTests
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-5"></a>
## PHASE 5: Luma Companion System

#### Done when
- [ ] LumaState, LumaEmotion, and reactions render in shared Compose UI.
- [ ] Idle detection (25s/45s/90s) fires correctly.
- [ ] Luma Boost offer triggers after configured failures and logs analytics.
- [ ] No duplicate narrator + Luma messages onscreen.
- [ ] Renders identically on BOTH platforms.
- [ ] `./gradlew :shared:build` and both platform builds succeed.

### Prompt for Claude Code:

```
Build the Luma companion system for AuraFlow. ALL code in shared/commonMain — Luma is rendered via Compose Canvas which works identically on both platforms.

(Full Luma spec: LumaState, LumaEmotion enum, LumaReactionEngine mapping all GameEvents to reactions — word, emotion, glow color, animation. Luma composable drawn on Canvas with spring physics, floating text, different skins. Idle detection via coroutine timer. Same detailed spec as original — see emotional vocabulary table with all events: LinkDrawn→"Warm.", LinkFailed→"No... no.", StageFailed→"...Oh.", StageCleared→"ALIVE!", Crescendo→"CRESCENDO!", NearMiss→"So close. SO CLOSE.", etc.)

### Koin: Register LumaController as factory. No platform code needed.

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-6"></a>
## PHASE 6: Haptic Feedback Engine

#### Done when
- [ ] expect/actual HapticEngine compiles for BOTH platforms.
- [ ] Android: VibrationEffect patterns with API level fallbacks.
- [ ] iOS: Core Haptics (CHHapticEngine) patterns with UIImpactFeedbackGenerator fallback.
- [ ] All 14 HapticType patterns mapped on BOTH platforms.
- [ ] Global haptics toggle via Multiplatform Settings.
- [ ] No crash when haptics hardware unavailable on either platform.
- [ ] `./gradlew :shared:build` and both platform builds succeed.

### Prompt for Claude Code:

```
Build the cross-platform haptic feedback engine for AuraFlow using expect/actual.

## expect class HapticEngine (shared/commonMain/.../platform/HapticEngine.kt)
- play(type: HapticType), setEnabled(enabled: Boolean), isSupported(): Boolean, release()

## HapticType enum (commonMain):
NODE_TAP, LINK_SNAP, LINK_FAIL, ENERGY_REFILL, STAGE_CLEAR, PERFECT_CLEAR, CRESCENDO, STAGE_FAIL, NEAR_MISS, SHAKE_RESET, PULSE_BURST, UNDO, BOSS_ENTRY, PRESSURE_WARNING

## Android actual (shared/androidMain):
- VibrationEffect API (API 26+). VibrationEffect.Composition for API 30+.
- Fallback to createOneShot()/createWaveform() for API 26-29.
- Check Vibrator.hasAmplitudeControl().
(Same detailed pattern table as original: Node tap=10ms/80amp, Link snap=tick+thud, etc.)

## iOS actual (shared/iosMain):
- CHHapticEngine with CHHapticPattern for each HapticType.
- CHHapticEvent (transient and continuous) with intensity/sharpness parameters.
- Fallback to UIImpactFeedbackGenerator/UINotificationFeedbackGenerator on older devices.
- CRITICAL: CHHapticEngine must be started before playing. Handle stoppedHandler/resetHandler for app lifecycle.

## HapticController (commonMain):
Maps GameEvents to HapticType. Checks settings toggle. Throttles rapid haptics.

## Settings: Use Multiplatform Settings (SharedPreferences on Android, NSUserDefaults on iOS).

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-7"></a>
## PHASE 7: Narrator & Dialogue System

#### Done when
- [ ] Dialogue overlay shows narrator lines with skip + auto-advance (shared Compose UI).
- [ ] Lore fragments load from resources.
- [ ] Accessibility: semantics labels set; text respects font scale on BOTH platforms.
- [ ] Analytics: `af_dialogue_shown`, `af_dialogue_skipped`.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build the narrator and dialogue system for AuraFlow. ALL code in shared/commonMain.

(Same spec as original: NarratorOverlay with typewriter animation, DialogueManager with queue priority, StageClear card, LoreFragment system. All Compose Multiplatform — works on both platforms.

Key difference: all strings use Compose Multiplatform resources (org.jetbrains.compose.resources) for localization support. On iOS: respect safe area insets so narrator text never overlaps notch or home indicator.)

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-8"></a>
## PHASE 8: World Progression & Stage Management

#### Done when
- [ ] World map loads stage metadata; unlock logic correct.
- [ ] Checkpoint/memory bloom restores progress after relaunch on BOTH platforms.
- [ ] World entry cinematics skippable after first view.
- [ ] Repository returns correct next-stage given completion state.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build the world progression and stage management system. ALL UI in shared/commonMain (Compose Multiplatform). Persistence via Room KMP.

(Same spec as original: vertical scrollable WorldMap, 4 worlds, boss gates, checkpoint stages, world entry cinematics, level pack loading from JSON + procedural generation. Room caching.

Key KMP differences: Navigation uses Jetpack Navigation Compose KMP (org.jetbrains.androidx.navigation:navigation-compose:2.9.2). Room persistence works on both platforms via the database builder factory from Phase 2.)

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-9"></a>
## PHASE 9: Visual Effects — Bloom, Crescendo, Particle System

#### Done when
- [ ] ParticleEngine runs <16ms/frame (p95) with 100 particles on Pixel 5 AND iPhone 12 equivalent.
- [ ] Bloom/FX toggles respect reduced-motion (via expect/actual accessibility check).
- [ ] Aura skin system swaps visual sets without restart.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build the visual effects system using Compose Multiplatform Canvas. ALL code in shared/commonMain.

(Same spec as original: ParticleEngine with Particle data class, 200 max particles, Link Flora/Dissolve, Stage Bloom, Crescendo Effect, Node Pulse Glow, Pressure Node Fade, Moving Node Trail, Intersection Burst, Aura Skins.

Key KMP differences:
- Canvas performance on iOS via Skiko/Metal. Pre-allocate all objects in remember{}.
- Reduced-motion check via expect/actual: Android checks ANIMATOR_DURATION_SCALE, iOS checks UIAccessibility.isReduceMotionEnabled.
- If iOS benchmarks exceed 16ms p95, add expect val maxParticleCount: Int with platform-specific limits.)

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-10"></a>
## PHASE 10: Audio — Generative Ambient Music & SFX

#### Done when
- [ ] expect/actual AudioEngine plays ambient + SFX on BOTH platforms.
- [ ] Android: Media3 ExoPlayer for loops, SoundPool for SFX.
- [ ] iOS: AVAudioEngine for loops and SFX, AVAudioSession for session management.
- [ ] Crossfade works on both platforms. Mute persists via Multiplatform Settings.
- [ ] Memory/buffer usage under target on both platforms.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build the cross-platform audio system for AuraFlow using expect/actual.

## expect class AudioEngine (shared/commonMain/.../platform/AudioEngine.kt)
- playAmbient(world), stopAmbient(), crossfadeAmbient(toWorld, durationMs)
- playSfx(sfx: SfxType), playLumaSound(sound: LumaSoundType)
- playNote(scaleIndex, world), resolveChord(world) — generative music
- setMasterVolume/setMusicVolume/setSfxVolume/setMuted
- onAppBackgrounded(), onAppForegrounded(), release()

## Android actual (shared/androidMain):
- Media3 ExoPlayer for ambient loops (streaming)
- SoundPool for SFX (pre-loaded, <500KB each, mono, 44.1kHz, OGG)
- AudioManager.requestAudioFocus() for audio focus

## iOS actual (shared/iosMain):
- AVAudioEngine with AVAudioPlayerNode for loops and SFX
- AVAudioSession configuration: .playback category, handle interruptions and route changes
- Audio format: MP3/AAC (OGG not natively supported; convert at build time or use MP3)
- Pre-load audio buffers during splash

## Sound Categories:
(Same as original: ambient loops per world, gameplay SFX, Luma sounds, generative music per scale)

## Audio Settings:
Master/Music/SFX sliders, mute toggle. Persist via Multiplatform Settings.

For initial development, create placeholder audio files. Note where real assets should be substituted.

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-11"></a>
## PHASE 11: Tutorial Flow (Stages 1-5)

#### Done when
- [ ] Tutorial stages 1-5 load and clear on BOTH platforms.
- [ ] Luma hints appear; narrator text is skippable.
- [ ] Energy autofill rules verified in shared logic.
- [ ] Automated shared unit test covers TutorialManager.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build the tutorial flow for stages 1-5. ALL logic and UI in shared/commonMain.

(Same spec as original: Stage 1 First Meadow with infinite energy, Stage 2 Energy Introduction, Stage 3 First Real Stage, Stage 4 Color Proximity, Stage 5 Difficulty Spike. Tutorial flags in Room DB. Tutorial overlay system in Compose Multiplatform.

No platform-specific code needed — Compose handles rendering on both platforms. Haptics go through expect/actual HapticEngine from Phase 6.)

### Build Verification:
./gradlew :shared:allTests
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-12"></a>
## PHASE 12: Dynamic Difficulty System

#### Done when
- [ ] DifficultyManager tracks failures and applies boosts (pure shared logic).
- [ ] Luma Boost after 3 fails. Boss special rules. Analytics logged.
- [ ] Unit tests in commonTest.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build the dynamic difficulty system. ALL code in shared/commonMain — pure Kotlin, no platform dependencies.

(Same spec as original: 2 fails → +12% energy, 3 fails → Luma Boost offer, 5 fails → +20% + ghost trail hint. Boss rules stricter. Score impact. Analytics via shared AnalyticsTracker expect/actual.

Koin: Register DifficultyManager as factory.)

### Build Verification:
./gradlew :shared:allTests
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-13"></a>
## PHASE 13: Engagement Systems — Streaks, Daily Challenge, Achievements

#### Done when
- [ ] Streak counter persists via Room on BOTH platforms. Uses kotlinx-datetime for date logic.
- [ ] Daily challenge deterministic seed works. Free attempt + ad retry logic.
- [ ] Achievements fire once and persist.
- [ ] Optional streak reminder notifications via expect/actual LocalNotificationManager.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build engagement systems. Core logic in shared/commonMain. Notifications via expect/actual.

## Streak System (shared/commonMain/.../engagement/StreakManager.kt):
(Same spec: daily streak with kotlinx-datetime, rewards at 3/7/14/30 days, warm return after absence)

## Daily Challenge (shared/commonMain/.../engagement/DailyChallengeManager.kt):
- Deterministic seed: year * 10000 + month * 100 + day (NOT String.hashCode — differs across platforms)
- One free attempt, ad retry via expect/actual AdManager (Phase 15 wires this)
- Difficulty scales with player progress

## Achievement System (shared/commonMain/.../engagement/AchievementManager.kt):
(Same 15 achievements as original. Persist in Room. UI in shared Compose Multiplatform.)

## Conversion Funnel Analytics Constants (shared/commonMain/.../core/telemetry/AnalyticsEvents.kt):
Add these constants in Phase 13 (required by Phase 14 Billing, Phase 15 Ads, Phase 16 Zen):
```kotlin
// Phase 13 engagement funnel
const val AF_STREAK_STARTED = "af_streak_started"
const val AF_STREAK_MILESTONE = "af_streak_milestone"          // param: days
const val AF_DAILY_CHALLENGE_STARTED = "af_daily_challenge_started"
const val AF_DAILY_CHALLENGE_COMPLETED = "af_daily_challenge_completed"
const val AF_ACHIEVEMENT_UNLOCKED = "af_achievement_unlocked"  // param: achievement_id
const val AF_NOTIFICATION_OPTED_IN = "af_notification_opted_in"
const val AF_NOTIFICATION_OPTED_OUT = "af_notification_opted_out"

// Monetization funnel (used by Phase 14)
const val AF_STORE_VIEWED = "af_store_viewed"
const val AF_PURCHASE_INITIATED = "af_purchase_initiated"      // param: product_id
const val AF_PURCHASE_COMPLETED = "af_purchase_completed"      // param: product_id, price
const val AF_PURCHASE_FAILED = "af_purchase_failed"            // param: product_id, error_code
const val AF_WARDEN_PASS_GATE_HIT = "af_warden_pass_gate_hit" // param: stage_id

// Ad funnel (used by Phase 15)
const val AF_AD_OFFERED = "af_ad_offered"
const val AF_AD_WATCHED = "af_ad_watched"
const val AF_AD_DISMISSED = "af_ad_dismissed"
const val AF_AD_FAILED_TO_LOAD = "af_ad_failed_to_load"
```

## Streak Notifications (expect/actual):
- expect class LocalNotificationManager with:
  - `requestPermission(): Flow<PermissionStatus>` — PermissionStatus = GRANTED | DENIED | NOT_DETERMINED
  - `scheduleStreakReminder(triggerTimeMs: Long)`
  - `cancelAllReminders()`
  - `permissionStatus: StateFlow<PermissionStatus>`
- Android actual: WorkManager for scheduled notification
  - Requires `POST_NOTIFICATIONS` permission on Android 13+ (API 33+). Request at a natural moment (after 3-day streak or from Settings), NOT on first launch.
  - If denied: show in-app reminder banner instead of push notification. Never re-request after denial without user initiating.
- iOS actual: UNUserNotificationCenter with UNTimeIntervalNotificationTrigger
  - Request authorization with `.alert` + `.sound` options
  - `UNUserNotificationCenter.current().requestAuthorization()` result maps to PermissionStatus
  - If denied: surface a Settings deep-link in-app banner
- Add `AnalyticsEvents.NOTIFICATION_PERMISSION_GRANTED` and `NOTIFICATION_PERMISSION_DENIED` events to Phase 13 analytics constants

### Build Verification:
./gradlew :shared:allTests
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-14"></a>
## PHASE 14: Monetization — Warden's Pass, Cosmetics, IAP

#### Done when
- [ ] expect/actual BillingManager compiles on BOTH platforms.
- [ ] Android: Play Billing v6 queries products, handles purchases, restores.
- [ ] iOS: StoreKit 2 queries products, handles purchases, restores.
- [ ] iOS: `Transaction.updates` listener is started at app launch (in `iOSAppDelegate.swift` or `MainViewController`), not lazily when the user opens the store. Missing this causes missed transactions for Ask-to-Buy and interrupted purchases.
- [ ] iOS: `.storekit` configuration file created in Xcode (Product → Scheme → Edit → StoreKit Config) for sandbox testing without App Store Connect.
- [ ] iOS: Gift redemption code flow is in-app only. Any web redirect for gift redemption = App Store rejection.
- [ ] Warden's Pass gate works. Store screen renders via shared Compose UI.
- [ ] Offline-safe pending states on both platforms.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Integrate cross-platform IAP using expect/actual BillingManager.

## expect class BillingManager (shared/commonMain/.../platform/BillingManager.kt):
- initialize(), queryProducts(), purchase(productId), restorePurchases(), hasPurchase(productId)
- purchaseEvents: SharedFlow<PurchaseResult>

## Shared models (commonMain): ProductInfo, ProductType, PurchaseResult (Success/Pending/Cancelled/Error)

## Android actual (shared/androidMain): Google Play Billing v6
- BillingClient with reconnection (exponential backoff: 1s, 2s, 4s, max 32s, 5 retries)
- launchBillingFlow() requires Activity reference (passed via Koin Android module)
- acknowledgePurchase() after success
- queryPurchasesAsync() for restore

## iOS actual (shared/iosMain): StoreKit 2
- Product.products(for:) for product queries
- Product.purchase() for buying
- Transaction.currentEntitlements for restore
- CRITICAL: `Transaction.updates` listener must start at app launch in a detached Task (in iOSAppDelegate or MainViewController), NOT lazily when user opens the Store screen. Missed transactions (Ask-to-Buy approvals, interrupted purchases) are lost if the listener starts late.
- JWS verification is automatic in StoreKit 2 — do NOT manually verify JWS in client code
- Bridge via Kotlin/Native ObjC interop or thin Swift helper class

## Products (same on both stores):
(Same product table as original: Warden's Pass $3.99, Aura Skins $0.99-1.99, Luma Skins $0.99, Seasonal Packs $1.99, Gift $3.99)

## Product ID Mapping:
Android uses product ID directly. iOS prepends bundle ID prefix. Mapping in commonMain ProductIds object.

## EntitlementManager (commonMain): reads purchase state, determines unlocks, writes to Room.

## Store Screen (shared Compose Multiplatform):
(Same spec as original: Warden's Pass card, Aura Skins section, Luma Skins section, Gift section)

## Purchase Trigger Points:
(Same as original: after Stage 50 boss, settings restore button, locked world nodes. NEVER interrupt gameplay.)

### Build Verification:
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
# iOS: Xcode build + StoreKit Configuration file for sandbox testing
```

---

<a id="phase-15"></a>
## PHASE 15: AdMob Rewarded Video Integration

#### Done when
- [ ] expect/actual AdManager compiles on BOTH platforms.
- [ ] Rewarded ad loads and shows ONLY after Daily Challenge fail on BOTH platforms.
- [ ] Consent sequencing correct on BOTH platforms: UMP form → ATT (iOS only) → Firebase.configure() → MobileAds.initialize() → ad load. Out-of-order = privacy violation + App Store rejection.
- [ ] Android: Firebase auto-init disabled via `firebase_analytics_collection_enabled=false` in AndroidManifest meta-data. Enabled programmatically after UMP consent.
- [ ] iOS: Firebase auto-init disabled in GoogleService-Info.plist. Enabled programmatically after UMP + ATT.
- [ ] Ad failure falls back to standard retry timer.
- [ ] Analytics emitted on both platforms.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Integrate AdMob Rewarded Video using expect/actual AdManager and ConsentManager.

## expect class AdManager (shared/commonMain/.../platform/AdManager.kt):
- initialize(consentGranted), preloadRewardedAd(), showRewardedAd(): AdRewardResult, adState: StateFlow<AdState>

## expect class ConsentManager (shared/commonMain/.../platform/ConsentManager.kt):
- requestConsentInfo(), showConsentForm(), consentStatus: StateFlow<ConsentStatus>, canShowAds()

## Android actual: Google Mobile Ads SDK + Google UMP SDK
- Consent sequencing: UMP ConsentForm.show() → check canRequestAds() → MobileAds.initialize() → ad load. Firebase Analytics enabled after UMP grants consent.
- MobileAds.initialize() only after UMP consent — NOT in Application.onCreate() unconditionally
- RewardedAd.load()/show() — requires Activity reference

## iOS actual: Google Mobile Ads iOS SDK (via CocoaPods) + ATT + UMP
- GADMobileAds.sharedInstance().start()
- GADRewardedAd.load()/present() — requires UIViewController

**CRITICAL — Consent sequencing on iOS (wrong order = App Store rejection + Firebase privacy violation):**
1. Show Google UMP consent form FIRST (GDPR/CCPA — required even for non-EU users if using Firebase)
2. After UMP resolves → request ATT (ATTrackingManager.requestTrackingAuthorization)
3. After ATT resolves → call Firebase.configure() (do NOT configure Firebase before consent)
4. After Firebase configured → initialize GADMobileAds and load ads

NEVER initialize Firebase auto-collection before consent. Set `FIREBASE_ANALYTICS_COLLECTION_ENABLED = NO` in GoogleService-Info.plist. Enable programmatically after step 3.

- Add NSUserTrackingUsageDescription to Info.plist
- If ATT denied: ConsentManager.canShowAds() returns false for personalized; may still show non-personalized if UMP granted

## Rules:
- Ads ONLY in Daily Challenge, ONLY after failure, ONLY as opt-in rewarded video.
- Main game is COMPLETELY ad-free. No banners, no interstitials. Ever.

## Flow (shared logic):
(Same as original: fail → "Watch to Try Again" / "Come Back Tomorrow" buttons, load/show/reward/dismiss handling)

### Build Verification:
./gradlew :androidApp:assembleDebug
# iOS: Xcode build with Google-Mobile-Ads-SDK pod, add GADApplicationIdentifier to Info.plist
```

---

<a id="phase-16"></a>
## PHASE 16: Zen Mode & Community Blueprints

#### Done when
- [ ] Zen Mode sandbox renders on BOTH platforms via shared Compose Canvas.
- [ ] Blueprint save/load via Room on both platforms.
- [ ] Performance stable with large node counts. Reduced-motion respected.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build Zen Mode and Community Blueprints. ALL UI in shared/commonMain (Compose Multiplatform). Persistence via Room KMP.

(Same spec as original: no energy/timer/scoring, color selector toolbar, grid size selector, node placement, link drawing, shake-to-reset via expect/actual ShakeDetector. Blueprint data model, save/load in Room, playable as levels.

Key KMP differences:
- Shake-to-reset: Android=SensorManager, iOS=CMMotionManager (expect/actual from Phase 3)
- iOS: toolbar positions respect safe area insets
- Blueprint timestamps use kotlinx-datetime)

### Build Verification:
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-17"></a>
## PHASE 17: Shareable Crescendo Clips

#### Done when
- [ ] expect/actual ShareManager and VideoCapture compile on BOTH platforms.
- [ ] Android: MediaCodec/MediaMuxer captures clip. Share via Intent + FileProvider.
- [ ] iOS: AVAssetWriter captures clip. Share via UIActivityViewController.
- [ ] Fallback static screenshot if encoding fails on either platform.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build shareable Crescendo clips using expect/actual for video capture and sharing.

## expect class VideoCapture (shared/commonMain/.../sharing/VideoCapture.kt):
- startRecording(width, height, fps), addFrame(frameData: ByteArray), stopRecording(): String?

## expect class ShareManager (shared/commonMain/.../platform/ShareManager.kt):
- shareImage(imageBytes, text, mimeType), shareText(text)

## Android actual:
- VideoCapture: MediaCodec (H.264) + MediaMuxer → MP4 in cache dir. FileProvider for URI.
- ShareManager: Intent.ACTION_SEND with chooser.

## iOS actual:
- VideoCapture: AVAssetWriter + AVAssetWriterInputPixelBufferAdaptor → MP4 in temp dir. Swift helper for bridge.
- ShareManager: UIActivityViewController presented from root UIViewController.

## Overlay branding, share flow, fallback screenshot — same as original spec.

### Build Verification:
./gradlew :androidApp:assembleDebug
# iOS: Xcode build — verify AVFoundation linking and share sheet
```

---

<a id="phase-18"></a>
## PHASE 18: Backend — Daily Challenges & Leaderboard API

#### Done when
- [ ] Backend endpoints documented with samples.
- [ ] Ktor client in shared/commonMain handles requests on BOTH platforms.
- [ ] Retry/backoff for 429/503. Auth header attached.
- [ ] Offline fallback works (client-side seed generation).
- [ ] Mock Ktor engine tests pass in commonTest.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Build backend + Ktor client. Backend is standard Spring Boot Kotlin (no KMP changes). Client uses Ktor in shared/commonMain.

## Backend: Same spec as original (Spring Boot, PostgreSQL, Hetzner VPS, daily challenge/leaderboard/blueprints/gift endpoints).

Additional endpoint for iOS:
POST /api/v1/validate/apple — App Store Server Notifications v2 webhook for purchase validation.

## Client: Ktor (shared/commonMain)

### Ktor HttpClient Setup:
- expect fun createPlatformHttpEngine(): HttpClientEngine
- Android actual: OkHttp engine
- iOS actual: Darwin engine

- Configure in commonMain:
  HttpClient(createPlatformHttpEngine()) {
      install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
      defaultRequest { header("X-Api-Key", apiKey) }
  }

- Retry/backoff: exponential (1s, 2s, 4s, max 3 retries) for 429/503
- Offline: queue leaderboard submissions locally (Room), sync on connectivity return
- Daily challenge fallback: client-side seed generation when offline

### Base URL Config:
- Android debug: http://10.0.2.2:8080  (Android emulator loopback to host machine)
- iOS Simulator debug: http://localhost:8080  (shares host network — works on Simulator)
- iOS physical device debug: http://{DEV_MACHINE_LAN_IP}:8080  (localhost fails on real device — use your machine's LAN IP, e.g. 192.168.1.x)
- Both release: https://api.auraflow.app
- Store the debug URL in a BuildConfig/build flag, NOT hardcoded — different for emulator vs physical vs release.

### Build Verification:
./gradlew :shared:allTests  # Ktor mock engine tests
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

<a id="phase-19"></a>
## PHASE 19: Polish — Animations, Transitions, Loading States

#### Done when
- [ ] Screen transitions smooth (p95 <16ms) on BOTH platforms.
- [ ] Loading states use in-world visuals on BOTH platforms.
- [ ] Edge cases handled: backgrounding, rotation lock, low memory (both platforms).
- [ ] iOS-specific: safe area insets, home indicator auto-hide, status bar management.
- [ ] Android-specific: edge-to-edge, predictive back gesture.
- [ ] Reduced-motion mode disables effects via expect/actual.
- [ ] Both platform builds succeed.

### Prompt for Claude Code:

```
Polish AuraFlow with animations, transitions, and platform-specific refinements.

## Screen Transitions (shared Compose Multiplatform):
(Same as original: shared element Home→Game, spring animations, crossfades)

## Loading States (shared): In-world Luma pulse, no generic progress bars.

## Micro-Animations (shared): Button scale, energy bar depletion, star rating stagger, score ticker.

## Performance Mode toggle: reduces particles (200→50), disables flora, simplifies glow.

## Platform-Specific Polish:

### iOS:
- Safe area insets: respect on ALL screens (Compose Multiplatform WindowInsets or expect/actual)
- Home indicator: auto-hide during gameplay (implement via UIKit interop)
- Status bar: hide during gameplay
- Scroll physics: iOS-native rubber-banding (Compose Multiplatform handles)
- Back swipe: works with Jetpack Navigation Compose KMP

### Android:
- Edge-to-edge: WindowCompat.setDecorFitsSystemWindows(window, false)
- Predictive back gesture: Android 14+ support
- System bars: transparent status bar, translucent nav bar during gameplay

## Edge Cases (both platforms):
- Backgrounding: pause timers, save state to Room immediately
- Rotation: lock to portrait (AndroidManifest + Info.plist)
- Low memory: reduce particle count (Android: ComponentCallbacks2, iOS: didReceiveMemoryWarning)
- Back gesture confirmation on game screen

## Reduced Motion (expect/actual):
- Android: Settings.Global.ANIMATOR_DURATION_SCALE == 0
- iOS: UIAccessibility.isReduceMotionEnabled
- When active: disable particles, sky rotation, flora growth; use instant transitions

### Build Verification:
./gradlew :androidApp:assembleDebug
# iOS: Xcode build + Instruments profiling
```

---

<a id="phase-20"></a>
## PHASE 20: Testing, Build Config & Store Prep

#### Done when
- [ ] Android: build variants, signing, ProGuard rules, `bundleRelease` succeeds.
- [ ] iOS: Xcode archive with release signing, TestFlight build uploaded.
- [ ] All shared + platform tests green.
- [ ] Play Store AND App Store listing assets drafted.
- [ ] Privacy policy hosted. Content ratings complete for both stores.
- [ ] iOS App Privacy nutrition labels complete.

### Prompt for Claude Code:

```
Prepare AuraFlow for BOTH Google Play Store and Apple App Store submission.

## Android Build Configuration:
- applicationId: "com.auraflow.garden", versionCode: 1, versionName: "1.0.0"
- Build variants: debug (test IDs, local API) / release (production IDs, ProGuard)
- Signing: release keystore in local.properties, signingConfigs in build.gradle.kts
- ProGuard: keep Room, kotlinx-serialization, Play Billing, AdMob, Firebase, Ktor, Koin, Compose

## iOS Build Configuration:
- Bundle ID: com.auraflow.garden, Version: 1.0.0, Build: 1
- Deployment Target: iOS 16.0, Devices: iPhone only
- Signing: Apple Developer Program, Distribution Certificate, Provisioning Profiles
- Capabilities: In-App Purchase, Associated Domains
- Archive: Xcode Product→Archive or xcodebuild command line
- TestFlight: upload, internal testers, Beta App Review

## Testing Checklist:

### Shared Unit Tests (shared/commonTest):
GameEngine, DifficultyManager, LevelLoader, StreakManager, AchievementManager, Ktor ApiService (mock engine), EntitlementManager

### Android Integration Tests:
BillingManager (Play test tracks), AdManager, Room persistence, API calls

### iOS Integration Tests:
StoreKit 2 (sandbox with .storekit config file), Google Mobile Ads test ads, Room persistence on iOS

### Manual QA — Android:
(Same 20-item checklist as original)

### Manual QA — iOS (additional):
- [ ] Safe area insets on iPhone 15 Pro (Dynamic Island) and iPhone SE
- [ ] Home indicator hides during gameplay
- [ ] Back swipe gesture works with navigation
- [ ] StoreKit 2 sandbox purchases complete
- [ ] Haptic feedback uses correct iOS patterns
- [ ] ATT prompt appears before ad/tracking requests
- [ ] Audio session management: pauses on call, resumes after
- [ ] App state restoration after iOS kills app
- [ ] Layout on iPhone SE (smallest) through iPhone 15 Pro Max (largest)
- [ ] VoiceOver accessibility: all interactive elements labeled

## Play Store Listing:
- Title: "Aura Flow: The Kinetic Garden"
- Short Description (80 chars): "Restore a cosmic garden. Draw connections. Find your calm."
- Content Rating: ESRB Everyone / PEGI 3
- 8 screenshots, privacy policy, data safety form

## App Store Listing:
- Name: "Aura Flow: The Kinetic Garden"
- Subtitle (30 chars): "Zen Puzzle Garden"
- Keywords (100 chars): "puzzle,zen,meditation,garden,calm,relax,connect,nature,mindful,beautiful"
- Screenshots (REQUIRED sizes for iPhone App Store — do NOT submit wrong sizes):
  - 6.7" display (1290×2796): iPhone 15 Pro Max / 16 Pro Max — **REQUIRED**
  - 5.5" display (1242×2208): iPhone 8 Plus — **REQUIRED** (Apple still mandates this size for compatibility)
  - Note: 6.1" is NOT a required screenshot size as of 2026. 5.5" + 6.7" covers all mandatory slots.
  - Optional: 6.1" (1179×2556) for iPhone 15 / 16 display
- Age Rating: 4+
- App Privacy Nutrition Labels: Usage Data (analytics), Diagnostics (crash logs), Identifiers (anonymous UUID)
- ATT disclosure, In-App Purchase review information

## Shared Privacy Policy (hosted at https://auraflow.app/privacy):
Covering both platforms, GDPR/CCPA compliance, no PII collected.

## Launch Markets: India, US, UK, Canada, Australia. English v1, Hindi/Telugu v2.

## Build Commands:
### Android:
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:assembleRelease
./gradlew :androidApp:bundleRelease
./gradlew :shared:allTests
./gradlew :androidApp:lintDebug

### iOS:
xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16' build
xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -configuration Release -archivePath build/iosApp.xcarchive archive
xcodebuild -exportArchive -archivePath build/iosApp.xcarchive -exportPath build/iosApp -exportOptionsPlist iosApp/ExportOptions.plist

## Post-Launch:
- Monitor Crashlytics daily for first week (both platforms)
- Track D1/D7/D30 retention, Warden's Pass conversion, ad view rate — split by platform
- iOS: monitor App Store review feedback (Apple may request changes)
- v1.1: seasonal pack, Hindi/Telugu, iPad support
- v1.2: community blueprints online, video crescendo clips
```

---

## APPENDIX: Execution Order, Dependencies & Time Estimates

| Phase | Depends On | Est. Time (KMP) | Priority |
|-------|-----------|------------------|----------|
| 1. Scaffolding | None | 5-7 hours | Must |
| 2. Data Models | Phase 1 | 4-5 hours | Must |
| 3. Game Rendering | Phase 2 | 9-13 hours | Must |
| 4. Game Logic | Phase 2, 3 | 6-8 hours | Must |
| 5. Luma Companion | Phase 3, 4 | 6-8 hours | Must |
| 6. Haptic Engine | Phase 4 | 4-6 hours | Must |
| 7. Narrator System | Phase 4, 5 | 4-5 hours | Must |
| 8. World Progression | Phase 4, 7 | 4-5 hours | Must |
| 9. Visual Effects | Phase 3 | 6-8 hours | Must |
| 10. Audio | Phase 4 | 8-10 hours | Should |
| 11. Tutorial | Phase 5, 6, 7 | 4-5 hours | Must |
| 12. Dynamic Difficulty | Phase 4 | 2-3 hours | Must |
| 13. Engagement | Phase 4, 8 | 4-6 hours | Must |
| 14. IAP/Billing | Phase 8 | 8-12 hours | Must |
| 15. AdMob | Phase 13 | 4-5 hours | Should |
| 16. Zen Mode | Phase 3, 9 | 4-5 hours | Should |
| 17. Share Clips | Phase 9 | 4-5 hours | Nice |
| 18. Backend | Phase 13 | 6-8 hours | Should |
| 19. Polish | All above | 8-10 hours | Must |
| 20. Store Prep | All above | 8-12 hours | Must |

**Total estimated: ~120-160 hours of Claude Code sessions**

The additional 30-40 hours over the original Android-only estimate account for:
- KMP project scaffolding and Xcode setup (~3 hours)
- iOS expect/actual implementations for platform APIs (~15-20 hours)
- iOS-specific testing and benchmarking (~5 hours)
- App Store submission preparation (~4-6 hours)
- Build system complexity and debugging (~3-5 hours)

### Minimum Viable Product (MVP) — Ship This First:
Phases 1-9, 11-14, 19-20 = ~90-115 hours
This gives you: Worlds 1-2 free, Warden's Pass IAP, core gameplay loop, Luma, haptics, tutorial, dynamic difficulty, and BOTH Play Store + App Store submission.

### Post-MVP Additions:
Phases 10, 15-18 = remaining features added in updates.

---

## HOW TO USE THIS DOCUMENT

### First-Time Setup
1. Ensure `CLAUDE.md`, `BUILD_STATE.md`, and `MISTAKES.md` exist in the project root.
2. Initialize a Git repository: `git init && git add -A && git commit -m "Initial project setup with build specifications"`
3. Open Claude Code in the AuraFlow project directory. Claude will auto-read `CLAUDE.md` and follow the Session Start Protocol.

### For Each Phase
1. **Session Start:** Claude reads `BUILD_STATE.md` + `MISTAKES.md`, verifies the build on BOTH platforms, and reports status.
2. **Paste the phase prompt** from this document. Claude executes it.
3. **Claude self-reviews** using the Self-Governance Protocol (compilation on both platforms, tests, lint, diff review, accessibility, performance).
4. **Phase Transition Gates** must ALL pass before moving on (compilation on both platforms, tests, lint, dependencies, iOS build verification, regression smoke, git checkpoint).
5. **Claude updates** `BUILD_STATE.md` and commits with tag `phase-N-complete`.
6. **If anything breaks:** Claude diagnoses root cause, fixes, re-tests on BOTH platforms, logs to `MISTAKES.md`, and re-runs all gates.

### Across Sessions
- Claude reads `BUILD_STATE.md` to know where it left off. It does NOT re-do completed work.
- Claude reads `MISTAKES.md` to avoid repeating past errors. Each entry has "Recurrence Risk" and "Platform" indicating which future phases and platforms are affected.
- After Phase 4, Claude creates and maintains `ARCHITECTURE.md` -- the persistent memory of system interfaces, event flows, DI graph, and expect/actual declarations.
- Each phase builds on the previous -- the Phase Transition Gates enforce this mechanically.

### If Something Goes Wrong
- **Build breaks:** Fix before proceeding. Never write new code on a broken build. Check BOTH platforms.
- **Regression:** Find which change caused it via `git diff phase-(N-1)-complete..HEAD`. Fix. Re-run full test suite.
- **Too many regressions:** Use the Rollback Protocol -- checkout the last good tag, branch, retry the phase with lessons learned.
- **iOS-specific issue:** Check MISTAKES.md for iOS entries. Common issues: missing actual implementation, platform API not available in commonMain, safe area insets, Core Haptics lifecycle, AVAudioSession configuration.
- **KMP-specific issue:** Check for platform-specific imports in commonMain, missing expect/actual pairs, JVM-only API usage (java.time, System.currentTimeMillis, LiveData).

### MVP Path (Ship First)
Phases 1-9, 11-14, 19-20 = ~90-115 hours. This gives you the full game on BOTH Android and iOS with core features.

### Post-MVP Additions
Phases 10, 15-18 = remaining features added in updates.

---

Good luck, Warden. The garden is waiting -- on both platforms.
