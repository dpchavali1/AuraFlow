# Build State Tracking System -- Integration Task List

## Summary

Three files have been created to manage the 90-120 hour AuraFlow build across
many Claude Code sessions:

1. `BUILD_STATE.md` -- project root. The single source of truth for what is done,
   what is in progress, and what is broken.
2. `MISTAKES.md` -- project root. Accumulated lessons learned. Prevents the same
   debugging session from happening twice.
3. `CLAUDE_PROTOCOLS.md` -- project root. Defines exactly how Claude Code must
   behave at session start, session end, mid-session milestones, phase completion,
   regression detection, and error recovery.

## Integration Steps

### [ ] Task 1: Replace the "HOW TO USE" Section in the Build Prompt

**Description**: Replace lines 1764-1773 of AuraFlow_ClaudeCode_BuildPrompt.md
(the current "HOW TO USE THIS DOCUMENT" section) with the updated version below.

**Replacement text**:

```markdown
## HOW TO USE THIS DOCUMENT

This project will take 90-120 hours across many Claude Code sessions spanning
weeks. Three tracking files manage continuity between sessions:

- `BUILD_STATE.md` -- Read at every session start. Update after every milestone.
  Tracks phase status, build health, files created, test results, known issues.
- `MISTAKES.md` -- Read at every session start. Update when debugging takes >10
  minutes. Prevents repeating the same errors across sessions.
- `CLAUDE_PROTOCOLS.md` -- The full specification for session start, session end,
  milestone, phase completion, regression, and error recovery procedures.

### For the Developer

1. Open Claude Code in your AuraFlow project directory.
2. At the start of every session, tell Claude: "Read BUILD_STATE.md and resume."
   Claude will execute the Session Start Protocol automatically.
3. Paste the relevant phase prompt from this document when Claude is ready for
   new work. You do not need to paste all phases -- just the current one.
4. When you are done for the day, tell Claude: "End session." Claude will
   execute the Session End Protocol (update state, commit, report).
5. Each phase builds on the previous. Do not skip ahead unless you understand
   the dependency chain (see the Appendix table).
6. If something breaks, tell Claude: "Phase N regressed." Claude will follow
   the Regression Detection Protocol.
7. To see what has been done, read BUILD_STATE.md directly -- it is the
   authoritative record.

### For Claude Code

At the start of every conversation on this project, you MUST:
1. Read BUILD_STATE.md and MISTAKES.md.
2. Run `./gradlew assembleDebug` to verify current state.
3. Report status and wait for developer direction.
4. Never re-create files that already exist (check BUILD_STATE.md file lists).
5. Follow all protocols in CLAUDE_PROTOCOLS.md.

The full protocol specification is in CLAUDE_PROTOCOLS.md. Read it on your first
session. On subsequent sessions, the rules should already be in your context from
reading BUILD_STATE.md -- but if in doubt, re-read CLAUDE_PROTOCOLS.md.

Good luck, Warden. The garden is waiting.
```

### [ ] Task 2: Add CLAUDE.md for Automatic Context Loading

**Description**: Create a CLAUDE.md file in the project root. Claude Code
automatically reads this file when it starts a session in a directory. This
eliminates the need for the developer to remember to say "read BUILD_STATE.md."

**File**: `/Users/dchavali/GitHub/AuraFlow/CLAUDE.md`

### [ ] Task 3: Initialize Git Repository

**Description**: The project directory is not yet a git repository. Initialize
it before starting Phase 1 so that the checkpoint system works.

### [ ] Task 4: First Session Dry Run

**Description**: Run the Session Start Protocol once to verify all files are
readable and the protocol produces sensible output even when nothing has been
built yet.
