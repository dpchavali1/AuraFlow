# AURA FLOW -- CLAUDE CODE SESSION PROTOCOLS
# This document defines exactly how Claude Code must behave across sessions.
# It replaces the "HOW TO USE THIS DOCUMENT" section of the build prompt.

---

## TABLE OF CONTENTS

1. Session Start Protocol
2. Session End Protocol
3. Mid-Session Milestone Protocol
4. Phase Completion Criteria (Objective Verification)
5. Regression Detection Protocol
6. Error Recovery Protocol
7. Rules for Not Re-Doing Work

---

## 1. SESSION START PROTOCOL

Every time a new Claude Code conversation begins on this project, Claude MUST
execute these steps IN ORDER before writing any new code.

### Step 1: Read State Files (mandatory, every session)

```
Read BUILD_STATE.md
Read MISTAKES.md
```

Parse BUILD_STATE.md to determine:
- Which phase is currently active
- Which sub-tasks within that phase are done vs. remaining
- What the current build health is (last known assembleDebug result)
- Any open blockers in the KNOWN ISSUES table

Parse MISTAKES.md to load all known anti-patterns into context.

### Step 2: Verify Build Health

Run the Gradle build to confirm the project still compiles:

```
./gradlew assembleDebug 2>&1 | tail -20
```

If this fails:
- DO NOT proceed to new work.
- Compare the error against MISTAKES.md entries.
- Fix the build first.
- Log the fix in MISTAKES.md if it reveals a new pattern.
- Update BUILD_STATE.md build health table.

If this succeeds:
- Update the BUILD_STATE.md build health table with PASS and today's date.

### Step 3: Run Existing Tests (if any exist)

```
./gradlew testDebugUnitTest 2>&1 | tail -30
```

If tests fail:
- Check whether the failures are pre-existing (listed in KNOWN ISSUES).
- If new failures: a regression occurred. Follow the Regression Detection Protocol (section 5).
- Do not proceed to new feature work until tests pass or failures are documented.

### Step 4: Report Status to Developer

Print a concise status summary:

```
--- AURA FLOW SESSION START ---
Build: PASS / FAIL
Tests: X passed, Y failed, Z skipped
Last completed phase: Phase N (VERIFIED on YYYY-MM-DD)
Current phase: Phase M (IN_PROGRESS)
  - Done: sub-task A, sub-task B
  - Remaining: sub-task C, sub-task D
Open blockers: (list or "none")
Next action: (what Claude will work on first)
-------------------------------
```

### Step 5: Wait for Developer Confirmation

After reporting status, wait for the developer to confirm the plan or redirect.
Do NOT auto-start coding. The developer may want to:
- Change priorities
- Address a blocker first
- Skip to a different phase
- Fix something manually before Claude proceeds

---

## 2. SESSION END PROTOCOL

Before the conversation ends (when the developer says they are done, or when
a natural stopping point is reached), Claude MUST:

### Step 1: Run Build Verification

```
./gradlew assembleDebug 2>&1 | tail -20
```

If it fails: Fix it before ending. Never leave the project in a broken state.

### Step 2: Run Tests

```
./gradlew testDebugUnitTest 2>&1 | tail -30
```

Note any new failures. If time permits, fix them. If not, log them in KNOWN ISSUES.

### Step 3: Update BUILD_STATE.md

Update ALL of the following sections:
- BUILD HEALTH table: current pass/fail status
- CURRENT POSITION: active phase, active sub-task, session count +1
- PHASE STATUS table: update status of any phases worked on
- ACTIVE PHASE SUB-TASKS: check off completed sub-tasks
- KNOWN ISSUES: add any new issues discovered
- FILES CREATED OR MODIFIED: list every file touched during this session
- TEST RESULTS LOG: append latest test run results
- SESSION LOG: append a summary row for this session

### Step 4: Update MISTAKES.md (if applicable)

If any debugging took more than 10 minutes on a single issue, document:
- What the symptom was
- What the root cause was
- What the fix was
- What pattern to follow in the future to avoid it

### Step 5: Commit to Git

Create a descriptive commit. The message format:

```
Phase N: [sub-task description]

- [bullet point of what was done]
- [bullet point of what was done]
- Build: PASS/FAIL
- Tests: X/Y passing
```

Then update the GIT CHECKPOINT LOG in BUILD_STATE.md with the commit hash.

