# AuraFlow: The Kinetic Garden — Claude Code Operating Manual

> This file auto-loads at session start. Follow these protocols exactly.
> For the full build specification, see `AuraFlow_ClaudeCode_BuildPrompt.md`.
> For the KMP architecture decisions, see `KMP_MIGRATION_PLAN.md`.

---

## SESSION START PROTOCOL (MANDATORY — EVERY SESSION)

Every new Claude Code session MUST begin with these steps, in order:

1. **Read state files:**
   ```bash
   cat BUILD_STATE.md
   cat MISTAKES.md
   ```
   If either file does not exist, create it from the templates in this project.

2. **Session 0 check — does the project exist?**
   ```bash
   ls gradlew 2>/dev/null && echo "PROJECT EXISTS" || echo "SESSION 0: No project yet"
   ```
   - If `gradlew` does **NOT** exist: This is **Session 0**. Skip steps 3 and 4. Report:
     `SESSION 0 — Project not yet created. BUILD_STATE.md exists only as a tracking template. Ready to begin Phase 1 scaffolding.`
     Then wait for developer confirmation to begin Phase 1.
   - If `gradlew` **does** exist: Proceed to step 3.

3. **Verify the claimed build state is real (BOTH platforms):**
   ```bash
   ./gradlew :shared:build 2>&1 | tail -20
   ./gradlew :androidApp:assembleDebug 2>&1 | tail -20
   ./gradlew :shared:allTests 2>&1 | tail -30
   ```
   `allTests` runs unit tests for ALL KMP targets (Android JVM + iOS Simulator on macOS). If BUILD_STATE.md says Phase N is complete but any build fails, the state file is stale. Fix the build before doing anything else.

4. **Report status to the developer:**
   ```
   SESSION START
   Last completed phase: Phase [N] — [Name]
   Current phase: Phase [N+1] — [Name] ([status])
   Build health: Android [PASS/FAIL] | iOS framework [PASS/FAIL] | Shared [PASS/FAIL]
   Test health: [X passed, Y failed]
   Known blockers: [list or "none"]
   Mistakes to watch for: [top 3 from MISTAKES.md relevant to current phase]
   Ready to proceed: [YES/NO]
   ```

5. **Wait for developer confirmation** before starting any work.

---

## SESSION END PROTOCOL (MANDATORY)

Before ending any session:

1. **Verify builds compile (both platforms):**
   ```bash
   ./gradlew :shared:build
   ./gradlew :androidApp:assembleDebug
   ```
2. **Run tests:** `./gradlew :shared:allTests`  (all KMP targets)
3. **Update BUILD_STATE.md** with current phase status, files created/modified, known issues.
4. **Update MISTAKES.md** if any errors were encountered during this session.
5. **Git commit** with descriptive message: `Phase N [sub-task]: [description]`
6. **Report session summary:**
   ```
   SESSION END
   Phase worked on: Phase [N] — [Name]
   Status: [complete / in progress — what remains]
   Files created: [list with module: shared/commonMain, androidMain, iosMain, androidApp, iosApp]
   Files modified: [list]
   Tests: [X passed, Y failed, Z new]
   Issues discovered: [list or "none"]
   Next step: [what to do next session]
   ```

---

## RULES — DO NOT VIOLATE

### Cross-Platform First
- **95% of code goes in `shared/src/commonMain/`.** Only platform-specific API implementations go in `androidMain/` or `iosMain/`.
- **NEVER put platform-specific imports in commonMain.** No `android.*`, `java.*`, `platform.UIKit.*`, or `platform.Foundation.*` imports in commonMain. Use expect/actual.
- **Use kotlinx libraries for cross-platform types:** `kotlinx-datetime` (not `java.time`), `kotlinx-coroutines` (not `android.os.Handler`), `kotlin.uuid.Uuid` (not `java.util.UUID`).

