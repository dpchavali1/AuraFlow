# AuraFlow: The Kinetic Garden -- KMP Cross-Platform Migration Plan

> **Document Status:** PROPOSED
> **Date:** 2026-04-02
> **Scope:** Migrate AuraFlow from Android-only (Kotlin/Jetpack Compose) to
> cross-platform Android + iOS using Kotlin Multiplatform + Compose Multiplatform.
> **Prerequisite:** This plan assumes the Android-only build is not yet started
> (Phase 1 NOT_STARTED per BUILD_STATE.md). The project should be scaffolded as
> KMP from day one rather than migrated post-facto.

---

## TABLE OF CONTENTS

1. [ADR-001: Adopt KMP + Compose Multiplatform](#adr-001)
2. [New Tech Stack](#new-tech-stack)
3. [Dependency Version Table (Build Guardrails)](#dependency-version-table)
4. [Project Structure](#project-structure)
5. [Package Migration Map](#package-migration-map)
6. [Platform Abstraction Layer (expect/actual)](#platform-abstraction-layer)
7. [What Stays Exactly the Same (Shared)](#what-stays-shared)
8. [What Cannot Be Shared (Platform-Specific)](#what-cannot-be-shared)
9. [iOS-Specific Requirements](#ios-specific-requirements)
10. [Build System Changes](#build-system-changes)
11. [Risk Assessment](#risk-assessment)
12. [Migration Phase Adjustments](#migration-phase-adjustments)

---

<a id="adr-001"></a>
## ADR-001: Adopt Kotlin Multiplatform + Compose Multiplatform for Cross-Platform

### Status
Proposed

### Context
AuraFlow is designed as a 2D puzzle game with Canvas-based rendering, MVVM
architecture, and a Kotlin-only codebase. The game is currently specified for
Android only. To reach iOS users without maintaining a separate Swift codebase,
we need a cross-platform strategy.

Options evaluated:
1. **Flutter** -- Would require a full rewrite in Dart. Discarded.
2. **React Native** -- JavaScript runtime overhead inappropriate for 60fps Canvas
   game rendering. Discarded.
3. **KMP + Compose Multiplatform** -- Existing Kotlin code transfers directly.
   Compose Multiplatform renders via Skiko/Metal on iOS with native 60fps Canvas
   support. Only platform APIs (billing, ads, haptics, audio) need platform
   implementations.
4. **KMP + SwiftUI for iOS UI** -- Would require rewriting all Compose UI in
   SwiftUI. The game Canvas code (the largest and most complex UI component)
   would need a complete reimplementation. Discarded.

### Decision
Adopt Kotlin Multiplatform (KMP) with Compose Multiplatform for shared UI.
Scaffold the project as KMP from the start rather than building Android-only and
migrating later.

### Consequences
**Easier:**
- Single Kotlin codebase for game logic, UI, models, and navigation.
- Canvas rendering code works identically on both platforms (Skiko on iOS uses
  Metal, matching Android's hardware-accelerated Canvas).
- Bug fixes and features ship to both platforms simultaneously.
- Domain models, game engine, scoring, difficulty, and engagement logic are
  written once.

**Harder:**
- Compose Multiplatform on iOS, while stable since 1.8.0 (May 2025), is younger
  than Jetpack Compose on Android. Edge cases may surface.
- Platform-specific APIs (billing, ads, haptics, audio, sharing, sensors) require
  expect/actual implementations and iOS-native knowledge (Swift/ObjC interop).
- Build complexity increases (Gradle multiplatform plugin, Xcode integration,
  CocoaPods/SPM for iOS dependencies).
- CI/CD must build and sign for both platforms.
- Team needs at least basic iOS/Xcode knowledge for the iosApp shell, App Store
  submission, and debugging iOS-specific issues.

---

<a id="new-tech-stack"></a>
## NEW TECH STACK

### Core Platform

| Component | Android-Only (Original) | KMP Cross-Platform (New) | Rationale |
|-----------|------------------------|--------------------------|-----------|
| Language | Kotlin 1.9.24 | **Kotlin 2.1.20** | Required by Compose Multiplatform 1.10.x. Kotlin 2.x includes the K2 compiler which is mandatory for native/iOS targets. |
| UI Framework | Jetpack Compose (BOM 2024.12.01) | **Compose Multiplatform 1.10.3** (JetBrains) | Renders via Skiko/Metal on iOS. Canvas API is identical. Compose Multiplatform for iOS is stable since 1.8.0. |
| Build Plugin | AGP 8.5.0 | **AGP 8.7.3** + **Kotlin Multiplatform Gradle Plugin 2.1.20** + **Compose Multiplatform Gradle Plugin 1.10.3** | AGP upgraded for Kotlin 2.1.x compatibility. KMP and Compose plugins added. |
| Gradle | (unspecified) | **Gradle 8.11.1** | Required minimum for AGP 8.7.x and Kotlin 2.1.x compatibility. |

### Architecture & DI

| Component | Android-Only (Original) | KMP Cross-Platform (New) | Rationale |
|-----------|------------------------|--------------------------|-----------|
| DI Framework | Hilt 2.51 (Dagger/kapt) | **Koin 4.1.1** (KMP-native) | Hilt depends on Dagger annotation processing (kapt/ksp) which is Android/JVM-only. Koin is a pure Kotlin DI framework with first-class KMP support. No code generation required. |
| Architecture | MVVM + Hilt ViewModel | **MVVM + Koin ViewModel** | ViewModel pattern preserved. Use `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0` for multiplatform ViewModel. Koin provides `koinViewModel()` as replacement for `hiltViewModel()`. |
| Navigation | Compose Navigation (Android) | **Jetpack Navigation Compose for KMP** (`org.jetbrains.androidx.navigation:navigation-compose:2.9.2`) | The AndroidX Navigation library now has official KMP support via JetBrains-published multiplatform artifacts. Same API as Jetpack Compose Navigation. |

### Data & Persistence

| Component | Android-Only (Original) | KMP Cross-Platform (New) | Rationale |
|-----------|------------------------|--------------------------|-----------|
| Database | Room 2.6.1 | **Room 2.8.3** (KMP) | Room now officially supports KMP (Android, iOS, JVM Desktop) as of 2.7.0+. Version 2.8.3 is stable. This avoids an unnecessary migration to SQLDelight. Room entities, DAOs, and migrations transfer with minimal changes. Uses KSP for code generation on all targets. |
| Database Driver (iOS) | N/A | **androidx.sqlite:sqlite-bundled** | Room on iOS uses a bundled SQLite driver. No separate native SQLite configuration needed. |
| Preferences | (DataStore implied) | **Multiplatform Settings 1.3.0** (`com.russhwolf:multiplatform-settings`) | Lightweight key-value storage. Wraps SharedPreferences on Android and NSUserDefaults on iOS. Simpler than DataStore for KMP. |
| JSON Serialization | Kotlin Serialization | **kotlinx-serialization-json 1.9.0** | Already KMP-native. No change needed beyond version bump for Kotlin 2.1.x compatibility. |

### Networking

| Component | Android-Only (Original) | KMP Cross-Platform (New) | Rationale |
|-----------|------------------------|--------------------------|-----------|
| HTTP Client | Retrofit (implied in Phase 18) | **Ktor Client 3.4.2** | Retrofit is JVM-only. Ktor Client is KMP-native with platform engines (OkHttp on Android, Darwin/URLSession on iOS). Same coroutine-based async model. |
| Serialization | Gson/Moshi (implied) | **Ktor Content Negotiation + kotlinx-serialization** | KMP-native JSON parsing via Ktor's built-in serialization support. |

### Platform Services

| Component | Android-Only (Original) | KMP Cross-Platform (New) | Rationale |
|-----------|------------------------|--------------------------|-----------|
| Analytics | Firebase Analytics (BOM 33.1.0) | **GitLive firebase-kotlin-sdk 2.4.0** (commonMain interface) + native Firebase SDKs | GitLive provides KMP wrappers around native Firebase SDKs. Alternatively, use expect/actual with native Firebase Android SDK and Firebase iOS SDK directly. |
| Crash Reporting | Firebase Crashlytics | **expect/actual CrashReporter** wrapping Crashlytics Android + Crashlytics iOS | No mature KMP wrapper for Crashlytics. Use expect/actual pattern. |
| Billing | Google Play Billing 6.2.1 | **expect/actual BillingManager**: Play Billing 6.2.1 (Android) + StoreKit 2 (iOS) | Fundamentally different APIs. Must be abstracted behind a common interface. |
| Ads | AdMob 23.0.0 | **expect/actual AdManager**: Google Mobile Ads Android + Google Mobile Ads iOS | AdMob has separate Android and iOS SDKs. Abstract behind common interface. |
| Audio | Media3/ExoPlayer 1.3.1 | **expect/actual AudioEngine**: Media3 (Android) + AVFoundation (iOS) | Platform audio APIs are fundamentally different. SoundPool (Android) vs AVAudioEngine (iOS) for SFX. |
| Haptics | VibrationEffect (Android) | **expect/actual HapticEngine**: VibrationEffect (Android) + Core Haptics (iOS) | Different haptic APIs. iOS Core Haptics provides richer patterns than Android on supported devices. |
| Image Loading | Coil (Android) | **Coil 3.4.0** (`io.coil-kt.coil3:coil-compose`) | Coil 3 is KMP-native. Supports Android, iOS, Desktop, Web. Drop-in replacement. |

### Async & Utilities

| Component | Android-Only (Original) | KMP Cross-Platform (New) | Rationale |
|-----------|------------------------|--------------------------|-----------|
| Coroutines | 1.8.1 | **kotlinx-coroutines 1.10.2** | KMP-native. Version bump for Kotlin 2.1.x compatibility. |
| Date/Time | java.time (implied) | **kotlinx-datetime 0.7.1** | java.time is JVM-only. kotlinx-datetime is KMP-native with identical semantics for date arithmetic, time zones, and formatting. |
| Collections | stdlib | **kotlinx-collections-immutable 0.3.8** | Already recommended in the build prompt for Compose stability. KMP-native. |

---

<a id="dependency-version-table"></a>
## DEPENDENCY VERSION TABLE (BUILD GUARDRAILS -- CROSS-PLATFORM)

These replace the original Android-only guardrails. Do not override unless a
phase explicitly requires it.

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

---

<a id="project-structure"></a>
## PROJECT STRUCTURE

```
AuraFlow/
├── build.gradle.kts                    # Root build file: plugin declarations
├── settings.gradle.kts                 # Module includes, version catalog
├── gradle/
│   └── libs.versions.toml              # Version catalog (ALL versions here)
├── gradle.properties                   # KMP & Compose flags
│
├── shared/                             # KMP shared module (95% of code lives here)
│   ├── build.gradle.kts                # KMP + Compose + Room + KSP config
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/com/auraflow/garden/
│       │       ├── di/                 # Koin modules (shared DI graph)
│       │       ├── data/
│       │       │   ├── local/          # Room DB, DAOs, entities, migrations
│       │       │   │   └── db/         # AuraFlowDatabase.kt, TypeConverters
│       │       │   ├── model/          # Domain models (Node, Link, Level, World, etc.)
│       │       │   ├── remote/         # Ktor API service, DTOs
│       │       │   └── repository/     # Repository interfaces + implementations
│       │       ├── domain/
│       │       │   ├── engine/         # GameEngine, TutorialManager
│       │       │   ├── scoring/        # Score calculation, combo, crescendo
│       │       │   └── difficulty/     # DifficultyManager
│       │       ├── ui/
│       │       │   ├── theme/          # AuraFlow Material 3 theme (dark-first)
│       │       │   ├── screens/
│       │       │   │   ├── home/       # Home screen, world map
│       │       │   │   ├── game/       # Main gameplay screen, GameCanvas
│       │       │   │   ├── zen/        # Zen Mode screen
│       │       │   │   ├── store/      # Warden's Pass & cosmetics store
│       │       │   │   └── settings/   # Settings screen
│       │       │   ├── components/     # Reusable composables (NarratorOverlay, etc.)
│       │       │   └── navigation/     # NavGraph, route definitions
│       │       ├── companion/          # Luma companion (state, reactions, composable)
│       │       ├── effects/            # ParticleEngine, bloom, flora, aura skins
│       │       ├── engagement/         # Streaks, achievements, daily challenge
│       │       ├── sharing/            # ShareManager interface + common logic
│       │       ├── platform/           # expect declarations for ALL platform APIs
│       │       │   ├── HapticEngine.kt         # expect class
│       │       │   ├── AudioEngine.kt          # expect class
│       │       │   ├── BillingManager.kt       # expect class
│       │       │   ├── AdManager.kt            # expect class
│       │       │   ├── ShareManager.kt         # expect class
│       │       │   ├── FileManager.kt          # expect class
│       │       │   ├── AnalyticsTracker.kt     # expect class
│       │       │   ├── CrashReporter.kt        # expect class
│       │       │   ├── ConsentManager.kt       # expect class
│       │       │   ├── SensorManager.kt        # expect class (shake detection)
│       │       │   └── PlatformContext.kt       # expect for platform context
│       │       ├── core/
│       │       │   ├── constants/      # Game constants, node colors, thresholds
│       │       │   └── telemetry/      # Analytics event names (af_ prefix)
│       │       └── util/               # Extensions, date helpers, math utilities
│       │
│       ├── commonTest/
│       │   └── kotlin/com/auraflow/garden/
│       │       ├── domain/             # GameEngine, Scoring, Difficulty tests
│       │       ├── data/               # Repository tests, Level loader tests
│       │       └── engagement/         # Streak, achievement, daily challenge tests
│       │
│       ├── androidMain/
│       │   └── kotlin/com/auraflow/garden/
│       │       ├── di/                 # Android-specific Koin modules
│       │       ├── platform/           # actual implementations
│       │       │   ├── HapticEngine.android.kt     # VibrationEffect
│       │       │   ├── AudioEngine.android.kt      # Media3 + SoundPool
│       │       │   ├── BillingManager.android.kt   # Play Billing v6
│       │       │   ├── AdManager.android.kt        # AdMob Android
│       │       │   ├── ShareManager.android.kt     # Intent.ACTION_SEND
│       │       │   ├── FileManager.android.kt      # AssetManager
│       │       │   ├── AnalyticsTracker.android.kt # Firebase Analytics
│       │       │   ├── CrashReporter.android.kt    # Crashlytics
│       │       │   ├── ConsentManager.android.kt   # Google UMP SDK
│       │       │   ├── SensorManager.android.kt    # Accelerometer
│       │       │   └── PlatformContext.android.kt   # Android Context
│       │       └── data/
│       │           └── local/
│       │               └── db/         # Room database builder (Android)
│       │
│       └── iosMain/
│           └── kotlin/com/auraflow/garden/
│               ├── di/                 # iOS-specific Koin modules
│               ├── platform/           # actual implementations
│               │   ├── HapticEngine.ios.kt         # Core Haptics (CHHapticEngine)
│               │   ├── AudioEngine.ios.kt          # AVFoundation + AVAudioEngine
│               │   ├── BillingManager.ios.kt       # StoreKit 2
│               │   ├── AdManager.ios.kt            # Google Mobile Ads iOS
│               │   ├── ShareManager.ios.kt         # UIActivityViewController
│               │   ├── FileManager.ios.kt          # NSBundle
│               │   ├── AnalyticsTracker.ios.kt     # Firebase Analytics iOS
│               │   ├── CrashReporter.ios.kt        # Crashlytics iOS
│               │   ├── ConsentManager.ios.kt       # ATT + UMP SDK iOS
│               │   ├── SensorManager.ios.kt        # CMMotionManager
│               │   └── PlatformContext.ios.kt       # UIApplication refs
│               └── data/
│                   └── local/
│                       └── db/         # Room database builder (iOS)
│
├── androidApp/                         # Android application shell (thin)
│   ├── build.gradle.kts                # Android app config, signing, ProGuard
│   └── src/main/
│       ├── AndroidManifest.xml         # Permissions, activities, meta-data
│       ├── kotlin/com/auraflow/garden/
│       │   ├── AuraFlowApplication.kt # Application class, Koin init
│       │   └── MainActivity.kt         # Single activity, setContent {}
│       └── res/
│           ├── values/strings.xml       # Android string resources
│           ├── values/colors.xml        # Theme colors
│           ├── xml/                     # Network security config, backup rules
│           └── mipmap/                  # App icons
│
├── iosApp/                             # Xcode project (thin Swift shell)
│   ├── iosApp.xcodeproj/              # Xcode project file
│   ├── iosApp/
│   │   ├── Info.plist                  # Bundle ID, permissions, capabilities
│   │   ├── AuraFlowApp.swift           # SwiftUI App entry point
│   │   ├── ContentView.swift           # Hosts ComposeUIViewController
│   │   ├── iOSAppDelegate.swift        # Firebase init, Koin startup
│   │   ├── Localizable.strings         # iOS string resources
│   │   ├── Assets.xcassets/            # App icons, launch images
│   │   └── Entitlements/
│   │       └── iosApp.entitlements     # In-App Purchase, Push capabilities
│   └── Podfile                         # CocoaPods for Firebase, AdMob
│
├── AuraFlow_ClaudeCode_BuildPrompt.md
├── BUILD_STATE.md
├── MISTAKES.md
├── CLAUDE.md
├── CLAUDE_PROTOCOLS.md
└── KMP_MIGRATION_PLAN.md              # This document
```

---

<a id="package-migration-map"></a>
## PACKAGE MIGRATION MAP

Every package from the original Android structure mapped to its KMP location.

| Original Android Package | KMP Location | Source Set | Notes |
|--------------------------|--------------|------------|-------|
| `com.auraflow.garden.di/` | `shared/.../di/` | commonMain + androidMain + iosMain | Koin modules replace Hilt modules. Common module definitions in commonMain. Platform-specific bindings in androidMain/iosMain. |
| `com.auraflow.garden.data.local/` | `shared/.../data/local/` | commonMain | Room entities, DAOs, TypeConverters, Database class. Room 2.8.3 KMP generates implementations for all targets. Database builder instantiation in androidMain/iosMain. |
| `com.auraflow.garden.data.model/` | `shared/.../data/model/` | commonMain | All domain models (Node, Link, Level, WorldType, GameState, PlayerProgress, StageResult). 100% shared. Replace `Offset` with custom `Position(x, y)` data class (Compose `Offset` is available in commonMain via Compose Multiplatform). |
| `com.auraflow.garden.data.repository/` | `shared/.../data/repository/` | commonMain | Repository interfaces and implementations. 100% shared. |
| `com.auraflow.garden.domain.engine/` | `shared/.../domain/engine/` | commonMain | GameEngine, TutorialManager. 100% shared. Pure Kotlin logic. |
| `com.auraflow.garden.domain.scoring/` | `shared/.../domain/scoring/` | commonMain | Score calculation, resonance chains, crescendo detection. 100% shared. |
| `com.auraflow.garden.domain.difficulty/` | `shared/.../domain/difficulty/` | commonMain | DifficultyManager. 100% shared. |
| `com.auraflow.garden.ui.theme/` | `shared/.../ui/theme/` | commonMain | Material 3 theme. Compose Multiplatform supports Material 3 in commonMain. |
| `com.auraflow.garden.ui.screens.home/` | `shared/.../ui/screens/home/` | commonMain | WorldMap, stage selection. 100% shared Compose UI. |
| `com.auraflow.garden.ui.screens.game/` | `shared/.../ui/screens/game/` | commonMain | GameCanvas, GameViewModel. Canvas API is identical in Compose Multiplatform. |
| `com.auraflow.garden.ui.screens.zen/` | `shared/.../ui/screens/zen/` | commonMain | Zen Mode. 100% shared. |
| `com.auraflow.garden.ui.screens.store/` | `shared/.../ui/screens/store/` | commonMain | Store UI shared. Calls BillingManager (expect/actual) for purchases. |
| `com.auraflow.garden.ui.screens.settings/` | `shared/.../ui/screens/settings/` | commonMain | Settings UI shared. Uses Multiplatform Settings for persistence. |
| `com.auraflow.garden.ui.components/` | `shared/.../ui/components/` | commonMain | NarratorOverlay, StageClearCard, TutorialOverlay, DialogueSystem. All shared. |
| `com.auraflow.garden.ui.navigation/` | `shared/.../ui/navigation/` | commonMain | NavGraph using `org.jetbrains.androidx.navigation:navigation-compose`. Same API. |
| `com.auraflow.garden.companion/` | `shared/.../companion/` | commonMain | Luma state, reactions, composable. 100% shared. |
| `com.auraflow.garden.haptics/` | `shared/.../platform/HapticEngine.kt` | expect in commonMain, actual in androidMain + iosMain | Requires platform APIs. |
| `com.auraflow.garden.audio/` | `shared/.../platform/AudioEngine.kt` | expect in commonMain, actual in androidMain + iosMain | Requires platform APIs. |
| `com.auraflow.garden.effects/` | `shared/.../effects/` | commonMain | ParticleEngine, bloom, flora, aura skins. 100% shared Canvas code. |
| `com.auraflow.garden.engagement/` | `shared/.../engagement/` | commonMain | Streaks, achievements, daily challenge. 100% shared. |
| `com.auraflow.garden.billing/` | `shared/.../platform/BillingManager.kt` | expect in commonMain, actual in androidMain + iosMain | Fundamentally different platform APIs. |
| `com.auraflow.garden.ads/` | `shared/.../platform/AdManager.kt` | expect in commonMain, actual in androidMain + iosMain | Different platform SDKs. |
| `com.auraflow.garden.sharing/` | `shared/.../platform/ShareManager.kt` | expect in commonMain, actual in androidMain + iosMain | Different sharing mechanisms. |
| `com.auraflow.garden.util/` | `shared/.../util/` | commonMain | Extensions, math. Replace `java.time` with `kotlinx-datetime`. Replace `java.util.UUID` with `kotlin.uuid.Uuid` (Kotlin 2.x). |

---

<a id="platform-abstraction-layer"></a>
## PLATFORM ABSTRACTION LAYER (expect/actual)

Every platform-specific API abstracted via Kotlin's expect/actual mechanism.

### 1. PlatformContext

```kotlin
// commonMain
expect class PlatformContext

// androidMain
actual typealias PlatformContext = android.content.Context

// iosMain
actual class PlatformContext  // empty, iOS doesn't need a Context equivalent
```

### 2. HapticEngine

```kotlin
// commonMain: shared/src/commonMain/kotlin/com/auraflow/garden/platform/HapticEngine.kt
expect class HapticEngine(context: PlatformContext) {
    fun play(type: HapticType)
    fun setEnabled(enabled: Boolean)
    fun isSupported(): Boolean
    fun release()
}

enum class HapticType {
    NODE_TAP,           // Single short tick
    LINK_SNAP,          // Satisfying snap (success)
    LINK_FAIL,          // Double buzz (wrong color)
    ENERGY_REFILL,      // Rising pattern (resonance)
    STAGE_CLEAR,        // Celebration sweep
    PERFECT_CLEAR,      // Golden pulse (deep thud)
    CRESCENDO,          // Complex composition
    STAGE_FAIL,         // Soft deflate
    NEAR_MISS,          // Tense double
    SHAKE_RESET,        // Quick sweep
    PULSE_BURST,        // Sharp double tap (intersection)
    UNDO,               // Gentle reverse
    BOSS_ENTRY,         // Deep heartbeat
    PRESSURE_WARNING,   // Escalating pulse
}

// androidMain: VibrationEffect + VibrationEffect.Composition (API 30+)
// - Check Build.VERSION.SDK_INT >= Build.VERSION_CODES.R for Composition API
// - Fallback to createOneShot() / createWaveform() for API 26-29
// - Check Vibrator.hasAmplitudeControl() for fine-grained patterns
actual class HapticEngine actual constructor(context: PlatformContext) {
    // Uses android.os.Vibrator / VibratorManager (API 31+)
    // Wraps VibrationEffect patterns per HapticType
}

// iosMain: Core Haptics (CHHapticEngine)
// - CHHapticPattern with CHHapticEvent for each HapticType
// - UIImpactFeedbackGenerator for simple impacts (fallback on older devices)
// - UINotificationFeedbackGenerator for success/error/warning
// - Check CHHapticEngine.capabilitiesForHardware().supportsHaptics
actual class HapticEngine actual constructor(context: PlatformContext) {
    // Uses CoreHaptics framework via Kotlin/Native interop
    // CHHapticEngine, CHHapticPattern, CHHapticEvent
}
```

### 3. AudioEngine

```kotlin
// commonMain
expect class AudioEngine(context: PlatformContext) {
    // Ambient loops
    fun playAmbient(world: WorldType)
    fun stopAmbient()
    fun crossfadeAmbient(toWorld: WorldType, durationMs: Long)

    // SFX
    fun playSfx(sfx: SfxType)

    // Luma sounds
    fun playLumaSound(sound: LumaSoundType)

    // Generative music
    fun playNote(scaleIndex: Int, world: WorldType)
    fun resolveChord(world: WorldType)

    // Volume control
    fun setMasterVolume(volume: Float)     // 0.0-1.0
    fun setMusicVolume(volume: Float)
    fun setSfxVolume(volume: Float)
    fun setMuted(muted: Boolean)

    // Lifecycle
    fun onAppBackgrounded()
    fun onAppForegrounded()
    fun release()
}

// androidMain: Media3 ExoPlayer (ambient loops) + SoundPool (SFX, <500KB each)
// - Audio focus via AudioManager.requestAudioFocus()
// - SoundPool pre-loaded during splash
// - ExoPlayer for streaming ambient loops
// - All SFX: mono, 44.1kHz, OGG, <500KB

// iosMain: AVFoundation
// - AVAudioEngine for mixing multiple audio streams
// - AVAudioPlayerNode for ambient loops
// - AVAudioPlayerNode for SFX (or SystemSoundID for short clicks)
// - AVAudioSession configuration for background/mixing
// - Audio session interruption handling
```

### 4. BillingManager

```kotlin
// commonMain
data class ProductInfo(
    val id: String,
    val localizedPrice: String,
    val title: String,
    val description: String,
    val type: ProductType,
)

enum class ProductType { ONE_TIME, CONSUMABLE }

sealed class PurchaseResult {
    data class Success(val productId: String) : PurchaseResult()
    data class Pending(val productId: String) : PurchaseResult()
    data class Cancelled(val productId: String) : PurchaseResult()
    data class Error(val productId: String, val message: String) : PurchaseResult()
}

expect class BillingManager(context: PlatformContext) {
    fun initialize()
    suspend fun queryProducts(productIds: List<String>): List<ProductInfo>
    suspend fun purchase(productId: String): PurchaseResult
    suspend fun restorePurchases(): List<String>  // returns owned product IDs
    fun hasPurchase(productId: String): Boolean
    val purchaseEvents: SharedFlow<PurchaseResult>
    fun release()
}

// androidMain: Google Play Billing Library v6
// - BillingClient with BillingClientStateListener
// - queryProductDetailsAsync() for product info
// - launchBillingFlow() for purchase (requires Activity reference)
// - acknowledgePurchase() after successful purchase
// - queryPurchasesAsync() for restore
// - Exponential backoff reconnection (1s, 2s, 4s, max 32s, 5 retries)
// - Pending purchase handling for offline scenarios

// iosMain: StoreKit 2 (Swift interop via Kotlin/Native)
// - Product.products(for:) to query products
// - Product.purchase() for buying
// - Transaction.currentEntitlements for restore
// - Transaction.updates for real-time transaction monitoring
// - AppTransaction.shared for receipt validation
// NOTE: StoreKit 2 APIs accessed via Kotlin/Native ObjC interop or
//       a thin Swift helper exposed as an ObjC-compatible framework
```

### 5. AdManager

```kotlin
// commonMain
sealed class AdState {
    object NotLoaded : AdState()
    object Loading : AdState()
    object Loaded : AdState()
    data class Error(val message: String) : AdState()
}

sealed class AdRewardResult {
    object Rewarded : AdRewardResult()
    object Dismissed : AdRewardResult()
    data class Error(val message: String) : AdRewardResult()
}

expect class AdManager(context: PlatformContext) {
    fun initialize(consentGranted: Boolean)
    fun preloadRewardedAd()
    val adState: StateFlow<AdState>
    suspend fun showRewardedAd(): AdRewardResult
    fun release()
}

// androidMain: Google Mobile Ads SDK (com.google.android.gms:play-services-ads)
// - MobileAds.initialize() in Application.onCreate()
// - RewardedAd.load() with AdRequest
// - RewardedAd.show() requires Activity reference
// - Test ad unit ID in debug, production in release (from BuildConfig)

// iosMain: Google Mobile Ads iOS SDK (via CocoaPods)
// - GADMobileAds.sharedInstance().start()
// - GADRewardedAd.load() with GADRequest
// - GADRewardedAd.present() requires UIViewController
// - Uses Kotlin/Native ObjC interop to call Google Mobile Ads iOS
```

### 6. ShareManager

```kotlin
// commonMain
expect class ShareManager(context: PlatformContext) {
    fun shareImage(imageBytes: ByteArray, text: String, mimeType: String)
    fun shareText(text: String)
}

// androidMain:
// - Intent.ACTION_SEND with EXTRA_STREAM for images
// - FileProvider for content URI
// - chooser Intent for share sheet

// iosMain:
// - UIActivityViewController with UIImage and text
// - Present from root UIViewController
```

### 7. FileManager

```kotlin
// commonMain
expect class FileManager(context: PlatformContext) {
    fun readAsset(path: String): ByteArray
    fun readAssetAsString(path: String): String
    fun assetExists(path: String): Boolean
}

// androidMain: context.assets.open(path)
// iosMain: NSBundle.mainBundle.pathForResource() + NSData
```

### 8. AnalyticsTracker

```kotlin
// commonMain
expect class AnalyticsTracker(context: PlatformContext) {
    fun setCollectionEnabled(enabled: Boolean)
    fun logEvent(name: String, params: Map<String, Any>)
    fun setUserId(userId: String)
    fun setUserProperty(name: String, value: String)
}

// androidMain: Firebase Analytics Android SDK
// - FirebaseAnalytics.getInstance(context)
// - logEvent(), setUserId(), setUserProperty()

// iosMain: Firebase Analytics iOS SDK (via CocoaPods)
// - FIRAnalytics.logEvent() via Kotlin/Native ObjC interop
```

### 9. CrashReporter

```kotlin
// commonMain
expect class CrashReporter(context: PlatformContext) {
    fun initialize()
    fun setCollectionEnabled(enabled: Boolean)
    fun logNonFatal(throwable: Throwable)
    fun log(message: String)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
}

// androidMain: Firebase Crashlytics Android
// - FirebaseCrashlytics.getInstance()

// iosMain: Firebase Crashlytics iOS (via CocoaPods)
// - FIRCrashlytics.crashlytics() via ObjC interop
```

### 10. ConsentManager

```kotlin
// commonMain
enum class ConsentStatus { UNKNOWN, REQUIRED, NOT_REQUIRED, OBTAINED }

expect class ConsentManager(context: PlatformContext) {
    fun requestConsentInfo()
    fun showConsentForm()
    val consentStatus: StateFlow<ConsentStatus>
    fun canShowAds(): Boolean
    fun canCollectAnalytics(): Boolean
}

// androidMain: Google UMP SDK (User Messaging Platform)
// - ConsentInformation.getInstance(context)
// - requestConsentInfoUpdate(), loadAndShowConsentFormIfRequired()
// - GDPR/CCPA compliance

// iosMain: App Tracking Transparency (ATT) + Google UMP SDK iOS
// - ATTrackingManager.requestTrackingAuthorization()
// - UMP SDK via CocoaPods for GDPR consent form
// - Must request ATT before showing personalized ads
```

### 11. SensorManager (Shake Detection)

```kotlin
// commonMain
expect class ShakeDetector(context: PlatformContext) {
    fun startListening(onShake: () -> Unit)
    fun stopListening()
}

// androidMain: android.hardware.SensorManager + TYPE_ACCELEROMETER
// - SensorEventListener, threshold-based shake detection

// iosMain: CMMotionManager (CoreMotion framework)
// - startAccelerometerUpdates(to:withHandler:)
// - Threshold-based shake detection on accelerometer data
// Alternative: UIResponder.motionEnded(.motionShake) but that requires
// the iOS host to forward shake events
```

### 12. NotificationManager (Streak Reminders -- optional)

```kotlin
// commonMain
expect class LocalNotificationManager(context: PlatformContext) {
    fun scheduleStreakReminder(delayHours: Int)
    fun cancelStreakReminder()
    fun requestPermission()
}

// androidMain: NotificationManager + AlarmManager or WorkManager
// iosMain: UNUserNotificationCenter
```

---

<a id="what-stays-shared"></a>
## WHAT STAYS EXACTLY THE SAME (SHARED in commonMain)

These components are 100% Kotlin with no platform dependencies. They go in
`shared/src/commonMain/` and are shared across Android and iOS with zero
duplication.

### Pure Game Logic (Zero Platform Dependencies)
- **GameEngine** -- energy calculation, link validation, intersection detection,
  resonance chain detection, crescendo detection, win/fail state transitions
- **Scoring system** -- base score, efficiency bonus, speed bonus, resonance
  bonus, no-intersection bonus, perfect/crescendo bonuses, star calculation
- **DifficultyManager** -- consecutive failure tracking, energy boost (+12%/+20%),
  Luma Boost offer timing, boss stage special rules, reset conditions
- **TutorialManager** -- stage 1-5 scripted flows, tutorial flag management
- **StreakManager** -- daily streak logic, break detection, midnight edge cases
- **AchievementManager** -- all 14+ achievement conditions, unlock tracking
- **DailyChallengeManager** -- deterministic seed generation
  (`seed = year * 10000 + month * 100 + day`), difficulty tier scaling
- **LevelLoader** -- JSON parsing of level definitions from assets
- **LevelDesigner** -- procedural level generation with solvability verification

### Domain Models (Data Classes)
- Node, NodeColor, Link, Level, WorldType, GameState
- PlayerProgress, StageResult, Blueprint, BlueprintEntity
- LumaState, LumaEmotion, LumaSkin
- ScoringModifiers, StageResult, DailyChallengeResult
- All enums: HapticType, SfxType, LumaSoundType, ParticleShape, etc.

### All Compose UI
- **GameCanvas** -- Canvas-based node rendering, link drawing, touch handling.
  Compose Canvas API is identical across platforms via Compose Multiplatform.
- **Node rendering** -- circle with glow, pulse animation, pressure node fade,
  moving node trail
- **Link rendering** -- bezier curves, flora growth, aura skin trails
- **Luma companion** -- Canvas-drawn orb, spring physics, floating text, glow
- **ParticleEngine** -- all particle effects (bloom, crescendo, flora, dissolve)
- **NarratorOverlay** -- typewriter animation, auto-fade, tap-to-dismiss
- **StageClearCard** -- score breakdown, star rating, next/retry buttons
- **WorldMap** -- vertical scrollable, world sections, stage nodes, lock states
- **Store screen** -- product cards, preview animations
- **Settings screen** -- toggles, sliders, all using Multiplatform Settings
- **Tutorial overlays** -- tooltips, highlight rings, guide lines
- **Theme** -- Material 3 colors, typography, dark/light mode

### Navigation
- NavGraph with routes: Home, Game, Zen, Store, Settings
- Route arguments (stageNumber, worldType)
- Back handling, deep links

### Networking
- Ktor-based ApiService for daily challenges and leaderboard
- Request/response DTOs with kotlinx-serialization
- Retry/backoff logic, offline fallback

### Room Database
- AuraFlowDatabase with all entities and DAOs
- TypeConverters for non-primitives
- Migration objects (explicit, never destructive)
- Schema export

---

<a id="what-cannot-be-shared"></a>
## WHAT CANNOT BE SHARED (Platform-Specific)

Every component that requires platform APIs, with the Android implementation and
its iOS equivalent.

| Component | Android Implementation | iOS Implementation | Abstraction |
|-----------|----------------------|-------------------|-------------|
| **Haptic feedback** | `android.os.VibrationEffect` + `VibrationEffect.Composition` (API 30+). Fallback: `createOneShot()` / `createWaveform()` (API 26+). Uses `Vibrator` / `VibratorManager`. | `CHHapticEngine` (Core Haptics, iOS 13+). `CHHapticPattern` with `CHHapticEvent` for each pattern. Fallback: `UIImpactFeedbackGenerator`, `UINotificationFeedbackGenerator`. | `expect class HapticEngine` |
| **Audio playback** | Media3 ExoPlayer for ambient loops. SoundPool for SFX (<500KB, OGG). `AudioManager` for audio focus. | `AVAudioEngine` with `AVAudioPlayerNode` for loops and SFX. `AVAudioSession` for audio session management. Audio route change handling. | `expect class AudioEngine` |
| **In-app billing** | Google Play Billing Library v6 (`BillingClient`, `launchBillingFlow`, `acknowledgePurchase`). Requires `Activity` reference. | StoreKit 2 (`Product.purchase()`, `Transaction.currentEntitlements`). Swift async/await via Kotlin/Native interop. | `expect class BillingManager` |
| **Rewarded ads** | Google Mobile Ads SDK (`RewardedAd.load()`, `RewardedAd.show()`). Requires `Activity`. Google UMP SDK for consent. | Google Mobile Ads iOS SDK (`GADRewardedAd`, `GADRequest`). Requires `UIViewController`. ATT (App Tracking Transparency) for consent. | `expect class AdManager` |
| **Share sheet** | `Intent.ACTION_SEND` with `FileProvider` for content URIs. `startActivity(Intent.createChooser(...))`. | `UIActivityViewController` with `UIImage` and `String` items. Present from root `UIViewController`. | `expect class ShareManager` |
| **Asset loading** | `AssetManager.open(path)` from `Context`. Assets in `src/androidMain/assets/`. | `NSBundle.mainBundle.pathForResource(name, ofType)`. Assets in Xcode project bundle. Alternatively, use Compose Multiplatform resources. | `expect class FileManager` |
| **Analytics** | Firebase Analytics Android SDK. `FirebaseAnalytics.getInstance(context).logEvent()`. Auto-init disabled via manifest meta-data. | Firebase Analytics iOS SDK (CocoaPods). `FIRAnalytics.logEvent(withName:parameters:)`. Configured in `GoogleService-Info.plist`. | `expect class AnalyticsTracker` |
| **Crash reporting** | Firebase Crashlytics Android. `mappingFileUploadEnabled = true` for deobfuscated reports. | Firebase Crashlytics iOS (CocoaPods). dSYM upload for symbolicated reports. | `expect class CrashReporter` |
| **Privacy consent** | Google UMP SDK. `ConsentInformation.requestConsentInfoUpdate()`. GDPR/CCPA forms. | ATT (`ATTrackingManager.requestTrackingAuthorization()`) + Google UMP iOS. Must show ATT prompt before personalized ads. | `expect class ConsentManager` |
| **Shake detection** | `SensorManager.getDefaultSensor(TYPE_ACCELEROMETER)` + `SensorEventListener`. Threshold: >12 m/s^2. | `CMMotionManager.startAccelerometerUpdates()`. Same threshold logic. Or `UIResponder.motionEnded(.motionShake)`. | `expect class ShakeDetector` |
| **Video encoding** (Phase 17) | `MediaCodec` + `MediaMuxer` for H.264 MP4. `PixelCopy` for Canvas frame capture. | `AVAssetWriter` + `AVAssetWriterInput` for H.264 MP4. `UIGraphicsImageRenderer` for frame capture. | `expect class VideoEncoder` (Phase 17 only) |
| **Push notifications** (future) | FCM (`FirebaseMessaging`). | APNs (`UNUserNotificationCenter`). | `expect class PushManager` |
| **App lifecycle** | `ProcessLifecycleOwner`, `LifecycleObserver`. | `UIApplicationDelegate` callbacks, `scenePhase` in SwiftUI. | Handled in app shells |
| **Database builder** | `Room.databaseBuilder(context, ...)` | `Room.databaseBuilder(name, factory)` with `setDriver(BundledSQLiteDriver())` | Platform-specific Room builder in androidMain/iosMain |
| **Deep links** | `Intent` filters in `AndroidManifest.xml`. | Universal Links in `apple-app-site-association` + Associated Domains entitlement. | App shell configuration |
| **App review prompt** | Google Play In-App Review (`ReviewManager`). | `SKStoreReviewController.requestReview()`. | `expect class ReviewManager` |

---

<a id="ios-specific-requirements"></a>
## iOS-SPECIFIC REQUIREMENTS

### 1. Minimum iOS Version Target

**iOS 16.0** (minimum)

Rationale:
- Core Haptics requires iOS 13.0+ (well covered)
- StoreKit 2 requires iOS 15.0+
- ATT (App Tracking Transparency) requires iOS 14.5+
- Compose Multiplatform requires iOS 15.0+ (Skiko/Metal rendering)
- iOS 16 as minimum covers 95%+ of active iOS devices as of early 2026
- iOS 16 provides modern SwiftUI lifecycle, improved StoreKit 2 APIs, and
  better Metal performance

### 2. Xcode Project Setup

```
iosApp/
├── iosApp.xcodeproj/
│   └── project.pbxproj
├── iosApp/
│   ├── AuraFlowApp.swift          # @main SwiftUI App struct
│   ├── ContentView.swift           # Hosts the Compose UI
│   ├── iOSAppDelegate.swift        # Firebase, Koin, lifecycle
│   ├── Info.plist                  # Permissions and configuration
│   ├── GoogleService-Info.plist    # Firebase config (NOT committed to git)
│   ├── Entitlements/
│   │   └── iosApp.entitlements     # In-App Purchase, Associated Domains
│   ├── Assets.xcassets/
│   │   ├── AppIcon.appiconset/     # All required icon sizes
│   │   ├── AccentColor.colorset/
│   │   └── LaunchImage.imageset/
│   └── Localizable.strings         # iOS-specific strings
├── Podfile                         # CocoaPods dependencies
└── Podfile.lock
```

**AuraFlowApp.swift** (entry point):
```swift
import SwiftUI
import shared  // The KMP shared framework

@main
struct AuraFlowApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
                .preferredColorScheme(.dark)  // Dark-first theme
        }
    }
}
```

**ContentView.swift** (hosts Compose):
```swift
import SwiftUI
import shared

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

**shared module** (Compose entry point for iOS):
```kotlin
// shared/src/iosMain/kotlin/com/auraflow/garden/MainViewController.kt
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    // Initialize Koin, then render the app
    AuraFlowApp()
}
```

### 3. CocoaPods vs SPM for iOS Dependencies

**Decision: CocoaPods** for the initial release.

Rationale:
- The `kotlin-cocoapods` Gradle plugin provides seamless integration between KMP
  and CocoaPods, automatically exposing the shared framework as a pod.
- Firebase iOS SDK and Google Mobile Ads iOS SDK both have mature CocoaPods support.
- SPM support for KMP frameworks is still maturing.
- CocoaPods allows declaring all iOS dependencies in a single Podfile alongside
  the shared KMP framework.

**Podfile:**
```ruby
platform :ios, '16.0'
use_frameworks!

target 'iosApp' do
  # KMP shared framework (auto-generated by kotlin-cocoapods plugin)
  pod 'shared', :path => '../shared'

  # Firebase
  pod 'FirebaseAnalytics', '~> 11.8'
  pod 'FirebaseCrashlytics', '~> 11.8'

  # Google Mobile Ads
  pod 'Google-Mobile-Ads-SDK', '~> 12.1'

  # Google UMP (User Messaging Platform) for consent
  pod 'GoogleUserMessagingPlatform', '~> 3.0'
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '16.0'
    end
  end
end
```

### 4. StoreKit 2 Product Setup

Mirror every Google Play product in App Store Connect:

| Play Store Product ID | App Store Product ID | Type | Price (USD) | Price (INR) |
|---|---|---|---|---|
| `wardens_pass` | `com.auraflow.garden.wardens_pass` | Non-Consumable | $3.99 | Rs 349 |
| `aura_cherry_blossom` | `com.auraflow.garden.aura.cherry_blossom` | Non-Consumable | $0.99 | Rs 79 |
| `aura_northern_lights` | `com.auraflow.garden.aura.northern_lights` | Non-Consumable | $1.49 | Rs 119 |
| `aura_ember_sparks` | `com.auraflow.garden.aura.ember_sparks` | Non-Consumable | $1.49 | Rs 119 |
| `aura_ocean_waves` | `com.auraflow.garden.aura.ocean_waves` | Non-Consumable | $0.99 | Rs 79 |
| `aura_diwali_diyas` | `com.auraflow.garden.aura.diwali_diyas` | Non-Consumable | $1.49 | Rs 119 |
| `aura_bundle_3` | `com.auraflow.garden.aura.bundle_3` | Non-Consumable | $2.99 | Rs 249 |
| `luma_water_droplet` | `com.auraflow.garden.luma.water_droplet` | Non-Consumable | $0.99 | Rs 79 |
| `luma_star_fragment` | `com.auraflow.garden.luma.star_fragment` | Non-Consumable | $0.99 | Rs 79 |
| `luma_lotus_petal` | `com.auraflow.garden.luma.lotus_petal` | Non-Consumable | $0.99 | Rs 79 |
| `luma_paper_lantern` | `com.auraflow.garden.luma.paper_lantern` | Non-Consumable | $0.99 | Rs 79 |
| `season_monsoon` | `com.auraflow.garden.season.monsoon` | Non-Consumable | $1.99 | Rs 159 |
| `season_diwali` | `com.auraflow.garden.season.diwali` | Non-Consumable | $1.99 | Rs 159 |
| `gift_garden` | `com.auraflow.garden.gift` | Consumable | $3.99 | Rs 349 |

**Product ID mapping in commonMain:**
```kotlin
// shared/src/commonMain/.../billing/ProductIds.kt
object ProductIds {
    const val WARDENS_PASS = "wardens_pass"
    // ... etc
}

// Each platform's BillingManager maps these to platform-specific IDs
// Android: uses the product ID directly (matches Play Console)
// iOS: prepends bundle ID prefix (matches App Store Connect)
```

**StoreKit 2 Implementation Notes:**
- StoreKit 2 is Swift-first with async/await. Access from Kotlin/Native via
  ObjC interop or a thin Swift helper class.
- Create a Swift file `StoreKitHelper.swift` that wraps StoreKit 2 calls and
  exposes them as ObjC-compatible methods Kotlin can call.
- Transaction verification is automatic with StoreKit 2 (JWS signatures).
- No server-side receipt validation needed for StoreKit 2 (unlike StoreKit 1).

### 5. App Store Submission Requirements vs Play Store

| Requirement | Play Store | App Store |
|-------------|------------|-----------|
| **Binary format** | AAB (Android App Bundle) | IPA (via Xcode Archive) |
| **Signing** | Upload key + App Signing by Google | Distribution certificate + provisioning profile |
| **Review time** | Typically 1-3 days | Typically 1-7 days (longer for first submission) |
| **Screenshots** | 2-8 per device type | 3-10 per device size (6.7", 6.5", 5.5" required) |
| **Privacy** | Privacy policy URL, Data safety form | Privacy policy URL, App Privacy labels (nutrition labels) |
| **Content rating** | IARC questionnaire | App Store age rating questionnaire |
| **IAP** | Configure in Play Console, 15-30% commission | Configure in App Store Connect, 15-30% commission |
| **Ads disclosure** | Declare in content rating | Declare in submission, ATT required for tracking |
| **Review guidelines** | Play Store policies | App Store Review Guidelines (stricter, especially 4.2 Minimum Functionality) |
| **TestFlight** | N/A (use Internal/Closed/Open testing tracks) | Required for beta distribution to external testers |
| **App Thinning** | Handled by AAB format | Handled by Xcode (bitcode no longer required in iOS 16+) |

**iOS-specific submission items:**
- `NSUserTrackingUsageDescription` in Info.plist (ATT prompt explanation)
- Export compliance (HTTPS = yes, add `ITSAppUsesNonExemptEncryption = NO` if no custom encryption)
- In-App Purchase review information (test credentials for review team)
- App Privacy nutrition labels covering: analytics, advertising, identifiers
- Screenshots for iPhone 6.7" (14 Pro Max), 6.5" (11 Pro Max), and iPad if supporting

### 6. iOS Accessibility APIs

| Android API | iOS Equivalent | Implementation |
|-------------|---------------|----------------|
| `contentDescription` | `accessibilityLabel` | Compose Multiplatform `Modifier.semantics { contentDescription = ... }` works on both platforms |
| `importantForAccessibility` | `isAccessibilityElement` | Handled by Compose semantics tree |
| Font scale (sp units) | Dynamic Type | Compose Multiplatform respects system font scaling on iOS automatically |
| `Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED` | `UIAccessibility.isDarkerSystemColorsEnabled` | Check via expect/actual, plus in-app color-blind mode toggle |
| `Settings.Global.ANIMATOR_DURATION_SCALE` (reduced motion) | `UIAccessibility.isReduceMotionEnabled` | expect/actual accessor. Gate particles, animations, sky rotation. |
| TalkBack | VoiceOver | Compose semantics tree maps to iOS accessibility tree automatically. VoiceOver reads `contentDescription` values. |
| Switch Access | Switch Control | Supported via standard accessibility tree |

**Critical:** Compose Multiplatform's accessibility support on iOS maps Compose
semantics to the native iOS accessibility tree. `Modifier.semantics {}` blocks,
`contentDescription`, and traversal order work on both platforms. No separate
implementation needed for basic accessibility. For advanced customization (custom
actions, rotor items), use expect/actual wrappers around `UIAccessibility` APIs.

### 7. iOS Performance Considerations

**Rendering:**
- Compose Multiplatform renders on iOS via Skiko, which uses **Metal** (Apple's
  GPU API) for hardware-accelerated rendering.
- Metal provides native GPU performance. No OpenGL fallback needed (OpenGL ES
  is deprecated on iOS since iOS 12).
- ProMotion displays (120Hz on iPhone 13 Pro+) are supported.
- Canvas draw operations are GPU-accelerated identical to Android's
  hardware-accelerated Canvas.

**Memory:**
- iOS enforces stricter memory limits than Android. The 220MB heap target from
  the original spec should be reduced to **180MB** for iOS.
- Use `autoreleasepool {}` in tight loops within iosMain code to prevent
  ObjC object accumulation.
- Kotlin/Native uses a tracing GC (since Kotlin 1.7.20) that is concurrent and
  does not freeze objects. No special threading restrictions.

**Startup:**
- Cold start target: **<2.5s on iOS** (vs <2.0s on Android). iOS app launch
  involves more system overhead (dyld, runtime initialization).
- Pre-warm Compose framework in `AppDelegate.application(_:didFinishLaunchingWithOptions:)`.
- Avoid heavy computation in `init` blocks of Kotlin/Native singletons.

**Battery:**
- Same 5%/15min target applies. Metal rendering is power-efficient.
- Reduce animation frame rate to 30fps when on Low Power Mode
  (`ProcessInfo.processInfo.isLowPowerModeEnabled`).

---

<a id="build-system-changes"></a>
## BUILD SYSTEM CHANGES

### 1. Gradle Multiplatform Plugin Configuration

**settings.gradle.kts:**
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AuraFlow"
include(":shared")
include(":androidApp")
```

**Root build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
```

**gradle/libs.versions.toml:**
```toml
[versions]
kotlin = "2.1.20"
agp = "8.7.3"
compose-multiplatform = "1.10.3"
ksp = "2.1.20-1.0.32"
koin = "4.1.1"
room = "2.8.3"
sqlite = "2.5.1"
ktor = "3.4.2"
coroutines = "1.10.2"
serialization = "1.9.0"
datetime = "0.7.1"
collections-immutable = "0.3.8"
multiplatform-settings = "1.3.0"
navigation-compose = "2.9.2"
lifecycle = "2.10.0"
coil = "3.4.0"
firebase-gitlive = "2.4.0"
play-billing = "6.2.1"
admob = "23.0.0"
media3 = "1.3.1"
leakcanary = "2.14"

[libraries]
# Kotlin
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }
kotlinx-collections-immutable = { module = "org.jetbrains.kotlinx:kotlinx-collections-immutable", version.ref = "collections-immutable" }

# Navigation
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }

# Lifecycle
lifecycle-viewmodel-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }

# Koin
koin-bom = { module = "io.insert-koin:koin-bom", version.ref = "koin" }
koin-core = { module = "io.insert-koin:koin-core" }
koin-android = { module = "io.insert-koin:koin-android" }
koin-compose = { module = "io.insert-koin:koin-compose" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# Multiplatform Settings
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatform-settings" }
multiplatform-settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "multiplatform-settings" }

# Coil
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }

# Firebase (GitLive KMP)
firebase-analytics = { module = "dev.gitlive:firebase-analytics", version.ref = "firebase-gitlive" }
firebase-crashlytics = { module = "dev.gitlive:firebase-crashlytics", version.ref = "firebase-gitlive" }

# Android-only
play-billing = { module = "com.android.billingclient:billing-ktx", version.ref = "play-billing" }
admob = { module = "com.google.android.gms:play-services-ads", version.ref = "admob" }
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
leakcanary = { module = "com.squareup.leakcanary:leakcanary-android", version.ref = "leakcanary" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

**shared/build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Navigation
            implementation(libs.navigation.compose)

            // Lifecycle / ViewModel
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            // Koin
            implementation(platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Room
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Utilities
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            // Image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }

        androidMain.dependencies {
            // Ktor Android engine
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

            // Koin Android
            implementation(libs.koin.android)

            // Android platform dependencies
            implementation(libs.play.billing)
            implementation(libs.admob)
            implementation(libs.media3.exoplayer)
        }

        iosMain.dependencies {
            // Ktor iOS engine
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.auraflow.garden.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Room schema export
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP for Room (all targets)
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}
```

**androidApp/build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    id("com.google.gms.google-services")        // Firebase
    id("com.google.firebase.crashlytics")         // Crashlytics
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}

android {
    namespace = "com.auraflow.garden"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.auraflow.garden"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            buildConfigField("String", "AD_UNIT_REWARDED",
                "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "API_BASE_URL",
                "\"http://10.0.2.2:8080\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "AD_UNIT_REWARDED",
                "\"ca-app-pub-XXXX/YYYY\"")
            buildConfigField("String", "API_BASE_URL",
                "\"https://api.auraflow.app\"")
        }
    }

    signingConfigs {
        create("release") {
            // Read from local.properties (not committed to git)
        }
    }
}

dependencies {
    implementation(project(":shared"))
    debugImplementation(libs.leakcanary)
}
```

**gradle.properties:**
```properties
# Kotlin Multiplatform
kotlin.mpp.enableCInteropCommonization=true
kotlin.mpp.applyDefaultHierarchyTemplate=true

# Compose
org.jetbrains.compose.experimental.uikit.enabled=true

# Android
android.useAndroidX=true

# Gradle
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# KSP
ksp.incremental=true
```

### 2. iOS Build Integration

**Xcode Build Phases:**

The Kotlin/Native compiler produces a `.framework` that Xcode links against.
There are two approaches:

**Option A: CocoaPods Integration (Recommended)**
- Add `cocoapods` block to `shared/build.gradle.kts`
- Run `./gradlew :shared:podInstall` to generate pod spec
- `pod install` in `iosApp/` directory
- Xcode builds the framework automatically via CocoaPods integration

```kotlin
// In shared/build.gradle.kts
kotlin {
    cocoapods {
        summary = "AuraFlow shared KMP module"
        homepage = "https://github.com/auraflow"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }
}
```

**Option B: Direct Framework Embedding**
- Build framework via `./gradlew :shared:assembleSharedXCFramework`
- Add a Run Script build phase in Xcode that invokes Gradle
- Embed the `.xcframework` in the Xcode project

### 3. CI/CD for Both Platforms

```yaml
# .github/workflows/build.yml
name: Build & Test

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # Android build (runs on Ubuntu)
  android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :shared:testDebugUnitTest
      - run: ./gradlew :androidApp:assembleDebug
      - run: ./gradlew :androidApp:assembleRelease
      - run: ./gradlew :androidApp:lintDebug

  # iOS build (runs on macOS)
  ios:
    runs-on: macos-15
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :shared:iosSimulatorArm64Test
      - run: |
          cd iosApp
          pod install
          xcodebuild build \
            -workspace iosApp.xcworkspace \
            -scheme iosApp \
            -configuration Debug \
            -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' \
            CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```

### 4. Signing for Both Platforms

**Android:**
- Same as original spec. Release keystore in `local.properties` (not committed).
- `signingConfigs` block in `androidApp/build.gradle.kts`.
- Play App Signing recommended (upload key locally, signing key managed by Google).

**iOS:**
- Apple Developer Program membership required ($99/year).
- Create Distribution Certificate in Apple Developer portal.
- Create App ID: `com.auraflow.garden` with capabilities: In-App Purchase,
  Associated Domains (for deep links), Push Notifications (future).
- Create Provisioning Profiles: Development (for testing) and Distribution
  (for App Store).
- In CI: use `fastlane match` or manual certificate/profile management via
  GitHub Secrets.
- Xcode Automatic Signing can be used for local development.

---

<a id="risk-assessment"></a>
## RISK ASSESSMENT

### 1. Compose Multiplatform Maturity on iOS

**Status: STABLE** (since version 1.8.0, May 2025)

- Compose Multiplatform for iOS was declared stable by JetBrains in May 2025.
- As of version 1.10.3 (current), it has had over a year of stable releases.
- Companies including Netflix, Cash App, and others run Compose Multiplatform
  in production on iOS.
- The Skiko rendering layer uses Metal for hardware-accelerated graphics.
- 120Hz ProMotion is supported on compatible devices.

**Risk Level: LOW.** The technology is production-proven for standard UI apps.

**Mitigation for game-specific concerns:**
- Benchmark Canvas performance on iOS early (Phase 3). If frame time exceeds
  16ms with 200 particles on an iPhone 12 (Metal), reduce particle budget for
  iOS or implement platform-specific particle count limits.
- Test on real devices, not just simulators. Simulator performance does not
  reflect Metal GPU performance.

### 2. Canvas Performance on iOS via Skiko

**Status: GOOD with caveats**

- Skiko on iOS renders via Metal, which is Apple's native GPU API. Performance
  is comparable to native Core Graphics / Core Animation for 2D rendering.
- Standard Canvas operations (drawCircle, drawPath, drawLine, drawArc) are
  well-optimized in Skiko's Metal backend.
- The "jank" associated with early Skiko releases has been resolved.

**Known performance characteristics:**
- Canvas object allocation rules from the original spec still apply: never
  allocate Paint/Path inside drawScope. This is even more important on iOS
  because Kotlin/Native's GC is concurrent but has different collection
  timing than Android's ART GC.
- Shader effects (blur, color matrix) may have different performance profiles
  on Metal vs Android's Skia/Vulkan/OpenGL.
- Text rendering in Canvas uses Skia's text engine, which may have subtle
  differences in font metrics vs iOS native text.

**Risk Level: LOW-MEDIUM.** Standard 2D Canvas rendering is well-supported.
Heavy particle effects need early benchmarking.

**Mitigation:**
- Phase 3 deliverable: Canvas performance benchmark on both Android Pixel 5
  emulator AND iOS iPhone 12 simulator / real device. If iOS misses 16ms p95
  target, reduce particle ceiling or simplify effects for iOS.
- Pre-allocate all Canvas objects in `remember {}` (same rule as Android).
- Use `withFrameNanos {}` for animation loops (works on both platforms).

### 3. Known Limitations of KMP for Game-Like Apps

| Limitation | Severity | Mitigation |
|------------|----------|------------|
| No direct access to Metal/Vulkan shaders from commonMain | LOW | AuraFlow uses Compose Canvas 2D operations, not custom shaders. If custom GPU shaders are needed later, use expect/actual with platform shader APIs. |
| Kotlin/Native has slightly higher memory overhead than JVM | LOW | Monitor heap on iOS. Reduce image cache size if needed. Use `autoreleasepool` in tight loops. |
| Audio latency on iOS may differ from Android | MEDIUM | SoundPool on Android has ~10ms latency. AVAudioEngine on iOS can achieve <5ms. Test haptic+audio sync on both platforms. |
| Kotlin/Native compile times are longer than JVM | MEDIUM | Use incremental compilation. Keep iosMain source sets thin (only expect/actual implementations). Consider enabling Kotlin/Native caching in CI. |
| No direct C/C++ interop from commonMain | LOW | Not needed for AuraFlow. Platform-specific native code goes in androidMain (JNI) or iosMain (cinterop). |
| `System.currentTimeMillis()` not available in commonMain | LOW | Use `kotlinx.datetime.Clock.System.now().toEpochMilliseconds()` instead. |
| `String.hashCode()` may differ across platforms | LOW (ALREADY HANDLED) | The build prompt already mandates deterministic seed: `year * 10000 + month * 100 + day`. No change needed. |
| Thread confinement differs on Kotlin/Native | LOW | Kotlin/Native's new memory model (default since 1.7.20) allows sharing mutable state across threads. No `@SharedImmutable` needed. Standard coroutine patterns work. |

**Risk Level: LOW.** AuraFlow's architecture (Canvas 2D rendering, coroutine-based
async, pure Kotlin game logic) is well-suited to KMP. The game does not require
low-level GPU access, custom native rendering, or C++ game engine integration.

### 4. Room Migration Path (from Room 2.6.1 Schema Design to Room 2.8.3 KMP)

Since the project has NOT started building yet (Phase 1 is NOT_STARTED), there
is no existing Room schema to migrate. This is the ideal scenario: design the
database schema for KMP from day one.

**If the project had existing Room data (hypothetical migration path):**

| Step | Action |
|------|--------|
| 1 | Upgrade Room from 2.6.1 to 2.8.3 in `build.gradle.kts` |
| 2 | Replace `kapt` with `ksp` for Room compiler (KMP requires KSP) |
| 3 | Move Room entities, DAOs, TypeConverters, Database class from `app/src/main/` to `shared/src/commonMain/` |
| 4 | Remove `android.arch.persistence.room.RoomDatabase` imports, replace with `androidx.room.RoomDatabase` (should already be the case with Room 2.6.1) |
| 5 | Replace `Room.databaseBuilder(context, ...)` calls with platform-specific builders in androidMain and iosMain |
| 6 | For iosMain: `Room.databaseBuilder<AuraFlowDatabase>(name = dbFilePath).setDriver(BundledSQLiteDriver()).build()` |
| 7 | Replace any `@Dao` methods using `LiveData` return types with `Flow` (KMP does not support LiveData) |
| 8 | Replace any `Cursor`-based queries with standard Room query methods |
| 9 | Verify schema export still works: `room { schemaDirectory("$projectDir/schemas") }` |
| 10 | Run Room schema validation tests on both Android and iOS |

**For AuraFlow specifically:** Since we are starting fresh, design all DAOs to
return `Flow<T>` (never `LiveData`), use `suspend` functions for writes, and
use `kotlinx-datetime` types with TypeConverters instead of `java.time`.

### 5. Overall Risk Summary

| Risk Category | Level | Impact | Probability | Mitigation Cost |
|---|---|---|---|---|
| Compose Multiplatform iOS stability | LOW | HIGH | LOW | Minimal -- it is stable |
| Canvas performance on iOS | LOW-MEDIUM | HIGH | LOW | Early benchmarking in Phase 3 |
| KMP game suitability | LOW | HIGH | LOW | Architecture is well-suited |
| Room KMP compatibility | LOW | MEDIUM | LOW | Starting fresh, no migration |
| Build complexity increase | MEDIUM | MEDIUM | HIGH | Expected cost of cross-platform |
| iOS platform API knowledge | MEDIUM | MEDIUM | MEDIUM | Team needs iOS/Swift basics |
| CI/CD complexity | MEDIUM | LOW | HIGH | Standard, well-documented setup |
| App Store review process | LOW | LOW | MEDIUM | Follow guidelines, allow extra time |

---

<a id="migration-phase-adjustments"></a>
## MIGRATION PHASE ADJUSTMENTS

Changes to each original build phase to accommodate KMP.

### Phase 1: Project Scaffolding (SIGNIFICANT CHANGES)

**Original:** Single Android module, AGP 8.5.0, Kotlin 1.9.24, Hilt, Compose Navigation.

**KMP version:**
- Create KMP multi-module project: `shared/`, `androidApp/`, `iosApp/`
- Kotlin 2.1.20 + Compose Multiplatform 1.10.3 + AGP 8.7.3
- Koin 4.1.1 replaces Hilt 2.51
- `org.jetbrains.androidx.navigation:navigation-compose:2.9.2` replaces Android-only navigation
- Set up Xcode project in `iosApp/` with Swift entry point
- Set up CocoaPods for iOS dependencies
- All `expect` class declarations created (empty `actual` stubs in iosMain)
- Verify build: `./gradlew assembleDebug` (Android) AND `xcodebuild` (iOS simulator)

**New "Done when" additions:**
- [ ] `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds
- [ ] `xcodebuild build` for iOS simulator succeeds
- [ ] NavGraph navigates between 5 placeholder screens on BOTH platforms
- [ ] Koin DI injects correctly on both platforms

**Estimated time change:** 2-3 hours --> 5-7 hours (KMP setup, Xcode project,
CocoaPods, verifying both platforms build)

### Phase 2: Core Data Models (MODERATE CHANGES)

- Room 2.8.3 with KSP for all targets instead of Room 2.6.1 with kapt
- Replace `java.time` references with `kotlinx-datetime`
- Replace `java.util.UUID` with `kotlin.uuid.Uuid`
- Room database builder in androidMain and iosMain (separate actual implementations)
- All entities, DAOs, TypeConverters in commonMain
- Use `Flow` return types exclusively (no LiveData)

**Estimated time change:** 3-4 hours --> 4-5 hours

### Phase 3: Game Rendering (MINIMAL CHANGES)

- Canvas API is identical in Compose Multiplatform
- `Modifier.pointerInput` works on both platforms
- Performance benchmark must include iOS device/simulator
- Shake-to-reset uses `expect class ShakeDetector` instead of direct
  SensorManager access

**Estimated time change:** 8-12 hours --> 9-13 hours (add iOS benchmarking)

### Phase 4-5: Game Logic + Luma (NO CHANGES)

Pure Kotlin logic. 100% in commonMain. Zero platform dependencies.

### Phase 6: Haptic Engine (SIGNIFICANT CHANGES)

- `expect class HapticEngine` in commonMain
- `actual class` in androidMain: same VibrationEffect code as original
- `actual class` in iosMain: new Core Haptics implementation
- Must map all 14 HapticType patterns to CHHapticPattern equivalents
- iOS haptic patterns may sound/feel different -- tuning required

**Estimated time change:** 2-3 hours --> 4-6 hours (iOS Core Haptics is new code)

### Phase 7-9: Narrator, World Map, Visual Effects (MINIMAL CHANGES)

All Compose UI. Fully in commonMain. Canvas particle system shared.

### Phase 10: Audio (SIGNIFICANT CHANGES)

- `expect class AudioEngine` in commonMain
- `actual class` in androidMain: same Media3 + SoundPool code
- `actual class` in iosMain: new AVFoundation + AVAudioEngine implementation
- Audio session management differs significantly between platforms
- Audio format: OGG for Android, CAF/M4A for iOS (or use MP3/AAC for both)

**Estimated time change:** 4-6 hours --> 8-10 hours (iOS AVFoundation is new code)

### Phase 11-13: Tutorial, Difficulty, Engagement (NO CHANGES)

Pure Kotlin logic + shared Compose UI. 100% commonMain.

### Phase 14: Monetization (SIGNIFICANT CHANGES)

- `expect class BillingManager` in commonMain
- `actual class` in androidMain: same Play Billing v6 code
- `actual class` in iosMain: new StoreKit 2 implementation
- May need a Swift helper for StoreKit 2 async/await interop
- Product IDs mapped between stores
- App Store Connect product configuration required

**Estimated time change:** 4-6 hours --> 8-12 hours (StoreKit 2 is new, App Store setup)

### Phase 15: AdMob (MODERATE CHANGES)

- `expect class AdManager` in commonMain
- `actual class` in androidMain: same AdMob code
- `actual class` in iosMain: Google Mobile Ads iOS SDK via CocoaPods
- `expect class ConsentManager` with ATT implementation for iOS
- ATT prompt must appear BEFORE ad loading on iOS

**Estimated time change:** 2-3 hours --> 4-5 hours (iOS AdMob + ATT)

### Phase 16-17: Zen Mode, Share (MINIMAL to MODERATE CHANGES)

- Zen Mode: 100% commonMain (Canvas-based)
- Share: `expect class ShareManager` with `UIActivityViewController` on iOS

### Phase 18: Backend (NO CHANGES)

Spring Boot backend is independent. Ktor client in commonMain replaces Retrofit.

**Estimated time change:** 6-8 hours --> 6-8 hours (Ktor is simpler than Retrofit)

### Phase 19: Polish (MODERATE CHANGES)

- Shared element transitions: verify behavior on iOS
- Test iOS-specific edge cases: app backgrounding, memory warnings, orientation
- iOS Low Power Mode handling
- iOS back gesture (swipe from left edge) handling

### Phase 20: Release (SIGNIFICANT CHANGES)

- Android: same Play Store prep as original
- **New:** App Store submission preparation
  - Xcode Archive + upload to App Store Connect
  - TestFlight beta distribution
  - App Store listing, screenshots for iPhone sizes
  - App Privacy labels
  - Review information for IAP testing
  - App Store review (1-7 day wait)

**Estimated time change:** 4-6 hours --> 8-12 hours (dual-platform release prep)

### Total Estimated Time

| Scope | Original Estimate | KMP Estimate |
|---|---|---|
| Full project (all 20 phases) | 90-120 hours | 120-160 hours |
| MVP (Phases 1-9, 11-14, 19-20) | 65-85 hours | 90-115 hours |

The additional 30-40 hours account for:
- KMP project scaffolding and Xcode setup (~3 hours)
- iOS expect/actual implementations for platform APIs (~15-20 hours)
- iOS-specific testing and benchmarking (~5 hours)
- App Store submission preparation (~4-6 hours)
- Build system complexity and debugging (~3-5 hours)

---

## APPENDIX A: KEY DECISIONS LOG

| Decision | Choice | Alternative Considered | Rationale |
|----------|--------|----------------------|-----------|
| DI Framework | Koin 4.1.1 | Kodein, Manual DI | Koin has best KMP support, familiar API for Hilt users, no code generation. Kodein is viable but smaller community. |
| Database | Room 2.8.3 (KMP) | SQLDelight 2.2.1 | Room now supports KMP (stable since 2.7.0). Keeping Room avoids learning a new ORM, preserves entity/DAO patterns from the original spec. SQLDelight would require rewriting all data access as raw SQL. |
| Navigation | Jetpack Navigation Compose (KMP) | Voyager, Decompose | Official JetBrains-published KMP port of AndroidX Navigation. Same API as the original Android-only spec. Voyager and Decompose are mature alternatives but introduce new paradigms. |
| Networking | Ktor Client 3.4.2 | (Retrofit not viable) | Only viable option for KMP HTTP. Kotlin-first, coroutine-native, multiplatform engines. |
| iOS deps mgmt | CocoaPods | Swift Package Manager | Better Kotlin/Native integration via `kotlin-cocoapods` plugin. Firebase and AdMob iOS SDKs have mature CocoaPods support. SPM for KMP is still maturing. |
| Firebase KMP | GitLive SDK + expect/actual | expect/actual only | GitLive provides KMP wrappers for Analytics. Crashlytics may still need expect/actual. Hybrid approach: use GitLive where available, expect/actual where not. |
| Image loading | Coil 3.4.0 | Kamel, custom | Coil 3 is the established Android choice, now KMP-native. Seamless upgrade. |
| Compose version | JetBrains Compose Multiplatform 1.10.3 | Google Jetpack Compose (Android-only) | Required for iOS rendering. JetBrains version wraps Jetpack Compose on Android and adds Skiko rendering for other platforms. |

## APPENDIX B: FILES TO CREATE IN PHASE 1

These files must exist at the end of Phase 1 for the KMP scaffold:

```
# Root
build.gradle.kts
settings.gradle.kts
gradle.properties
gradle/libs.versions.toml
gradle/wrapper/gradle-wrapper.properties

# Shared module
shared/build.gradle.kts
shared/src/commonMain/kotlin/com/auraflow/garden/di/SharedModule.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/theme/AuraFlowTheme.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/theme/Color.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/theme/Typography.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/navigation/NavGraph.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/navigation/Routes.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/screens/home/HomeScreen.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/screens/game/GameScreen.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/screens/zen/ZenScreen.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/screens/store/StoreScreen.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/screens/settings/SettingsScreen.kt
shared/src/commonMain/kotlin/com/auraflow/garden/ui/App.kt
shared/src/commonMain/kotlin/com/auraflow/garden/platform/PlatformContext.kt
shared/src/androidMain/kotlin/com/auraflow/garden/di/AndroidModule.kt
shared/src/androidMain/kotlin/com/auraflow/garden/platform/PlatformContext.android.kt
shared/src/iosMain/kotlin/com/auraflow/garden/di/IosModule.kt
shared/src/iosMain/kotlin/com/auraflow/garden/platform/PlatformContext.ios.kt
shared/src/iosMain/kotlin/com/auraflow/garden/MainViewController.kt

# Android app
androidApp/build.gradle.kts
androidApp/src/main/AndroidManifest.xml
androidApp/src/main/kotlin/com/auraflow/garden/AuraFlowApplication.kt
androidApp/src/main/kotlin/com/auraflow/garden/MainActivity.kt
androidApp/src/main/res/values/strings.xml
androidApp/src/main/res/values/colors.xml
androidApp/proguard-rules.pro

# iOS app
iosApp/iosApp.xcodeproj/project.pbxproj
iosApp/iosApp/AuraFlowApp.swift
iosApp/iosApp/ContentView.swift
iosApp/iosApp/iOSAppDelegate.swift
iosApp/iosApp/Info.plist
iosApp/iosApp/Assets.xcassets/
iosApp/iosApp/Entitlements/iosApp.entitlements
iosApp/Podfile
```

---

## APPENDIX C: RULE CHANGES FROM ORIGINAL SPEC

Rules from `CLAUDE.md` and `AuraFlow_ClaudeCode_BuildPrompt.md` that change
under KMP:

| Original Rule | KMP Replacement |
|---|---|
| "Only call `hiltViewModel()` at screen-level composables" | "Only call `koinViewModel()` at screen-level composables. Pass state down as parameters." |
| "All `@Module` must have `@InstallIn`" | Not applicable. Koin uses `module { }` DSL. Ensure modules are loaded in `startKoin { modules(...) }`. |
| "Every Activity/Fragment using Hilt must be `@AndroidEntryPoint`" | Not applicable. Koin injection does not require annotation. |
| "Use `viewModelScope`" | Same. `viewModelScope` works with KMP ViewModel via `org.jetbrains.androidx.lifecycle`. |
| "Room: `exportSchema = true`" | Same. Room KMP supports schema export via `room { schemaDirectory(...) }` in Gradle. |
| "Room: Never use `fallbackToDestructiveMigration()`" | Same. Still applies. |
| "Firebase auto-init disabled via AndroidManifest meta-data" | Same for Android. On iOS, disable auto-init via `GoogleService-Info.plist` flag `FIREBASE_ANALYTICS_COLLECTION_ENABLED = NO`. |
| "Accompanist is deprecated, do not add" | Same. Not available in KMP anyway. |
| "Use `enableEdgeToEdge()`" | Android only. iOS edge-to-edge is default. Use `.ignoresSafeArea()` in SwiftUI host. |
| "`String.hashCode()` is JVM-implementation-dependent" | Same. Already handled with deterministic seed formula. |
| "Build command: `./gradlew assembleDebug`" | Android: `./gradlew :androidApp:assembleDebug`. Also verify iOS: `./gradlew :shared:compileKotlinIosSimulatorArm64`. |

---

## APPENDIX D: UPDATED BUILD COMMANDS

```bash
# Shared module
./gradlew :shared:compileKotlinAndroid                    # Compile shared for Android
./gradlew :shared:compileKotlinIosSimulatorArm64          # Compile shared for iOS Simulator
./gradlew :shared:compileKotlinIosArm64                   # Compile shared for iOS Device

# Android
./gradlew :androidApp:assembleDebug                       # Android debug build
./gradlew :androidApp:assembleRelease                     # Android release build
./gradlew :shared:testDebugUnitTest                       # Shared unit tests (Android host)
./gradlew :androidApp:lintDebug                           # Android lint

# iOS
./gradlew :shared:iosSimulatorArm64Test                   # Shared unit tests (iOS host)
./gradlew :shared:assembleSharedXCFramework               # Build iOS framework
cd iosApp && pod install                                  # Install iOS dependencies
xcodebuild build -workspace iosApp.xcworkspace \
  -scheme iosApp -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 16'    # Build iOS app

# Both
./gradlew :shared:allTests                                # Run tests on all targets
./gradlew clean                                           # Clean all build artifacts
./gradlew :androidApp:bundleRelease                       # Android Play Store bundle
```

---

*This document is the authoritative reference for the KMP cross-platform
architecture. All build phases should reference this document for library choices,
module structure, and platform abstraction patterns.*