### Step 6: Report What Happened and What Is Next

Print a concise end-of-session summary:

```
--- SESSION END ---
Worked on: Phase N - [description]
Completed: [list of sub-tasks finished]
Remaining in phase: [list of sub-tasks not done]
Build: PASS/FAIL
Tests: X/Y passing
New issues: (list or "none")
Next session should: [specific first action]
-------------------
```

---

## 3. MID-SESSION MILESTONE PROTOCOL

After completing each sub-task within a phase (not just at session end):

1. Run `./gradlew assembleDebug` to verify the build.
2. Run any relevant tests.
3. Update the ACTIVE PHASE SUB-TASKS table in BUILD_STATE.md.
4. If the sub-task introduced new files, update FILES CREATED OR MODIFIED.
5. If the build broke and required non-obvious fixes, update MISTAKES.md.

This prevents large amounts of work from being lost if a later sub-task
introduces a problem that is hard to untangle.

---

## 4. PHASE COMPLETION CRITERIA

A phase moves from CODE_COMPLETE to VERIFIED only when ALL of the following pass.
These criteria go beyond the "Done when" checklists in the build prompt.

### Universal Criteria (apply to every phase)

1. **Build compiles**: `./gradlew assembleDebug` succeeds with zero errors.
2. **No new warnings introduced**: Lint warnings from this phase are either
   fixed or documented with justification in KNOWN ISSUES.
3. **Unit tests pass**: `./gradlew testDebugUnitTest` with zero failures
   related to this phase's code.
4. **No regressions**: All previously verified phases still work. Specifically,
   any tests from earlier phases must still pass.
5. **"Done when" checklist complete**: Every checkbox from the build prompt's
   "Done when" section for this phase is checked. Quote each criterion and
   state how it was verified (test name, manual check, etc.).
6. **Files documented**: Every file created or modified is listed in the
   FILES CREATED OR MODIFIED section of BUILD_STATE.md.
7. **No TODO/FIXME debt carried forward silently**: Any TODO comments added
   during the phase must be logged in KNOWN ISSUES with the phase they should
   be resolved in.

### Phase-Specific Verification

#### Phase 1: Project Scaffolding
- [ ] `./gradlew assembleDebug` succeeds
- [ ] `./gradlew assembleRelease` succeeds (catches ProGuard issues early)
- [ ] NavGraph navigates between all 5 placeholder screens without crash
- [ ] Theme colors match spec: Primary #6C63FF, Secondary #00B4D8, etc.
- [ ] Light/dark mode toggle works
- [ ] All package directories exist under com.auraflow.garden

#### Phase 2: Core Data Models
- [ ] All model classes compile independently (no circular deps)
- [ ] Room schema exports to schemas/ directory
- [ ] Sample level JSON parses without error in a unit test
- [ ] LevelDesigner generates a valid level (unit test)
- [ ] DAOs compile and basic CRUD test passes (instrumentation or Robolectric)

#### Phase 3: Game Rendering
- [ ] GameCanvas renders nodes at correct positions from a test level
- [ ] Link drawing from node A to node B creates a Link object
- [ ] Color mismatch rejection works (tap violet, drag to teal = rejected)
- [ ] Undo removes last link and refunds energy (unit test)
- [ ] Frame time under 16ms with 10 nodes on Pixel 5 emulator (profile)
- [ ] Touch target is 56dp even though visual node is 40dp

#### Phase 4: Game Logic
- [ ] Unit test: stage with all pairs linked returns StageCleared
- [ ] Unit test: energy exhausted with pairs remaining returns StageFailed
- [ ] Unit test: resonance chain of 3 same-color links gives 15% refund
- [ ] Unit test: near-miss detection (1 pair remaining + energy out)
- [ ] Unit test: intersection detection between crossing line segments
- [ ] StageResult persists to Room and can be queried back

#### Phase 5: Luma Companion
- [ ] Luma renders on screen and drifts with spring physics
- [ ] Correct word appears for at least 3 different game events
- [ ] Idle detection fires at 25s, 45s, 90s thresholds
- [ ] Luma Boost offer appears after configured failures
- [ ] Reduced-motion mode disables Luma animations

#### Phase 6: Haptic Engine
- [ ] HapticEngine injectable via Hilt
- [ ] At least 5 haptic patterns fire on corresponding events
- [ ] Haptics toggle in settings actually stops vibrations
- [ ] No crash on emulator (which has no vibrator hardware)
- [ ] Unit test maps HapticType enum to correct pattern selection