### Never Re-Do Work
- ALWAYS read BUILD_STATE.md before writing any code.
- Before creating any new file, search the project: `find shared/src -name "*FileName*"`. If it exists, read it first. Extend — do not duplicate.
- Before adding any dependency, check `shared/build.gradle.kts` and `gradle/libs.versions.toml`.
- Before defining any constant, check `shared/src/commonMain/.../data/model/` and `shared/src/commonMain/.../util/` for existing definitions.
- Modify existing files with targeted edits rather than rewriting them wholesale.

### Never Proceed With a Broken Build
- If `./gradlew :shared:build` fails, stop. Fix it before writing new code.
- If `./gradlew :androidApp:assembleDebug` fails, stop. Fix it.
- If a previously passing test now fails, that is a regression. Fix it before proceeding.
- If a "Done when" item from any prior phase is no longer true, fix it before proceeding.

### Always Learn From Mistakes
- Read MISTAKES.md before starting each phase. Pay attention to "Recurrence Risk" fields.
- After every error: diagnose root cause, fix, re-test, log to MISTAKES.md with platform tag (Android/iOS/Shared).
- Never paper over errors. Fix root causes, not symptoms.

### Always Review Your Own Work
- After completing any phase, run the full self-review (see build prompt: SELF-GOVERNANCE PROTOCOL).
- Check your diff: `git diff --stat`. Did you modify files outside this phase's scope?
- Did you put platform code in commonMain?
- Did you hardcode any string that should be in string resources?
- Did you duplicate code that exists in a shared location?

---

## KEY FILES

| File | Purpose |
|------|---------|
| `AuraFlow_ClaudeCode_BuildPrompt.md` | Master 20-phase build spec with governance protocols |
| `KMP_MIGRATION_PLAN.md` | KMP architecture decisions, dependency versions, expect/actual contracts |
| `AuraFlow_Alignment_Review.md` | Gap analysis and improvement recommendations |
| `BUILD_STATE.md` | Phase progress, build health, files inventory, session log |
| `MISTAKES.md` | Error log with root causes, fixes, and prevention rules |
| `ARCHITECTURE.md` | System interfaces, event flows, DI graph (create after Phase 4) |
| `AuraFlow_GameScript_v2.docx` | Original game design document |

---

## TECH STACK — KMP CROSS-PLATFORM (LOCKED VERSIONS)

### Core Platform
| Component | Version | Notes |
|-----------|---------|-------|
| Kotlin | 2.1.20 | K2 compiler, required by Compose Multiplatform 1.10.x |
| Compose Multiplatform | 1.10.3 | JetBrains, iOS stable since 1.8.0 |
| AGP | 8.7.3 | For Kotlin 2.1.x compatibility |
| Gradle | 8.11.1 | Required by AGP 8.7.x |
| KSP | 2.1.20-1.0.32 | For Room code generation |

