---
mode: 'agent'
description: 'Investigate and fix a bug, then add a regression test'
---

## Task

Investigate the reported bug, identify the root cause, apply a minimal fix, and add a regression test so the bug cannot silently re-appear.

## Bug Details

Description: ${input:bug_description:Describe the bug — what happens vs. what should happen?}
Steps to reproduce (if known): ${input:reproduction_steps:Steps to reproduce the bug (optional)}
Affected area (module / screen / class): ${input:affected_area:Which module, screen, or class is affected? (optional)}

## Investigation Checklist

1. **Reproduce** the bug locally or trace the code path described.
2. **Consult `rules and agents.md`** to ensure the fix adheres to the project's architectural constraints.
3. **Identify the root cause** — is it in:
   - A use case (`usecase:*`)?
   - A repository or DAO (`repository`)?
   - A ViewModel state mutation (`app`)?
   - A Compose recomposition / state issue?
   - A cloud sync or background worker?
4. **Check for related tests** — are there existing tests that should have caught this?

## Fix Guidelines

- Make the **smallest possible change** that fixes the root cause; avoid refactoring unrelated code.
- Do not suppress compiler warnings or lint errors to silence the bug.
- If the bug is in a pure Kotlin class (use case, domain logic), prefer fixing it there rather than working around it in the ViewModel or UI.
- Log errors using the `Logger` interface from `logging-api`; never use `println` or `android.util.Log` directly.
- Validate inputs early and use guard clauses / early returns.

## Regression Test

- Write at least **one unit test** that fails on the original buggy code and passes after the fix.
- Place the test in `src/test/` of the module where the fix was made.
- Use **JUnit 4** + **MockK**.
- Name the test in plain English describing the scenario (e.g., `"returns empty list when reminder is expired"`).
- Follow the AAA (Arrange / Act / Assert) pattern.

## Output Checklist

- [ ] Root cause identified and documented in a code comment (if non-obvious)
- [ ] Minimal fix applied
- [ ] Regression test added
- [ ] All existing tests still pass
- [ ] No new lint warnings introduced