#### Phase 7: Narrator & Dialogue
- [ ] Narrator text appears during stage entry with typewriter animation
- [ ] Text auto-fades after 4 seconds
- [ ] Tap-to-dismiss works
- [ ] Luma and narrator text never overlap on screen simultaneously
- [ ] All displayed strings come from strings.xml (no hardcoded English)
- [ ] Analytics events fire for dialogue shown and skipped

#### Phase 8: World Progression
- [ ] World map renders all 4 worlds with correct lock states
- [ ] Tapping a locked premium world shows Warden's Pass prompt
- [ ] Stage unlock is linear: cannot access stage N+1 without clearing N
- [ ] World entry cinematic plays on first entry, not on subsequent entries
- [ ] Checkpoint ("Memory Bloom") appears every 10 stages visually

#### Phase 9: Visual Effects
- [ ] Particle system renders at least 100 particles at 60fps
- [ ] Stage bloom effect plays on stage clear
- [ ] Flora grows along completed links
- [ ] Aura skin selector changes link trail visuals
- [ ] Reduce-effects toggle cuts particle count and disables skins
- [ ] No GPU overdraw regression (check with Layout Inspector)

#### Phase 10: Audio
- [ ] Ambient loop plays for at least one world
- [ ] SFX fires for node_tap, link_success, link_fail (at minimum)
- [ ] Volume sliders in settings actually change volume
- [ ] Audio stops when app is backgrounded
- [ ] No memory leak from audio buffers (check with profiler)
- [ ] Mute toggle persists across app restart

#### Phase 11: Tutorial
- [ ] Stage 1 is completable with guided highlights
- [ ] Stage 1 has no fail state (infinite energy)
- [ ] Stage 2 introduces the energy bar with tooltip
- [ ] Stage 3 has a real fail state that triggers retry
- [ ] Stages 4-5 are playable without tutorial hand-holding
- [ ] Tutorial flags persist: replaying stage 1 skips the guides

#### Phase 12: Dynamic Difficulty
- [ ] Unit test: 2 consecutive failures adds 12% energy boost
- [ ] Unit test: Luma Boost offered after 3 failures
- [ ] Unit test: boost caps at 20% after 5 failures
- [ ] Stages cleared with boost cannot earn 3 stars
- [ ] Dynamic difficulty resets on stage clear or navigation away
- [ ] Boss stages use stricter thresholds (3 failures, 10% cap)

#### Phase 13: Engagement Systems
- [ ] Streak increments on daily stage clear
- [ ] Streak resets after 2+ day gap
- [ ] Daily challenge generates same puzzle for same date (unit test with fixed seed)
- [ ] Achievement unlocks persist to Room and show in UI
- [ ] At least 3 achievements can be triggered in a test scenario
- [ ] Streak break shows warm return screen, not punishment

#### Phase 14: Monetization
- [ ] BillingClient connects successfully (on device or with test account)
- [ ] Product list queries and displays localized prices
- [ ] Purchase flow completes (test track)
- [ ] Restore purchases works
- [ ] Warden's Pass purchase unlocks worlds 3-4, Zen Mode, Blueprints
- [ ] Offline pending purchase state handled

#### Phase 15: AdMob
- [ ] Test ad loads and displays in Daily Challenge flow
- [ ] Ad only appears after Daily Challenge failure (never elsewhere)
- [ ] Ad completion grants retry attempt
- [ ] Ad skip returns to fail card with no retry
- [ ] Consent dialog shows in applicable regions
- [ ] Ad failure path still allows "come back tomorrow" gracefully

#### Phase 16: Zen Mode & Blueprints
- [ ] Zen Mode loads with no energy bar, no timer, no fail state
- [ ] Node placement, linking, and deletion work
- [ ] Blueprint saves to Room with title and node data
- [ ] Blueprint loads as a playable level
- [ ] Zen Mode time tracking works for achievement

#### Phase 17: Share Clips
- [ ] Screenshot capture works at crescendo moment
- [ ] Overlay branding (stage number, app name) renders on screenshot
- [ ] Share intent opens system share sheet
- [ ] Fallback to static image works if video encoding fails
- [ ] No crash on share cancel