### Architecture & DI
| Component | Version | Notes |
|-----------|---------|-------|
| Koin BOM | 4.1.1 | Replaces Hilt (Hilt is Android-only) |
| Navigation Compose (KMP) | 2.9.2 | `org.jetbrains.androidx.navigation:navigation-compose` |
| Lifecycle ViewModel (KMP) | 2.10.0 | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` |

### Data & Persistence
| Component | Version | Notes |
|-----------|---------|-------|
| Room (KMP) | 2.8.3 | Supports Android + iOS. Uses KSP, not kapt. |
| SQLite Bundled | 2.5.1 | `androidx.sqlite:sqlite-bundled` — for iOS Room driver |
| Multiplatform Settings | 1.3.0 | Replaces DataStore for key-value prefs |

### Networking
| Component | Version | Notes |
|-----------|---------|-------|
| Ktor Client | 3.4.2 | Replaces Retrofit (JVM-only). OkHttp engine (Android), Darwin engine (iOS) |
| kotlinx-serialization-json | 1.9.0 | KMP-native JSON |

### Platform Services (expect/actual)
| Component | Android | iOS |
|-----------|---------|-----|
| Billing | Play Billing 6.2.1 | StoreKit 2 (native) |
| Ads | AdMob 23.0.0 | Google Mobile Ads iOS SDK |
| Audio | Media3 1.3.1 / SoundPool | AVFoundation / AVAudioEngine |
| Haptics | VibrationEffect | Core Haptics (CHHapticEngine) |
| Analytics | Firebase Android (GitLive 2.4.0) | Firebase iOS (GitLive 2.4.0) |
| Image Loading | Coil 3.4.0 | Coil 3.4.0 (KMP-native) |

### Async & Utilities
| Component | Version |
|-----------|---------|
| kotlinx-coroutines | 1.10.2 |
| kotlinx-datetime | 0.7.1 |
| kotlinx-collections-immutable | 0.3.8 |

Never override these unless a phase prompt explicitly authorizes it.

---

## PROJECT STRUCTURE

```
AuraFlow/
├── shared/                              # KMP shared module (95% of code)
│   └── src/
│       ├── commonMain/kotlin/.../       # ALL shared code (game logic, UI, models)
│       ├── androidMain/kotlin/.../      # Android expect/actual (haptics, audio, billing, ads)
│       └── iosMain/kotlin/.../          # iOS expect/actual (Core Haptics, AVFoundation, StoreKit 2)
├── androidApp/                          # Android app shell (MainActivity, manifest)
├── iosApp/                              # Xcode project (ContentView.swift, iOS app lifecycle)
├── gradle/libs.versions.toml            # ALL dependency versions
└── [governance files]                   # CLAUDE.md, BUILD_STATE.md, MISTAKES.md, etc.
```

---

## BUILD COMMANDS

```bash
# Shared module
./gradlew :shared:build                              # Compile shared for all targets
./gradlew :shared:allTests                            # Shared unit tests (all KMP targets)
./gradlew :shared:compileKotlinIosSimulatorArm64      # Verify iOS framework compiles

# Android
./gradlew :androidApp:assembleDebug                   # Android debug APK
./gradlew :androidApp:assembleRelease                 # Android release (ProGuard/R8)
./gradlew :androidApp:connectedDebugAndroidTest        # Android instrumentation tests
./gradlew :androidApp:lintDebug                        # Android lint
./gradlew :androidApp:bundleRelease                    # Play Store bundle

# iOS
# Build via Xcode: open iosApp/iosApp.xcworkspace (NOT .xcodeproj — CocoaPods requires workspace), select simulator, Cmd+B
# Or: xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' build

# Both
./gradlew clean                                        # Clean all build artifacts
```

---

## HARD RULES

- **Cross-platform first:** 95% in commonMain. Only platform API bridges in androidMain/iosMain.
- **No platform imports in commonMain.** Use expect/actual for all platform APIs.
- **Strings:** ALL user-facing text in Compose string resources (multiplatform). Never hardcode English.
- **Analytics:** All event names prefixed `af_`. Defined as constants, never as string literals.
- **Accessibility:** Color-blind shapes on nodes, Dynamic Type / font scale, Reduce Motion / reduced-motion, haptics toggle. VoiceOver (iOS) + TalkBack (Android).
- **Privacy:** No PII in analytics. Firebase auto-init disabled until consent granted. iOS App Tracking Transparency before any tracking.
- **Ads:** ONLY in Daily Challenge, ONLY after failure, ONLY rewarded video. Zero ads elsewhere. Ever.
- **Room:** Never use `fallbackToDestructiveMigration()`. Always write explicit migrations. Use BundledSQLiteDriver on iOS.
- **Canvas:** Never allocate Paint/Path objects inside drawScope. Pre-allocate in `remember {}`.
- **DI:** Use `koinViewModel()` at screen-level composables only. Pass state down as parameters. Never use Hilt.
- **Coroutines:** Use `viewModelScope` for ViewModel work. Use `withFrameNanos` for animations. Use kotlinx-datetime, not java.time.
- **Kotlin/Native (iOS):** Be aware of frozen object restrictions. Coroutines work normally with the new memory model. No `GlobalScope`.
