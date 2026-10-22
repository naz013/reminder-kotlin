# M3 Expressive Adoption

Short record of how Material 3 Expressive was adopted in this app. It replaces a ~5,400-line audit log that
recorded each screen's findings and fixes one by one; that history is in git (`git log -- docs/m3-expressive-adoption.md`).
Related docs: [m3-expressive-guidelines.md](m3-expressive-guidelines.md) (the spec digest used to audit a screen)
and [m3-expressive-screen-inventory.md](m3-expressive-screen-inventory.md) (per-screen status; every listed
screen is currently marked Done).

## Status

Adopted app-wide. Every screen in the inventory has been audited against the guidelines and fixed.
New screens should follow the conventions below rather than repeating the audit.

## What M3 Expressive is

An additive evolution of Material 3, not a replacement: an emphasized variant of every type style, a larger
shape library with morphing, a spring-based `MotionScheme`, deliberate use of the color roles, and new
components (button groups, FAB menu, loading indicator, floating toolbar, split button). Official
background: [m3.material.io/blog/building-with-m3-expressive](https://m3.material.io/blog/building-with-m3-expressive).

## Version gotcha (why the dependency matters)

Material3 1.4.0 compiled most of the Expressive API as `internal` (`MotionScheme`, `MaterialExpressiveTheme`,
emphasized typography), so it was unusable from app code. It became public in the 1.5.0 alphas. The repo is
now on `androidx.compose.material3` **1.5.0-beta01** (`gradle/libs.versions.toml`). Do not downgrade below the
1.5 line, and check this first if an Expressive API suddenly "does not exist".

## Foundation (in `ui-common`)

- `AppTheme` (`Theme.kt`) wraps content in `MaterialExpressiveTheme`, switched app-wide in one step.
- `MaterialTheme.motionScheme` is the single source of motion. No custom spring wrapper exists or is needed.
- A shared `EmptyState` composable (`foundation/component/EmptyState.kt`) replaced per-screen duplicates.
- Shared tokens such as `TopAppbarColor` are used by every `TopAppBar`.
- Shape morphing and polygon `MaterialShapes` were deliberately not adopted.

## Conventions every screen follows

These were the recurring findings across the audit. Apply them when writing or changing a screen:

1. **Motion:** use `MaterialTheme.motionScheme` specs, not literal `tween()` durations.
2. **Typography:** use the `*Emphasized` styles, not `fontWeight = Bold/Medium` overrides.
3. **Top bars:** use the shared `TopAppbarColor` token instead of a hard-coded container color. Back and close
   buttons need a real `contentDescription`, never `null`.
4. **Touch targets:** at least 48dp for anything tappable.
5. **Icons:** through `DrawableCatalog` / `AppIcons`, never a bare `R.drawable.*` (also a rule in `CLAUDE.md`).
6. **Surfaces:** prefer a color-fill change on scroll over a drop-shadow elevation; use surface-container roles
   rather than alpha-blended colors for captions and chips.
7. **Empty states:** use the shared `EmptyState`.
8. **Shared scaffolds:** fix issues in the shared scaffold (for example `SettingsScaffold`) rather than per
   screen, since one scaffold feeds dozens of screens.
9. **Compose API order:** `modifier: Modifier = Modifier` is the first parameter (see `CLAUDE.md`).

## Known judgment calls

- The hue per Home navigation tile is a taste decision, set per section in `GetNavigationItemsUseCase.kt`
  from `ThemeProvider.AppColorIndex`, and can be changed there without structural work.
- Claims in the old log were sometimes wrong (two "landed" fixes turned out not to have landed). When in doubt,
  trust the current source over any historical write-up.

## Auditing a new screen

Use the checklist in [m3-expressive-guidelines.md](m3-expressive-guidelines.md), fix what it finds, then add or
update the screen's row in [m3-expressive-screen-inventory.md](m3-expressive-screen-inventory.md).