#### Phase 18: Backend
- [ ] Docker container builds and starts
- [ ] Daily challenge endpoint returns valid level JSON
- [ ] Leaderboard POST and GET work via curl
- [ ] Android client fetches daily challenge from running backend
- [ ] Offline fallback (client-side seed generation) works when backend unreachable

#### Phase 19: Polish
- [ ] Screen transitions play without frame drops
- [ ] Loading states show in-world messages (no generic spinners)
- [ ] Button press animations work globally
- [ ] Performance Mode toggle reduces particle count
- [ ] Back gesture on game screen shows confirmation dialog
- [ ] Portrait lock is enforced

#### Phase 20: Play Store Prep
- [ ] `./gradlew bundleRelease` produces a signed AAB
- [ ] ProGuard rules keep Billing, Firebase, Room, Media3 working in release
- [ ] Unit test suite fully green
- [ ] Play Store listing text drafted and within character limits
- [ ] Privacy policy URL is accessible
- [ ] Content rating questionnaire answers documented

---

## 5. REGRESSION DETECTION PROTOCOL

A regression is when previously working functionality breaks due to new changes.

### When to Check for Regressions

- After every phase completion (before marking VERIFIED)
- When the Session Start Protocol reveals test failures
- When the developer reports something that used to work is now broken

### How to Handle a Regression

1. Identify which phase's functionality regressed.
2. Change that phase's status from VERIFIED to REGRESSED in BUILD_STATE.md.
3. Log the regression in KNOWN ISSUES with severity HIGH or BLOCKER.
4. Fix the regression BEFORE continuing new phase work.
5. After fixing, re-run that phase's verification criteria.
6. Only restore VERIFIED status when all criteria pass again.
7. Log the regression and fix in MISTAKES.md for future reference.

---

## 6. ERROR RECOVERY PROTOCOL

When Claude Code encounters an error it cannot immediately resolve:

### Build Failure
1. Read the full error output (not just the last line).
2. Check MISTAKES.md for matching patterns.
3. If the error is a dependency conflict: check DEPENDENCY VERSIONS table.
4. If the error is in generated code (Hilt, Room): clean and rebuild first.
   ```
   ./gradlew clean assembleDebug
   ```
5. If clean build fails: isolate the change. Check git diff to find what changed.
6. If still stuck after 3 attempts: log in KNOWN ISSUES as BLOCKER and report
   to the developer with the full error output and what was tried.

### Test Failure
1. Read the test failure message and stack trace.
2. Determine if this is a new failure or pre-existing.
3. If new: the current session's changes likely caused it. Review recent changes.
4. If pre-existing: check KNOWN ISSUES. If not listed, add it.
5. Do not suppress or delete failing tests to make the suite pass.

### Runtime Crash
1. Capture the stack trace from logcat.
2. Identify the crash location.
3. Check if the crash is reproducible.
4. Fix and add a test that covers the crash scenario.
5. Log in MISTAKES.md.

---

## 7. RULES FOR NOT RE-DOING WORK

These rules prevent Claude Code from wasting time on already-completed work.

### Rule 1: Always Read BUILD_STATE.md First
Before writing any code, check what already exists. The FILES CREATED OR MODIFIED
section lists every file in the project and which phase created it.

### Rule 2: Never Recreate Existing Files
If BUILD_STATE.md says a file was created in Phase 2, do not recreate it in
Phase 5 unless there is a specific, documented reason to restructure it.
Instead, MODIFY the existing file.

### Rule 3: Check Before Generating
Before generating a new class, check if it already exists:
```
find app/src -name "ClassName.kt" 2>/dev/null
```
If it exists, read it first to understand its current state before modifying.

### Rule 4: Respect the Dependency Chain
The build prompt's appendix lists phase dependencies. If Phase 5 depends on
Phases 3 and 4, assume those phases' code exists and import from it. Do not
re-implement GameEngine in Phase 5 -- import it.

### Rule 5: Incremental Over Wholesale
When modifying an existing file, use targeted edits (add a function, modify
a data class, add an import) rather than rewriting the entire file. Wholesale
rewrites risk losing work from previous phases.

### Rule 6: Test Preservation
Never delete or modify existing tests unless they test functionality that has
been intentionally changed. If a test needs updating due to an API change,
update it -- do not delete it.

### Rule 7: When in Doubt, Ask
If it is unclear whether something has been implemented, check the codebase
first. If still unclear, ask the developer rather than guessing and potentially
duplicating or overwriting work.
