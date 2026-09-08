# M3 Expressive Adoption Plan

Research + gap analysis for adopting Material 3 Expressive, starting with `HomeScreen`
(`ChronologicalHomeScreen`) and `AgendaScreen`. This is a planning document, not a change log — no
production code was modified while writing it.

Source: [m3.material.io/blog/building-with-m3-expressive](https://m3.material.io/blog/building-with-m3-expressive)
(May 2025), plus the linked Typography and Shape spec pages.

For the full list of app screens and their per-screen migration status, see
[`m3-expressive-screen-inventory.md`](m3-expressive-screen-inventory.md). For the underlying spec rules
(layout/breakpoints, color roles, type scale, shape scale, elevation, motion, states, accessibility
targets) used to audit each screen, see [`m3-expressive-guidelines.md`](m3-expressive-guidelines.md).

## 1. What M3 Expressive actually is

M3 Expressive is **not** a new major version ("M4") and doesn't replace Material 3 — it's an additive
evolution: new component variants, a richer type scale, an expanded shape system, and a spring-based
motion system, all layered on the existing M3 color/token model. Google's framing, backed by 46 UX studies
(~18k participants): expressive UI is preferred across age groups, scores higher on playfulness/energy/
friendliness, and let users spot key UI elements up to 4x faster — i.e. this is pitched as a usability
improvement, not just a reskin.

The update has four building blocks:

| Building block | What's new |
|---|---|
| **Typography** | The type scale gains an **emphasized** variant of all 15 styles (baseline + emphasized = 30 tokens total, same size steps from Display Large to Label Small). Emphasized styles carry more weight/width and are meant for selected states, primary actions, unread badges — not swapped in wholesale. |
| **Shape** | A library of 35 named shapes (beyond the old rounded-rect corner scale) plus built-in **shape morphing** — animated interpolation from one shape to another (e.g. square → circle), used natively in loading indicators and button groups. |
| **Color** | Same token model (primary/secondary/tertiary, containers, surface ramp, **fixed** roles), but explicit guidance to use more of the palette deliberately — contrast between primary/secondary/tertiary to build hierarchy instead of leaning on one accent everywhere. |
| **Motion** | A spring-based `MotionScheme` (spatial springs for position/size, effect springs for color/opacity) replacing hand-tuned `tween()` durations, applied consistently via `MaterialTheme.motionScheme`. |

Plus 14 new/updated components: app bars, button groups, extended FAB, **FAB menu**, loading indicator,
navigation bar/rail updates, sliders, **split button**, **toolbars** (floating).

Seven design tactics tie it together (useful as a review checklist later): vary shape deliberately, use
color contrast for hierarchy, use emphasized type to guide attention, group content into containers, add
fluid motion, let components adapt to context (foldables/large screens), and reserve 1–2 "hero moments" per
screen rather than making everything loud.

## 2. Where this repo already stands

Compose BOM is `2026.06.01` and `androidx.compose.material3:material3` was pinned to **1.4.0**
([`gradle/libs.versions.toml:39,42`](../gradle/libs.versions.toml)) as of the original version of this
doc.

**Correction #1 (superseded by Correction #2 below, kept for history)**: an earlier pass of this doc
claimed 1.4.0 "carries most of the Expressive APIs... some still gated behind
`@ExperimentalMaterial3ExpressiveApi`" and that emphasized `Typography` was the one exception. That
claim about 1.4.0 was itself wrong.

**Correction #2 (verified by decompiling the actual `material3-android-1.4.0-sources.jar` from the Gradle
cache, and cross-checked against Google's Maven `maven-metadata.xml`)**: in 1.4.0, essentially the entire
Expressive API surface is compiled into the module as `internal` and unreachable from application code —
not just Typography:
- `MaterialShapes` does not exist anywhere in the 1.4.0 jar (zero references).
- `MaterialExpressiveTheme` exists but is `internal fun` — not callable outside the material3 module.
- `MotionScheme` is an `internal interface` — the *type* itself isn't public, so `MaterialTheme.motionScheme`
  (also `internal`) can't be read or overridden from outside the module either.
- `LoadingIndicator`, `ButtonGroup`, and `FloatingToolbar` have no public composable functions anywhere in
  the sources (only internal design-token files).
- `Typography.xxxEmphasized` fields are real but every public `Typography(...)` constructor set them equal
  to the baseline style (as the original Correction #1 found) — same "present but inert" pattern.

At the time of this correction, Google's Maven metadata confirmed **1.4.0 was still the latest stable
material3 release**; `1.5.0` was 27 alphas deep with no beta yet. All of the above — `MaterialShapes`
(public `object` of 30+ `RoundedPolygon`s plus a `toShape()` converter), `MotionScheme` (public
`interface` with `standard()`/`expressive()` factories), `MaterialExpressiveTheme` (public `fun`), and
real emphasized `Typography` defaults — are genuinely public starting in `1.5.0-alpha27` (also verified by
decompiling that jar).

**Decision**: bumped `androidx-compose-material3` to `1.5.0-alpha27` in
[`gradle/libs.versions.toml`](../gradle/libs.versions.toml) (matching the version already pinned for
`material3-adaptive-navigation-suite` in the same file) to get real access to this API surface, accepting
the risk of a pre-release dependency with no announced stable date. Verified with a full
`./gradlew :app:assembleProDebug` both immediately after the bump (no other code changes) and after the
`ui-common` foundation work in §3 landed — both succeeded with no breakage in existing screens.

### `ui-common` foundation audit

- **Color** — [`compose/Color.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Color.kt)
  and [`compose/Theme.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Theme.kt)
  already define the *full* M3 role set for light/dark, including the surface container ramp
  (`surfaceContainerLowest` → `surfaceContainerHighest`) and the theme-independent **Fixed** roles
  (`primaryFixed`/`primaryFixedDim`/etc. — explicitly commented as Expressive roles). This part is already
  expressive-ready; the gap is that screens barely touch secondary/tertiary or the fixed roles today (see
  §3).
- **Typography** — [`compose/Type.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Type.kt)
  is just `internal val AppTypography = Typography()` — the stock baseline scale, no custom sizes, no
  emphasized styles wired up at all. This is the single biggest gap: there is currently no way for a screen
  to opt into an emphasized style even if it wanted to.
- **Shape** — `Theme.kt`'s `MaterialTheme(...)` call never passes a `shapes` parameter, so the app runs on
  default M3 `Shapes()`. There's no `Shape.kt` / shared shape tokens file in `ui-common` at all. Corner
  radii are hardcoded ad hoc per call site instead: `12.dp` for header tiles
  ([`ChronologicalHomeScreen.kt:288`](../feature/feature-home/src/main/kotlin/com/github/naz013/feature/home/ChronologicalHomeScreen.kt)),
  `16.dp` default in [`SplitButton.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/SplitButton.kt),
  `28.dp` in [`SearchBar.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/component/SearchBar.kt),
  `MaterialTheme.shapes.medium` in the home event card. None of this is wrong, but it's undocumented tribal
  knowledge rather than a shared scale, and there's no shape-morph usage anywhere.
- **Motion** — durations are literal constants scattered per file (`BANNER_ANIMATION_DURATION_MS = 300` in
  `HomeScreen.kt`, `TILE_ANIMATION_DURATION_MS = 250` / `LIST_ITEM_ANIMATION_DURATION_MS = 250` in
  `ChronologicalHomeScreen.kt`), all `tween()`-based. `SearchBar.kt`'s clear-icon transition is the one spot
  already using a `spring()` (`DampingRatioMediumBouncy`). No shared `MotionScheme` or spring tokens exist.
- **Components** — `ui-common/compose/foundation/` already has a good base to extend rather than replace:
  `MenuIconButton`, `PrimaryIconButton`, `SplitButton`/`OutlinedSplitButton` (a hand-rolled version of what
  1.4.0 now ships natively), `SearchBar`, `AppDropdownMenu`, `BottomSheet`, dialogs. `DynamicScreen` /
  `DynamicParameter` / `DeviceScreenConfiguration` already give the app a working "adapt to context"
  mechanism (used today for the Home header grid's column count) — this maps directly onto the Expressive
  "component flexibility" tactic and doesn't need to be invented.

### `HomeScreen` (`ChronologicalHomeScreen`) audit

- Header greeting uses `headlineMedium` — a strong candidate for an emphasized style once available.
- Header navigation tiles ([`ChronologicalHomeScreen.kt:263-328`](../feature/feature-home/src/main/kotlin/com/github/naz013/feature/home/ChronologicalHomeScreen.kt))
  are the closest thing to a "hero" element on the screen (colored icon chip + count), but currently use a
  flat `12.dp` `RoundedCornerShape` and only `surfaceContainer`/`secondaryContainer` — no tertiary, no fixed
  roles, no shape variety versus the event cards below them.
  - **Note**: `HeaderNavigationItem.color` ([`HomeScreenState.kt:18-24`](../feature/feature-home/src/main/kotlin/com/github/naz013/feature/home/HomeScreenState.kt))
    is populated but the tile composable ignores it entirely — worth deciding whether that field should
    drive per-tile color (closer to tactic #2, "rich and nuanced colors") or should be removed as dead data.
  - Add button (`AddButton`) is a plain `MenuIconButton` + dropdown — a natural fit for the new **FAB menu**
    component once adopted, especially since it already toggles a 3–4 item action menu.
- Event rows use one shape/elevation treatment for every entry regardless of type (reminder vs. birthday
  vs. overdue) — tactic #1 ("vary shape/style to draw attention") isn't applied; overdue items in particular
  have no visual distinction beyond text content today.
- Stagger-in animations (`TILE_STAGGER_DELAY_MS`, `LIST_ITEM_STAGGER_DELAY_MS`) are a reasonable existing
  "fluid motion" gesture, but hand-tuned per screen rather than driven by a shared spring scheme — a good
  candidate to migrate onto `MaterialTheme.motionScheme` once that's wired up, so tuning happens once in
  `ui-common` instead of per screen.
- Banners in `HomeScreen.kt` (Privacy/Login/WhatsNew) are functionally identical `ElevatedCard`s — fine as
  is, low priority for expressive treatment.

### `AgendaScreen` audit

- Top bar, search bar, filter chips, and bottom sheet are all stock M3 components used correctly, but with
  no emphasis differentiation: selected `FilterChip`s, the active filter `Badge`, and section headers
  (`titleMedium`) are exactly where the spec calls out emphasized type as the recommended pattern (selected
  chips/menu items, badges) and are not using it.
- `AgendaList` renders headers and rows with uniform `8.dp` spacing and no shape distinction between a
  `UiAgendaReminder` and `UiAgendaBirthday` row — same gap as Home: type is conveyed only through icon/text,
  not through shape or color-role variety.
- Empty state (`AgendaEmptyState`) and the equivalent on Home (`EmptyEventsState`) are near-duplicates that
  belong in `ui-common` as a shared composable regardless of the Expressive work — worth doing opportunistically
  during this migration since both screens will already be touched.

## 3. `ui-common` foundation work — landed

Done once, in `ui-common`, before touching either screen — so Home and Agenda don't end up with two
divergent hand-rolled interpretations of "expressive." All items below shipped together with the 1.5.0-alpha27
bump from §2, verified via `./gradlew :app:assembleProDebug` and a visual check on-device (light + dark).

1. **Typography — unblocked by the version bump, no `ui-common` code change needed.** `Type.kt`'s
   `internal val AppTypography = Typography()` already worked correctly once `1.5.0-alpha27` landed —
   the public no-arg `Typography()` constructor now defaults every `xxxEmphasized` field to the real
   emphasized token value (see §2, Correction #2), so `MaterialTheme.typography.headlineMediumEmphasized`
   etc. is usable from any screen today with zero further wiring. Per-screen adoption (swapping the ad hoc
   `fontWeight = FontWeight.Bold`/`.Medium` overrides for real emphasized styles) is Phase 2/3 work, not
   done yet.
2. **Shape** — added [`Shape.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Shape.kt)
   with an `AppShapes` object (`tile` = 12.dp, `card` = 16.dp, `pill` = 28.dp — the exact values already
   hardcoded ad hoc at the call sites listed in §2), so those call sites have a shared token to migrate onto
   as they're touched in Phase 2/3, rather than a big sweep now. Deliberately **does not** use
   `MaterialShapes`' polygon shapes / shape morphing — that's explicitly out of scope for Phases 1–3 (see
   §5 Open questions), so wiring it into the foundation now would be unused code.
3. **Motion — no new `ui-common` file needed.** Originally planned as a custom spring-spec object, but
   once `MaterialTheme.motionScheme` is genuinely public (via the version bump + item 4 below), it *is*
   the shared source of truth — a hand-rolled wrapper around it would just be redundant indirection. Phase
   2/3 call sites replace their local `tween()` duration constants (`TILE_ANIMATION_DURATION_MS`,
   `BANNER_ANIMATION_DURATION_MS`, etc.) with `MaterialTheme.motionScheme.defaultSpatialSpec()`/
   `fastEffectsSpec()`/etc. directly, once each screen is touched.
4. **Theme entry point** — [`Theme.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Theme.kt)'s
   `AppTheme` now calls `MaterialExpressiveTheme(colorScheme, typography = AppTypography, content)` instead
   of `MaterialTheme(...)`. Switched app-wide in one step (see §5 — `app` is the only module that wires
   `AppTheme`, so a staged dual-theme period wouldn't reduce risk, just add a mode to maintain). `shapes`
   and `motionScheme` are left at their `MaterialExpressiveTheme` defaults (`Shapes()` /
   `MotionScheme.expressive()`) rather than overridden, since no screen depends on non-default values yet.
5. **Shared empty state** — added [`EmptyState.kt`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/component/EmptyState.kt)
   (`icon: Painter`, `message: String`) and pointed `ChronologicalHomeScreen.kt`'s `ListState.Empty` branch
   and `AgendaScreen.kt`'s `ListState.Empty` branch at it, removing both private `EmptyEventsState` /
   `AgendaEmptyState` composables they previously duplicated.

## 4. Screen-by-screen plan (Home + Events)

Sequence: land the `ui-common` foundation (§3) first behind no visible change, then apply tactics to each
screen incrementally so every step stays reviewable and shippable on its own.

**Home (`ChronologicalHomeScreen`) — landed**, verified via `./gradlew :feature:feature-home:testDebugUnitTest`
(36 tests, including 2 new ones for the overdue computation below) + `:app:assembleProDebug` + on-device
screenshots in light and dark.
- Greeting → `headlineMediumEmphasized` (kept at Medium rather than Large — this screen's one clear "hero"
  moment per tactic #7, no reason to also bump the size).
- Header navigation tiles → shape now `AppShapes.tile` (was a literal `RoundedCornerShape(12.dp)`, same
  value, now a shared token). `HeaderNavigationItem.color` question resolved: the icon chip background is
  now `item.color.copy(alpha = 0.16f)` (tonal) with the icon tinted the raw `item.color`, replacing the
  flat `secondaryContainer` for every tile. Title/subtitle text swapped from `labelSmall`/`titleMedium` +
  manual `FontWeight.Bold`/`.Medium` overrides to real `labelSmallEmphasized`/`titleMediumEmphasized`.
  **Caveat found while wiring this up, since fixed** (see §5): `GetNavigationItemsUseCase.kt` originally
  hardcoded `color = Color.Green` for every section, so tiles briefly all rendered the same green tint
  before real per-section colors landed.
- Event rows (`EventCard`) → shape now `AppShapes.card` (16dp, distinct from the tile's 12dp — this is the
  "vary shape vs. the tiles" gap the audit flagged). Color priority is now `isSelected` (`primaryContainer`,
  unchanged) → `isOverdue` (`errorContainer`/`onErrorContainer`, new) → `type == Birthday`
  (`tertiaryContainer`/`onTertiaryContainer`, new) → default. `HomeEvent.isOverdue` is a new field, computed
  in `GetActiveEventsForTheDayUseCase.toHomeEvent(reminder, group)` as `!dueDateTime.isAfter(now)` — the
  same comparison `ModelDateTimeFormatter.getRemaining` already uses internally to decide when to show the
  "Overdue" string, just exposed as a boolean instead of re-parsing that string in the UI layer. Deliberately
  did **not** also give overdue/birthday rows a distinct *shape* from upcoming rows (only color) — three
  shapes in one dense list read as noisy against tactic #7 ("reserve 1-2 hero moments... rather than making
  everything loud"); shape variety stays a Home-vs-tile distinction, not a per-row one.
- `EventCard`'s main text and `groupName` swapped from `bodyMedium`/`bodySmall` + manual `FontWeight.Medium`
  to `bodyMediumEmphasized`/`bodySmallEmphasized`. `TimeSectionRow`'s time label did the same
  (`bodyMediumEmphasized`).
- Stagger animations → `TILE_ANIMATION_DURATION_MS`/`LIST_ITEM_ANIMATION_DURATION_MS` `tween()` constants
  replaced by `MaterialTheme.motionScheme.defaultSpatialSpec()`/`defaultEffectsSpec()` at each
  `AnimatedVisibility` call site (spatial for `scaleIn`/`slideInVertically`, effects for `fadeIn`). The
  per-item stagger *delay* choreography (`TILE_STAGGER_DELAY_MS` etc.) is unchanged — that's a content
  sequencing decision, not something `MotionScheme` models.
- Also discovered `HomeEvent.color` (group/birthday color, separate field from `HeaderNavigationItem.color`)
  is similarly computed but never read by `EventCard` — **not** wired up in this pass, since the plan didn't
  call for it and stacking it on top of the new overdue/birthday container colors risks two conflicting
  color signals on the same card. Flagged for a future decision, not decided here.
- Add button (FAB menu) and shape morphing → still out of scope for this pass (§5).

**Agenda (`AgendaScreen`) — landed**, verified via `./gradlew :ui:ui-agenda:testDebugUnitTest` +
`:feature:feature-agenda:testDebugUnitTest` (including 3 new tests for the overdue computation below) +
`:app:assembleProDebug` + on-device screenshots in light and dark.
- Section headers (`UiAgendaHeader` text in `AgendaList`) → `titleMediumEmphasized`.
- Selected filter chips (`CategoryChipRow`/`SmartListChipRow`/`TagFilterChipRow`/`GroupFilterChipRow`, all
  four backing the filter bottom sheet) → new shared `FilterChipLabel` composable applies
  `labelLargeEmphasized` when `selected`, otherwise the stock `labelLarge` FilterChip already used — matches
  the spec's own "selected chips" emphasis guidance without touching unselected chips' appearance.
- Active-filter `Badge` → no typography to change (it's an unlabeled dot indicator), left as is.
- Event rows → **not done directly in `ReminderAgendaRow`/`BirthdayAgendaRow`** as originally scoped — both
  are thin wrappers around a shared `AgendaListItem` composable in `ui-common` (also used by Groups and
  Reminders Archive, per its own docstring), so the actual work landed there instead: added an
  `isOverdue: Boolean` param that drives `errorContainer`/default container color exactly like Home's
  `EventCard`, and migrated its shape from `MaterialTheme.shapes.medium` (12dp) to `AppShapes.card` (16dp) —
  matching Home's event-card shape, since the two were previously inconsistent (Home's tile was 12dp, Home's
  card 16dp, but Agenda's card was still the old 12dp default). **Side effect**: since `AgendaListItem` is
  shared, Groups' `GroupReminderRow` and Reminders Archive's `ArchiveReminderRow` also picked up the 16dp
  shape (cosmetic, `isOverdue` defaults `false` so no color change for them) — not a redesign of those
  screens, just a consequence of centralizing the token.
  - `ReminderAgendaRow` passes a new `UiAgendaReminder.isOverdue` through, computed in
    `UiAgendaItemAdapter.toUiAgendaReminderV2` the same way Home computes it (`state.isActive &&
    !dueDateTime.isAfter(now)`) — gated on `isActive` so a disabled reminder that still displays a stale
    "Overdue" text badge (pre-existing behavior in `UiReminderCommonAdapter.getRemainingV2`, unrelated to
    this change) doesn't also get the red highlight; verified this exact case on-device (a disabled
    reminder tagged "Overdue" stayed the default color, an active overdue one turned red).
  - `BirthdayAgendaRow` → deliberately **not** given the same overdue/birthday container-color treatment.
    Unlike Home (which had no existing birthday signal), Agenda's birthday rows already show a distinct
    per-birthday colored dot (`item.color`, genuinely varied — no `Color.Green`-style stub here); stacking a
    second color signal (card background) on top would be redundant per tactic #7.
- Filter bottom sheet → no change; already uses `AppModalBottomSheet` + (now-emphasized-when-selected) stock
  chips correctly.

## 5. Open questions

- Should `MaterialExpressiveTheme` be adopted app-wide in one PR, or should `ui-common` support both and let
  screens opt in gradually? Given `app` is the only module wiring DI/theme today, an app-wide switch is
  probably lower-risk than a dual-theme period, but it means Home + Events would ship alongside whatever
  else renders through `AppTheme` at the same time — worth confirming with whoever owns rollout risk here.
- `HeaderNavigationItem.color` — **fully resolved and landed** (see §4 Home for the UI wiring). The
  `Color.Green`-for-everything stub in `GetNavigationItemsUseCase.kt` is fixed too: rather than inventing new
  hex values, each of the 9 sections now gets a distinct entry from `ThemeProvider.AppColorIndex` via
  `ThemeProvider.themedColor(context, code)` — the same theme-adaptive (light/dark aware) color system
  already used for Group and Birthday colors elsewhere in the app (`colorBirthdayCalendar()`, group color
  pickers), so this isn't a new, disconnected color scheme. Mapping: Calendar → BLUE, Agenda → DEEP_PURPLE,
  Notes → AMBER, Birthdays → PINK, Google Tasks → GREEN, Workflow → INDIGO, Groups → TEAL, Tags → CYAN,
  Routines → ORANGE. Required adding `ContextProvider` to `GetNavigationItemsUseCase`'s constructor
  (auto-resolved by Koin's `factoryOf`, no `KoinModule.kt` change needed). Verified on-device in light and
  dark — each tile now reads as genuinely distinct rather than a uniform tint.
  **The exact hue-per-section pairing is a product/taste call** — changing it is a one-line edit per section
  in `GetNavigationItemsUseCase.kt` (`sectionColor(AppColorIndex.X)`), not a structural change.
- `HomeEvent.color` (group/birthday color) — same "computed but unread" shape as `HeaderNavigationItem.color`
  was, discovered while landing the Home overdue/birthday work above. Not wired up — open question whether
  it should also drive some part of `EventCard`'s appearance without conflicting with the new
  overdue/birthday container-color logic, or be considered dead data.
- No decision yet on how far to take shape morphing (loading indicators, FAB menu open/close) versus just
  adopting the static shape scale — morphing is the highest-effort, highest-novelty piece of Expressive and
  probably shouldn't block the first pass on Home/Events.

## 6. Reminders screens — audit

Audit pass over all 10 "Reminders" rows in `m3-expressive-screen-inventory.md`, using the checklist in
[`m3-expressive-guidelines.md`](m3-expressive-guidelines.md) §10. **Audit only — no code changed in this
pass.** Files read in full: `BuildReminderScreen.kt`, `ReminderHelpScreen.kt`, `RecurHelpScreen.kt`,
`SelectApplicationScreen.kt`, `MapEditorScreen.kt`, `PreviewReminderScreen.kt`,
`ReminderFullscreenMapScreen.kt`, `RemindersArchiveScreen.kt`, `ReminderActionScreen.kt`,
`TodoEditScreen.kt`, plus `SubTasksValueEditor.kt` (shared editor used by both `BuildReminderScreen` and
`TodoEditScreen`, and already mid-edit on this branch).

### Cross-cutting patterns (found on 2+ screens — fix once, verify everywhere it repeats)

1. **Back-button `contentDescription = null`** — `SelectApplicationScreen.kt:72`,
   `PreviewReminderScreen.kt:112` (the non-detail-pane branch only), `RemindersArchiveScreen.kt:146`,
   `TodoEditScreen.kt:70` all pass `null` for the leading `MenuIconButton`'s content description. This
   isn't a style gap, it's a real accessibility defect (guidelines §8) — and it's inconsistent within the
   same module: `BuildReminderScreen.kt`, `ReminderHelpScreen.kt`, `RecurHelpScreen.kt`, and
   `MapEditorScreen.kt` all correctly pass `stringResource(R.string.cd_back)` for the same control. 4 of
   10 screens have the bug, 6 don't — a one-line fix per file.
2. **Off-scale hardcoded corner radius `RoundedCornerShape(24.dp)`** — `BuildReminderScreen.kt:566`
   (`QuickStartButton`) and `TodoEditScreen.kt:148` (the "more options" `FilledTonalButton`). 24dp isn't
   one of the 10 official shape-scale steps (guidelines §4.1) — the nearest are 20dp (large increased) or
   28dp (extra large). `ui-common`'s `AppShapes.pill` is already 28dp and would fix both call sites with
   one shared token instead of two independent literals.
3. **Manual `FontWeight` overrides instead of emphasized type tokens** — heaviest in
   `ReminderActionScreen.kt` (`SimpleHeaderContent`, `ContactHeaderContent`, `EmailHeaderContent`,
   `AppHeaderContent`, `ActionsSection`'s main button — 6+ call sites use `FontWeight.Bold/SemiBold/Medium`
   on baseline styles). This is exactly the "weight" trigger guidelines §3.1 calls out for swapping to a
   real `xxxEmphasized` token instead. The alarm/ringing screen is arguably this app's single strongest
   "hero moment" candidate (it's the loudest, most attention-grabbing screen by design), so it's a natural
   first place to try real emphasized styles once `MaterialTheme.typography.xxxEmphasized` is adopted
   per-screen (still Phase 2/3 work per §3 above).
4. **Ad hoc `.copy(alpha = 0.3f/0.5f/0.6f/0.7f)` for de-emphasis instead of `onSurfaceVariant`** —
   `RemindersArchiveScreen.kt`'s empty state (`ArchiveEmptyState`), `ReminderActionScreen.kt` (alpha-blends
   `onSurfaceVariant` even further, e.g. `contactInfo` text at `.copy(alpha = 0.7f)`, subject line at
   `0.6f`). Material already ships `onSurfaceVariant` as the token for exactly this "lower-emphasis text on
   a surface" case (guidelines §2.2) — hand-blended alpha won't track future contrast-level changes
   (§2.4's three-contrast-level system) the way the role reference would. Other rows in the same group
   (e.g. `PreviewReminderScreen.kt`'s `SubTaskRow`) already do this correctly with the role directly, so
   the fix is "match the sibling row," not a new pattern.
5. **`TopAppBar` `containerColor` sourced inconsistently** — `BuildReminderScreen.kt`,
   `ReminderHelpScreen.kt`, `RecurHelpScreen.kt`, and `TodoEditScreen.kt` all use the shared
   `TopAppbarColor` token; `SelectApplicationScreen.kt` and `PreviewReminderScreen.kt` instead call
   `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` directly,
   and `RemindersArchiveScreen.kt` uses `Color.Transparent` (relying on a wrapping `Surface` for the real
   color). All three end up visually similar today only because `background` happens to be unstyled, but
   the token bypass means a future palette/token change to `TopAppbarColor` silently won't reach 3 of 7
   app-bar-having screens in this group.
6. **Literal `tween()` instead of `MaterialTheme.motionScheme` spring specs** — `MapEditorScreen.kt`'s
   sheet drag/dismiss/settle animations (`tween(200)`, twice) and `SubTasksValueEditor.kt`'s check/uncheck
   scale+fade (`tween(CHECK_ANIMATION_MS)`, 4 call sites). Consistent with the rest of the app not having
   adopted `motionScheme` on a per-screen basis yet (§3 above) — not urgent to fix in isolation, but these
   are concrete candidates once that phase starts (a fast spatial spring for the sheet drag, fast effects
   spring for the check/uncheck fade).

### Screen-specific findings

- **`ReminderFullscreenMapScreen.kt`** — uses `ExtendedFloatingActionButton` (the baseline/56dp/pill-shaped
  variant), which guidelines §9.1 flags as **no longer recommended** — replace with the small extended
  FAB. This is the one clear deprecated-component hit in the whole Reminders group.
- **`RemindersArchiveScreen.kt`** — manually reintroduces an M2-style drop-shadow-on-scroll app bar
  (`Surface(shadowElevation = animateDpAsState(...))`) instead of the M3 color-fill scroll behavior. The
  correct pattern already exists in this same module — `ReminderHelpScreen.kt` uses
  `TopAppBarDefaults.enterAlwaysScrollBehavior()` — so this is a "match your sibling file" fix, not new
  research.
- **`ReminderActionScreen.kt`** — `CardDefaults.cardElevation(defaultElevation = 2.dp)` on both header and
  todo-list cards: 2dp isn't one of the defined M3 elevation levels (0/1/3/6/8/12dp, guidelines §5) — it
  sits off-scale between the "filled card" default (0dp) and "elevated card" default (1dp). Also worth
  noting positively: this screen's portrait/landscape adaptive layout switch
  (`ReminderActionScreenPortrait`/`...Landscape` via `deviceScreenConfiguration()`) is a genuine adaptive
  layout, and its snooze bottom sheet correctly reuses the real `AppModalBottomSheet` + `BottomSheetList`
  components. The one landscape/portrait split is orientation-based rather than the five official
  breakpoints, so it won't necessarily do the right thing on a tablet in portrait (which is `Medium`, not
  `MobileLandscape`) — worth a closer look if tablet support for this screen matters.
- **`MapEditorScreen.kt`** — the hand-rolled bottom-sheet clone (documented reason: needs to keep the
  embedded map in the same composition, not a separate `Popup`) has no max-width cap, so on
  Expanded/Large/XL breakpoints it stretches edge-to-edge — M3 bottom sheets are specced with a 640dp
  max-width (guidelines §9.5). Its scrim also hardcodes `Color.Black.copy(alpha = ...)` rather than
  `MaterialTheme.colorScheme.scrim`. Its 28dp top-corner radius is spec-correct (bottom sheets are
  specced at exactly 28dp) even though it's a literal rather than a token.
- **`SelectApplicationScreen.kt`** and **`PreviewReminderScreen.kt`** — both build list rows by hand
  (`Card` + `Row` + `Icon` + `Text`) rather than using a `ListItem`-shaped pattern; `PreviewReminderScreen`
  additionally mixes `surfaceContainerLow`/`surfaceContainer`/plain default `surface` across its various
  `Card` sections (`HeaderCard`, `DetailsCard`, `SubTasksSection` use explicit surface-container roles;
  `NoteRow`/`GoogleTaskRow`/`CalendarEventRow`/`MapSection` don't, defaulting to plain `surface`) without
  an obvious rule for which container level each section should get. Lower priority than the items above —
  worth a pass once the group's higher-value fixes land, not before.
- **`SubTasksValueEditor.kt`** — otherwise the strongest accessibility implementation in this group
  (per-row custom `CustomAccessibilityAction`s exposing a TalkBack-reachable equivalent for the
  drag-to-reorder gesture, merged semantics content descriptions, haptic feedback, test tags) — worth
  treating as the reference pattern when fixing the back-button bug elsewhere in this group. One real gap:
  its check/remove `IconButton`s are explicitly sized to 40dp (`Modifier.size(40.dp)`), below the 48×48dp
  minimum touch target guidelines §8 calls out — a deliberate density trade-off for a compact checklist
  row, but worth a second look since §1.4 says not to drop below 48dp by default.
- **`TodoEditScreen.kt`** — duplicates `BuildReminderScreen.kt`'s private `OfflineOnlyRow` composable
  verbatim (same layout, same `MaterialTheme.colorScheme.onBackground` tint choice) rather than sharing
  one; worth deduplicating into `ui-common` regardless of any M3 fix, since any future shape/color/type
  change to that row would otherwise need to land in two places. On the positive side, `TodoSectionHeader`
  deliberately colors its label with `MaterialTheme.colorScheme.tertiary` — a correct, deliberate use of an
  accent color role for a smaller structural element (guidelines §2.2's tertiary-role guidance), not a gap.
- **`ReminderHelpScreen.kt`** and **`RecurHelpScreen.kt`** — thin `Scaffold` + `TopAppBar` wrappers around
  a `WebView` loading a bundled HTML asset. Little surface area for an M3 compliance issue at the Compose
  layer (both correctly use the shared `TopAppbarColor` token and proper back-button content descriptions)
  — any further improvement would mean restyling the HTML/CSS assets themselves
  (`how_to_create_a_reminder.html`, `doc_rfc_5545.html`), which is out of scope for a Compose-focused pass
  unless requested separately.

### Suggested fix order

Cheapest-and-highest-value first: (1) the 4 missing back-button content descriptions — mechanical,
zero design judgment; (2) the two off-scale `RoundedCornerShape(24.dp)` call sites → `AppShapes.pill`; (3)
`RemindersArchiveScreen`'s scroll-shadow app bar → copy `ReminderHelpScreen`'s scroll-behavior pattern; (4)
`ReminderFullscreenMapScreen`'s deprecated `ExtendedFloatingActionButton` → small extended FAB. Items 3–6
in the cross-cutting list (alpha-blended de-emphasis, app-bar color token drift, `tween()` vs.
`motionScheme`, off-scale card elevation) are more naturally folded into whichever future PR next touches
each screen for other reasons, per this doc's existing "touch it once, fix it right" approach on Home/
Agenda — no need for a dedicated sweep.

## 7. Workflow & Routines screens — audit

Audit pass over the 8 Workflow/Routines screens, using the checklist in
[`m3-expressive-guidelines.md`](m3-expressive-guidelines.md) §10. **Audit only — no code changed in this
pass.** Files read in full: `WorkflowGalleryScreen.kt`, `WorkflowRulesForGroupScreen.kt`,
`WorkflowRulesForReminderScreen.kt`, `builder/WorkflowRuleBuilderScreen.kt`, `RoutinesListScreen.kt`,
`RoutineEditScreen.kt`, `preview/RoutinePreviewScreen.kt`, `execution/RoutineExecutionScreen.kt`, plus the
shared composables these screens pull in — `WorkflowRuleRow.kt`, `WorkflowTemplateCard.kt`, and (partially,
for the bottom-sheet pattern check) `builder/WorkflowRuleBuilderPickers.kt`.

### Cross-cutting patterns (found on 2+ screens — fix once, verify everywhere it repeats)

1. **Back-button `contentDescription = null`** — every single back-arrow instance across both feature
   modules has this bug, with no exceptions: `WorkflowGalleryScreen.kt:50`,
   `WorkflowRulesForGroupScreen.kt:53-57` (`renderAsDetailPane == false` branch),
   `WorkflowRulesForReminderScreen.kt:53-57` (same), `builder/WorkflowRuleBuilderScreen.kt:78-82` (same),
   `RoutinesListScreen.kt:61`, `RoutineEditScreen.kt:120` (same branch pattern),
   `preview/RoutinePreviewScreen.kt:90` (same), `execution/RoutineExecutionScreen.kt:62`. That's 8 of 8
   screens. Every one of the four `renderAsDetailPane`-aware screens gets the *close* icon's content
   description right (`stringResource(...acc_close)`) but drops it for the *back* icon in the same
   conditional — same defect shape the Reminders audit found (§6, "4 of 10 screens"), except here it's
   total: 0 of 8 screens pass a real `stringResource(...cd_back)` for the back arrow. Mechanical one-line
   fix per file, `cd_back` already exists as a string resource (`ui-common/src/main/res/values/strings.xml:933`).
2. **`TopAppBar` `containerColor` sourced inconsistently, including within a single feature module** —
   `WorkflowGalleryScreen.kt:62`, `WorkflowRulesForGroupScreen.kt:61`,
   `WorkflowRulesForReminderScreen.kt:61`, `builder/WorkflowRuleBuilderScreen.kt:86`,
   `RoutinesListScreen.kt:74`, and `preview/RoutinePreviewScreen.kt:109` all call
   `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` directly,
   while `RoutineEditScreen.kt:134` and `execution/RoutineExecutionScreen.kt:66` use the shared
   `TopAppbarColor` token from `ui-common`. This isn't purely cosmetic even though `containerColor` matches
   either way: `TopAppbarColor` (`ui-common/compose/ComponentColors.kt:28-33`) also sets
   `titleContentColor = MaterialTheme.colorScheme.onBackground`, which the six raw-`topAppBarColors(...)`
   call sites don't — they fall back to `TopAppBarDefaults`'s own default title color (`onSurface`), a
   different token from what the "correct" token-using screens get. Same finding shape as the Reminders
   audit's pattern #5: a future palette change to `TopAppbarColor` silently won't reach 6 of these 8
   screens, and two screens in the *same* `feature-routine` module (`RoutinesListScreen`/
   `RoutinePreviewScreen` vs. `RoutineEditScreen`/`RoutineExecutionScreen`) already disagree on title-text
   color sourcing today.
3. **Manual `FontWeight.Bold` overrides on baseline styles instead of emphasized type tokens** — all four
   instances are concentrated in `execution/RoutineExecutionScreen.kt`: the "Complete step" button label
   (`:82`, `titleMedium`), the "Step X of N" counter (`:144`, `labelLarge`), the step title (`:150`,
   `headlineSmall`), and the finished-state headline (`:229`, `headlineSmall`). This is exactly the
   "weight" trigger guidelines §3.1 calls out for swapping to a real `xxxEmphasized` token, and it's the
   same shape of finding as `ReminderActionScreen.kt` in the Reminders audit (also a step-by-step/alarm-style
   screen with 6+ `FontWeight` overrides) — Routine Execution is this group's equivalent "loudest screen"
   candidate for a first emphasized-type trial once that's adopted per-screen. No `FontWeight` overrides
   found anywhere in `feature-workflow`.
4. **Literal `tween()` instead of `MaterialTheme.motionScheme` spring specs** —
   `preview/RoutinePreviewScreen.kt`'s check/uncheck scale+fade (`tween(CHECK_ANIMATION_MS)`, 4 call sites
   at `:201-202` and `:212-213`) is the same pattern already flagged once for `SubTasksValueEditor.kt` in
   the Reminders audit. Noting once here per that audit's guidance not to over-flag a whole-app gap — not
   urgent in isolation, a natural fix alongside item 3 above since both touch the same file area.

### Screen-specific findings

- **`preview/RoutinePreviewScreen.kt`** — uses `ExtendedFloatingActionButton` (`:77`, no color override, so
  the baseline/56dp/pill-shaped default), which guidelines §9.1 flags as **no longer recommended** —
  replace with the small extended FAB. This is the one clear deprecated-component hit in this screen group,
  mirroring the exact same finding already made for `ReminderFullscreenMapScreen.kt` in the Reminders audit.
  Also: its check-toggle `IconButton` is explicitly sized to 40dp (`:196`, `Modifier.size(40.dp)`), below
  the 48×48dp minimum touch target (guidelines §8) — the same gap already flagged for
  `SubTasksValueEditor.kt`'s row-action buttons in the Reminders audit, and for the same reason (compact
  checklist-row density). On the positive side, the row correctly uses `onSurfaceVariant` for de-emphasized
  text/icons (completed-step title, timestamp, duration — `:229`, `:239`, `:245`) rather than hand-blended
  alpha, and its `RoutineBanner` correctly reads `state.contentColor`/`state.backgroundColor` as a paired
  container/on-container role rather than mismatching them.
- **`RoutinesListScreen.kt`** — `RoutinesEmptyState` (`:164-176`) hand-blends
  `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)` for the icon tint and `.copy(alpha = 0.5f)` for
  the message text, instead of using `onSurfaceVariant` (guidelines §2.2) the way sibling empty states
  elsewhere in the app already do — same "match your sibling" fix as the Reminders audit's pattern 4.
- **`execution/RoutineExecutionScreen.kt`** — beyond the `FontWeight` findings above, its bottom action bar
  (`Surface(shadowElevation = 4.dp)` wrapping the "Complete step" button, `:71`) is an M2-style
  drop-shadow-on-surface treatment rather than the M3 color-fill/tonal approach, and 4dp isn't one of the
  six defined elevation levels (0/1/3/6/8/12dp, guidelines §5) — the nearest "3" is 3dp/6dp, or 0dp if the
  intent was flat. Comparable in spirit to `RemindersArchiveScreen.kt`'s scroll-shadow app bar flagged in
  the Reminders audit, though this is a bottom bar rather than a top app bar so there's no existing
  in-module "correct" sibling to copy from directly. On the positive side, this screen correctly uses the
  `TopAppbarColor` token (see cross-cutting #2) and its `RunningContent`/`FinishedContent` split cleanly
  separates the two state branches without duplicating layout logic.
- **`RoutineEditScreen.kt`** — no deprecated components, no off-scale shape/elevation literals found; the
  step-reorder `IconButton`s (`:385-402`) rely on default `IconButton` sizing (48dp), correctly meeting the
  touch-target minimum unlike `RoutinePreviewScreen`'s checklist row. Worth a minor accessibility note: the
  "move up" control is implemented by rotating a chevron-down icon 180° (`:389`,
  `Modifier.graphicsLayer { rotationZ = 180f }`) rather than using a distinct up-chevron icon — functionally
  fine and correctly labeled via `contentDescription`, just a slightly indirect implementation. Positively,
  `SectionHeader` (`:344-352`) deliberately colors section labels with `MaterialTheme.colorScheme.tertiary`
  — the same correct, deliberate tertiary-accent usage already called out approvingly for
  `TodoEditScreen.kt`'s `TodoSectionHeader` in the Reminders audit.
- **`builder/WorkflowRuleBuilderScreen.kt`** — reuses the same `ui-common` builder-item-card pattern as
  `BuildReminderScreen.kt` (`BuilderListItemCard`/`BuilderItemStatus` for the trigger/condition/action rows,
  `:95-165`) rather than reinventing its own row styling, and its trigger/condition/action picker sheets
  (`WorkflowTriggerPickerSheet` etc. in `builder/WorkflowRuleBuilderPickers.kt`) correctly build on the
  shared `AppModalBottomSheet`/`BottomSheetHeader`/`BottomSheetItem`/`BottomSheetList` components rather
  than a hand-rolled sheet — both genuine positive-compliance findings, this screen is the closest thing to
  a fully "already follows the established pattern" screen in this group. One small inline gap: the "Add
  condition" `TextButton`'s leading `Icon` (`:133`) passes `contentDescription = null` — correct here since
  the button's own text label already conveys the action (icon is purely decorative next to visible text),
  not a defect.
- **`WorkflowGalleryScreen.kt`, `WorkflowRulesForGroupScreen.kt`, `WorkflowRulesForReminderScreen.kt`** —
  near-identical scaffolding (all three render the same rules-list-then-templates-list shape via the shared
  `WorkflowRuleRow`/`WorkflowTemplateCard` composables), so most findings are already captured in the
  cross-cutting section. `WorkflowRulesForGroupScreen.kt`/`WorkflowRulesForReminderScreen.kt` both use a
  plain `FloatingActionButton` (`:64-69` / `:64-73`) with no color override — this resolves to the default
  M3 primary-container FAB, which is correct and *not* the deprecated surface-color/small-FAB variant, worth
  noting as a non-issue given how easy it would be to assume otherwise. `WorkflowTemplateCard.kt`'s
  description text uses `titleSmall` (`:41`) for what is a secondary/longer-form description string rather
  than a `bodySmall`/`bodyMedium` role meant for reading — a minor type-role mismatch (guidelines §3.1: body
  roles for longer passages, title roles for short high-emphasis text) worth a look, though the text in
  practice is short (one sentence) so this is low-severity.

### Suggested fix order

Cheapest-and-highest-value first: (1) the 8 missing back-button content descriptions — mechanical, zero
design judgment, `cd_back` string already exists; (2) `RoutinePreviewScreen`'s deprecated
`ExtendedFloatingActionButton` → small extended FAB, matching the fix already planned for
`ReminderFullscreenMapScreen` in the Reminders audit; (3) `RoutinesListScreen`'s hand-blended alpha in
`RoutinesEmptyState` → `onSurfaceVariant`; (4) `RoutinePreviewScreen`'s 40dp check-toggle touch target →
48dp, alongside the same fix wherever `SubTasksValueEditor.kt` lands its own fix, since both are the same
compact-checklist-row trade-off; (5) `RoutineExecutionScreen`'s `FontWeight.Bold` cluster and off-scale
`shadowElevation = 4.dp` bottom bar — naturally fold into whichever future PR next touches this screen, per
the Reminders audit's existing "touch it once, fix it right" approach, rather than a dedicated sweep. The
`TopAppBar` color-token inconsistency (cross-cutting #2) is lower priority to fix in isolation since it's
currently a color-source drift rather than a visible bug, but is worth folding into item 5's PR since
`RoutineExecutionScreen` already gets it right and the fix is copy-paste from that file.

## 8. Notes & Birthdays screens — audit

Audit pass over all 9 "Notes"/"Birthdays" rows. Files read in full: `NotesScreen.kt`, `NotesNavGraph.kt`
(to confirm how `NotesArchiveEntry` reuses `NotesScreen`), `NoteEditScreen.kt`, `NoteEditFloatingBar.kt`,
`PreviewNoteScreen.kt`, `PreviewNoteReminderRow.kt`, `ImagePreviewScreen.kt`, `BirthdaysScreen.kt`,
`EditBirthdayScreen.kt`, `PreviewBirthdayScreen.kt`, `BirthdayActionScreen.kt`. **Audit only — no code
changed in this pass.**

### Cross-cutting patterns (found on 2+ screens)

1. **Back-button `contentDescription = null`** — `NotesScreen.kt:403` (`NotesTopBar`, shared by both Notes
   List and Notes Archive since Archive renders the same composable), `BirthdaysScreen.kt:362`
   (`BirthdaysTopBar`), `EditBirthdayScreen.kt:85`, and `PreviewBirthdayScreen.kt:99` all pass `null` for
   the leading `MenuIconButton`. The Birthday Editor/Preview cases are a more subtle version of the same
   bug: both screens correctly localize the description for the `renderAsDetailPane` **close** icon
   (`stringResource(R.string.acc_close)`) but fall through to `null` for the plain **back-arrow** icon in
   the same ternary —
   `contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else null`. Screens
   that get this right elsewhere in the same feature: `NoteEditScreen.kt:98`, `PreviewNoteScreen.kt:67`,
   and `ImagePreviewScreen.kt:58` all correctly pass `stringResource(R.string.cd_back)`. Same exact defect
   class already documented for the Reminders group (§6) — this is now a 4-file recurrence in this group
   alone, on top of the 4 already found in Reminders.

2. **`TopAppBar` never sources the shared `TopAppbarColor` token** — none of the 9 screens in this group
   use `ui-common`'s `TopAppbarColor` (`containerColor = background` + `titleContentColor = onBackground`,
   paired). Instead: `NotesScreen.kt:423-425` and `EditBirthdayScreen.kt:105` and
   `PreviewBirthdayScreen.kt:115` each hand-roll `TopAppBarDefaults.topAppBarColors(containerColor =
   MaterialTheme.colorScheme.background)` — same container value as the token, but missing the paired
   `titleContentColor`, so a future edit to the shared token silently won't reach these three call sites.
   `BirthdaysScreen.kt:383` uses `Color.Transparent` and relies on a wrapping `Surface(color =
   MaterialTheme.colorScheme.background, ...)` for the real fill — functionally fine but yet another
   distinct sourcing strategy. `NoteEditScreen.kt:125-130`, `PreviewNoteScreen.kt:134`, and
   `ImagePreviewScreen.kt:62` also use `Color.Transparent`, but there the rationale is different and
   legitimate: those three screens tint the whole app bar to the note's own custom
   background/content color (`state.background`/`state.content`), which a shared static token couldn't
   express anyway. Net effect: this group has the same "app-bar color sourcing inconsistency" already
   flagged for Reminders (§6, item 5), but worse — 0 of 9 screens here use the shared token at all, versus
   4 of 7 in the Reminders group.

3. **Ad hoc `.copy(alpha = 0.3f/0.5f/0.7f)` for de-emphasis instead of `onSurfaceVariant`** —
   `NotesScreen.kt:371,378` (`NotesEmptyState`) and `BirthdaysScreen.kt:312,317` (`BirthdaysEmptyState`)
   use identical `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)` / `0.5f` for their empty-state
   icon/caption — almost certainly copy-pasted from one to the other, so it's one fix applied twice.
   `BirthdayActionScreen.kt:361` alpha-blends `onSurfaceVariant.copy(alpha = 0.7f)` for its phone-number
   caption — the exact same construct already flagged for `ReminderActionScreen.kt`'s `contactInfo` text in
   the Reminders audit (same alarm/ringing screen family, same bug).

4. **Manual `FontWeight` overrides instead of emphasized type tokens** — `BirthdayActionScreen.kt`'s
   `ContactHeaderContent` (lines 319, 330, 340, 351 — `Bold`/`Medium`/`Medium`/`Normal`) and `ActionsSection`
   (line 403 — `SemiBold` on the main action button label) mirror almost line-for-line the pattern already
   documented as the Reminders group's heaviest instance, `ReminderActionScreen.kt`'s equivalent header/
   action content. Same guidance applies: this is the "weight" trigger for swapping to a real
   `xxxEmphasized` typography token once adopted per-screen. Lower-severity instance:
   `NotesScreen.kt:570` (`SelectableOptionRow`) uses `fontWeight = if (selected) FontWeight.SemiBold else
   FontWeight.Normal` — this one is at least state-driven (the "context" trigger — selected item in a
   popup list), which guidelines §3.1 calls the more defensible reason to reach for emphasized type, but
   it's still a manual weight swap rather than the token.

5. **Off-scale hardcoded corner radius `RoundedCornerShape(12.dp)`** — `BirthdayActionScreen.kt:233`
   (`BirthdayHeader`'s `Card`). 12dp itself *is* on the official 10-step scale ("medium"), and it exactly
   duplicates `ui-common`'s `AppShapes.tile` (`ui-common/compose/Shape.kt:12`, also `RoundedCornerShape(12.dp)`)
   — so this is a pure token-hygiene gap (guidelines §4.1: "a screen using a literal `RoundedCornerShape(Ndp)`
   instead of `AppShapes.*`... is a token-hygiene gap, even if the rendered radius happens to match the
   scale"), fixable by swapping the literal for `AppShapes.tile` with zero visual change.
   `NoteEditFloatingBar.kt:103` uses `RoundedCornerShape(percent = 50)` for its pill-shaped container —
   conceptually correct (fully rounded = the "full" step) but still a literal rather than a shared token.

6. **Off-scale `CardDefaults.cardElevation`** — `BirthdayActionScreen.kt:238` uses
   `defaultElevation = 2.dp`, an exact repeat of the `ReminderActionScreen.kt` header-card finding already
   in §6 (2dp sits off the defined 0/1/3/6/8/12dp scale). Same fix applies to both screens at once.
   `NoteEditFloatingBar.kt:105-106` separately sets both `shadowElevation = 4.dp` and
   `tonalElevation = 4.dp` on its floating-toolbar `Surface` — 4dp also isn't a resting-level value on the
   scale (nearest defined levels are 3dp/level 2 and 6dp/level 3).

7. **Literal `tween()`/hand-tuned `spring()` instead of `MaterialTheme.motionScheme`** —
   `NoteEditFloatingBar.kt` (`tween(FLOATING_BAR_ANIMATION_DURATION_MS / 2)`, plus two manually-tuned
   `spring(dampingRatio = ..., stiffness = ...)` calls), `PreviewNoteReminderRow.kt` (`tween(250)` twice),
   and `PreviewBirthdayScreen.kt` (`tween(DETAIL_ROW_ANIMATION_DURATION_MS)` twice). Consistent with the
   whole-app gap already noted once for Reminders — flagging once here too, not urgent in isolation per the
   guidelines' own instruction not to over-flag this.

### Screen-specific findings

- **`NotesScreen.kt`** — Beyond the cross-cutting items above: positively, this screen correctly reuses
  `ui-common`'s `SelectionOverlay`/`SelectionTopBar` for its long-press multiselect flow (per
  `docs/multiselect.md`'s documented pattern) rather than a bespoke implementation. `NotesArchiveEntry`
  (`NotesNavGraph.kt:227-282`) reuses the exact same `NotesScreen` composable as the list, just with
  `onArchiveClick`/`onSettingsClick`/`onAddClick` all passed as `null` to hide those actions — a clean
  single-composable reuse, so there's no separate "archive screen" to audit; whatever is fixed on
  `NotesScreen` fixes both rows in the inventory.
- **`BirthdaysScreen.kt`** — Manually reintroduces the same M2-style drop-shadow-on-scroll app bar already
  flagged for `RemindersArchiveScreen.kt` in the Reminders audit: `Surface(color = ...,
  shadowElevation = headerElevation)` at lines 68/92-95/113, animating a hand-tracked `LazyListState`
  scroll flag instead of using `TopAppBarDefaults.enterAlwaysScrollBehavior()`
  (`ReminderHelpScreen.kt`'s pattern, elsewhere in this same app). Separately, line 377 uses
  `Icons.Default.FilterList` from `androidx.compose.material.icons` — a bare Material-icons-library
  reference rather than going through `DrawableCatalog`/`AppIcons` as CLAUDE.md's icon convention requires;
  every other icon on this screen and its siblings correctly uses `AppIcons.*`/`R.drawable.ic_fluent_*` via
  the catalog, so this one is the outlier. On the positive side: `FilterChip` (lines 284-288) is a
  correctly-used, unmodified M3 component, and the filter bottom sheet correctly reuses
  `AppModalBottomSheet`/`BottomSheetHeader` from `ui-common` rather than a hand-rolled sheet.
- **`NoteEditScreen.kt`** / **`NoteEditFloatingBar.kt`** — The floating pill-shaped toolbar
  (`NoteEditFloatingBar`, a `Surface(shape = RoundedCornerShape(percent = 50), ...)` with a horizontally
  scrolling `Row` of icon buttons that expand into `CloudBubble` popovers) is functionally very close to
  the new M3 Expressive **floating toolbar** component (guidelines §9.6: "horizontal/vertical,
  standard/vibrant color, can pair with a FAB, can hold many controls") — worth evaluating the real
  component once it's stable in this repo's `material3` version, per the same "hand-rolled vs. now-public
  API" note already made for `ui-common`'s `SplitButton` in the Reminders audit. The back button and
  save/share/delete actions all correctly pass real `stringResource` content descriptions (no accessibility
  gap here). `TopAppBar`'s `Color.Transparent` container is a deliberate, correct choice given the screen
  tints its entire chrome to the note's own color (see cross-cutting #2) — not a bug, just a different
  reason than the other `Color.Transparent` users in this group.
- **`PreviewNoteScreen.kt`** — Correct back-button content description; no deprecated components. Its
  attached-reminder card (`PreviewNoteReminderRow.kt:90-93`) is worth calling out as the *positive*
  reference pattern for this group: `shape = MaterialTheme.shapes.extraSmall` (a real theme-shape token,
  not a literal) plus default (unset) card elevation — i.e. the spec-correct 0dp filled-card resting
  elevation. This is the opposite of `BirthdayActionScreen.kt`'s hand-rolled 12dp/2dp header card and would
  be a good model to point the fix at.
- **`ImagePreviewScreen.kt`** (Note Image Preview) — Cleanest screen in the group: correct back-button
  description, minimal chrome (a transparent `TopAppBar` with a "`x` of `y`" page counter over a
  `HorizontalPager`), no Cards/shapes/elevation to get wrong. Nothing to flag.
- **`EditBirthdayScreen.kt`** — Beyond the back/close content-description split noted in cross-cutting #1:
  positively, this screen is genuinely list-detail-pane aware — the `renderAsDetailPane` parameter swaps
  the leading icon between close (embedded two-pane detail) and back (pushed full-screen), matching
  guidelines §1.3's list-detail canonical layout guidance for back-button placement varying by
  presentation. Its two `Card`s (date/switch section, contact section) both correctly use
  `CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)` with no elevation
  override — spec-correct.
- **`PreviewBirthdayScreen.kt`** — Shares `EditBirthdayScreen`'s `renderAsDetailPane` close/back pattern
  (same partial content-description bug, cross-cutting #1) and the same adaptive-pane awareness (positive).
  Structurally this screen does **not** follow `PreviewReminderScreen`'s Card-based detail-section layout —
  worth noting explicitly since the two looked likely to share the pattern: instead it uses a flat column
  of `DetailRow`s directly on the screen background, no `Card`/`surfaceContainer` roles anywhere, so none
  of the "which container level does each section get" inconsistency flagged for `PreviewReminderScreen` in
  the Reminders audit applies here. The name row is a deliberate, well-placed single hero moment
  (`MaterialTheme.typography.headlineSmall` + `colorScheme.primary`, guidelines' "1-2 hero moments"
  principle) and the staggered `AnimatedDetailRow`/`AnimatedAvatar` entrance motion is a nice touch that
  correctly matches the app's existing `ChronologicalHomeScreen` stagger pattern (own doc comment cites it)
  — it just still uses literal `tween()` (cross-cutting #7) rather than `motionScheme`.
- **`BirthdayActionScreen.kt`** — This screen is structurally near-identical to `ReminderActionScreen.kt`
  (same `DeviceScreenConfiguration`-driven portrait/landscape split, same header-card shape, same
  `SplitButton` usage for the primary/secondary action group) and inherits that screen's exact findings:
  the 2dp off-scale card elevation and heavy `FontWeight` overrides (cross-cutting #4, #6). Its
  `SplitButton` usage (lines 411-455) is the same hand-rolled `ui-common` component already flagged in the
  Reminders audit as worth checking against the real M3 Expressive split-button API — no new finding, just
  a second, consistent use of the same component. Like `ReminderActionScreen`, its portrait/landscape split
  is orientation-based rather than breakpoint-based, so it won't necessarily do the right thing on a tablet
  in portrait (`Medium`, not `MobileLandscape`) — worth a closer look only if tablet support for this alarm
  screen matters.

### Suggested fix order

Cheapest-and-highest-value first: (1) the 4 missing/partial back-and-close-button content descriptions
(`NotesScreen.kt:403`, `BirthdaysScreen.kt:362`, `EditBirthdayScreen.kt:85`, `PreviewBirthdayScreen.kt:99`)
— mechanical, zero design judgment, and can land in the same PR as the equivalent Reminders-group fix; (2)
`BirthdayActionScreen.kt:238`'s off-scale `defaultElevation = 2.dp` and line 233's
`RoundedCornerShape(12.dp)` → `AppShapes.tile` — both fixable alongside the already-known
`ReminderActionScreen.kt` twin so the two alarm screens stay visually consistent; (3)
`BirthdaysScreen.kt`'s scroll-shadow app bar → copy `ReminderHelpScreen`'s
`TopAppBarDefaults.enterAlwaysScrollBehavior()` pattern, same fix already queued for
`RemindersArchiveScreen`; (4) `BirthdaysScreen.kt:377`'s bare `Icons.Default.FilterList` → a cataloged
`AppIcons`/`DrawableCatalog` entry, one-line convention fix. Cross-cutting items 2–4 and 7 (app-bar color
token drift, alpha-blended de-emphasis, manual `FontWeight` vs. emphasized type, `tween()` vs.
`motionScheme`) are, as with the Reminders group, more naturally folded into whichever future PR next
touches each screen for other reasons rather than a dedicated sweep.

## 9. Groups / Tags / Places screens — audit

Audit pass over all 8 Groups/Tags/Places rows in the screen inventory. **Audit only — no code changed in
this pass.** Files read in full: `GroupsScreen.kt`, `GroupListItem.kt`, `GroupDetailsScreen.kt`,
`GroupReminderRow.kt`, `EditGroupScreen.kt` (module `feature:feature-group`); `TagsScreen.kt`,
`TagEditScreen.kt`, `TagDetailsScreen.kt`, `TagDetailRows.kt` (module `feature:feature-tags`);
`PlacesScreen.kt`, `PlaceListItemCard.kt`, `EditPlaceScreen.kt` (module `feature:feature-places`); plus the
shared `ColorPickerCard.kt` and `ColorSlider.kt` (`ui-common`) that back the Group/Tag color pickers.

### Cross-cutting patterns (found on 2+ screens)

1. **Back-button (and one save-button) `contentDescription = null`** — every one of the 8 screens has this
   bug, making it the most widespread instance of this pattern found in any audit group so far (the
   Reminders group audit found it on 4/10). Two shapes of the bug:
   - Screens with a `renderAsDetailPane` toggle (close vs. back icon) pass `null` unconditionally for the
     back-icon branch and only supply a real string for the close-icon branch:
     `GroupDetailsScreen.kt:81`, `EditGroupScreen.kt:82`, `TagEditScreen.kt:47`, `TagDetailsScreen.kt:79`,
     `EditPlaceScreen.kt:53`.
   - Top-level list screens with no detail-pane branch pass `null` outright for their sole back icon:
     `GroupsScreen.kt:73`, `TagsScreen.kt:86`, `PlacesScreen.kt:53`.
   - **`TagEditScreen.kt:61`** has a second, distinct instance: the save action itself is an icon-only
     `MenuIconButton(icon = AppIcons.Fluent.Checkmark, contentDescription = null, ...)` with no fallback
     text label at all. This is a real accessibility defect, not just the detail-pane pattern — a screen
     reader has literally no way to identify this control. It's also inconsistent with its two sibling
     editors: `EditGroupScreen.kt:96-100` and `EditPlaceScreen.kt:58-62` both use a self-labeling
     `MenuTextButton(text = stringResource(R.string.save), ...)` for the identical action, so `TagEditScreen`
     is the outlier both in component choice and in missing the description.
2. **`TopAppBar` `containerColor` never sourced from the shared `TopAppbarColor` token** — unlike the
   Reminders group (where 4/7 screens used the token and 3 bypassed it), here **all 8 screens** identically
   call `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` directly:
   `GroupsScreen.kt:85`, `GroupDetailsScreen.kt:98`, `EditGroupScreen.kt:102`, `TagsScreen.kt:98`,
   `TagEditScreen.kt:65`, `TagDetailsScreen.kt:90`, `PlacesScreen.kt:57`, `EditPlaceScreen.kt:69`. Internally
   consistent within this group, but it means none of these 8 screens will pick up a future palette change
   to `TopAppbarColor` (`ui-common/compose/ComponentColors.kt:28`) the way screens that already use the token
   would.
3. **Hand-blended `.copy(alpha = 0.3f / 0.5f)` for empty-state de-emphasis instead of `onSurfaceVariant`** —
   four screens have near-identical, independently duplicated empty-state composables that all alpha-blend
   `onSurface` rather than using the token built for this: `GroupsScreen.kt:184,189` (`GroupsEmptyState`),
   `TagsScreen.kt:253,258` (`TagsEmptyState`), `PlacesScreen.kt:149,154` (`PlacesEmptyState`),
   `TagDetailsScreen.kt:249,254` (`TagDetailsEmptyState`). This is the same gap the Reminders audit flagged
   for `RemindersArchiveScreen`'s `ArchiveEmptyState`, and it compounds with a second, non-M3 issue: these
   four composables are ~90% identical to each other (icon + `bodyLarge` message, same padding) and to
   `ui-common`'s already-existing `EmptyState.kt` (`icon: Painter, message: String`) built during the
   Home/Agenda pass (§3 item 5) — none of the four have been migrated onto it.
4. **Three different type roles for the same "list row primary label" purpose across sibling list
   screens** — `GroupListItem.kt:84` uses `titleMedium` for the group title, `TagsScreen.kt:205`
   (`TagListItem`) uses `bodyLarge` for the tag name, and `PlaceListItemCard.kt:62` uses `titleLarge` for the
   place name. All three are the single, largest, primary text in an otherwise structurally identical
   "icon/dot + label + overflow menu" list row, in three screens that ship in the same feature group and are
   reachable from the same settings/menu surface — worth picking one role (guidelines §3.1's `titleMedium`
   is the closest fit to what two of the three already lean toward) and applying it consistently rather than
   leaving the visual weight of "item name in a list" up to whichever screen was written last.
5. **List-item container-color sourcing also diverges between siblings** — `GroupListItem.kt:60` falls back
   to the plain `CardDefaults.cardColors().containerColor` default for a non-highlighted row, while
   `TagsScreen.kt:193` (`TagListItem`) explicitly sets `MaterialTheme.colorScheme.surfaceContainer` for the
   same non-highlighted state. Both are legitimate role choices individually, but per guidelines §2.2 ("this
   mapping should stay consistent... add more surface container levels for hierarchy instead") two visually
   near-identical row components picking different defaults is worth reconciling once either is touched.
6. **No max-width / line-length cap on any of the 8 screens** — none of `EditGroupScreen`'s settings column,
   `PlacesScreen`'s/`GroupsScreen`'s `LazyColumn` cards, or `TagsScreen`'s `FlowRow` constrain content width,
   so on Expanded/Large/XL breakpoints (§1.4's 40-60 character guidance) card text and form fields will
   stretch edge-to-edge inside whatever pane hosts them. Lower priority than items 1-5 — these screens are
   generally used inside the app's existing two-pane `renderAsDetailPane` shell (already width-bounded by the
   pane), so this is a "worth a look if a screen ever renders full-width on a large screen" note, not a
   confirmed defect, similar to how the Reminders audit treated `ReminderActionScreen`'s
   orientation-vs-breakpoint gap.

### Screen-specific findings

- **`GroupsScreen.kt` / `GroupListItem.kt`** — otherwise clean: no deprecated components, no manual
  `FontWeight` overrides anywhere in this file, and the `isHighlighted` selection state
  (`GroupListItem.kt:59-62`) is a genuinely correct role pairing — `primaryContainer` fill with a matching
  `primary`-colored 1dp border, not an ad hoc highlight color. `DefaultChip` (`GroupListItem.kt:130-142`)
  uses a literal `RoundedCornerShape(8.dp)` rather than an `AppShapes`/`MaterialTheme.shapes` token — 8dp
  *is* on the official shape scale (small), so this isn't an off-scale value, just untokenized (guidelines
  §4.1's token-hygiene note, lower severity than an off-scale literal).
- **`GroupDetailsScreen.kt`** — good reuse of shared `SettingsItem`/`SettingsSectionHeader` components for
  the notification-overrides section rather than hand-rolling rows. `SectionHeader` (`:187-197`) correctly
  uses `titleMedium` for a section header per guidelines §3.1's own recommendation for that role. No
  deprecated components; overflow menu (edit/delete) mirrors `TagDetailsScreen`'s identical pattern.
- **`GroupReminderRow.kt`** — a thin wrapper around the shared `AgendaListItem` component (already migrated
  to `AppShapes.card` per the Home/Agenda pass, §4) — nothing new to flag, this is the "reuse the shared
  component" pattern working as intended.
- **`EditGroupScreen.kt`** — good component choices throughout: `Switch` for the independent "make default"
  toggle (correct per guidelines §9.6's switch-vs-checkbox-vs-radio guidance), `AlertDialog`/
  `SingleChoiceDialog` for confirmations, `Slider` for the delay-minutes picker. Reuses the shared
  `ColorPickerCard` (see below) rather than a bespoke color picker. Delete is placed as a direct
  `MenuIconButton` in the app bar (`:88-94`) rather than behind an overflow menu — see the cross-screen
  delete-placement note below.
- **`TagsScreen.kt` / `TagListItem`** — the one screen in this group with a materially different layout
  (`FlowRow` chip grid instead of a `LazyColumn` list), which is a reasonable, deliberate content-shape
  choice for short tag names rather than a gap. `isHighlighted` selection uses the same correct
  `primaryContainer` + `primary`-border pairing as `GroupListItem`. See cross-cutting items 4-5 for its type
  role and container-color divergence from `GroupListItem`/`PlaceListItemCard`.
- **`TagEditScreen.kt`** — see cross-cutting item 1 for its save-button accessibility defect and component
  inconsistency versus `EditGroupScreen`/`EditPlaceScreen`. Otherwise minimal — a single `OutlinedTextField`
  plus the shared `ColorPickerCard`.
- **`TagDetailsScreen.kt`** — correctly uses `FilterChip` for the reminder/note/task/birthday content-type
  filter (`TypeFilterRow`, `:150-170`) — right component for a filterable single/no-selection set per
  guidelines §9.6. Reuses the shared `SearchBar` — consistent with the guidelines' note that this repo's
  `SearchBar` is the reference implementation to check new search UI against. Empty state duplicated per
  cross-cutting item 3.
- **`TagDetailRows.kt`** — purely a dispatcher over already-shared row composables
  (`ReminderAgendaRow`/`NoteCard`/`BirthdayAgendaRow`/`GoogleTaskRow`) — good reuse, nothing new to flag; it
  inherits whatever compliance state those shared components already have from the Reminders/Agenda audits.
- **`PlacesScreen.kt` / `PlaceListItemCard.kt`** — otherwise clean list screen; conditionally hides the
  search bar only when the list is empty and unsearched (`PlacesScreen.kt:75`), a reasonable content-aware
  adaptation. `PlaceListItemCard`'s marker icon (`:51-59`) is a non-interactive 20dp decorative icon (not a
  button), so the 48dp touch-target rule doesn't apply to it. See cross-cutting item 4 for its `titleLarge`
  outlier.
- **`EditPlaceScreen.kt`** — the one editor in this group that places its delete action behind an overflow
  menu (`OverflowMenuButton`, `:109-131`) rather than as a direct app-bar icon like `EditGroupScreen`/
  `TagEditScreen` do — a minor cross-screen consistency question (not itself an M3 violation, "component
  variant matches purpose" is satisfied either way) worth a look alongside item 1's back-button fixes since
  all three editors will already be touched.
- **Color-picker chrome (`ColorPickerCard.kt`, `ColorSlider.kt`, shared by `EditGroupScreen` and
  `TagEditScreen`) — genuinely spec-correct, worth calling out as a positive.** The card container uses
  `MaterialTheme.colorScheme.surfaceContainer`, the title label uses `titleMedium` + `primary`, the
  selected-swatch indicator dot borders itself in `outlineVariant` (`ColorPickerCard.kt:94`), and the
  slider's selection ring uses `onSurface` with a deliberate luminance-based black/white fallback only when
  the ring would otherwise vanish against a swatch of the same color (`ColorSlider.kt:84-93`, with a clear
  why-comment). None of the *surrounding chrome* is ad hoc hex — only the swatch palette itself is arbitrary
  brand color, which is expected and correctly out of scope per the guidelines' own carve-out for color
  pickers.
- **No deprecated M3 components found anywhere in this group** (no segmented buttons, nav drawer, bottom app
  bar, non-flexible medium/large app bar, baseline/surface FAB, or stacked-FAB speed dial) — worth stating
  explicitly since it's the fastest, highest-confidence category of finding and this group has none.
- **No off-scale elevation and no manual `FontWeight.Bold/SemiBold/Medium` overrides found anywhere in this
  group** — every `Card` in these 8 screens uses default elevation, and no screen hand-overrides a baseline
  type style's weight. This is a cleaner baseline than the Reminders group (which had 6+ manual-weight call
  sites in `ReminderActionScreen.kt` alone) — the flip side is these screens also have zero adoption of the
  new emphasized type tokens for their selection states (`isHighlighted` rows, selected `FilterChip`), which
  is a "worth adopting" opportunity per guidelines §3.1 rather than a compliance gap.
- **Repo-convention note, not an M3 spec gap** — per `CLAUDE.md`'s icon rule ("a drawable resource ID is
  never referenced as a bare `R.drawable.ic_fluent_*` from feature/screen code... use `AppIcons.*` /
  `DrawableCatalog.*`"), nearly every file in this group bypasses the catalog: `PlacesScreen.kt:60`
  (`painterResource(R.drawable.ic_fluent_add)`), the "more options" overflow icon in `GroupListItem.kt:114`,
  `GroupDetailsScreen.kt:166`, `TagsScreen.kt:227`, `TagDetailsScreen.kt:205`, `PlaceListItemCard.kt:72`,
  `EditPlaceScreen.kt:120`, plus every `PopupMenuItem(iconRes = R.drawable.ic_fluent_*, ...)` call site
  (e.g. `GroupsScreen.kt:159,167`, `TagsScreen.kt:171,176,223-224`, `PlaceListItemCard.kt:93-95`). This is
  flagged separately from the M3 findings above because it's a codebase-hygiene rule, not a Material spec
  requirement — but it's real, mechanical, and affects essentially every file audited here.

### Suggested fix order

Cheapest-and-highest-value first: (1) the 9 missing back/save-button content descriptions across all 8
screens — mechanical, zero design judgment, the single highest-confidence accessibility fix (`TagEditScreen`'s
save button is the one true "screen reader has no idea what this is" case, the other 8 are the
detail-pane-vs-back pattern already seen in the Reminders audit); (2) migrate the four duplicated
alpha-blended empty states (`GroupsEmptyState`, `TagsEmptyState`, `PlacesEmptyState`,
`TagDetailsEmptyState`) onto `ui-common`'s existing `EmptyState.kt`, which fixes the `onSurfaceVariant` gap
and the code duplication in one move; (3) point all 8 `TopAppBar`s at the shared `TopAppbarColor` token
instead of `MaterialTheme.colorScheme.background` directly; (4) pick one type role for a list row's primary
label and apply it to `GroupListItem`, `TagListItem`, and `PlaceListItemCard` alike (currently
`titleMedium`/`bodyLarge`/`titleLarge` respectively); (5) the `DrawableCatalog`/`AppIcons` convention cleanup
and the minor delete-placement/container-color inconsistencies are lower priority — naturally fold into
whichever future PR next touches each screen, per the "touch it once, fix it right" approach already
established for Home/Agenda/Reminders.

## 10. Calendar & Google Tasks screens — audit

Audit pass over the 8 Calendar/Google Tasks screens. **Audit only — no code changed in this pass.** Files
read in full: `CalendarScreen.kt`, `TimelinePager.kt` (the composable that does all of `TimelineScreen.kt`'s
actual grid/event rendering, read alongside it the same way `MonthPage`/`MonthDayCell` are inlined into
`CalendarScreen.kt`), `TimelineScreen.kt`, `GoogleCalendarEventPreviewScreen.kt`, `GoogleTasksScreen.kt`,
`TaskListScreen.kt`, `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`, `EditGoogleTaskListScreen.kt`,
plus `CalendarModeToggleButton.kt` (the shared view-mode switcher rendered identically by both Calendar
screens) and the four nav-graph files (`CalendarNavGraph.kt`, `GoogleCalendarEventPreviewNavGraph.kt`,
`GoogleTasksNavGraph.kt`, and the `ui-common` helpers `ComponentColors.kt` / `Shape.kt` /
`DetailScreenContentWidth.kt`) to confirm how each screen is actually hosted at larger breakpoints rather
than guessing from the screen file alone.

### Cross-cutting patterns (found on 2+ screens — fix once, verify everywhere it repeats)

1. **Back-button `contentDescription = null`** — every single screen in this batch does this on its
   leading `MenuIconButton`, matching exactly the bug already documented for the Reminders group:
   `CalendarScreen.kt:101`, `TimelineScreen.kt:63`, `GoogleTasksScreen.kt:75`, `TaskListScreen.kt:74` (the
   non-detail-pane branch), `PreviewGoogleTaskScreen.kt:62`, `EditGoogleTaskScreen.kt:74`,
   `EditGoogleTaskListScreen.kt:59`, and `GoogleCalendarEventPreviewScreen.kt:48` (again only the
   non-detail-pane branch — the detail-pane "close" icon on the same line correctly passes
   `stringResource(R.string.acc_close)`). 8 for 8 — this is a real accessibility defect (guidelines §8),
   not a style gap, and it's a one-line fix per file identical to the fix already scoped for the Reminders
   group.
2. **`TopAppBar` `containerColor` bypasses the shared `TopAppbarColor` token, and does so two different
   ways** — `ui-common`'s `TopAppbarColor` (`ComponentColors.kt:28-33`) resolves to
   `containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground`.
   None of the 8 screens use it:
   - `GoogleCalendarEventPreviewScreen.kt:59`, `GoogleTasksScreen.kt:88`, `TaskListScreen.kt:99`,
     `PreviewGoogleTaskScreen.kt:78`, `EditGoogleTaskScreen.kt:102`, `EditGoogleTaskListScreen.kt:79` all
     call `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)`
     directly — same `containerColor` value as the token, but silently dropping `titleContentColor`, so
     the title falls back to the component default (`onSurface`) instead of `onBackground`. A future
     palette change where `background`/`onBackground` diverge from `surface`/`onSurface` would show up
     inconsistently across these 6 screens vs. any screen that does use the token.
   - `CalendarScreen.kt:109` and `TimelineScreen.kt:74` instead use
     `TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)` — a third distinct value.
   Same defect shape as the Reminders group's finding #5, but worse here: in Reminders 4/7 screens used the
   real token; in this batch 0/8 do.
3. **Off-scale hardcoded corner radius** — `RoundedCornerShape(12.dp)` in `GoogleTasksScreen.kt:180`
   (`TaskListTile`'s colored background) actually *is* on-scale (12dp = "medium" per guidelines §4.1) but
   is a literal instead of `ui-common`'s `AppShapes.tile` (`Shape.kt:12`, also 12dp) — a token-hygiene gap,
   not a scale violation. `TimelinePager.kt:301` (`HolidayChip`) and `TimelinePager.kt:482`
   (`TimelineEventBlock`) both use `RoundedCornerShape(6.dp)`, which genuinely is off the 10-step scale
   (guidelines §4.1) — the nearest steps are 4dp (extra small) or 8dp (small). Both call sites back
   compact/dense chips, so 4dp is probably the better fit than rounding up to 8dp and losing the
   "small chip" feel.
4. **Ad hoc `.copy(alpha = ...)` for de-emphasis instead of a role token** — `CalendarScreen.kt:242`
   (`MonthDayCell`, other-month day number: `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)`),
   `GoogleTasksScreen.kt:205` (empty-state icon, `onSurface.copy(alpha = 0.3f)`) and `:210` (empty-state
   text, `onSurface.copy(alpha = 0.5f)`) all hand-blend `onSurface` down to a lower-emphasis tone where
   `MaterialTheme.colorScheme.onSurfaceVariant` (or, for the calendar cell, `outline`/`outlineVariant`
   for a very muted "not this month" number) already exists as the token for exactly this case (guidelines
   §2.2/§2.4). Same pattern flagged in the Reminders audit — same fix ("use the role directly instead of
   blending your own").
5. **Missing `detailScreenContentWidth()` / any breakpoint-aware width cap on preview & edit screens** —
   `ui-common`'s `Modifier.detailScreenContentWidth()` (`DetailScreenContentWidth.kt:21-26`) caps content at
   a fixed max-width on tablet/desktop breakpoints and only `GoogleCalendarEventPreviewScreen.kt:77` uses
   it. `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`, and `EditGoogleTaskListScreen.kt` — three
   screens that are structurally the same shape (a scrolling `Column` of read-only or editable fields) as
   `GoogleCalendarEventPreviewScreen` — never cap their content width, so their `Column`s (and the
   `OutlinedTextField`s / `DetailRow`s inside them) will run edge-to-edge on Medium/Expanded/Large
   breakpoints, well past the 40–60 character line-length target (guidelines §1.4). This is compounded by
   finding-specific detail #1 below: unlike `GoogleCalendarEventPreviewScreen`, these three Google Tasks
   screens aren't wired into any two-pane/side-panel scene at all, so on a tablet they're the *only* thing
   on screen (no list pane alongside them) and still don't limit their own width.

### Screen-specific findings

- **`CalendarScreen.kt` (Month) and `TimelineScreen.kt`/`TimelinePager.kt` (Timeline)** — neither screen
  adapts its grid/pane layout to breakpoints at all, and this is confirmed by design, not just by absence
  of code: `CalendarNavGraph.kt:26-33`'s own doc comment states Calendar's entries are tagged as a
  `sidePanelHost()` specifically because "Calendar itself is never resized or hidden by this — it always
  renders at full size; only the supporting entry floats over it." So the month grid always renders exactly
  7 equal-width day columns and the timeline always renders exactly as many day columns as the selected
  mode (1/3/7), regardless of Compact vs. Expanded vs. Large width — guidelines §1.1/§1.3 flag a calendar
  grid as a natural candidate for using the extra width at Medium+ (more visible days, wider day cells, or
  a permanent side pane for the day/agenda detail instead of the current side-sheet-over-full-size-Calendar
  approach). This is the single largest layout gap in the batch.
- **`TimelinePager.kt`** — `TimelineEventBlock`'s clickable event box (`:474-485`) uses
  `.heightIn(min = EVENT_BLOCK_MIN_HEIGHT)` where `EVENT_BLOCK_MIN_HEIGHT = 26.dp` (`:64`) — for a short
  (e.g. 15-30 minute) event this is the actual rendered+clickable height, well under the 48×48dp minimum
  touch target (guidelines §8). `HolidayChip` (`:287-338`) is similarly compact (`padding(horizontal = 4.dp,
  vertical = 3.dp)` around 12sp label text) and likely also renders under 48dp tall. Both are deliberate
  density trade-offs for a data-dense timeline (same category as the Reminders group's `SubTasksValueEditor`
  40dp icon buttons, which was flagged there as "worth a second look" rather than a hard blocker) — worth
  the same treatment here, especially for `TimelineEventBlock` since it's the primary way to open an event
  from this screen.
- **`CalendarModeToggleButton.kt`** (shared by both Calendar screens) — `CalendarModeRow`'s selected-state
  label (`:108-114`) uses `fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal` on a
  baseline `titleMedium` style. This is exactly the "context" trigger guidelines §3.1 calls out for
  swapping to a real `titleMediumEmphasized` token instead of a manual weight override once
  `MaterialTheme.typography.xxxEmphasized` is adopted per-screen (still Phase 2/3 work per §3). Worth noting
  as **positive compliance** alongside the gap: the same row already signals "selected" correctly through
  more than just weight — `secondaryContainer`/`onSecondaryContainer` color (guidelines §2.2's correct
  secondary-role usage) plus a trailing checkmark icon — so this is a genuinely minor, one-property fix on
  an otherwise well-built selection row, not a rebuild.
- **`GoogleTasksScreen.kt`** and **`TaskListScreen.kt`** — both use `ExtendedFloatingActionButton` in its
  baseline 56dp pill-shaped form (`GoogleTasksScreen.kt:93-99`, `TaskListScreen.kt:103-109`), which
  guidelines §9.1 explicitly marks **no longer recommended** — replace with the small extended FAB.
  `PreviewGoogleTaskScreen.kt:84-89`'s "Complete" FAB is the same deprecated variant. Three of the four
  FAB-having screens in this batch hit this; it's the clearest deprecated-component finding in the group
  (mirrors `ReminderFullscreenMapScreen.kt` in the Reminders audit).
- **`GoogleTasksScreen.kt`** — `NotLoggedInContent` (`:216-248`) hosts a raw `AndroidView`-wrapped
  `com.google.android.gms.common.SignInButton` (a non-Material Google-branded button) as the primary CTA
  when logged out. This is outside Material's component catalog entirely (Google's sign-in branding
  requirements force this), so it's not a "fix it" item, but worth flagging as a screen-visible exception to
  the rest of the checklist — guidelines §8's "prefer standard platform controls" doesn't really apply to a
  third-party branded control like this.
- **`GoogleCalendarEventPreviewScreen.kt`** — the strongest-adapted screen in the batch: correctly swaps
  close/back icon + content description via `renderAsDetailPane` (`:47-49`), correctly uses
  `detailScreenContentWidth()` (`:77`), and per `GoogleCalendarEventPreviewNavGraph.kt:22-27` is wired with
  both `ListDetailSceneStrategy.detailPane()` and `sidePanelSupporting()` metadata so it renders as a real
  Material 3 side sheet over Calendar at Medium+ width. Worth treating as the reference pattern for the
  three under-adapted Google Tasks screens named in cross-cutting #5 — it's a near-identical "preview a
  single item" screen shape, just already done right.
- **`PreviewGoogleTaskScreen.kt`** — has no `renderAsDetailPane` parameter at all (unlike
  `TaskListScreen.kt` and `GoogleCalendarEventPreviewScreen.kt`, which both support it), and
  `GoogleTasksNavGraph.kt:76` pushes `GoogleTasksNavKey.TaskPreview` with no `ListDetailSceneStrategy` /
  side-panel metadata — confirmed by the nav graph's own comment (`:37-47`) that `TaskPreview` is
  "intentionally left untagged" because it's reused from Tags' own preview links too. Net effect: this
  screen always pushes full-screen even at Large/XL breakpoints, with no width cap of its own either (see
  cross-cutting #5) — the two gaps compound into the screen actually stretching edge-to-edge on a tablet,
  unlike its Calendar-side counterpart.
- **`EditGoogleTaskScreen.kt`** and **`EditGoogleTaskListScreen.kt`** — both correctly use
  `OutlinedTextField` with `isError`/`supportingText` for validation state (guidelines §9.6 text-field
  state visibility, done right) and both correctly source `FieldCard`/dialog surfaces from
  `MaterialTheme.colorScheme.surfaceContainer` rather than a hardcoded value. `EditGoogleTaskScreen.kt`'s
  `FieldCard` (`:222-252`) has no elevation override (uses the `Card` default), which is spec-correct but
  worth noting since it sits directly above `TwoOptionDialog`/`ListPickerDialog`, both plain `AlertDialog`s
  at the correct default elevation too — nothing off-scale to flag here, this pair of screens is otherwise
  clean aside from the cross-cutting items above.

### Suggested fix order

Cheapest-and-highest-value first: (1) the 8 missing back-button content descriptions — mechanical, zero
design judgment, same fix already scoped for the Reminders group; (2) route all 8 screens' `TopAppBar`
through the shared `TopAppbarColor` token instead of the two different ad hoc overrides (`background`
literal vs. `Color.Transparent`); (3) the three deprecated `ExtendedFloatingActionButton` (baseline)
instances → small extended FAB; (4) `TimelinePager.kt`'s two `RoundedCornerShape(6.dp)` sites → 4dp
(extra small) or a new `AppShapes` step if 4dp turns out to be needed elsewhere too; (5) add
`detailScreenContentWidth()` to `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`, and
`EditGoogleTaskListScreen.kt` to match `GoogleCalendarEventPreviewScreen.kt`. The Calendar Month/Timeline
breakpoint-adaptation gap (screen-specific finding #1) and the sub-48dp timeline touch targets are the two
items that need actual design input rather than a mechanical fix, so they're natural candidates for a
follow-up design pass rather than this sweep.

## 11. Settings screens (part A) — audit

Audit pass over 16 Settings screens (the "part A" half — general/backup/reminders/calendar/birthday/note/
location/security/PIN), using the checklist in `m3-expressive-guidelines.md` §10. **Audit only — no code
changed in this pass.** Files read in full: `SettingsHubScreen.kt`, `GeneralSettingsScreen.kt`,
`BackupSettingsScreen.kt`, `RemindersSettingsScreen.kt`, `ManagePresetsScreen.kt`,
`NotificationCustomizationHelpScreen.kt`, `CalendarSettingsScreen.kt`, `HolidayCountryScreen.kt`,
`BirthdaySettingsScreen.kt`, `NoteSettingsScreen.kt`, `LocationSettingsScreen.kt`, `MapStyleScreen.kt`,
`SecuritySettingsScreen.kt`, `AddPinScreen.kt`, `ChangePinScreen.kt`, `DisablePinScreen.kt`, plus the
shared components these screens all build on — `ui-common`'s `SettingsItem.kt`
(`SettingsItem`/`SettingsSwitchItem`/`SettingsCheckboxItem`/`SettingsSectionHeader`), `PinInput.kt`, and
`ComponentColors.kt` (`TopAppbarColor`), and `feature-settings`'s `SettingsScaffold.kt` and its call sites
in `SettingsNavGraph.kt`, `SecurityNavGraph.kt`, `LocationNavGraph.kt`, and `app`'s
`SettingsCrossFeatureEntries.kt`.

### Cross-cutting patterns (found on 2+ screens — fix once, verify everywhere it repeats)

1. **Back/close-button `contentDescription = null` — centralized in the shared `SettingsScaffold`, fans
   out to nearly every screen in this batch.** Unlike the Reminders group (where the same bug was
   scattered per-screen), here it lives in exactly one place: `SettingsScaffold.kt:33` passes
   `contentDescription = null` to the leading `MenuIconButton` unconditionally. Every screen that routes
   through `SettingsScaffold` inherits the bug: confirmed call sites wrapping 13 of this batch's 16
   screens — `SettingsHubScreen`, `GeneralSettingsScreen`, `BackupSettingsScreen`, `CalendarSettingsScreen`,
   `NoteSettingsScreen`, `LocationSettingsScreen`, `MapStyleScreen`, `SecuritySettingsScreen`,
   `AddPinScreen`, `ChangePinScreen`, `DisablePinScreen`, `ManagePresetsScreen`
   (`app/.../SettingsCrossFeatureEntries.kt:250-253`), `RemindersSettingsScreen`
   (`SettingsCrossFeatureEntries.kt:115-119`), and `BirthdaySettingsScreen`
   (`SettingsCrossFeatureEntries.kt:210-214`). `HolidayCountryScreen.kt:67` independently re-implements the
   same bug (`contentDescription = null` on its own inline `TopAppBar`, not routed through
   `SettingsScaffold`). Only **`NotificationCustomizationHelpScreen.kt:46`** gets this right
   (`contentDescription = stringResource(R.string.cd_back)`) — worth using as the reference fix. Net: 15 of
   16 screens in this batch are affected, all traceable to two source locations. This is the single
   highest-value fix available in this whole pass — one line in `SettingsScaffold.kt` (plus one in
   `HolidayCountryScreen.kt`) fixes essentially the entire group at once, versus a dozen scattered edits.
2. **`TopAppBar` color sourced by inline `containerColor` instead of the shared `TopAppbarColor` token —
   also centralized in `SettingsScaffold`.** `SettingsScaffold.kt:37` calls
   `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` directly
   instead of `colors = TopAppbarColor` (`ui-common`'s `ComponentColors.kt:28-33`). The two resolve to the
   same `containerColor` today, but `SettingsScaffold`'s version omits `titleContentColor = onBackground`
   (falling back to the M3 default title color instead), so it's not actually pixel-identical, and a future
   edit to the shared token silently won't reach any of the ~13 screens listed in finding #1.
   `HolidayCountryScreen.kt:71` has the identical bypass, independently.
   `NotificationCustomizationHelpScreen.kt:50` is again the one screen in this batch using the token
   correctly (`colors = TopAppbarColor`). Same fix-once opportunity as #1: correcting `SettingsScaffold.kt`
   fixes the whole group.
3. **Shared `SettingsItem`/`SettingsSwitchItem`/`SettingsSectionHeader` (`ui-common`) is used consistently
   and correctly across every list-based screen in this batch — a genuine positive finding, not a gap.**
   `GeneralSettingsScreen`, `BackupSettingsScreen`, `RemindersSettingsScreen`, `CalendarSettingsScreen`,
   `BirthdaySettingsScreen`, `NoteSettingsScreen`, `LocationSettingsScreen`, `MapStyleScreen`, and
   `SecuritySettingsScreen` all build their rows from these shared composables rather than reinventing
   `Row`+`Icon`+`Text`+`Switch` per screen — exactly the consolidation the guidelines' Lists section (§9.6)
   calls for, and a real contrast with the Reminders group's `SelectApplicationScreen`/
   `PreviewReminderScreen`, which hand-roll their rows. Within the shared component itself
   (`SettingsItem.kt`), note two smaller, low-priority items: (a) the search-highlight flash animation
   (`SettingsItem.kt:100-104`) uses a literal `tween(durationMillis = ...)` rather than
   `MaterialTheme.motionScheme` — consistent with the whole-app motion gap already noted once in the
   Reminders audit, not re-flagging in depth here; (b) `SettingsSectionHeader` colors its label text with
   `MaterialTheme.colorScheme.tertiary` (`SettingsItem.kt:259`) — a correct, deliberate accent-role use for
   a small structural element (guidelines §2.2), not a gap.
4. **`AlertDialog` + `Slider` for numeric "seek" pickers is reimplemented per-screen instead of sharing one
   composable.** `RemindersSettingsScreen.kt:377-395` (repeat-interval seek), `BirthdaySettingsScreen.kt`'s
   private `SeekValueDialog` (lines 237-273), `NoteSettingsScreen.kt:96-122` (opacity seek), and
   `LocationSettingsScreen.kt`'s `Radius`/`Tracker` dialogs (lines 144-190) are four near-identical
   `AlertDialog { Column { Text + Slider } }` blocks — same shape, same `bodyLarge`/`titleLarge` value text,
   same confirm/cancel button pair — that never converged on one shared component the way the choice/color
   pickers did (`SingleChoiceDialog`, `MultiChoiceDialog`, `rememberColorPickerDialogDispatcher`, all
   already shared from `ui-common`'s `foundation.dialog` package and reused correctly by
   `CalendarSettingsScreen`, `BirthdaySettingsScreen`, `LocationSettingsScreen`, `GeneralSettingsScreen`'s
   own private single-choice dialog notwithstanding — see finding #6). Not an M3-compliance gap by itself
   (each instance is a plain, spec-correct basic dialog), but worth folding into a `SeekValueDialog` in
   `ui-common` alongside the other shared dialogs, since any future shape/motion/elevation change to "the
   value-picker dialog" currently needs four edits instead of one.
5. **Ad hoc `.copy(alpha = 0.3f/0.5f)` for de-emphasis instead of `onSurfaceVariant`.**
   `ManagePresetsScreen.kt`'s `EmptyState` (icon at `alpha = 0.3f`, line 70; message text at
   `alpha = 0.5f`, line 75) and `HolidayCountryScreen.kt`'s empty-search state (`alpha = 0.5f`, line 96)
   both hand-blend `onSurface` down instead of using `onSurfaceVariant`, the token that already exists for
   exactly this case (guidelines §2.2) and would track the three-contrast-level system (§2.4) that raw alpha
   blending won't. Same pattern already flagged once in the Reminders audit — recorded here since it recurs
   independently in this batch, not because it's new.
   By contrast, `RemindersSettingsScreen.kt:259-263` does this correctly: it uses the shared `DisabledAlpha`
   constant from `ui-common` (not a magic-number alpha) to dim the DND from/to time text when the dependent
   toggle is off — worth treating as the local reference pattern for "controlled row dimming" versus the
   two ManagePresets/HolidayCountry instances of ad hoc de-emphasis.
6. **`SingleChoiceDialog` exists as a shared `ui-common` component but `GeneralSettingsScreen` still
   reimplements its own private copy.** `CalendarSettingsScreen.kt`, `BirthdaySettingsScreen.kt`, and
   `LocationSettingsScreen.kt` all import and use
   `com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog`. `GeneralSettingsScreen.kt`
   instead defines its own private `SingleChoiceDialog` (lines 125-170) with the same
   `AlertDialog` + `selectableGroup()` + `RadioButton` + `Role.RadioButton` shape — functionally equivalent,
   independently maintained. `RemindersSettingsScreen.kt:368-375` uses the shared one. A one-screen
   deduplication, not a design-judgment call.

### Screen-specific findings

- **`HolidayCountryScreen.kt`** — beyond repeating cross-cutting findings #1/#2/#5 independently (it isn't
  routed through `SettingsScaffold`, so it needed its own copy of the same three bugs), this screen is
  otherwise clean: its `CountryListItem` (lines 122-152) correctly uses `Card` + `primary`-tinted checkmark
  for the selected state, and the loading/empty/ready three-way `when` (lines 85-116) is a reasonable,
  simple list state machine.
- **`ManagePresetsScreen.kt`** — no `TopAppBar`/back button of its own (correctly delegates that to the
  `SettingsScaffold` wrapper in `app/.../SettingsCrossFeatureEntries.kt`, so cross-cutting findings #1/#2
  apply to it via that wrapper, not directly in this file). Its own gap is #5 above (alpha-blended empty
  state). Otherwise a minimal, appropriately un-adorned list screen — reuses `PresetListItem` from
  `feature-reminder`'s builder package rather than reinventing a row.
- **`RemindersSettingsScreen.kt`** — the largest screen in this batch (24 rows across 5
  `SettingsSectionHeader` groups) and a good demonstration that the section-header pattern scales without
  needing extra visual hierarchy — no findings beyond the cross-cutting ones. Positive note:
  `dndValueColor` (lines 259-263) correctly derives from the shared `DisabledAlpha` token rather than a
  magic number (see finding #5).
- **`CalendarSettingsScreen.kt`** — `ColorSwatch` (lines 211-219) is a plain hardcoded `24.dp` circle used as
  a trailing color preview; this is a fine, minimal use of a raw dp size for a decorative swatch (not a
  corner-radius/shape-scale concern since it's a `CircleShape`, and 24dp swatches aren't a touch target on
  their own — the whole row is). No compliance gap, noted only because it's the one non-token numeric
  literal in an otherwise fully token-driven screen.
- **`NotificationCustomizationHelpScreen.kt`** — the one screen in this batch that gets both the back-button
  content description and the `TopAppbarColor` token right (see findings #1/#2) — a thin `Scaffold` +
  `TopAppBar` wrapper around a bundled HTML `WebView`, same pattern as the Reminders group's
  `ReminderHelpScreen.kt`/`RecurHelpScreen.kt`. Little further Compose-layer surface area to audit; any
  additional work would mean restyling `notification_customization.html` itself, out of scope here.
- **`BirthdaySettingsScreen.kt`** — its private `SeekValueDialog` (lines 237-273) correctly wires
  `LocalHapticFeedback` + `hapticFeedbackEnabled` through to the slider drag, matching the equivalent logic
  in `NoteSettingsScreen.kt:105-108` — good, deliberate parity between the two independent seek-dialog
  implementations even though they aren't shared (see cross-cutting finding #4).
- **`PinInput.kt`** (shared component backing all three PIN screens) — `PinDigitButton` (lines 130-148) uses
  `tonalElevation = 2.dp` (line 137), which isn't one of the defined M3 elevation levels (0/1/3/6/8/12dp,
  guidelines §5) — it sits between the "filled card" default (0dp) and "elevated" default (1dp/3dp).
  `shadowElevation = 1.dp` on the same `Surface` does match a real level. Low priority: `Surface`
  `tonalElevation` is a softer, continuous-feeling parameter than a component's named "resting elevation,"
  and a 64dp circular digit button is exactly the kind of standalone, already-prominent element guidelines
  §2.4 treats leniently — but it's still a literal off-scale number rather than a token, worth a mention.
  Positive: `DigitButtonSize = 64.dp` clears the 48×48dp minimum touch target with real margin, and
  `RowSpacing = 24.dp` between buttons is comfortably over the 8dp minimum target spacing (guidelines §8).

### PIN flow (Add/Change/Disable) consistency check

`AddPinScreen.kt`, `ChangePinScreen.kt`, and `DisablePinScreen.kt` are structurally identical: same
`Column` + `Spacer(weight 1f)` + uppercased `headlineMedium` prompt + `Spacer(48.dp)` + shared `PinInput` +
`Spacer(weight 1.4f)` skeleton, differing only in which string resource drives the prompt text per stage.
This is a strong, deliberate consistency story across the 3-screen flow — no drift found between them. The
one thing all three share (inherited from `PinInput`, not screen-specific) is the off-scale
`tonalElevation` noted above. None of the three screens has a `contentDescription = null` issue directly
(they have no back button of their own — that's owned by the wrapping `SettingsScaffold`, so cross-cutting
finding #1 is what actually governs their back-navigation accessibility, not anything in these three files).

### Suggested fix order

1. **Fix `SettingsScaffold.kt`'s back-button `contentDescription = null` (line 33) and its
   `TopAppBarDefaults` color bypass (line 37 → `colors = TopAppbarColor`).** Two one-line changes in a
   single shared file that correct both cross-cutting findings #1 and #2 for 13 of this batch's 16 screens
   at once — by far the highest leverage fix available in this pass.
2. **Apply the same two fixes to `HolidayCountryScreen.kt`** (lines 67 and 71) — the one screen that
   independently duplicated both bugs outside `SettingsScaffold`.
3. **Swap `ManagePresetsScreen.kt`'s and `HolidayCountryScreen.kt`'s alpha-blended empty-state text/icon
   for `onSurfaceVariant`** (finding #5) — mechanical, matches the already-correct pattern used elsewhere
   in the same codebase (`RemindersSettingsScreen.kt`'s `DisabledAlpha` usage).
4. **Deduplicate `GeneralSettingsScreen`'s private `SingleChoiceDialog` onto the shared `ui-common` one**
   (finding #6) — removes a maintained-twice component with no behavior change.
5. **Consider consolidating the four hand-rolled seek/slider `AlertDialog`s** (`RemindersSettingsScreen`,
   `BirthdaySettingsScreen`, `NoteSettingsScreen`, `LocationSettingsScreen`) **into one shared
   `SeekValueDialog` in `ui-common`**, alongside the existing `SingleChoiceDialog`/`MultiChoiceDialog`/color
   picker dialogs (finding #4) — lower urgency than 1-4 since each instance is individually spec-correct
   today, but the same "touch it once, fix it right" logic the Reminders audit applied to its own dedup
   findings.

## 12. Settings screens (part B) — audit

Audit pass over the remaining 13 "Settings" rows (part B). **Audit only — no code changed in this pass.**
Files read in full: `CloudBackupSettingsScreen.kt`, `CloudServicesScreen.kt`, `OtherSettingsScreen.kt`,
`OtherNavGraph.kt` (covering `PermissionsEntry`/`OssEntry`/`PrivacyPolicyEntry`/`TermsEntry`/
`GeminiFunctionsEntry`/`SettingsWebView`), `WhatsNewScreen.kt`, `DigestSettingsScreen.kt`,
`HeaderItemsSettingsScreen.kt`, `TroubleshootingScreen.kt`, `ProVersionScreen.kt`, plus the shared
components each of these builds on: `SettingsScaffold.kt`, `SettingsItem.kt` (ui-common),
`MenuIconButton.kt`, `TooltipIconButton.kt`, `AnimatedGradientBackground.kt`, and `ComponentColors.kt`'s
`TopAppbarColor`.

### Cross-cutting patterns (found on 2+ screens — fix once, verify everywhere it repeats)

1. **Back/close-button `contentDescription = null` in the shared `SettingsScaffold`** —
   `SettingsScaffold.kt:31-36` passes `contentDescription = null` to the `MenuIconButton` that renders
   every settings sub-screen's navigation icon. Because `SettingsScaffold` is the shared wrapper, this
   single line silently strips the accessibility label from **10 of the 13 screens in this group**: Cloud
   Backup Settings, Other Settings, Permissions, Open Source Licenses, Privacy Policy, Terms of Service,
   Gemini Functions, AI Digest Settings, Header Items Settings, and Troubleshooting all route through this
   one composable. This is the same defect class the Reminders audit found scattered per-file (guidelines
   §8) — here it's concentrated in one shared component, so it's simultaneously the single highest-leverage
   fix in this whole pass and the reason the bug is so widespread — and it's the exact same
   `SettingsScaffold.kt:33` root cause the "Settings part A" audit (§11) already identified for the other
   10 screens in that batch, so fixing it once here fixes both halves of Settings at once. Notably, the
   three screens that **don't** use `SettingsScaffold` — `CloudServicesScreen.kt:58`, `WhatsNewScreen.kt:54`,
   `ProVersionScreen.kt:59` — all hand-roll their own header row and correctly pass a real
   `contentDescription` (`stringResource(R.string.cd_back)`, or `R.string.acc_close` when rendered as a
   detail pane in `ProVersionScreen.kt:57-58`). The bug is isolated to the shared path, not systemic across
   the group.
2. **`TopAppBar` color hardcoded in `SettingsScaffold` instead of the shared `TopAppbarColor` token** —
   `SettingsScaffold.kt:37` calls `TopAppBarDefaults.topAppBarColors(containerColor =
   MaterialTheme.colorScheme.background)` directly instead of `ui-common`'s `TopAppbarColor` (already
   adopted by `feature-reminder`'s `BuildReminderScreen`/`TodoEditScreen`/`ReminderHelpScreen`/
   `RecurHelpScreen`/`NotificationCustomizationHelpScreen` per the Reminders audit). This is the same
   token-bypass gap called out there, but again concentrated in one file affecting all 10 `SettingsScaffold`
   screens at once (same root cause as §11's finding #2 for the other Settings half) — worse, it isn't even
   color-for-color equivalent: `SettingsScaffold` only overrides `containerColor`, leaving
   `titleContentColor` at the M3 default (`onSurface`), while `TopAppbarColor` explicitly sets
   `titleContentColor = MaterialTheme.colorScheme.onBackground`. In this app's current static color scheme
   `onSurfaceLight`/`onBackgroundLight` happen to be the identical hex (`0xFF221A14`, see `Color.kt:25,27`),
   so there's no visible difference today — but a future token update to either role would silently diverge
   the two only in screens still bypassing the shared token (guidelines §2.1's "hardcoding instead of
   pointing to a token" gap).
3. **Duplicated gradient-hero header block across 3 screens, carrying the same two M3 gaps in triplicate** —
   `CloudServicesScreen.kt:49-74`, `WhatsNewScreen.kt:45-70`, and `ProVersionScreen.kt:48-75` each hand-roll
   an identical `Row` (status-bar padding, 16dp leading spacer, `TooltipIconButton` wrapping an
   `IconButton`) rather than sharing one composable — the `AnimatedGradientBackground` behind them is
   already a shared `ui-common` component (correctly reused all 3 places), but the header row on top of it
   isn't. Concretely, all three copies repeat:
   - **Sub-48dp touch target**: the back/close `IconButton` is explicitly sized `Modifier.size(40.dp)`
     (`CloudServicesScreen.kt:63`, `WhatsNewScreen.kt:59`, `ProVersionScreen.kt:64`) — below the 48×48dp
     minimum interactive target guidelines §8/§1.4 call out, overriding what would otherwise be
     `IconButton`'s spec-correct default.
   - **Off-token corner radius + hand-blended alpha**: `RoundedCornerShape(20.dp)` on the content card
     (`CloudServicesScreen.kt:88,121`, `WhatsNewScreen.kt:86`, `ProVersionScreen.kt:91`) is a valid step on
     the shape scale ("large increased," §4.1) but a bare literal rather than an `AppShapes`/
     `MaterialTheme.shapes` token, paired with `MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)` for
     the card fill and `MaterialTheme.colorScheme.background.withAlpha(0.25f)` for the back-button chip
     (same four line groups) — both are the ad hoc alpha-blend pattern guidelines §2.2/§2.4 flag in favor
     of a real surface-container role or opacity token that would track future contrast-level changes.
   Because all three symptoms repeat verbatim in three places, extracting one shared "gradient screen
   header" composable would fix the touch target and both hardcodes in a single change instead of three.
4. **Settings-row composable reuse is inconsistent within this group** — `SettingsItem`/`SettingsSwitchItem`
   (ui-common) is correctly reused by `CloudBackupSettingsScreen.kt` (all 4 rows), `OtherSettingsScreen.kt`
   (all 12 rows), `DigestSettingsScreen.kt` (both rows), and `TroubleshootingScreen.kt`'s "Send Logs" row —
   4 of the group's screens share one row implementation, matching §11's finding that this app's Settings
   screens generally do reuse the shared row correctly. `HeaderItemsSettingsScreen.kt`, however, reinvents
   its own row layout twice (`PinnedHeaderItemRow` at lines 111-146, `ConfigurableHeaderItemRow` at lines
   148-189) — similar icon+title+trailing structure to `SettingsItem` but with different paddings (icon gap
   8dp vs. `SettingsItem`'s 20dp `IconSpacing`) and a hand-rolled `contentAlpha`/`DisabledAlpha` calculation
   instead of `SettingsItem`'s built-in `enabled` handling. The divergence has a legitimate reason —
   `SettingsItem` has no slot for a leading drag handle or the drag-gesture wiring this screen needs — but
   it means any future visual tweak to the shared row (spacing, disabled-state color, `AppShapes` shape once
   list rows get one) has to be re-applied here by hand. Worth considering a `SettingsItem` variant with an
   optional leading drag-handle slot rather than a fully separate implementation.

### Screen-specific findings

- **`HeaderItemsSettingsScreen.kt`** — the drag handle's actual touch/gesture-detection area is
  `dragHandleModifier.size(20.dp)` (line 168), well under the 48×48dp minimum guidelines §8 requires for
  interactive targets — the icon itself is 20dp and the drag gesture (`detectDragGesturesAfterLongPress`,
  lines 74-105) is attached to that same undersized region, with no larger invisible hit-target padding
  around it. There's also no TalkBack-reachable equivalent for the reorder gesture (no
  `CustomAccessibilityAction`, unlike `SubTasksValueEditor.kt`'s reorder rows, which the Reminders audit
  documented as this app's reference pattern for exactly this problem) — a screen-reader user has no way to
  reorder header sections at all today.
- **`ProVersionScreen.kt`** — this is the group's clearest "hero moment" candidate (a marketing/upsell
  screen with its own `AnimatedGradientBackground` treatment), but its typography is entirely baseline: the
  "Pro advantages" headline (`headlineSmall`, line 86) and every advantage line (`titleMedium`, line 102)
  use plain baseline styles with only a `tertiary` color accent for emphasis — no `xxxEmphasized` token
  anywhere. Per §3, `MaterialTheme.typography.headlineSmallEmphasized` etc. has been usable with zero
  further wiring since the 1.5.0-alpha27 bump, so this screen could adopt real emphasized type today rather
  than relying on color alone to carry the "hero" weight — a more spec-correct match for tactic #7
  ("reserve 1-2 hero moments per screen") than any other screen in this group, since it's the one screen
  here explicitly designed to stand out.
- **`OtherSettingsScreen.kt`** — the "Permissions" (line 114) and "Allow Permission" (line 119)
  `SettingsItem` rows omit `icon`, while every other row in the same list (Privacy Policy, Terms,
  Troubleshooting, Feedback, Rate, Tell Friends, What's New, Gemini Functions, AI Digest, Buy Me a Coffee,
  Open Source Licenses, About) has one — a minor internal-consistency gap rather than an M3 compatibility
  issue, but worth a one-line fix (`DrawableCatalog`/`AppIcons` already has permission-related icons
  elsewhere in the app) since `SettingsItem`'s icon column reserves the same width whether or not an icon
  is supplied, leaving those two rows visually mis-aligned against their siblings.
- **`CloudBackupSettingsScreen.kt`** — uses a plain `CircularProgressIndicator` (line 89) for the
  in-progress "please wait" state. Not a compliance gap (progress indicators are still valid, §9.3), but a
  reasonable candidate for the newer `LoadingIndicator` component (recommended replacement for most
  indeterminate <5s waits) if/when this app starts adopting that component elsewhere — optional, not urgent.
- **`OtherNavGraph.kt`'s `SettingsWebView`** (lines 230-263) — confirmed to be the shared implementation
  behind `PermissionsEntry`, `OssEntry`, `PrivacyPolicyEntry`, `TermsEntry`, and `GeminiFunctionsEntry`, the
  same reuse pattern the Reminders audit found for `ReminderHelpScreen`/`RecurHelpScreen`'s WebView hosting.
  Audited once here rather than per-entry. All 5 entries correctly go through `SettingsScaffold` for their
  app bar (so they inherit cross-cutting findings #1/#2 above, but introduce no new gaps of their own) —
  the `WebView` itself has no Compose-layer M3 surface area to audit; any further improvement would mean
  restyling the bundled HTML/CSS assets (`permissions.html`, `oss.html`, `app_functions.html`), which is
  out of scope for a Compose-focused pass, matching the same conclusion the Reminders audit reached for
  `ReminderHelpScreen`/`RecurHelpScreen`.
- **`DigestSettingsScreen.kt`** and **`TroubleshootingScreen.kt`**'s non-empty-state paths — both are
  minimal, correctly reuse `SettingsItem`/`SettingsSwitchItem`, and introduce no findings beyond the
  cross-cutting `SettingsScaffold` issues above. `TroubleshootingScreen.kt`'s battery-optimization `Card`
  (lines 57-93) and empty-state Lottie illustration (lines 96-116) both use baseline components with no
  off-scale elevation or shape overrides — positive compliance, nothing to flag.

### Suggested fix order

Cheapest-and-highest-value first: (1) `SettingsScaffold.kt:33`'s `contentDescription = null` → a real
back/close label (mirroring `ProVersionScreen`'s `renderAsDetailPane`-aware pattern where relevant) — one
line, fixes accessibility on 10 screens at once (shared with §11's identical finding); (2)
`SettingsScaffold.kt:37` → point at the shared `TopAppbarColor` token instead of the hardcoded
`topAppBarColors(...)` call — same leverage, same file; (3) extract `CloudServicesScreen`/
`WhatsNewScreen`/`ProVersionScreen`'s duplicated gradient-header `Row` into one shared `ui-common`
composable, fixing the sub-48dp `IconButton` touch target and the literal `RoundedCornerShape(20.dp)`/
hand-blended alpha pair in one place instead of three; (4) give `HeaderItemsSettingsScreen`'s drag handle a
real 48dp hit target and a TalkBack-reachable reorder action, following `SubTasksValueEditor`'s
already-documented reference pattern; (5) once per-screen emphasized-type adoption starts elsewhere in the
app, `ProVersionScreen` is this group's strongest candidate given its existing hero-moment framing
(gradient background, tertiary accent) — swap `headlineSmall`/`titleMedium` for their `Emphasized`
counterparts, which cost nothing further to wire up.

## 13. Backup / Insights / Onboarding / Widget Configuration screens — audit

Audit pass over the 11 remaining screens in this group. **Audit only — no code changed in this pass.**
Files read in full: `LocalBackupScreen.kt`, `InsightsScreen.kt`, `PinLoginScreen.kt`, `PinInput.kt` (the
numeric keypad `PinLoginScreen` composes), `SingleNoteWidgetConfigScreen.kt`, `NotesWidgetConfigScreen.kt`,
`CalendarWidgetConfigScreen.kt`, `EventsWidgetConfigScreen.kt`, `BirthdaysWidgetConfigScreen.kt`,
`CombinedWidgetConfigScreen.kt`, `TasksWidgetConfigScreen.kt`, plus the two shared components all seven
widget-config screens are built on — `WidgetConfigScaffold.kt` and `ColorSlider.kt` — and
`ComponentColors.kt` (the `TopAppbarColor` token referenced below).

### Cross-cutting patterns (found on 2+ screens)

1. **Back/close-button `contentDescription = null`** — `LocalBackupScreen.kt:45`, `InsightsScreen.kt:59`,
   and `WidgetConfigScaffold.kt:46` all pass `null` for the leading `MenuIconButton`'s content description.
   Because `WidgetConfigScaffold` is the one shared `Scaffold`/`TopAppBar` shell reused by all 7
   widget-config screens (confirmed via `SingleNoteWidgetConfigScreen.kt`, `NotesWidgetConfigScreen.kt`,
   `CalendarWidgetConfigScreen.kt`, `EventsWidgetConfigScreen.kt`, `BirthdaysWidgetConfigScreen.kt`,
   `CombinedWidgetConfigScreen.kt`, `TasksWidgetConfigScreen.kt` — each just calls it with a
   title/callbacks), this single line is actually the same bug found in the Reminders audit (guidelines
   §8), now reproduced across 9 of this group's 11 screens from 3 code locations — the same
   "one shared-scaffold line fixes most of the group" shape already seen twice in the Settings audits
   (§11/§12). `PinLoginScreen.kt` is the positive counter-example here — both its close button (line 80)
   and fingerprint button (line 62) correctly pass a real `stringResource(R.string.cd_back)` /
   `stringResource(R.string.enter_your_pin)` via `TooltipIconButton`, matching the good pattern from
   `BuildReminderScreen.kt`/`ReminderHelpScreen.kt` in the Reminders group. Fixing
   `WidgetConfigScaffold.kt:46` alone resolves 7 of the 9 affected screens in one edit.
2. **`TopAppBar` `containerColor` sourced inconsistently** — `LocalBackupScreen.kt:47`,
   `InsightsScreen.kt:63`, and `WidgetConfigScaffold.kt:58` all call
   `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` directly
   instead of the shared `TopAppbarColor` token (`ui-common`'s `ComponentColors.kt:28`). This is the exact
   pattern flagged repeatedly across every prior audit group, now present in every `TopAppBar`-having
   screen in this group (9 of 11 — all except `PinLoginScreen`, which has no `TopAppBar`). It's not just a
   container-color match by coincidence: `TopAppbarColor` also pins `titleContentColor = onBackground`,
   which these three call sites leave at `TopAppBarDefaults`' own default instead — a second, smaller drift
   beyond just bypassing the token.
3. **Raw `R.drawable.*` / `painterResource(R.drawable.*)` instead of the `DrawableCatalog`/`AppIcons`
   catalog, despite the icon already being cataloged** — `WidgetConfigScaffold.kt:45`
   (`painterResource(R.drawable.ic_fluent_dismiss)`, shared by all 7 widget-config screens),
   `PinLoginScreen.kt:65,88`, and `PinInput.kt:99,117` (`ic_fluent_dismiss`, `ic_fluent_fingerprint`,
   `ic_fluent_text_asterisk`) all look up a drawable resource ID directly. This is a real CLAUDE.md
   convention violation, not a judgment call: `DrawableCatalog.Fluent.Dismiss` / `AppIcons.Fluent.Dismiss`
   and `AppIcons.Fluent.TextAsterisk` already exist (`ui-common`'s `DrawableCatalog.kt:116`,
   `AppIcons.kt:62,128`) — these call sites just aren't using them. `LocalBackupScreen.kt` and
   `InsightsScreen.kt`, by contrast, correctly use `AppIcons.Builder.ArrowLeft` — so, like finding #1, this
   is a "match your sibling file" fix in the same module family (`ui-common/login`), not new research. The
   widget-config mock-preview icons (`ic_fluent_settings`, `ic_fluent_add`, `ic_fluent_chevron_left/right`,
   etc., e.g. `CalendarWidgetConfigScreen.kt:103-136`) are excluded from this finding — they're purely
   decorative pixels inside a static "what the widget will look like" graphic, not real navigable icons, so
   the bypass there is far lower-value to fix.
4. **Hand-blended `.copy(alpha = ...)` for de-emphasis instead of `onSurfaceVariant`** — heaviest in
   `InsightsScreen.kt`: `StreakCard` (lines 176, 181), `RoutineInsightCard` (lines 206, 211, 221), and
   `InsightsEmptyState` (lines 239, 244) all use
   `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f/0.5f/0.3f)` for secondary/caption text and the
   empty-state icon — 7 call sites in one file, and `onSurfaceVariant` is never used anywhere in it. Same
   anti-pattern as the Reminders audit's cross-cutting #4; the fix is a straight role-swap, no new pattern
   needed.
5. **`ColorSlider` (shared by every widget-config screen) has no accessibility semantics and an under-48dp
   touch target** — `ui-common`'s `ColorSlider.kt` is a hand-rolled `Canvas` + `pointerInput` drag/tap
   picker (lines 43-108) with no `Modifier.semantics`, no `progressSemantics()`, and no real M3
   `Slider`/`ListItem` role behind it — it's invisible to TalkBack/switch-access entirely (guidelines §8's
   "standard platform controls over custom-built equivalents" and the accessibility landmark/state-layer
   requirements in §7/§8 don't apply because there's no semantics node at all to carry them). It's also
   sized at only `height(36.dp)` in `SingleNoteWidgetConfigScreen.kt:106` and `height(40.dp)` everywhere
   else it's used (`NotesWidgetConfigScreen.kt:64`, `CalendarWidgetConfigScreen.kt:67`,
   `EventsWidgetConfigScreen.kt:78`, `BirthdaysWidgetConfigScreen.kt:67`,
   `CombinedWidgetConfigScreen.kt:105`, `TasksWidgetConfigScreen.kt:67`) — below the 48×48dp minimum
   interactive-target guidelines §8 calls out. Since every one of the 7 widget-config screens uses
   `ColorSlider` for at least one (`SingleNoteWidgetConfigScreen` uses it twice) of its color pickers, this
   is the single highest-reach accessibility gap in the whole group — note this is a *second*, independent
   hand-rolled color-picker component from the `ColorSlider`/`ColorPickerCard` pair the Groups/Tags/Places
   audit (§9) called out as genuinely spec-correct; this widget-config copy is a materially different,
   lower-quality implementation, not the same component reused.
6. **Baseline `CircularProgressIndicator` instead of the newer Loading indicator component** —
   `LocalBackupScreen.kt:95` (in-progress state) and `InsightsScreen.kt:70` (list loading state) both use
   the pre-Expressive `CircularProgressIndicator`. Guidelines §9.3 recommends the new Loading indicator
   component as the default replacement for most <5s indeterminate waits like these. Low urgency
   (visual-only swap, no behavior change) — noting once here rather than flagging it twice below.

### Screen-specific findings

- **`LocalBackupScreen.kt`** (Export/Import — same file, two `LocalBackupMode` variants) — otherwise a
  clean, small form screen: `OutlinedTextField` error state is visually obvious (`isError` + inline error
  `Text` in `MaterialTheme.colorScheme.error`, lines 82-90), the primary action is a real filled `Button`
  (no deprecated FAB/extended-FAB misuse), and there's no attempt at a bespoke multi-pane layout for what is
  fundamentally a single-purpose dialog-shaped form — appropriate per guidelines' "not every screen needs
  breakpoint logic."
- **`InsightsScreen.kt`** — chart bars in `WeeklyTrendCard` (lines 135-144) correctly source their fill
  from `MaterialTheme.colorScheme.primary` rather than a hardcoded hex, and use
  `RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)` which lands exactly on the "extra small"
  shape-scale step (guidelines §4.1) — both good, worth calling out positively since this is the one screen
  in the group with an actual data-viz element. All three card types (`WeeklyTrendCard`, `StreakCard`,
  `RoutineInsightCard`) consistently use `MaterialTheme.colorScheme.surfaceContainer`, so — unlike
  `PreviewReminderScreen.kt` in the Reminders audit — there's no inconsistent surface-container-level drift
  here.
- **`PinLoginScreen.kt` / `PinInput.kt`** (Activity-hosted via `PinLoginActivity.kt`, same directory) —
  `PinInput.kt`'s `PinDigitButton` (lines 130-148) hand-rolls the numeric keypad buttons as a raw
  `Surface(onClick = ...)` with `CircleShape` and `tonalElevation = 2.dp` / `shadowElevation = 1.dp`,
  instead of a real M3 `IconButton`/`FilledIconButton` (which now supports round shape up to XL size
  natively per guidelines §9.1). `2.dp` tonal elevation is off-scale — not one of the defined levels
  (0/1/3/6/8/12dp, guidelines §5), and sits awkwardly between "elevated card" (1dp) and "scrolled app bar"
  (3dp) — the same off-scale-elevation `PinDigitButton` finding the Settings part A audit (§11) already
  made for this exact shared component. On the positive side: the 64dp `DigitButtonSize` comfortably clears
  the 48×48dp minimum touch target (guidelines §8), `Surface(onClick=...)` does get the default
  ripple/state-layer treatment for free even though it isn't a "real" button component, and — as noted in
  cross-cutting #1 — this screen has the group's only fully-correct back/action-button content
  descriptions. The dot indicator row (`PinDots`, lines 109-128) uses `.copy(alpha = 0f)` to hide
  not-yet-entered digits; that's a legitimate show/hide technique, not the de-emphasis anti-pattern in
  cross-cutting #4, so not flagged as a gap.
- **Widget Configuration screens (all 7)** — genuinely consistent with each other structurally: every
  screen is `WidgetConfigScaffold { ... }` plus a `*WidgetMockPreview` composable and one or more
  `ColorSlider`s inside a `Card(containerColor = surfaceContainer)`, so any fix to the shared scaffold or
  `ColorSlider` (findings #1, #2, #3, #5) fixes all 7 screens at once rather than needing 7 separate
  patches. One small, purely CLAUDE.md-convention inconsistency inside that otherwise-uniform group:
  `CombinedWidgetConfigScreen.kt:34` puts `modifier: Modifier = Modifier` first in its parameter list (the
  correct position per this repo's convention), while `SingleNoteWidgetConfigScreen.kt:53`,
  `NotesWidgetConfigScreen.kt:38`, `CalendarWidgetConfigScreen.kt:38`, `EventsWidgetConfigScreen.kt:47`,
  `BirthdaysWidgetConfigScreen.kt:38`, and `TasksWidgetConfigScreen.kt:38` all put it last; the same split
  repeats one level down in the private `*WidgetMockPreview` composables (`BirthdaysWidgetMockPreview` puts
  `modifier` first at line 76, while `NotesWidgetMockPreview`, `CalendarWidgetMockPreview`,
  `EventsWidgetMockPreview`, and `TasksWidgetMockPreview` all put it last). Not an M3 spec issue, just
  worth folding in if these files are touched for the fixes above. The `home_screen_widget_corner_radius`
  dimen (8dp, `extensions/appwidgets/src/main/res/values/dimens.xml:3`) used for every mock-preview's
  rounded corner lands on the "small" shape-scale step and is appropriately *not* routed through
  `AppShapes` — it has to mirror the actual home-screen widget's own corner treatment (a platform/launcher
  -level shape decision), not this app's in-app chrome, so a literal dimen resource here is the right call,
  not a token-hygiene gap.
- **`EventsWidgetConfigScreen.kt`** — the only widget-config screen that puts its text-size control behind
  an `AlertDialog` (lines 84-112) with two `TextButton` actions, rather than inline on the screen the way
  `SingleNoteWidgetConfigScreen.kt` does for its own text-size `Slider` (lines 83-97). Both are individually
  spec-correct (a basic M3 dialog with text-button actions is a legitimate pattern), but it's a minor
  cross-screen UX inconsistency worth a second look if these two screens are ever touched together — not a
  compliance gap on its own.

### Suggested fix order

Cheapest-and-highest-value first: (1) the single `WidgetConfigScaffold.kt:46` content-description fix,
which resolves 7 of the 9 affected screens at once, plus the two remaining one-line fixes in
`LocalBackupScreen.kt:45` and `InsightsScreen.kt:59`; (2) route `WidgetConfigScaffold.kt:45`,
`PinLoginScreen.kt:65,88`, and `PinInput.kt:99,117` through the already-cataloged `AppIcons.Fluent.Dismiss`
/ `.Fingerprint` / `.TextAsterisk` instead of raw `painterResource(R.drawable.*)`; (3) swap the three
bypassed `TopAppBar` `containerColor` call sites (`LocalBackupScreen.kt:47`, `InsightsScreen.kt:63`,
`WidgetConfigScaffold.kt:58`) to the shared `TopAppbarColor` token; (4) `InsightsScreen.kt`'s 7
`.copy(alpha = ...)` call sites → `onSurfaceVariant`; (5) `ColorSlider` is the one item here that needs
real design/engineering time rather than a token swap — giving it semantics (a `Slider`-equivalent role so
screen readers can operate it) and raising its touch-target height to 48dp is the highest-value fix in the
whole group since it silently affects every widget setup screen the app ships, but it's also the one most
likely to need a follow-up design pass rather than a same-PR mechanical fix.

## 14. Shared-scaffold fixes — landed

First implementation pass following the §6–§13 audits, targeting the two highest-leverage findings: both
were a single shared `Scaffold`/`TopAppBar` wrapper with a `contentDescription = null` back button and a
`TopAppBar` color hardcoded instead of the shared `TopAppbarColor` token, each silently affecting dozens of
screens at once. Verified via `./gradlew :feature:feature-settings:compileDebugKotlin
:extensions:appwidgets:compileDebugKotlin :app:compileProDebugKotlin` (all green).

**`SettingsScaffold.kt`** (`feature-settings`) — this scaffold's leading icon isn't always a back arrow: a
`navigationIcon: Int` parameter lets callers swap in a close (X) icon via `settingsNavigationIcon(screenTitle,
renderAsDetailPane)` when there's nothing to "go back" to (opened directly with a caller-supplied title, or
rendered as a two-pane detail pane). Fixing the content description correctly meant matching that same
conditional, not just hardcoding one string:
- Added `settingsNavigationContentDescription(screenTitle, renderAsDetailPane)`, a `@Composable` twin of
  `settingsNavigationIcon` returning `acc_close` or `cd_back` for the same inputs.
- Added a `navigationContentDescription: String = stringResource(R.string.cd_back)` parameter to
  `SettingsScaffold`, wired into the `MenuIconButton`'s `contentDescription` (previously `null`).
- Replaced `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` with
  `colors = TopAppbarColor` (the shared `ui-common` token, which also correctly sets `titleContentColor`).
- Updated all 11 call sites across the app that pass a non-default `navigationIcon =
  settingsNavigationIcon(...)` to also pass the matching `navigationContentDescription =
  settingsNavigationContentDescription(...)`, so the two stay in sync per screen:
  `SettingsNavGraph.kt` (7 sites: Backup, General, Calendar, AI Digest, Note, Developer, Troubleshooting),
  `SecurityNavGraph.kt` (Security), `OtherNavGraph.kt` (Other Settings), and `app`'s
  `SettingsCrossFeatureEntries.kt` (2 sites: Reminders Settings, Birthday Settings). Call sites that never
  override `navigationIcon` (Settings Hub, Manage Presets, the 3 PIN screens, Location, Map Style, Cloud
  Backup, Permissions, OSS Licenses, Privacy Policy, Terms, Gemini Functions, Header Items) needed no
  per-file change — they pick up the new default automatically.
- **Not fixed by this change**: `CloudServicesScreen.kt`, `WhatsNewScreen.kt`, and `ProVersionScreen.kt`
  don't use `SettingsScaffold` at all (§12) and were already correct on this specific finding, but still
  carry their own duplicated-gradient-header gaps (§12 finding #3) — untouched here.

**`HolidayCountryScreen.kt`** (`feature-settings/calendar/country`) — this screen independently duplicated
both bugs outside `SettingsScaffold` (§11 finding) with its own hand-rolled `Scaffold`/`TopAppBar`. Since
it's a plain back-only screen (no detail-pane title override, no save action), the fix was to delete the
hand-rolled top bar entirely and switch to `SettingsScaffold`, rather than patch the two bugs in place —
one fewer ad hoc `Scaffold` to keep in sync with the shared token/description conventions going forward.
Removed the now-unused `Scaffold`/`TopAppBar`/`TopAppBarDefaults`/`MenuIconButton`/`ExperimentalMaterial3Api`
imports. Verified via `./gradlew :feature:feature-settings:compileDebugKotlin` and
`:feature:feature-settings:detekt` (no findings in this file).

**`WidgetConfigScaffold.kt`** (`extensions/appwidgets`) — simpler case: the leading icon here is always a
fixed dismiss/X icon (`ic_fluent_dismiss`), not a conditional back-arrow, so the fix is a straight content
description (`stringResource(R.string.acc_close)` — "Close" is the semantically correct label for a dismiss
icon, not "Move back") plus the same `colors = TopAppbarColor` swap. Resolves the same-shaped bug for all 7
widget-config screens at once (Single Note, Notes, Calendar, Events, Birthdays, Combined Buttons, Google
Tasks) since every one of them is built on this one shared composable. `ui.common.R.string.acc_close`
resolves through the existing `com.github.naz013.appwidgets.R` import — this repo builds with
`android.nonTransitiveRClass = false`, so a dependency module's resources are already visible on a
downstream module's own `R` class without a separate import.

**Screens now carrying a partial fix** (back-button label + app-bar color token corrected; other
per-screen findings from §6–§13 — hardcoded shapes, alpha-blended de-emphasis, deprecated components, etc.
— are unchanged and still open): Settings Hub, General Settings, Backup Settings, Reminders Settings,
Manage Presets, Calendar Settings, Birthday Settings, Note Settings, Location Settings, Map Style, Security
Settings, Add/Change/Disable PIN, Cloud Backup Settings, Other Settings, Permissions, Open Source Licenses,
Privacy Policy, Terms of Service, Gemini Functions, AI Digest Settings, Header Items Settings,
Troubleshooting, Select Holiday Country (25 Settings screens via `SettingsScaffold`), plus Single
Note/Notes/Calendar/Events/Birthdays/Combined Buttons/Google Tasks Widget Config (7 screens via
`WidgetConfigScaffold`) — 32 screens total. Tracked as "In progress" rather than "Done" in
`m3-expressive-screen-inventory.md` since this is one fix out of several still open per screen.

## 15. Gradient-hero header/card dedup — landed

Fixed §12 finding #3: `CloudServicesScreen.kt`, `WhatsNewScreen.kt`, and `ProVersionScreen.kt` each
hand-rolled an identical status-bar-padded header `Row` (leading spacer, `TooltipIconButton`-wrapped
`IconButton`) and an identical translucent content-card `Surface`, both repeated verbatim three times with
the same two M3 gaps in each copy.

**New shared composables** (`ui-common`, `com.github.naz013.ui.common.compose.foundation.component`,
alongside the `AnimatedGradientBackground` they're meant to be used on top of):
- **`GradientScreenHeader.kt`** — the back/close action row. Takes `onBackClick`, `contentDescription`, and
  an optional `icon` (defaults to the back arrow; `ProVersionScreen` overrides it to the dismiss icon when
  `renderAsDetailPane`). Fixes the sub-48dp touch target directly: the old per-screen copies hardcoded
  `Modifier.size(40.dp)` on the `IconButton`, overriding its spec-correct 48dp default — the shared version
  drops that override and lets `IconButton` size itself correctly.
- **`GradientHeroCard.kt`** — the translucent content card. Takes an optional `verticalArrangement` (each
  screen's card needed a different one — `Arrangement.spacedBy(16.dp)`/`spacedBy(12.dp)`/default) and a
  `ColumnScope` content lambda. Uses a new `AppShapes.largeIncreased = RoundedCornerShape(20.dp)` token
  (added to `ui-common`'s `Shape.kt`) instead of the bare `RoundedCornerShape(20.dp)` literal that was
  previously repeated at 4 call sites (`CloudServicesScreen.kt` alone had two) — 20dp is already the
  spec-correct "large increased" step on the shape scale (guidelines §4.1), so this only names it, it
  doesn't change the value. The card-fill and back-button-chip alpha blends (`surface.copy(alpha = 0.85f)`,
  `background.withAlpha(0.25f)`) are legitimately intentional here — a translucent scrim/card is the
  correct effect over an animated gradient, not a token-bypass mistake like the `SettingsScaffold`/
  `WidgetConfigScaffold` cases in §14 — so they're kept as alpha blends, just centralized as single private
  constants inside the two new composables instead of being retyped at 6 call sites across 3 files.

All three screens (`CloudServicesScreen.kt`, `WhatsNewScreen.kt`, `ProVersionScreen.kt`) were rewritten to
call `GradientScreenHeader`/`GradientHeroCard` instead of hand-rolling the `Row`/`Surface` inline, dropping
now-unused `Row`/`IconButton`/`Icon`/`TooltipIconButton`/`CircleShape`/`RoundedCornerShape`/`clip`/
`background`/`withAlpha`/`size`/`statusBarsPadding`/`width` imports per file. Verified via
`./gradlew :ui:ui-common:compileDebugKotlin :feature:feature-settings:compileDebugKotlin
:app:compileProDebugKotlin` (all green) and `:ui:ui-common:detekt :feature:feature-settings:detekt` (zero
findings in any of the 5 touched/new files).

**Screens fixed**: Cloud Services, What's New, Pro Version — the 3 screens in the Settings group that were
explicitly called out in §14 as *not* covered by the `SettingsScaffold` fix (they don't use that scaffold).
Combined with §14, all Settings-group screens with a shared-chrome gap identified in §11/§12 now have that
specific gap fixed; per-screen findings (deprecated components, ad hoc `FontWeight`, etc.) from §6–§13 are
unaffected and still open.

**Suggested next step**: no more shared-scaffold-level fixes remain identified in the Settings group audit.
Remaining work is per-screen (see each group's "Suggested fix order" in §6–§13) — e.g. Reminders' heaviest
`FontWeight`/motion gaps in `ReminderActionScreen.kt`, or the Widget Config group's `ColorSlider`
accessibility gap (no semantics, sub-48dp touch target, shared by all 7 widget-config screens) flagged in
§13 as the single highest-reach a11y gap in the whole audit.

## 16. `ColorSlider` accessibility fix — landed

Fixed §13 finding #5: `ui-common`'s `ColorSlider.kt` — a hand-rolled `Canvas` + `pointerInput` drag/tap
color-swatch strip — had no accessibility semantics at all (invisible to TalkBack/switch-access) and a
sub-48dp touch target at every call site.

**Correction to §13/§9 while investigating**: the original audit described the widget-config screens'
`ColorSlider` as "a *second*, independent hand-rolled color-picker component... a materially different,
lower-quality implementation, not the same component reused" as the one `ColorPickerCard.kt` uses for
Groups/Tags/Places (§9's genuinely-spec-correct callout). That's not the case — there is exactly one
`ColorSlider` composable in `ui-common`, and it's shared much more widely than either audit section
realized: the 7 widget-config screens, `ColorPickerCard.kt` (Groups/Tags/Places, and transitively
`RoutineColorPicker.kt`), `ColorPickerDialog.kt`, the map style picker (`MapPickerCards.kt`), and 4 call
sites in the note editor's background/gradient color panels (`NoteEditPanels.kt`) all render through this
one component. That widens this fix's reach well beyond the Widget Config group §13 scoped it to.

**The fix** (`ColorSlider.kt`):
- Added a `contentDescription: String` parameter (default: the existing, already-localized
  `R.string.acc_select_color` = "Select color") and a `Modifier.semantics { }` block giving the strip an
  adjustable, "slider-equivalent" accessibility role: `contentDescription`, a `progressBarRangeInfo` sized
  to the swatch count (so TalkBack announces the current position), and a `setProgress` action so swiping
  up/down while focused moves the selection — there was previously no semantics node here at all, not even
  a click action, since a bare `Canvas` + `pointerInput` is invisible to accessibility services by default.
- The sub-48dp touch target isn't fixable from inside the component: height is entirely caller-supplied via
  `modifier` (`.height(36.dp)`/`.height(40.dp)` at every call site, below the 48×48dp minimum), and an
  outer `.height()` constraint can't be widened by anything the component adds internally. Fixed by bumping
  every call site's explicit height to `48.dp` instead.

**Call sites updated** (14 total, all passing a `contentDescription` where a meaningfully specific one was
already available nearby, falling back to the generic default otherwise):
- **Widget Config** (7 screens, `extensions/appwidgets`): `CalendarWidgetConfigScreen.kt`,
  `EventsWidgetConfigScreen.kt`, `CombinedWidgetConfigScreen.kt`, `BirthdaysWidgetConfigScreen.kt`,
  `TasksWidgetConfigScreen.kt`, `NotesWidgetConfigScreen.kt` — each already had a `Text(stringResource(
  R.string.background))` label directly above its `ColorSlider`, reused as `contentDescription`.
  `SingleNoteWidgetConfigScreen.kt` has two sliders, similarly labeled `R.string.text_color` and
  `R.string.foreground_color`.
- **`ColorPickerCard.kt`** (`ui-common`, backs Groups/Tags/Places and `RoutineColorPicker.kt`) — passes its
  existing `title` parameter straight through as `contentDescription`, so the card's own visible label and
  its accessibility label always match.
- **`ColorPickerDialog.kt`** (`ui-common`) — reuses the dialog's own `title`/`titleRes`, falling back to the
  generic default if the caller passed neither.
- **`MapPickerCards.kt`**'s `MarkerStyleCard` (`ui-map`) — no adjacent label existed, so this one got a new
  reference to the existing `R.string.style_of_marker` string (already used elsewhere in the same picker
  flow) rather than the generic default.
- **`NoteEditPanels.kt`** (`feature-note`, 4 sliders): the two gradient sliders reuse their existing
  adjacent `R.string.gradient_start_color`/`R.string.gradient_end_color` labels; the plain background-color
  slider (`ColorPanel`) and the solid-fill slider (`SolidColorControls`) had no adjacent label, so both keep
  the generic default.

No new string resources were added — every `contentDescription` override reuses a string that was already
defined and already fully localized across all 26 locales, which is why this fix didn't need the usual
"append to every `values-*/strings.xml`" step from `CLAUDE.md`'s string-resource convention.

Verified via `./gradlew :ui:ui-common:compileDebugKotlin :ui:ui-map:compileDebugKotlin
:ui:ui-routine:compileDebugKotlin :feature:feature-note:compileDebugKotlin
:extensions:appwidgets:compileDebugKotlin :app:compileProDebugKotlin` (all green) and detekt on every
touched module — zero new findings; the handful detekt reported were pre-existing debt on untouched lines
(confirmed by diffing line numbers against the actual edits) plus two already-known pre-existing issues
(`SingleNoteWidgetConfigScreen.kt`'s import ordering, `RoutineColorPicker.kt`'s `routineColorSliderTestTag`
naming) neither of which this change touches.

**Not fixed**: the drag/tap gesture itself still isn't independently focusable per-swatch (TalkBack gets
one adjustable node for the whole strip, not N discrete "select red"/"select blue" targets) — a fuller fix
would restructure the component from one `Canvas` into N individually-`selectable()` composables, which is
a larger rework than this pass scoped. The current fix is a genuine, real improvement (nothing → an
operable adjustable control with a real label) but not full parity with a native M3 `Slider`/segmented
control's accessibility behavior.

**Suggested next step**: no further shared-component-level a11y gaps are currently identified. Remaining
work is per-screen per each group's "Suggested fix order" in §6–§13 — e.g. Reminders' `FontWeight`/motion
gaps in `ReminderActionScreen.kt`, or Calendar's sub-48dp `TimelinePager.kt` event blocks (§10).

## 17. `ReminderActionScreen.kt` type/elevation/color-token fixes — landed

Fixed 3 of §6's findings on this screen (cross-cutting #3 and #4, plus the screen-specific card-elevation
note) — the manual `FontWeight` overrides, the alpha-blended de-emphasis, and the off-scale card elevation.
The alarm/ringing screen was flagged as this app's single strongest "hero moment" candidate, so it was the
natural first place to try real emphasized type. `MapEditorScreen.kt`'s and `SubTasksValueEditor.kt`'s
literal `tween()` motion gap (cross-cutting #6) is a *different* screen's finding, not this one's — despite
how the "Suggested next step" line above reads, `ReminderActionScreen.kt` itself has no animation code at
all to fix; motion in this group remains open for those two files.

**Precedent check before touching anything**: §3's note that `MaterialTheme.typography.xxxEmphasized`
adoption was still "Phase 2/3 work" turned out to be stale — `AgendaScreen.kt` and
`ChronologicalHomeScreen.kt` already use `titleMediumEmphasized`/`bodyMediumEmphasized`/
`headlineMediumEmphasized`/etc. in production, so the token exists and has real precedent in this exact
Material3 version; this fix follows that established pattern rather than introducing a new one.

**The fixes** (`ReminderActionScreen.kt`, all 5 header-content composables plus `ActionsSection`'s main
button):
- **9 manual `FontWeight` overrides → emphasized style tokens** — every `style = bodyLarge, fontWeight =
  FontWeight.Medium`/`style = titleMedium, fontWeight = FontWeight.Bold` pair (contact/email/app/link
  header name + detail lines) became a single `style = bodyLargeEmphasized`/`titleMediumEmphasized`
  reference; `SimpleHeaderContent`'s `headlineSmall + FontWeight.Bold` became `headlineSmallEmphasized`;
  `ActionsSection`'s main button label's `titleMedium + FontWeight.SemiBold` became `titleMediumEmphasized`.
  `LinkHeaderContent` had the identical `bodyLarge + FontWeight.Medium` pattern too — not named explicitly
  in §6's finding #3 list, but fixed for the same reason ("match the sibling row" already used to justify
  §6 finding #4). The now-fully-unused `FontWeight` import was removed.
- **3 ad hoc `.copy(alpha = 0.7f/0.6f)` de-emphasis call sites → plain `onSurfaceVariant`** — the contact
  phone number, the email address, and the email subject line (which also keeps its `FontStyle.Italic` —
  removing the ad hoc alpha doesn't touch the separate italic distinction). Matches finding #4 exactly.
- **Off-scale `CardDefaults.cardElevation(defaultElevation = 2.dp)` removed from both cards** (the header
  card and the todo-list card) — 2dp sits between the filled-card default (0dp) and elevated-card default
  (1dp) on the M3 elevation scale (guidelines §5) and isn't one of the 6 defined levels. Both cards already
  set an explicit `containerColor` (a "filled card" pattern that conventionally relies on color contrast
  rather than elevation for hierarchy), so removing the override entirely restores `CardDefaults`' own
  spec-correct 0dp default rather than picking a different magic number.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (both
green) and `:feature:feature-reminder:detekt` — the one new-looking hit
(`CyclomaticComplexMethod` on the top-level `ReminderActionScreen` composable) and every `Indentation` hit
in the file were confirmed pre-existing by diffing detekt's reported line numbers against the actual edited
hunks — none land inside a changed line; they're all in this file's untouched `when(event)` block, pre-
existing `Modifier` chain formatting, or the four `@Preview` composables (also untouched).

**Not fixed**: `ReminderActionScreen.kt`'s single landscape/portrait split (orientation-based, not the five
official breakpoints) and `SelectApplicationScreen.kt`/`PreviewReminderScreen.kt`'s hand-built list rows
and inconsistent surface-container roles remain open per §6. The group's actual motion gap
(`MapEditorScreen.kt`, `SubTasksValueEditor.kt`) is untouched by this pass.

**Suggested next step**: `MapEditorScreen.kt`'s literal `tween()` sheet-drag animation and missing
640dp bottom-sheet max-width, or `SubTasksValueEditor.kt`'s sub-48dp check/remove `IconButton`s (§6's
own strongest accessibility implementation in the group, bar this one gap) are the remaining concrete
items in the Reminders group's fix order.

## 18. `BirthdayActionScreen.kt` type/elevation/shape/color-token fixes — landed

§8 documented `BirthdayActionScreen.kt` as structurally near-identical to `ReminderActionScreen.kt`
(same `DeviceScreenConfiguration` portrait/landscape split, same header-card shape, same `SplitButton`
usage) and noted it "inherits that screen's exact findings" — its own suggested fix order explicitly called
for fixing both together "so the two alarm screens stay visually consistent." This section applies §17's
exact fix to this twin screen, plus one extra token swap §8 called out specifically for this file.

**The fixes** (`BirthdayActionScreen.kt`, its single `ContactHeaderContent` plus `ActionsSection`'s main
button — this screen has one header variant, not `ReminderActionScreen.kt`'s five):
- **6 manual `FontWeight` overrides → emphasized style tokens**: the name (`titleLarge` → `titleLargeEmphasized`),
  birthday date and age lines (`bodyLarge`/`bodyMedium` → their `Emphasized` equivalents), and the main
  action button label (`titleMedium` → `titleMediumEmphasized`) — same "weight" trigger as §17.
- **1 redundant `fontWeight = FontWeight.Normal` deleted outright**, not swapped — `FontWeight.Normal` is
  already the baseline default weight, so the override was a no-op to begin with; unlike the other 6 sites,
  there was no actual weight to preserve by moving to an emphasized token; the "contact name if different
  from text" line just needed the dead parameter removed.
- **1 ad hoc `.copy(alpha = 0.7f)` de-emphasis site → plain `onSurfaceVariant`** (the phone number line) —
  same as §17's finding #4 treatment.
- **Off-scale `defaultElevation = 2.dp` removed** from the one header card, same reasoning as §17.
- **`RoundedCornerShape(12.dp)` → `AppShapes.tile`** on the same header card — §8's own suggested fix order
  named this swap specifically (`BirthdayActionScreen.kt:233`, bundled with the elevation fix). `AppShapes.tile`
  is already defined as exactly `RoundedCornerShape(12.dp)`, so this is a pure token-naming change with zero
  visual difference — unlike §17's `ReminderActionScreen.kt`, which wasn't flagged for this same swap (its
  12dp corner radii weren't called out as a finding there, so left as-is to keep this pass scoped to what
  each screen's audit actually flagged).

The unused `FontWeight` and `RoundedCornerShape` imports were removed; `AppShapes` was added.

Verified via `./gradlew :feature:feature-birthday:compileDebugKotlin :app:compileProDebugKotlin` (both
green) and `:feature:feature-birthday:detekt` — this file carries substantially more pre-existing
`Indentation`/`ImportOrdering` debt than `ReminderActionScreen.kt` did (the whole file, including both
`@Preview` composables, already had it), but every single reported line was confirmed pre-existing the same
way as §17: diffed against the actual edited hunks, none land inside a changed line.

**Not fixed**: same as §17 — the orientation-based (not breakpoint-based) portrait/landscape split remains
open, and this screen has no motion code to fix either. The pre-existing `Indentation`/`ImportOrdering`
debt found throughout the file during verification is untouched (out of scope for this pass, and far larger
than what §8 itself flagged as a finding — not something to silently bundle in).

With §17 and §18 both landed, the two alarm/ringing screens are visually consistent again on every point §8
flagged them as twins on.

## 19. `MapEditorScreen.kt` motion/max-width/scrim-token fixes — landed

Fixed the remaining §6 finding on this file: cross-cutting #6 (literal `tween()` sheet drag/dismiss/settle
animations) plus its own two screen-specific notes (no 640dp bottom-sheet max-width, hardcoded scrim
color). This is the hand-rolled bottom-sheet clone used by the Arriving/Leaving map value editor — it can't
use the real `AppModalBottomSheet` because it needs to keep the embedded Google Map in the same composition
rather than a separate `Popup` window, so it reimplements drag/dismiss/scrim by hand.

**The fixes** (`MapEditorScreen.kt`):
- **2 `tween(200)` calls → `MaterialTheme.motionScheme.defaultSpatialSpec()`** — guidelines §6 classifies
  spring speed by tier: "fast" for small components (switches, buttons), "**default**" for partial-screen
  animations, and its own worked example for the default tier is literally "bottom sheet." Since that's
  exactly what this hand-rolled sheet is standing in for, this uses `defaultSpatialSpec()`, not
  `fastSpatialSpec()` — worth calling out because an earlier note on this same finding (§6's cross-cutting
  #6 text) suggested "fast" without checking the guidelines' own tier table first; the guidelines are the
  more authoritative source here and this fix follows them rather than that earlier loose paraphrase.
  `MaterialTheme.motionScheme` is `@Composable`-only, so the spec is resolved once into a
  `val sheetMotionSpec: AnimationSpec<Float>` in the composable body and captured by the local
  `dismiss()`/`settle()` functions (which aren't themselves `@Composable`), rather than calling
  `MaterialTheme.motionScheme` from inside them directly, which wouldn't compile.
- **640dp bottom-sheet max-width added** — `Modifier.fillMaxWidth().widthIn(max = 640.dp)` on the sheet's
  outer `Column` (guidelines §9.5's standard M3 bottom-sheet spec), so the sheet stops stretching
  edge-to-edge on Expanded/Large/XL breakpoints. No extra centering logic needed: the `Column` already sits
  inside `Alignment.BottomCenter` in the parent `BoxWithConstraints`, which centers on the child's actual
  measured width — once that width is capped at 640dp instead of the full screen, centering falls out for
  free.
- **Scrim color token** — `Color.Black.copy(alpha = scrimAlpha)` → `MaterialTheme.colorScheme.scrim.copy(alpha
  = scrimAlpha)`, per the finding's own direction. The now-unused `Color` and `tween` imports were removed.

The 28dp top-corner radius (`RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`) was confirmed
spec-correct by §6 already (bottom sheets are specced at exactly 28dp) and left as a literal rather than
forced into a token, matching that finding's own conclusion — not everything flagged in an audit needs a
change, only the parts actually called out as wrong.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (both
green) and `:feature:feature-reminder:detekt` — one `ImportOrdering` hit, confirmed pre-existing (the same
unsorted `com.github.naz013.*` import block pattern already seen in §17/§18's files, untouched by this
diff).

**Not fixed**: `SubTasksValueEditor.kt`'s `tween(CHECK_ANIMATION_MS)` (4 call sites, cross-cutting #6's
other named file) and its sub-48dp check/remove `IconButton`s are a different file, untouched by this pass.

**Suggested next step**: `SubTasksValueEditor.kt` is the last item explicitly named in §6's cross-cutting
findings without a landed fix — its check/uncheck scale+fade animation is the "fast effects spring"
counterpart to this section's "default spatial spring" (small-component motion vs. partial-screen motion,
per the same guidelines §6 tier table), and its sub-48dp `IconButton`s are a real accessibility gap on what
§6 called this group's otherwise-strongest accessibility implementation.

## 20. `SubTasksValueEditor.kt` motion/touch-target fixes — landed

Fixed the last unaddressed §6 finding: cross-cutting #6's second named file (`tween(CHECK_ANIMATION_MS)`,
4 call sites) plus the screen-specific note that its check/remove `IconButton`s are sized to 40dp, below
the 48×48dp minimum touch target guidelines §8/§1.4 call out. §6 called this row otherwise this group's
*strongest* accessibility implementation (per-row custom `CustomAccessibilityAction`s exposing a
TalkBack-reachable equivalent for the drag-to-reorder gesture, merged semantics content descriptions,
haptic feedback, test tags) — this fix closes its one real gap without touching any of that.

**The fixes** (`SubTasksValueEditor.kt`, `ShopItemRow`):
- **4 `tween(CHECK_ANIMATION_MS)` calls → `MaterialTheme.motionScheme` specs, split by category** — the
  checked/unchecked icon swap uses both `scaleIn`/`scaleOut` (a transform → spatial) and `fadeIn`/`fadeOut`
  (an opacity change → effects), so each got its own spec rather than reusing one for both:
  `scaleIn(checkSpatialSpec)`/`scaleOut(checkSpatialSpec)` from `fastSpatialSpec()`, and
  `fadeIn(checkEffectsSpec)`/`fadeOut(checkEffectsSpec)` from `fastEffectsSpec()`. "Fast" is the correct
  tier here per guidelines §6's own table (small components — switches, buttons — not §19's "default"
  partial-screen tier), matching what the earlier loose note on this finding already got right (unlike its
  guess on §19's sheet-drag speed). Both specs are resolved once per row into `val`s at the top of
  `ShopItemRow` and reused across both `AnimatedVisibility` blocks, rather than re-reading
  `MaterialTheme.motionScheme` 4 times. The now-unused `CHECK_ANIMATION_MS` constant and `tween` import
  were removed.
- **Both `IconButton`s' `Modifier.size(40.dp)` → a new `ROW_BUTTON_SIZE = 48.dp` constant** — the check
  toggle and the remove button. Bumping the remove button's outer size also happens to fix its icon's
  effective rendering size as a side effect: it wraps its `Icon` in `.fillMaxSize().padding(12.dp)`, so a
  40dp box rendered a cramped 16dp icon; at 48dp the icon renders at the standard 24dp. `ROW_HEIGHT` (a
  different, unrelated 40dp constant used only for the drag-reorder step-size calculation, not a touch
  target) was deliberately left untouched — conflating the two would have been a scope-creep mistake.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (both
green). Detekt flagged one real-looking hit worth explaining: `CyclomaticComplexMethod` on `ShopItemRow`
(complexity 16 vs. threshold 15) — none of this pass's edits added any branching (all four replacements
were argument swaps, no new `if`/`when`/loops), so before trusting that assumption this was verified
empirically rather than by inspection alone: `git stash` set the file back to its pre-edit state, detekt
was re-run, and it reported the identical complexity-16 finding on the untouched original — confirming this
is pre-existing debt, not a regression, before the stash was popped back.

**Not fixed**: this closes every item §6 named specifically by file. What's left in the Reminders group is
`SelectApplicationScreen.kt`/`PreviewReminderScreen.kt`'s hand-built list rows and inconsistent
surface-container roles, and the deprecated baseline `ExtendedFloatingActionButton` on
`ReminderFullscreenMapScreen.kt` — both lower-priority per §6's own suggested fix order, and neither touched
across §17-§20.

With §17 through §20 landed, every cross-cutting and screen-specific finding from §6 that was fixable
without a larger structural rework (list-row componentization, breakpoint-based layout) now has a landed
fix.

## 21. `SelectApplicationScreen.kt`/`PreviewReminderScreen.kt` list-row and surface-container fixes — landed

Fixed §6's last-remaining screen-specific finding for the Reminders group: hand-built list rows and
`PreviewReminderScreen.kt`'s surface-container roles with "no obvious rule for which container level each
section should get." Deliberately did **not** introduce Material3's `ListItem()` composable — nothing else
in this app uses it (the codebase's own convention for a clickable row is a hand-rolled `Card` + `Row`, e.g.
`ui-common`'s `AgendaListItem`/`BuilderListItemCard`), so switching just these two files to a different row
primitive would trade one inconsistency for another. Instead, each file's rows were brought in line with
patterns *already established elsewhere in the same file or module*.

**`SelectApplicationScreen.kt`** (`ApplicationListItem`): `Modifier.clickable` wrapping the `Card` → the
`Card`'s own `onClick` param, matching `BuilderListItemCard.kt`'s idiom for a clickable list row (merges
click semantics into the `Card`'s own node instead of layering a separate `clickable` modifier over it).
Removed the now-unused `clickable` import.

**`PreviewReminderScreen.kt`**:
- **Surface-container roles normalized** — `NoteRow`, `GoogleTaskRow`, `CalendarEventRow`, and
  `MapSection`'s `Card`s now explicitly set `containerColor = MaterialTheme.colorScheme.surfaceContainer`,
  matching what `DetailsCard`/`SubTasksSection` already used. This establishes the two-tier rule §6 found
  missing: `HeaderCard` (the hero summary card) keeps `surfaceContainerLow`, every other card on the screen
  now consistently uses `surfaceContainer` — no more silent default-`surface` fallback on 4 of 7 card
  sections.
- **`AttachmentRow`s de-duplicated into the file's own grouped-card pattern** — previously rendered as bare
  `Row`s directly as individual `LazyColumn` `items()`, unlike every other multi-row section on this screen
  (`DetailsCard`/`SubTasksSection` group their rows inside one `Card` with `HorizontalDivider`s between).
  Reused the existing `DetailsCard(rows: List<@Composable () -> Unit>)` composable directly —
  `state.attachments.map { file -> { AttachmentRow(file) } }` — the same pattern `detailRows(state)` already
  uses for the main details list, rather than inventing a new grouping composable. `CalendarEventRow` was
  deliberately left as its own individual `Card` per event (not grouped) since each row carries its own
  Open/Delete action buttons — grouping multiple action rows into one card was a plausible but separate
  design change, not something this finding called for.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (both
green) and `:feature:feature-reminder:detekt`. Detekt reported two `MaxLineLength` hits in
`PreviewReminderScreen.kt` (lines 473, 687 post-edit); both fall on lines untouched by this diff (the
offline-only detail row and the sub-task check icon's `iconColor` ternary) — pre-existing debt whose line
numbers shifted from the 8 lines this pass inserted, not new findings. No findings at all were reported
against `SelectApplicationScreen.kt`.

**Not fixed**: the deprecated baseline `ExtendedFloatingActionButton` on `ReminderFullscreenMapScreen.kt`
(§6's one remaining named item, a straightforward component swap) remains open. `TodoEditScreen.kt`'s
duplicated `OfflineOnlyRow` (worth deduplicating into `ui-common` regardless of any M3 fix) also remains
open — it's a code-sharing cleanup, not an M3-compliance gap.

**Suggested next step**: with §6's audit now fully worked through except the FAB swap and the
`OfflineOnlyRow` dedup, a natural next step is either of those two small, well-scoped Reminders items, or
moving to a different screen group entirely — Notes, Groups/Tags/Places, Calendar/Google Tasks, or
Workflow/Routines (§7-§13) — none of which have had fixes landed yet beyond incidental touches from the
shared-component work in §15-§16.

## 22. `ReminderFullscreenMapScreen.kt` deprecated baseline extended FAB — landed

Fixed §6's one remaining named item: the "move to place" button used the baseline
`ExtendedFloatingActionButton` (56dp pill shape), which guidelines §9.1's deprecation table flags directly —
"Baseline or surface-color extended FAB → Small extended FAB." This isn't a hand-rolled approximation the
app needs to build; M3 1.5.0-alpha27 (the version already on this project's classpath) ships a real
`SmallExtendedFloatingActionButton` composable with the same `text`/`icon`/`onClick`/`modifier` parameters as
the baseline one, so this was confirmed against the actual library source (extracted from the Gradle cache's
sources jar, not assumed from memory) before touching the file — its default shape resolves to
`FloatingActionButtonDefaults.smallExtendedFabShape` (`ExtendedFabSmallTokens.ContainerShape`), distinct from
the baseline's `extendedFabShape` (`ExtendedFabPrimaryTokens.ContainerShape`), which is exactly the "now
boxier" shape change the guidelines describe.

**The fix**: swapped the composable name only — `ExtendedFloatingActionButton` →
`SmallExtendedFloatingActionButton` — and its import. Every existing call-site argument (`onClick`, `icon`,
`text`, `modifier`) carried over unchanged since the two composables share the same parameter names; no
other behavior (position, padding, navigation-bar inset handling) changed.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (both
green) and `:feature:feature-reminder:detekt`. Detekt reported `Indentation` findings on lines 36-39 of the
file; `git diff` confirms those exact lines (the `modifier =` block) are untouched by this change — the
composable-name swap and its import are the only lines this diff touches — so this is pre-existing debt, not
a regression, without needing a stash-based check this time since the diff itself is the proof.

**Not fixed**: `TodoEditScreen.kt`'s duplicated `OfflineOnlyRow` — a code-sharing cleanup, not an
M3-compliance gap, and the only item left unaddressed from §6's full findings list.

**Suggested next step**: §6's Reminders audit is now fully closed out — every cross-cutting pattern and
every screen-specific finding either has a landed fix or (for the `OfflineOnlyRow` dedup) was explicitly
scoped out as non-M3 cleanup. The natural next step is moving to a different screen group — Notes,
Groups/Tags/Places, Calendar/Google Tasks, or Workflow/Routines (§7-§13) — none of which have had dedicated
fixes land yet beyond incidental touches from the shared-component work in §15-§16.

## 23. `TodoEditScreen.kt`/`BuildReminderScreen.kt` duplicated `OfflineOnlyRow` dedup — landed

Fixed §13's flagged code-sharing gap (not an M3-compliance defect on its own, but explicitly called out
alongside the rest of the Reminders findings): `TodoEditScreen.kt` and `BuildReminderScreen.kt` each had
their own private `OfflineOnlyRow` composable. On closer comparison the two were **not** byte-identical as
originally described ("verbatim") — `BuildReminderScreen.kt`'s version had extra padding on the row and
icon, plus a `start = 56.dp` indent on the description text that aligns it under the label rather than the
icon; `TodoEditScreen.kt`'s version had none of that, so its description sat flush left under the icon
instead of the label. `BuildReminderScreen.kt`'s version is the deliberately-aligned one (56dp exactly
accounts for the row's 8dp padding + icon's 8dp start padding + 24dp icon width + 16dp spacer), so it was
kept as the canonical version rather than splitting the difference or picking arbitrarily.

**The fix**: extracted `OfflineOnlyRow` into a new file,
`com.github.naz013.feature.reminder.compose.OfflineOnlyRow.kt`, marked `internal` per this repo's
module-visibility convention (used by two packages within the same `feature-reminder` module, no other
module needs it — doesn't belong in `ui-common`). Both `BuildReminderScreen.kt` and `TodoEditScreen.kt` now
import it instead of declaring their own copy; both call sites were otherwise unchanged. As a byproduct,
`TodoEditScreen.kt`'s screen now renders with the same (better) description-text alignment
`BuildReminderScreen.kt` already had — a real visual fix, not just a code-sharing one, since deduplication
necessarily meant picking one behavior.

Removed now-unused imports from both files: `TodoEditScreen.kt` lost `Icon`, `Spacer`, `Switch`,
`Alignment`, `Row`, and `width` (all only used by the now-removed local composable); `BuildReminderScreen.kt`
kept its `Icon`/`Spacer`/`Switch` imports since it still uses them elsewhere.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (clean,
zero warnings) and `:feature:feature-reminder:detekt`. Detekt's `Indentation` findings in both files and the
pre-existing `ImportOrdering` finding in `TodoEditScreen.kt` were confirmed pre-existing by diffing against
`git show HEAD` at the shifted line numbers (content identical, only offset by the lines this pass
added/removed) — the same pre-existing debt this file already carried before touching it. The new
`OfflineOnlyRow.kt` file itself has zero detekt findings. `NoUnusedImports` briefly fired on the first pass
(the `Row`/`width` imports weren't caught until this check) and was fixed before the final verification run.

This was the last item named across §6 (Reminders) and §13. Every finding from that audit pass now either
has a landed fix or was explicitly scoped out as out of range for M3 compliance work.

**Suggested next step**: move to a different screen group entirely — Notes, Groups/Tags/Places,
Calendar/Google Tasks, or Workflow/Routines (§7-§13) — none of which have had dedicated fixes land yet beyond
incidental touches from the shared-component work in §15-§16.

## 24. `NotesScreen.kt` back-button/app-bar-token/alpha-blend fixes — landed

§8 audited Notes and Birthdays together; this is the first fix landed for the Notes half specifically
(Birthdays' alarm screen was already fixed in §18). `NotesScreen.kt` carries three of that audit's
cross-cutting findings at once, all cheap and zero-judgment per §8's own "suggested fix order," so all three
landed together in one pass rather than three separate ones: it's a single file, and splitting a
three-line-touch across three PRs would have been process overhead for no real benefit.

**The fixes** (`NotesScreen.kt`):
- **Cross-cutting #1: back-button `contentDescription = null` → `stringResource(R.string.cd_back)`** — in
  `NotesTopBar`, shared by both Notes List and Notes Archive (`NotesArchiveEntry` reuses this exact
  composable per §8's own note), so one fix covers both inventory rows. Matches the sibling fix already
  pattern-established for the Reminders group (§14 onward) and the exact string other `feature-note` screens
  (`NoteEditScreen.kt`, `PreviewNoteScreen.kt`, `ImagePreviewScreen.kt`) already used correctly.
- **Cross-cutting #2: hand-rolled `TopAppBarDefaults.topAppBarColors(containerColor = background)` →
  `ui-common`'s shared `TopAppbarColor` token** — the hand-rolled version was missing the paired
  `titleContentColor = onBackground` half of the token, so this is a real (if currently invisible) drift
  risk, not just a style nit. Removed the now-unused `TopAppBarDefaults` import (its only use in this file).
- **Cross-cutting #3: `NotesEmptyState`'s `onSurface.copy(alpha = 0.3f)` / `0.5f` → `onSurfaceVariant`** —
  the icon tint and caption color, matching the equivalent fix already applied program-wide for this exact
  construct (Reminders' §6 flagged the same pattern; guidelines §2.2/§2.4 prefer the role token over a
  hand-blended alpha so future contrast-level changes reach it automatically).

**Not fixed** (deliberately, in this pass): `NotesScreen.kt:570`'s `SelectableOptionRow` `fontWeight = if
(selected) FontWeight.SemiBold else FontWeight.Normal` (cross-cutting #4) — §8 itself calls this the more
defensible case for a manual weight swap (state-driven "context" trigger per guidelines §3.1, not the
"already-bold-so-swap-for-consistency" trigger), and per the Reminders precedent (§17) this kind of type-role
swap only landed when doing a screen's full type-token pass, not as an isolated one-liner. Also out of scope
here: `NoteEditFloatingBar.kt`/`PreviewNoteReminderRow.kt`'s motion and shape/elevation findings (cross-cutting
#5-#7) — different files, not touched by this pass.

Verified via `./gradlew :feature:feature-note:compileDebugKotlin :app:compileProDebugKotlin` (clean, zero
warnings) and `:feature:feature-note:detekt`. Detekt reported `ArgumentListWrapping`/`MaxLineLength` (lines
588-591, `SortMenuButton`'s long `Triple(...)` option list) and `Wrapping` (line 340, a destructuring lambda
in `noteMenuItems`) — `git diff` confirms none of those lines were touched by this change, so all are
pre-existing debt.

**Suggested next step**: `NoteEditScreen.kt`/`NoteEditFloatingBar.kt` next — its off-scale
`shadowElevation`/`tonalElevation = 4.dp` and literal `tween()`/hand-tuned `spring()` calls (cross-cutting
#6-#7) are well-scoped, mechanical-ish fixes; or `PreviewNoteReminderRow.kt`'s `tween(250)` (also
cross-cutting #7). The Birthdays half of §8 (`BirthdaysScreen.kt`'s scroll-shadow app bar and bare
`Icons.Default.FilterList`, `EditBirthdayScreen.kt`/`PreviewBirthdayScreen.kt`'s back/close content-description
split) remains untouched by this Notes-scoped pass.

## 25. `NoteEditFloatingBar.kt` elevation/motion fixes — landed

Fixed §8's cross-cutting #6 and #7 findings for `NoteEditFloatingBar.kt`: the off-scale
`shadowElevation`/`tonalElevation = 4.dp` on the bar's `Surface`, and its literal `tween()` plus two
hand-tuned `spring(dampingRatio = ..., stiffness = ...)` calls.

**Elevation**: 4dp isn't one of the defined M3 resting levels (guidelines §5: 0/1/3/6/8/12dp). Guidelines
§5's own table lists "toolbar" explicitly under level 2 (3dp) — and §8's own audit text already names this
component as functionally a hand-rolled version of the M3 Expressive floating toolbar — so `3.dp` (not
`6.dp`, level 3, which the table reserves for FABs/dialogs/pickers/search) is the correct target, not an
arbitrary pick. Both `shadowElevation` and `tonalElevation` were changed to `3.dp`.

**Motion**: replaced both hand-tuned `spring()` calls (the bar's own press-scale feedback, and its one-time
entrance `scaleIn`) plus the `tween(FLOATING_BAR_ANIMATION_DURATION_MS / 2)` fade with
`MaterialTheme.motionScheme` specs. Classified as **fast** tier (not §19's "default" tier used for the
sheet-drag fix): guidelines §6 names bottom sheet/expanded nav rail as the default-tier example and
switches/buttons as the fast-tier example — this bar is a small floating control (closer in scale to a
button/FAB than a partial-screen surface), so fast fits better. Since the press-feedback scale and the
entrance scale are both spatial (size/scale) changes, they share one `fastSpatialSpec()` value
(`barSpatialSpec`) rather than resolving `motionScheme` twice; the fade is a separate `fastEffectsSpec()`
value (`barEffectsSpec`), since opacity is an effects change, not spatial. Removed the now-unused
`FLOATING_BAR_ANIMATION_DURATION_MS` constant and the `Spring`/`spring`/`tween` imports; added
`FiniteAnimationSpec`/`MaterialTheme`.

Verified via `./gradlew :feature:feature-note:compileDebugKotlin :app:compileProDebugKotlin` (clean, zero
warnings) and `:feature:feature-note:detekt` — zero findings against this file (the module-wide detekt task
still fails overall on unrelated pre-existing debt, as established in prior sections).

**Not fixed**: the larger "evaluate the real M3 Expressive floating toolbar component" question §8 raised
for this file remains explicitly out of scope — that's a component-replacement research task, not a
mechanical elevation/motion fix, and was never part of what this pass was asked to do.

**Suggested next step**: `PreviewNoteReminderRow.kt`'s `tween(250)` (cross-cutting #7, the last untouched
motion finding in the Notes half of §8) or `NotesScreen.kt:570`'s `SelectableOptionRow` `FontWeight` (§24's
deliberately-deferred cross-cutting #4). The Birthdays half of §8 remains entirely untouched.

## 26. `PreviewNoteReminderRow.kt` motion fix — landed

Fixed §8's last untouched motion finding (cross-cutting #7) in the Notes half of the audit: each attached
reminder card's staggered entrance used two separate `tween(REMINDER_ANIMATION_DURATION_MS)` calls (both at
250ms) — a `fadeIn` and a `slideInVertically`.

Replaced both with `MaterialTheme.motionScheme` specs, split by property type like every prior motion fix in
this series: `fadeIn` (opacity) → `fastEffectsSpec()`, `slideInVertically` (position) → `fastSpatialSpec()`.
Classified **fast**, not §19's "default" tier — this is a single card's per-item entrance inside a
horizontally-scrolling row, closer in scale to the small-component examples guidelines §6 names (switches,
buttons) than to a partial-screen surface like a bottom sheet. Both specs are resolved once per
`PreviewNoteReminderRow` call (hoisted above the `LazyRow`, not recomputed per item inside `itemsIndexed`),
matching the "resolve once, reuse across call sites" pattern already established in §20/§25. Removed the
now-unused `REMINDER_ANIMATION_DURATION_MS` constant and the `tween` import; added
`FiniteAnimationSpec`/`IntOffset` (the latter because `slideInVertically`'s spec is
`FiniteAnimationSpec<IntOffset>`, not `<Float>` — confirmed against `MotionScheme`'s actual generic
`fun <T> fastSpatialSpec(): FiniteAnimationSpec<T>` signature rather than assumed).

Verified via `./gradlew :feature:feature-note:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-note:detekt` — zero findings against this file.

This closes out every cross-cutting and screen-specific finding §8 raised for the **Notes** half of the
group (List/Archive: §24; Editor's floating bar: §25; Editor's attached-reminder row: this section) except
the one deliberately-deferred `SelectableOptionRow` `FontWeight` swap (§24's note on cross-cutting #4). The
**Birthdays** half of §8 (`BirthdaysScreen.kt`'s scroll-shadow app bar and bare `Icons.Default.FilterList`,
`EditBirthdayScreen.kt`/`PreviewBirthdayScreen.kt`'s back/close content-description split,
`PreviewBirthdayScreen.kt`'s own `tween()` calls) remains entirely untouched — a separate audit group from
what this pass was scoped to.

## 27. Birthdays half of §8 — back/close, app-bar token, alpha-blend, bare-icon, and motion fixes — landed

Worked through the Birthdays half of §8 across its three remaining screens (`BirthdayActionScreen.kt` was
already fixed in §18). **Correction first**: §20/§23 both claimed "§6 is fully closed" for the Reminders
group, but that was wrong — `RemindersArchiveScreen.kt` was named in §6 for the *exact same*
back-button-`null` bug and scroll-shadow-app-bar pattern fixed here, and it was never actually touched
across §14-§26. That screen's back-button bug and scroll-shadow app bar remain genuinely open in the
Reminders group; the "fully closed" language in those two sections was an overclaim and should be
disregarded for that specific item.

**`BirthdaysScreen.kt`** (3 fixes, same "cheap fixes in one file, one pass" bundling as §24):
- **Back-button `contentDescription = null` → `stringResource(R.string.cd_back)`** in `BirthdaysTopBar`
  (cross-cutting #1).
- **Bare `Icons.Default.FilterList` → `AppIcons.Fluent.Filter`** — a cataloged icon already existed
  (`DrawableCatalog.Fluent.Filter`/`AppIcons.Fluent.Filter`), so this was a straight swap, not a new catalog
  entry. Removed the now-unused `androidx.compose.material.icons.Icons`/`.filled.FilterList` imports — the
  only bare Material-icons-library reference in this screen (everything else already used
  `AppIcons`/`DrawableCatalog`).
- **`BirthdaysEmptyState`'s `onSurface.copy(alpha = 0.3f/0.5f)` → `onSurfaceVariant`** (cross-cutting #3),
  identical fix to §24's `NotesEmptyState` (§8 called these "almost certainly copy-pasted from one to the
  other").

**Deliberately not touched**: the scroll-shadow-on-scroll app bar (`Surface(shadowElevation =
animateDpAsState(...))`) and its `TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)`.
§8 suggested copying `ReminderHelpScreen.kt`'s `TopAppBarDefaults.enterAlwaysScrollBehavior()` pattern, but
on inspection that reference implementation doesn't actually wire `Modifier.nestedScroll(...)` to a
scrollable at all — it's a WebView screen manually forcing `scrollBehavior.state.contentOffset` from a raw
Android scroll listener, not real M3 scroll-driven app-bar behavior. Copying it verbatim would just be a
different hack. `BirthdaysScreen.kt` has a genuine `LazyColumn`, so it could be wired to `nestedScroll`
properly — but doing so surfaces a real design choice, not a mechanical swap: `enterAlwaysScrollBehavior()`
would make the whole `topBar` slot (app bar **and** the `SearchBar` beneath it, since both currently move
together under one `Surface`) slide off-screen on scroll-down, a real UX behavior change from today's
"always visible, just gains a shadow" behavior; `pinnedScrollBehavior()` keeps the app bar always visible
and would color-fill it automatically on scroll, but only the `TopAppBar` itself, not the wrapping
`Surface`/`SearchBar` — so the "both lift together" cue disappears. Neither is a drop-in match for the
current design without a product call, so this was left open rather than guessed at, exactly like
`RemindersArchiveScreen.kt`'s identical unresolved case (see correction above).

**`EditBirthdayScreen.kt`** (2 fixes):
- **Back/close content-description split** — `if (renderAsDetailPane) stringResource(acc_close) else null`
  → `else stringResource(cd_back)`, matching the pattern already used correctly elsewhere in this same file
  (the delete/save actions).
- **Hand-rolled `TopAppBarDefaults.topAppBarColors(containerColor = background)` → shared `TopAppbarColor`
  token** (cross-cutting #2) — same missing-`titleContentColor` gap as §24.

**`PreviewBirthdayScreen.kt`** (3 fixes):
- Same back/close content-description split and `TopAppbarColor` token swap as `EditBirthdayScreen.kt`.
- **Motion**: fixed both `tween(DETAIL_ROW_ANIMATION_DURATION_MS)` calls in `AnimatedDetailRow` (cross-cutting
  #7, the item named in §8) — `fadeIn`→`fastEffectsSpec()`, `slideInVertically`→`fastSpatialSpec()`,
  classified fast for the same reason as §26's identical `PreviewNoteReminderRow.kt` fix (a per-row stagger
  entrance, not a partial-screen animation). While in this file, also fixed `AnimatedAvatar`'s hand-tuned
  `spring(dampingRatio = Spring.DampingRatioMediumBouncy)` the same way, even though §8's cross-cutting #7
  text named only the two `tween()` calls, not this spring — it's the same category of issue in the same
  file's same "entrance stagger" motion system, and fixing one while leaving the other as a hand-tuned
  literal would have left the screen internally inconsistent. Classified fast despite the code comment
  calling this avatar "this screen's one hero element": guidelines' speed tiers are keyed to a component's
  physical/spatial scale (small component vs. partial-screen surface), not its narrative importance, and a
  single 72dp avatar is scale-wise a small component regardless of role. Removed the now-unused
  `DETAIL_ROW_ANIMATION_DURATION_MS` constant and `Spring`/`spring`/`tween` imports; added
  `FiniteAnimationSpec`/`IntOffset`.

Verified via `./gradlew :feature:feature-birthday:compileDebugKotlin :app:compileProDebugKotlin` (clean,
zero warnings) and `:feature:feature-birthday:detekt`. All three files reported pre-existing
`Indentation`/`ArgumentListWrapping`/`ImportOrdering`/`MaxLineLength` findings; each was checked against
`git show HEAD` at the shifted line numbers (content identical, offset matching exactly the lines this pass
added/removed in each file) and confirmed pre-existing, not introduced.

**Not fixed**: the scroll-shadow app bar redesign (see above — a real UX decision, deferred pending product
input, same as `RemindersArchiveScreen.kt`'s identical open item in the Reminders group).

This closes every §8 finding for both Notes and Birthdays except the two deliberately-deferred items: Notes'
`SelectableOptionRow` `FontWeight` swap (§24) and Birthdays' scroll-shadow app bar (this section) — plus the
newly-identified parallel gap in Reminders' `RemindersArchiveScreen.kt` (this section's correction).

**Suggested next step**: `RemindersArchiveScreen.kt`'s back-button bug is a one-line mechanical fix
independent of the larger scroll-behavior question and could land on its own; otherwise, a different screen
group entirely — Groups/Tags/Places, Calendar/Google Tasks, or Workflow/Routines (§9-§13) — none of which
have had dedicated fixes land yet.

## 28. `RemindersArchiveScreen.kt` back-button fix — landed

Fixed the gap §27 surfaced: `RemindersArchiveScreen.kt`'s back-button `contentDescription = null` (§6
cross-cutting #1) was named in the original Reminders audit but never actually landed, despite §20/§23
claiming §6 was "fully closed." One-line swap in `RemindersArchiveTopBar`:
`contentDescription = null` → `stringResource(R.string.cd_back)`, matching every other correctly-fixed
back button across the Reminders/Notes/Birthdays groups.

Scoped narrowly to just this, per the request — `ArchiveEmptyState`'s identical `onSurface.copy(alpha =
0.3f/0.5f)` alpha-blend (the same construct fixed for its Notes/Birthdays siblings in §24/§27) and the
scroll-shadow app bar redesign (deferred in §27 for the identical Birthdays case, same open product
question) both remain untouched in this file.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-reminder:detekt`. The change is a same-line-count swap (`null` → a `stringResource(...)`
call) with zero line-count shift, so `git diff` alone confirms every reported `Indentation`/`ImportOrdering`
finding (including one in the untouched sibling file `RemindersArchiveScreenState.kt`) sits outside the one
changed line — no stash or `git show HEAD` comparison needed this time.

This closes the last outstanding item from §6's audit. Between this and §27, every named finding across §6
(Reminders) and §8 (Notes & Birthdays) now either has a landed fix or is one of three explicitly-deferred,
judgment-requiring items: Notes' `SelectableOptionRow` `FontWeight` swap, and the scroll-shadow-app-bar
redesign shared by `RemindersArchiveScreen.kt` and `BirthdaysScreen.kt`.

**Suggested next step**: a different screen group entirely — Groups/Tags/Places, Calendar/Google Tasks, or
Workflow/Routines (§9-§13) — none of which have had dedicated fixes land yet.

## 29. Groups/Tags/Places screens — back/save-button content-description fixes — landed

Fixes item 1 of §9's suggested fix order (the "cheapest-and-highest-value first" mechanical accessibility
fix) across all 8 screens named in that audit. Two shapes, matching the pattern already fixed for
Reminders/Notes/Birthdays:

- **Detail-pane back/close split** (unconditional `null` for the back-icon branch) fixed in
  `GroupDetailsScreen.kt`, `EditGroupScreen.kt`, `TagEditScreen.kt`, `TagDetailsScreen.kt`,
  `EditPlaceScreen.kt`: `contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else
  null` → `else stringResource(R.string.cd_back)` (reformatted to a multi-line `if`/`else` to keep each
  branch readable, matching `EditBirthdayScreen.kt`'s existing shape from §27).
- **Top-level list screens' sole back icon** fixed in `GroupsScreen.kt`, `TagsScreen.kt`, `PlacesScreen.kt`:
  `contentDescription = null` → `contentDescription = stringResource(R.string.cd_back)`.
- **`TagEditScreen.kt`'s save button** (§9's distinct, more severe finding — an icon-only `MenuIconButton`
  with no fallback text label, the one true "screen reader has no idea what this is" case in the group, not
  just the detail-pane pattern): `contentDescription = null` → `stringResource(R.string.save)`, giving it the
  same self-labeling floor as its sibling editors' `MenuTextButton(text = stringResource(R.string.save))`
  even though the icon-only component choice itself (vs. `EditGroupScreen`/`EditPlaceScreen`'s
  `MenuTextButton`) is left as-is — §9 flagged the component-choice inconsistency as a separate, lower-severity
  note from the missing description, and only the description is a correctness bug.

`TagsScreen.kt`, `TagEditScreen.kt`, and `TagDetailsScreen.kt` import their own module's `R`
(`com.github.naz013.tags.R`) as the unqualified `R` alias for their own strings (`tags`, `new_tag`, etc.), so
the shared `ui-common` strings (`cd_back`, `acc_close`, `save`) are referenced fully-qualified
(`com.github.naz013.ui.common.R.string.cd_back`) in those three files to avoid any ambiguity with the
existing unqualified `R` import — `GroupsScreen.kt`/`GroupDetailsScreen.kt`/`EditGroupScreen.kt`/
`PlacesScreen.kt`/`EditPlaceScreen.kt` already import `com.github.naz013.ui.common.R` directly, so those five
use the plain `R.string.*` form.

Verified via `./gradlew :feature:feature-group:compileDebugKotlin :feature:feature-tags:compileDebugKotlin
:feature:feature-places:compileDebugKotlin :app:compileProDebugKotlin` (clean) and detekt on all three
modules. Every one of the three modules carries substantial pre-existing `Indentation`/`ArgumentListWrapping`/
`ImportOrdering`/`MaxLineLength` debt (some of it in files this pass never touched, e.g. `GroupListItem.kt`,
`GroupsViewModel.kt`); for the 8 files actually edited, every reported finding was checked against `git show
HEAD` at the shifted line number (content identical, offset matching exactly the net lines each edit
added — 0 for the single-line swaps, +4 for the multi-line `if`/`else` reformats) and confirmed pre-existing,
not introduced by this pass.

**Not fixed**: everything else in §9's suggested fix order — the 4 duplicated alpha-blended empty states
(`GroupsEmptyState`, `TagsEmptyState`, `PlacesEmptyState`, `TagDetailsEmptyState`) not yet migrated onto
`ui-common`'s `EmptyState.kt`; all 8 `TopAppBar`s still calling `TopAppBarDefaults.topAppBarColors(containerColor
= background)` directly instead of the shared `TopAppbarColor` token; the three-way type-role split
(`titleMedium`/`bodyLarge`/`titleLarge`) across `GroupListItem`/`TagListItem`/`PlaceListItemCard`; and the
lower-priority `DrawableCatalog`/`AppIcons` convention cleanup plus the minor delete-placement/
container-color inconsistencies. All deliberately deferred — this pass covered only item 1, per how each
prior "continue with X" request in this project has been scoped to one coherent unit of work at a time.

**Suggested next step**: item 2 of §9's fix order — migrate the four duplicated alpha-blended empty states
onto `ui-common`'s `EmptyState.kt`, which closes the `onSurfaceVariant` gap and the code duplication in one
move, the same "fixes two things at once" shape as the back-button pass just landed here.

## 30. Groups/Tags/Places empty states migrated onto shared `EmptyState.kt` — landed

Fixes item 2 of §9's suggested fix order: the four duplicated, independently-hand-rolled empty-state
composables (`GroupsEmptyState`, `TagsEmptyState`, `PlacesEmptyState`, `TagDetailsEmptyState`) all deleted
and replaced with calls to `ui-common`'s existing `EmptyState.kt` (`icon: Painter, message: String`,
already built during the Home/Agenda pass, §3 item 5, and already consumed by `AgendaScreen.kt`/
`ChronologicalHomeScreen.kt`).

**A pre-condition surfaced during this pass**: §9 said migrating onto `EmptyState.kt` would "fix the
`onSurfaceVariant` gap... in one move," which implied the shared component already used
`onSurfaceVariant`. It didn't — `EmptyState.kt` itself still had the identical
`onSurface.copy(alpha = 0.3f)` / `onSurface.copy(alpha = 0.5f)` alpha-blend as the four duplicates it was
meant to replace. Fixed first, before migrating any call site: `tint`/`color` in `EmptyState.kt` →
`MaterialTheme.colorScheme.onSurfaceVariant`. This is a shared component, so the fix also applies
retroactively to `AgendaScreen.kt`'s and `ChronologicalHomeScreen.kt`'s existing `EmptyState` usages — both
call it with only `icon`/`message`/`modifier` (no color override), so this is a strict improvement for them
too, not a behavior change requiring their own sign-off.

Call-site migrations:
- `GroupsScreen.kt`: `GroupsEmptyState(modifier = ...)` → `EmptyState(icon = AppIcons.Fluent.Group, message
  = stringResource(R.string.no_groups), modifier = ...)`.
- `TagsScreen.kt`: → `EmptyState(icon = AppIcons.Builder.Tag, message = stringResource(R.string.no_tags),
  ...)`.
- `TagDetailsScreen.kt`: → `EmptyState(icon = AppIcons.Builder.Tag, message =
  stringResource(R.string.tag_has_no_items), ...)`.
- `PlacesScreen.kt`: → `EmptyState(icon = painterResource(R.drawable.ic_fluent_place), message =
  stringResource(R.string.no_places), ...)` — left as `painterResource` rather than moving onto
  `AppIcons`/`DrawableCatalog`, since that convention cleanup is §9's separately-tracked, lower-priority
  item 5, out of scope for this pass.

Each of the four `private fun XxxEmptyState` composables was deleted outright rather than kept as a thin
wrapper, along with their now-unused `Column`/`Icon`/`Arrangement`-in-that-role imports (`Column` and
`Alignment` were kept where the same file uses them elsewhere for something unrelated, e.g.
`TagDetailsScreen.kt`'s main-content `Column` and `Box`'s `contentAlignment = Alignment.Center`).

Verified via `./gradlew :ui:ui-common:compileDebugKotlin :feature:feature-group:compileDebugKotlin
:feature:feature-tags:compileDebugKotlin :feature:feature-places:compileDebugKotlin
:app:compileProDebugKotlin` (clean) and detekt on all four modules. Every reported finding in the five
touched files was cross-checked by reading the current file at the reported line and confirming the content
is an unrelated pre-existing block (a `Modifier` chain, a `PaddingValues`/`buildList` call, or the file's
`@Preview` state literal) that merely shifted position when each duplicated composable was deleted — not
something this pass introduced.

**Not fixed**: the remaining §9 items — all 8 `TopAppBar`s still calling
`TopAppBarDefaults.topAppBarColors(containerColor = background)` directly instead of the shared
`TopAppbarColor` token (item 3), the `titleMedium`/`bodyLarge`/`titleLarge` list-row type-role split across
`GroupListItem`/`TagListItem`/`PlaceListItemCard` (item 4), and the `DrawableCatalog`/`AppIcons` convention
cleanup plus minor delete-placement/container-color inconsistencies (item 5, including `PlacesScreen.kt`'s
`ic_fluent_place` left un-migrated above).

**Suggested next step**: item 3 of §9's fix order — point all 8 screens' `TopAppBar`s at the shared
`TopAppbarColor` token instead of `MaterialTheme.colorScheme.background` directly, the same mechanical,
low-judgment swap already applied across the Reminders/Notes/Birthdays groups.

## 31. Groups/Tags/Places `TopAppBar`s pointed at shared `TopAppbarColor` token — landed

Fixes item 3 of §9's suggested fix order across all 8 screens: `colors =
TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` →
`colors = TopAppbarColor` in `GroupsScreen.kt`, `GroupDetailsScreen.kt`, `EditGroupScreen.kt`,
`TagsScreen.kt`, `TagEditScreen.kt`, `TagDetailsScreen.kt`, `PlacesScreen.kt`, `EditPlaceScreen.kt` — the
same shared `ui-common` token (`ComponentColors.kt:28`) already adopted across the Reminders/Notes/Birthdays
groups, so these 8 screens now pick up any future palette change to it instead of being frozen at today's
hand-rolled equivalent.

`TopAppBarDefaults` became unused in all 8 files (each only called it for this one line) and was removed
from every import list. `MaterialTheme` stayed imported in 7 of the 8 — each still uses it elsewhere
(`colorScheme.primary` for an icon tint, a `Card`'s container color, a `typography` role, etc.) — except
`TagEditScreen.kt`, where this was the file's *only* `MaterialTheme` reference, so that import was removed
too rather than left dangling.

Verified via `./gradlew :feature:feature-group:compileDebugKotlin :feature:feature-tags:compileDebugKotlin
:feature:feature-places:compileDebugKotlin :app:compileProDebugKotlin` (clean — confirms no leftover unused
or missing imports) and detekt on all three modules. Every reported finding sat in an unrelated, pre-existing
block (a `Modifier` chain in a loading/empty branch, a `PaddingValues`/`buildList` call, `TagsScreen.kt`'s
`Card` `containerColor` line) at the same position as this session's prior full reads of each file — since
every edit here was either a net-zero line-count change (one import line removed, one added; one `colors =`
line replaced 1-for-1) or, for `TagEditScreen.kt`, a net -1 shift from the extra `MaterialTheme` import
removal — and `TagEditScreen.kt` itself reported zero detekt findings both before and after.

**Not fixed**: §9's last two items — the `titleMedium`/`bodyLarge`/`titleLarge` list-row type-role split
across `GroupListItem`/`TagListItem`/`PlaceListItemCard` (item 4), and the `DrawableCatalog`/`AppIcons`
convention cleanup plus minor delete-placement/container-color inconsistencies (item 5).

**Suggested next step**: item 4 — pick one type role (guidelines §3.1's `titleMedium` is the closest fit to
what two of the three siblings already lean toward) and apply it consistently across `GroupListItem`,
`TagListItem`, and `PlaceListItemCard`, the last content-visible fix in §9 before only the lower-priority
convention cleanup (item 5) remains.

## 32. Groups/Tags/Places list-row type-role split unified on `titleMedium` — landed

Fixes item 4 of §9's suggested fix order: the three-way split where `GroupListItem.kt`, `TagsScreen.kt`'s
`TagListItem`, and `PlaceListItemCard.kt` each used a different type role for the same "list row primary
label" purpose is now unified on `titleMedium`, per guidelines §3.1's own recommendation for that role —
already the closest fit, since `GroupListItem.kt` was already using it correctly.

- `GroupListItem.kt`: already `titleMedium` — no change needed, it was the reference point for this fix.
- `TagsScreen.kt`'s `TagListItem`: `MaterialTheme.typography.bodyLarge` → `titleMedium` for the tag name
  `Text`.
- `PlaceListItemCard.kt`: `MaterialTheme.typography.titleLarge` → `titleMedium` for the place name `Text`.

Both are single-line style swaps with no import or structural changes. Verified via
`./gradlew :feature:feature-tags:compileDebugKotlin :feature:feature-places:compileDebugKotlin
:app:compileProDebugKotlin` (clean) and detekt on both modules. Every reported finding in `TagsScreen.kt`
(the same `Card` `containerColor`/`TagMenu` `PopupMenuItem` lines flagged in every prior pass touching this
file) and `PlaceListItemCard.kt` (a set of `Modifier` chains in `Card`/`Row`/`Icon`/`Text` — identical to
this session's own first read of the file, before any edit) sat at unchanged positions, since each edit here
was a zero-line-count-shift swap.

This closes every content-visible finding from §9. Only item 5 remains: the `DrawableCatalog`/`AppIcons`
convention cleanup (bare `painterResource(R.drawable.ic_fluent_*)` calls across nearly every file in this
group, including `PlaceListItemCard.kt`'s own marker icon and the overflow-menu icon repeated in most of
these files) plus the minor, lower-priority delete-placement (`EditPlaceScreen.kt`'s overflow menu vs.
`EditGroupScreen.kt`/`TagEditScreen.kt`'s direct app-bar icon) and container-color (`GroupListItem.kt`'s
plain default vs. `TagListItem`'s explicit `surfaceContainer`) inconsistencies §9 flagged as lower severity.

**Suggested next step**: item 5's `DrawableCatalog`/`AppIcons` convention cleanup — a repo-hygiene fix per
`CLAUDE.md`'s icon rule rather than an M3 spec gap, but real and mechanical across nearly every file in this
group; alternatively, this fully closes §9, so a different screen group entirely — Calendar/Google Tasks or
Workflow/Routines (§10-§13) — is also a reasonable next target since neither has had dedicated fixes land
yet.

## 33. Groups/Tags/Places `DrawableCatalog`/`AppIcons` convention cleanup — landed

Fixes item 5 of §9's suggested fix order — a `CLAUDE.md` repo-hygiene rule ("a drawable resource ID is never
referenced as a bare `R.drawable.ic_fluent_*` from feature/screen code... use `AppIcons.*`/`DrawableCatalog.*`"),
not an M3 spec gap, but real and mechanical across nearly every file in this group. Every catalog entry
needed (`Add`, `ColorBackground`, `Delete`, `Edit`, `MoreVertical`, `Place`, `Share`, `Star`) already existed
in both `DrawableCatalog.Fluent` and `AppIcons.Fluent` — this pass was pure call-site migration, no new
catalog entries required.

Two shapes, mirroring the convention's own split:
- **`painterResource(R.drawable.ic_fluent_*)` passed where a `Painter` is expected** (`Icon`'s `painter =`,
  `MenuIconButton`'s `icon =`) → `AppIcons.Fluent.*`: the "more options" overflow-menu icon in
  `GroupListItem.kt`, `GroupDetailsScreen.kt`, `EditGroupScreen.kt`'s delete icon, `TagsScreen.kt`,
  `TagDetailsScreen.kt`, `EditPlaceScreen.kt`, `PlaceListItemCard.kt` (both its marker `Place` icon and its
  overflow icon), and `PlacesScreen.kt` (its add-button icon and, from §30, the `EmptyState` icon param).
- **`R.drawable.ic_fluent_*` passed where a plain `@DrawableRes Int` is expected** (`PopupMenuItem.iconRes`,
  and the `Int?`/`Int` return types of `GroupListItem.kt`'s `iconResOrNull()` and `PlaceListItemCard.kt`'s
  `placeMenuItems()`) → `DrawableCatalog.Fluent.*`: every `PopupMenuItem(iconRes = ...)` call site across
  `GroupsScreen.kt`, `GroupDetailsScreen.kt`, `TagsScreen.kt`, `TagDetailsScreen.kt`, `EditPlaceScreen.kt`,
  and `PlaceListItemCard.kt`.

`painterResource` became unused and was removed from every one of the 9 files it was only used for this
purpose in (`GroupListItem.kt`, `GroupDetailsScreen.kt`, `EditGroupScreen.kt`, `TagsScreen.kt`,
`TagDetailsScreen.kt`, `EditPlaceScreen.kt`, `PlaceListItemCard.kt`, `PlacesScreen.kt` — `EditGroupScreen.kt`
and `PlacesScreen.kt` already imported `AppIcons`, so only needed the `painterResource` removal).
`GroupListItem.kt` and `PlaceListItemCard.kt` didn't import `AppIcons` at all before this pass (both only
ever went through bare `painterResource`) — added alongside `DrawableCatalog` in both.

Verified via `./gradlew :feature:feature-group:compileDebugKotlin :feature:feature-tags:compileDebugKotlin
:feature:feature-places:compileDebugKotlin :app:compileProDebugKotlin` (clean — confirms every catalog
reference resolved and no leftover unused imports) and detekt on all three modules. Every reported finding
was cross-checked against this session's own earlier reads of each file, accounting for the exact net
line-count shift each file's import-list edit introduced (0 net for files where a removed `painterResource`
import was offset by an added `AppIcons`/`DrawableCatalog` import; +1 net for `GroupListItem.kt`,
`GroupsScreen.kt`, and `PlaceListItemCard.kt`, where an import was added without a matching removal) — every
finding landed on an unrelated pre-existing `Modifier` chain, `Card` `containerColor` line, or
`PopupMenuItem` argument list at exactly the expected shifted position.

This closes every finding from §9's Groups/Tags/Places audit — cross-cutting items 1-5 and every
screen-specific note now either has a landed fix or is one of the two remaining explicitly-lower-priority
items §9 itself called out as "worth a look if a screen is touched again, not a confirmed defect": the
delete-placement inconsistency (`EditPlaceScreen.kt`'s overflow menu vs. `EditGroupScreen.kt`/
`TagEditScreen.kt`'s direct app-bar icon) and the container-color inconsistency (`GroupListItem.kt`'s plain
default vs. `TagListItem`'s explicit `surfaceContainer`) — both individually-legitimate role choices, not
compliance gaps, per §9's own text.

**Suggested next step**: a different screen group entirely — Calendar/Google Tasks or Workflow/Routines
(§10-§13) — since §9 (Groups/Tags/Places) is now fully closed and neither of those groups has had dedicated
fixes land yet.

## 34. Calendar/Google Tasks screens — back-button content-description fixes — landed

Fixes item 1 of §10's suggested fix order (the "cheapest-and-highest-value first" mechanical accessibility
fix, same pattern already closed for Reminders/Notes/Birthdays/Groups-Tags-Places) across all 8 screens named
in that audit. Two shapes:

- **Plain single back icon** (no detail-pane branch) fixed in `CalendarScreen.kt`, `TimelineScreen.kt`,
  `GoogleTasksScreen.kt`, `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`,
  `EditGoogleTaskListScreen.kt`: `contentDescription = null` → `contentDescription =
  stringResource(R.string.cd_back)`.
- **Detail-pane back/close split** (unconditional `null` for the back-icon branch) fixed in
  `TaskListScreen.kt` and `GoogleCalendarEventPreviewScreen.kt`: `contentDescription = if
  (renderAsDetailPane) stringResource(R.string.acc_close) else null` → `else
  stringResource(R.string.cd_back)` (reformatted to a multi-line `if`/`else`, the same shape used across
  every other group this session).

6 of the 8 files have no explicit `R` import at all — each lives directly in its module's root package
(`com.github.naz013.feature.googletask`, `com.github.naz013.feature.calendar.monthview`, etc.), so `R`
resolves implicitly to that module's own generated class, which — per `android.nonTransitiveRClass = false`
— already merges in `ui-common`'s `cd_back`/`acc_close` strings with no import needed (confirmed by these
files already calling other `ui-common` strings like `R.string.acc_close`/`R.string.more_options` the same
way). `GoogleCalendarEventPreviewScreen.kt` is the one exception (`com.github.naz013.feature.calendar.preview`,
a sub-package) and already imports `com.github.naz013.ui.common.R` explicitly for the same reason.

Verified via `./gradlew :feature:feature-calendar:compileDebugKotlin :feature:feature-googletask:compileDebugKotlin
:app:compileProDebugKotlin` (clean) and detekt on both modules. Every edit was a single-line, zero-net-shift
swap (or, for the two detail-pane files, a straightforward 1-line-to-4-line `if`/`else` expansion with no
other file changes), so every reported finding was checked directly against `git show HEAD` at the exact
same or shift-adjusted line number and confirmed identical pre-existing content — largely `Modifier` chains
and `PopupMenuItem`/`AddEventRow` argument lists in `CalendarScreen.kt`/`TimelineScreen.kt`, plus a set of
already-unused imports in `GoogleTasksScreen.kt` (`Row`, `fillMaxWidth`, `Card`, `CardDefaults`,
`GoogleTaskItemState`) unrelated to this fix and untouched by it.

**Not fixed**: everything else in §10 — item 2 (route all 8 `TopAppBar`s through the shared `TopAppbarColor`
token instead of the two different ad hoc overrides), item 3 (the three deprecated baseline
`ExtendedFloatingActionButton` instances in `GoogleTasksScreen.kt`/`TaskListScreen.kt`/
`PreviewGoogleTaskScreen.kt`), item 4 (`TimelinePager.kt`'s two off-scale `RoundedCornerShape(6.dp)` sites),
item 5 (`detailScreenContentWidth()` missing from `PreviewGoogleTaskScreen.kt`/`EditGoogleTaskScreen.kt`/
`EditGoogleTaskListScreen.kt`), the ad hoc `.copy(alpha = ...)` de-emphasis pattern (cross-cutting #4), and
the two design-judgment items (Calendar Month/Timeline breakpoint adaptation, sub-48dp timeline touch
targets) §10 itself flagged as needing product input rather than a mechanical fix.

**Suggested next step**: item 2 of §10's fix order — point all 8 screens' `TopAppBar`s at the shared
`TopAppbarColor` token, replacing both ad hoc variants (`containerColor = background` on 6 screens,
`containerColor = Color.Transparent` on the 2 Calendar screens) — the same mechanical swap already applied
across every other screen group this session.

## 35. Calendar/Google Tasks `TopAppBar`s pointed at shared `TopAppbarColor` token — landed

Fixes item 2 of §10's suggested fix order across all 8 screens: `colors = TopAppBarDefaults.topAppBarColors(...)`
→ `colors = TopAppbarColor` in `CalendarScreen.kt`, `TimelineScreen.kt`, `GoogleTasksScreen.kt`,
`TaskListScreen.kt`, `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`, `EditGoogleTaskListScreen.kt`,
`GoogleCalendarEventPreviewScreen.kt` — replacing both ad hoc variants §10 flagged: the 6
`containerColor = MaterialTheme.colorScheme.background` screens (same visible color as the token, but
silently missing `titleContentColor`) and the 2 Calendar screens' `containerColor = Color.Transparent`.

The `Color.Transparent` case got a closer look before swapping it, since transparent-vs-opaque is a real
visual difference, not just a token-hygiene one: neither `CalendarScreen.kt` nor `TimelineScreen.kt` wraps
its `TopAppBar` in any `Surface`/background layer of its own — the `topBar` slot is a bare `Column`
(`CalendarScreen.kt`) or the `TopAppBar` directly (`TimelineScreen.kt`) — so a transparent app bar was
already just showing `Scaffold`'s own default container color underneath, which *is*
`MaterialTheme.colorScheme.background`. Switching to `TopAppbarColor` is therefore visually a no-op for the
container color and a strict improvement for `titleContentColor` (now `onBackground` instead of the
component default `onSurface`), not a behavior change.

`TopAppBarDefaults` was removed from every file's imports (each only used it here). `MaterialTheme` stayed
imported everywhere else it's still referenced. `TimelineScreen.kt` lost its `Color` import too — the
`Color.Transparent` reference removed by this fix was that file's only use of it.

Verified via `./gradlew :feature:feature-calendar:compileDebugKotlin :feature:feature-googletask:compileDebugKotlin
:app:compileProDebugKotlin` (clean — confirms no dangling imports) and detekt on both modules. Every
reported finding was cross-checked: `CalendarScreen.kt`/`TimelineScreen.kt`'s `ImportOrdering` flags predate
this entire session (confirmed via `git show HEAD` — the `com.github.naz013.ui.common.R` /
`com.github.naz013.feature.calendar.*` import block was already out of lexicographic order before any fix
landed here), and the remaining `Wrapping`/`ArgumentListWrapping`/`Indentation`/`MaxLineLength` findings
(`Modifier` chains, `PopupMenuItem`/`AddEventRow` argument lists) plus `GoogleTasksScreen.kt`'s unused
imports all sit at positions matching each file's exact net line-shift from this edit (0 for 6 files whose
import swap was 1-for-1; -1 for `TimelineScreen.kt`, which lost two imports and gained one).

**Not fixed**: items 3-5 of §10 — the three deprecated baseline `ExtendedFloatingActionButton` instances,
`TimelinePager.kt`'s off-scale `RoundedCornerShape(6.dp)` sites, and missing `detailScreenContentWidth()` on
three Google Tasks screens — plus the ad hoc `.copy(alpha = ...)` de-emphasis pattern and the two
design-judgment items (Calendar breakpoint adaptation, sub-48dp timeline touch targets).

**Suggested next step**: item 3 — replace the three deprecated baseline `ExtendedFloatingActionButton`
instances (`GoogleTasksScreen.kt`, `TaskListScreen.kt`, `PreviewGoogleTaskScreen.kt`) with
`SmallExtendedFloatingActionButton`, the same fix already landed for `ReminderFullscreenMapScreen.kt` in the
Reminders group (§22) — the clearest deprecated-component finding left in this group.

## 36. Google Tasks deprecated baseline FAB fixes — landed

Fixes item 3 of §10's suggested fix order: all three baseline `ExtendedFloatingActionButton` instances (the
56dp pill-shaped variant guidelines §9.1 marks no longer recommended) swapped for
`SmallExtendedFloatingActionButton`, the same component and fix already landed for
`ReminderFullscreenMapScreen.kt` in the Reminders group (§22).

- `GoogleTasksScreen.kt`'s "New task" FAB (passes `containerColor`/`contentColor` overrides sourced from
  `state.fabContainerColor`/`state.fabContentColor`).
- `TaskListScreen.kt`'s "New task" FAB (identical shape/params to the one above).
- `PreviewGoogleTaskScreen.kt`'s "Complete" FAB (simpler — just `icon`/`text`/`onClick`, no color override).

Confirmed against the M3 1.5.0-alpha27 source (`FloatingActionButton.kt:659-678`, the `text`/`icon`/`onClick`
overload) that `SmallExtendedFloatingActionButton` accepts the identical `containerColor`/`contentColor`
parameters as the baseline component before making the swap, so this was a pure component-name change with
no parameter restructuring needed in any of the three files — every call site's existing arguments carried
over unchanged.

`ExtendedFloatingActionButton` was removed from each file's imports and replaced with
`SmallExtendedFloatingActionButton` at the same alphabetical position (between `Scaffold` and `Text`) — a
1-for-1 import swap, so no other import changed.

Verified via `./gradlew :feature:feature-googletask:compileDebugKotlin :app:compileProDebugKotlin` (clean —
confirms `SmallExtendedFloatingActionButton`'s signature really does match 1:1) and detekt on the module.
`TaskListScreen.kt` and `PreviewGoogleTaskScreen.kt` report zero findings; `GoogleTasksScreen.kt`'s findings
are the same pre-existing unused imports (`Row`, `fillMaxWidth`, `Card`, `CardDefaults`,
`GoogleTaskItemState`) flagged in every prior pass touching this file, at unchanged positions since the
import edit was net-zero.

**Not fixed**: items 4-5 of §10 — `TimelinePager.kt`'s two off-scale `RoundedCornerShape(6.dp)` sites, and
missing `detailScreenContentWidth()` on `PreviewGoogleTaskScreen.kt`/`EditGoogleTaskScreen.kt`/
`EditGoogleTaskListScreen.kt` — plus the ad hoc `.copy(alpha = ...)` de-emphasis pattern and the two
design-judgment items (Calendar breakpoint adaptation, sub-48dp timeline touch targets).

**Suggested next step**: item 4 — `TimelinePager.kt`'s `HolidayChip`/`TimelineEventBlock`
`RoundedCornerShape(6.dp)` sites are genuinely off the 10-step shape scale (guidelines §4.1); swap to 4dp
(extra small) per §10's own recommendation. Alternatively, item 5 — add `detailScreenContentWidth()` to the
three under-adapted Google Tasks screens, mirroring `GoogleCalendarEventPreviewScreen.kt`'s existing usage.

## 37. `TimelinePager.kt` off-scale corner-radius fix — landed

Fixes item 4 of §10's suggested fix order: `HolidayChip`'s (`:301`) and `TimelineEventBlock`'s (`:482`)
`RoundedCornerShape(6.dp)` — genuinely off the 10-step M3 shape scale per guidelines §4.1, unlike
`GoogleTasksScreen.kt`'s `RoundedCornerShape(12.dp)` finding from the same audit (on-scale, just untokenized)
— replaced with `MaterialTheme.shapes.extraSmall`.

Went with the real M3 theme token rather than a literal `RoundedCornerShape(4.dp)` or a new custom
`AppShapes` constant: confirmed against the M3 1.5.0-alpha27 source
(`ShapeTokens.kt:76`, `CornerValueExtraSmall = CornerSize(4.0.dp)`) that the default `extraSmall` shape
role already resolves to exactly 4dp, and this repo's own `AppTheme`/`Theme.kt` never overrides `shapes`
away from the M3 defaults — so `MaterialTheme.shapes.extraSmall` *is* the 4dp guidelines §10 asked for,
sourced from the actual design-system role instead of either a bare literal or a bespoke addition to
`ui-common`'s `AppShapes` (`tile`/`card`/`largeIncreased`/`pill`) for a value the theme already names.
Both call sites back compact/dense chips (a holiday label chip and a short-duration timeline event block),
so 4dp over rounding up to 8dp (`small`) keeps the "small chip" visual weight §10 called out as the reason
not to round up.

`RoundedCornerShape` became unused in `TimelinePager.kt` after both sites were fixed (its only two call
sites in the file) and was removed from the imports; `CircleShape` (used elsewhere in the file) stayed.

Verified via `./gradlew :feature:feature-calendar:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
detekt on the module. `TimelinePager.kt` carries substantial pre-existing `Indentation`/`ImportOrdering`
debt across the whole file (confirmed via `git show HEAD` — including an already-out-of-order
`com.github.naz013.domain.PublicHoliday` import that predates this session); every reported finding was
checked against the original file's content at the exact -1 line shift this edit introduced (one import
line removed, nothing added) and confirmed identical, unrelated pre-existing content.

**Not fixed**: item 5 of §10 — `detailScreenContentWidth()` missing from `PreviewGoogleTaskScreen.kt`/
`EditGoogleTaskScreen.kt`/`EditGoogleTaskListScreen.kt` — plus the ad hoc `.copy(alpha = ...)` de-emphasis
pattern and the two design-judgment items (Calendar breakpoint adaptation, sub-48dp timeline touch targets)
§10 itself flagged as needing product input.

**Suggested next step**: item 5 — add `Modifier.detailScreenContentWidth()` to the three under-adapted
Google Tasks screens, mirroring `GoogleCalendarEventPreviewScreen.kt`'s existing usage; this closes every
mechanical, no-judgment-required item in §10, leaving only the ad hoc alpha-blend pattern (a one-line swap
to `onSurfaceVariant` per site, same as every prior group) and the two genuine design-judgment items.

## 38. `detailScreenContentWidth()` added to the three under-adapted Google Tasks screens — landed

Fixes item 5 of §10's suggested fix order — the last mechanical, no-judgment-required item in the whole
section. `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`, and `EditGoogleTaskListScreen.kt` each had
their Scaffold content `Column` filling the full available width unconditionally, unlike
`GoogleCalendarEventPreviewScreen.kt` — the reference pattern named in §10 — which already caps a
structurally-identical "preview/edit a single item" screen to a comfortable reading width on tablet/desktop
via `ui-common`'s `Modifier.detailScreenContentWidth()` (`DetailScreenContentWidth.kt:21-26`).

Applied the identical shape used by the reference screen in all three files: the Scaffold's content lambda
now wraps the existing scrolling `Column` in a `Box(modifier = Modifier.fillMaxSize()...padding(padding),
contentAlignment = Alignment.TopCenter)`, and the inner `Column`'s own `.fillMaxSize()` (or, for
`PreviewGoogleTaskScreen.kt`, its implicit full width) is replaced with `.detailScreenContentWidth()` —
which itself already includes the `fillMaxWidth()`/`widthIn(max = 640.dp).fillMaxWidth()` the removed
modifier used to provide, so no width capability is lost on Compact. `Box`/`Alignment` were already imported
in `PreviewGoogleTaskScreen.kt`; added to the other two, alongside the new
`com.github.naz013.ui.common.compose.foundation.navigation.detailScreenContentWidth` import in all three
files. Content inside each `Column` was re-indented by two spaces to nest correctly under the new `Box`; no
logic or content changed.

Verified via `./gradlew :feature:feature-googletask:compileDebugKotlin :app:compileProDebugKotlin` (clean)
and `:feature:feature-googletask:detekt`. The 6 reported findings (5 unused-import findings in
`GoogleTasksScreen.kt`, 1 top-level-constant-naming finding in `GoogleTasksNavGraph.kt`) are both in files
this fix never touched — confirmed via `git status`/`git show HEAD` that neither file has any changes in
this session — so all are pre-existing debt, unrelated to this fix.

**Not fixed**: the ad hoc `.copy(alpha = ...)` de-emphasis pattern (cross-cutting #4) and the two
design-judgment items §10 itself flagged as needing product input rather than a mechanical fix (Calendar
Month/Timeline breakpoint adaptation, sub-48dp timeline touch targets). These are the only §10 findings left
open.

**Suggested next step**: §10's mechanical items are now fully closed. What remains needs either a product
decision (the two design-judgment items) or is a cross-cutting pattern better tackled as its own pass across
every screen that uses it rather than scoped to Calendar/Google Tasks alone (the alpha-blend de-emphasis
pattern) — a good candidate would be starting that cross-cutting alpha-blend pass, or moving on to auditing
the next unaudited screen group in `docs/m3-expressive-screen-inventory.md`.

## 39. Calendar/Google Tasks ad hoc alpha-blend de-emphasis fixes — landed

Fixes §10's cross-cutting #4, the last item in the section (both design-judgment items excepted) — three
sites hand-blending `onSurface` down to a lower-emphasis tone instead of using the role token that already
exists for it (guidelines §2.2/§2.4).

- **`CalendarScreen.kt:247`** (`MonthDayCell`, other-month day number) —
  `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)` → `MaterialTheme.colorScheme.outlineVariant`.
  Went with `outlineVariant` rather than the `onSurfaceVariant` used everywhere else in this cleanup: §10's
  own audit text called for "`outline`/`outlineVariant` for a very muted 'not this month' number" — this
  number is a step more de-emphasized than ordinary secondary/caption text (which does use
  `onSurfaceVariant`), and per guidelines' role table `outline` is for meaningful boundaries while `outline
  variant` is the decorative, lower-contrast sibling — the better match for a muted, non-interactive glyph
  that's deliberately supposed to recede.
- **`GoogleTasksScreen.kt`'s `GoogleTasksEmptyState`** (empty-state icon and text, `onSurface.copy(alpha =
  0.3f)` / `0.5f`) — rather than a bare role swap, migrated the whole composable onto `ui-common`'s shared
  `EmptyState` (`EmptyState.kt:17-40`, already `onSurfaceVariant`-correct since §30's fix), matching the
  precedent set for Groups/Tags/Places' four empty states in §30. `GoogleTasksEmptyState` was used from two
  call sites — `GoogleTasksScreen.kt:151` and the sibling `TaskListScreen.kt:140` (same module, `internal`
  visibility) — both now call `EmptyState(icon = AppIcons.Fluent.TaskListAdd, message =
  stringResource(R.string.no_google_tasks))` directly, and the private composable was deleted outright. This
  also incidentally fixes a `DrawableCatalog` convention violation the alpha-blend audit hadn't flagged: the
  deleted composable referenced `painterResource(R.drawable.ic_fluent_task_list_add)` bare, whereas
  `AppIcons.Fluent.TaskListAdd` (already cataloged) is the only sanctioned way to obtain that painter outside
  `DrawableCatalog`/`AppIcons` themselves.

Removing `GoogleTasksEmptyState` made `androidx.compose.foundation.layout.size` (only used for that
composable's `Modifier.size(64.dp)`) genuinely unused in `GoogleTasksScreen.kt`; removed it. `EmptyState`
imported newly in both `GoogleTasksScreen.kt` and `TaskListScreen.kt`.

Verified via `./gradlew :feature:feature-googletask:compileDebugKotlin :feature:feature-calendar:compileDebugKotlin :app:compileProDebugKotlin`
(clean) and detekt on both modules. `feature-googletask`'s findings, after removing the newly-unused `size`
import, are identical (module-relative line numbers aside) to the pre-existing baseline confirmed earlier in
§38 — 5 unused imports in `GoogleTasksScreen.kt` and 1 naming finding in `GoogleTasksNavGraph.kt`, neither
file's *other* content touched by this fix. `feature-calendar`'s detekt run surfaces substantial pre-existing
debt across many files unrelated to this change (`TimelinePager.kt`, `KoinModule.kt`, test files, etc.); the
single line this fix touched (`CalendarScreen.kt:247`) is a like-for-like one-line swap with zero net
line-count change, and every detekt finding actually located in `CalendarScreen.kt` (lines 3/194/196/288/294)
was confirmed identical against `git show HEAD` at those same line numbers — none of them is the line this
fix changed, and none is new.

**Not fixed**: the two items §10 itself flagged as needing product/design input rather than a mechanical fix
— the Calendar Month/Timeline breakpoint-adaptation gap, and sub-48dp timeline touch targets. With this,
§10 (Calendar & Google Tasks) has no remaining mechanical findings; only those two design-judgment items are
open.

**Suggested next step**: §10 is now fully closed except for the two design-judgment items, which need
product input rather than more audit-and-fix passes. A good next step is moving on to the next unaudited
screen group in `docs/m3-expressive-screen-inventory.md`, or tackling the alpha-blend de-emphasis pattern
as a cross-cutting pass over the screen groups already audited but not yet fixed for it (per the "Not fixed"
notes accumulated across §16-§27's audits).

## 40. Workflow/Routines screens — back-button content-description fixes — landed

Every group in `docs/m3-expressive-screen-inventory.md` already has at least one landed "Audited" pass (this
was checked directly against the file rather than assumed), so there is no literal "unaudited screen group"
left. Workflow/Routines (§7) is the closest match to that intent: an audit exists but, unlike every other
group, zero fixes had landed against it yet. Picked up its own suggested fix order's item 1 — the same
cheapest-and-highest-value mechanical fix every other screen group started with.

Fixes §7's cross-cutting #1: all 8 of 8 screens in this group passed `contentDescription = null` for their
back arrow (§7's audit found this was total — 0 of 8 screens got it right, worse than the 4-of-10 and 4-of-9
partial hit rates found in Reminders/Notes-Birthdays). `cd_back` already exists as a string resource, so
this is the same one-line-per-file swap already applied to every other screen group:

- **`WorkflowGalleryScreen.kt:50`** and **`RoutinesListScreen.kt:61`** / **`RoutineExecutionScreen.kt:62`**
  (no `renderAsDetailPane` branch) — bare `contentDescription = null` → `stringResource(R.string.cd_back)`.
- **`WorkflowRulesForGroupScreen.kt`**, **`WorkflowRulesForReminderScreen.kt`**,
  **`builder/WorkflowRuleBuilderScreen.kt`**, **`RoutineEditScreen.kt`**, and
  **`preview/RoutinePreviewScreen.kt`** (the `renderAsDetailPane`-aware screens) — the `null` branch of the
  existing close/back ternary → `stringResource(R.string.cd_back)`, leaving the already-correct
  `acc_close` branch untouched.

`builder/WorkflowRuleBuilderScreen.kt` lives in the `feature.workflow.builder` sub-package and already
imports `com.github.naz013.feature.workflow.R` explicitly for its own module's strings, so — matching that
file's own existing style for the sibling `acc_close` reference two lines above — its fix uses the
fully-qualified `com.github.naz013.ui.common.R.string.cd_back` rather than a second, colliding bare `R`
import. Every other file lives in its module's root package (or already imports `com.github.naz013.ui.common.R`,
for the two `feature-routine` files in sub-packages), so bare `R.string.cd_back` resolves there directly, the
same as it has for every prior group's back-button fix this session.

Verified via `./gradlew :feature:feature-workflow:compileDebugKotlin :feature:feature-routine:compileDebugKotlin :app:compileProDebugKotlin`
(clean) and detekt on both modules. `feature-routine` surfaced 1 pre-existing finding
(`RoutineNavGraph.kt` naming) and `feature-workflow` surfaced 5 (3 `ImportOrdering`, 1 naming, 1
`MaxLineLength` in `WorkflowRuleBuilderScreen.kt:159`) — none in a file/line this fix touched except that
last one, which sits 80 lines below the edited block; confirmed identical against `git show HEAD` at the
same line number (this fix's edit was a like-for-like one-line content swap inside an existing multi-line
`if`/`else`, zero net line-count change, so no line-shift accounting was needed).

**Not fixed**: §7's remaining cross-cutting findings — the `TopAppBar` color-token inconsistency (6 of 8
screens use a raw `TopAppBarDefaults.topAppBarColors(...)` instead of the shared `TopAppbarColor` token),
the `FontWeight.Bold` cluster and off-scale `shadowElevation = 4.dp` bottom bar concentrated in
`RoutineExecutionScreen.kt`, and `RoutinesListScreen.kt`'s alpha-blended `RoutinesEmptyState`. Also open:
`RoutinePreviewScreen.kt`'s deprecated baseline `ExtendedFloatingActionButton` and its 40dp check-toggle
touch target (below the 48dp minimum).

**Suggested next step**: item 2 of §7's fix order — `RoutinePreviewScreen.kt`'s deprecated baseline
`ExtendedFloatingActionButton` → `SmallExtendedFloatingActionButton`, the same mechanical swap already
applied to three Google Tasks screens in §36. Alternatively, item 3 — `RoutinesListScreen.kt`'s
`RoutinesEmptyState` migrated onto the shared `EmptyState.kt`, matching the precedent from §30/§39.

## 41. `RoutinePreviewScreen.kt` deprecated baseline FAB fix — landed

Fixes item 2 of §7's suggested fix order: `RoutinePreviewScreen.kt:77`'s baseline `ExtendedFloatingActionButton`
(no color override, so the deprecated 56dp pill-shaped default guidelines §9.1 flags as no longer
recommended) → `SmallExtendedFloatingActionButton`, the identical mechanical swap already applied to three
Google Tasks screens in §36 — same import 1-for-1, same call-site rename, no other changes (the `icon`/
`text`/`onClick` lambda arguments are unchanged, since both composables share that overload's signature).
Import placed alphabetically between `Scaffold` and `Text`, matching §36's convention.

Verified via `./gradlew :feature:feature-routine:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-routine:detekt` — the only finding is the same pre-existing `RoutineNavGraph.kt` naming
issue already confirmed pre-existing in §40; nothing in `RoutinePreviewScreen.kt` itself was flagged.

**Not fixed**: §7's remaining findings — the `TopAppBar` color-token inconsistency spanning both Workflow and
Routines, `RoutineExecutionScreen.kt`'s `FontWeight.Bold` cluster and off-scale bottom-bar
`shadowElevation`, `RoutinesListScreen.kt`'s alpha-blended `RoutinesEmptyState`, and
`RoutinePreviewScreen.kt`'s own remaining gap — its 40dp check-toggle touch target, below the 48dp minimum
(the same compact-checklist-row trade-off already flagged for `SubTasksValueEditor.kt` in the Reminders
audit, §20).

**Suggested next step**: item 3 of §7's fix order — `RoutinesListScreen.kt`'s hand-blended
`RoutinesEmptyState` migrated onto the shared `EmptyState.kt`, matching the precedent from §30/§39.
Alternatively, fold the `TopAppBar` color-token fix across both Workflow and Routines into one pass, the
same shape as §35's Calendar/Google Tasks fix.

## 42. `RoutinesListScreen.kt`'s `RoutinesEmptyState` migrated onto shared `EmptyState.kt` — landed

Fixes item 3 of §7's suggested fix order: `RoutinesEmptyState`'s hand-blended `onSurface.copy(alpha = 0.3f)`
(icon) / `0.5f` (text) → `ui-common`'s shared `EmptyState` (`EmptyState.kt:17-40`, `onSurfaceVariant`-correct
since §30), the same migration pattern applied to Groups/Tags/Places' four empty states in §30 and Google
Tasks' in §39. `RoutinesEmptyState` had exactly one call site (`RoutinesListScreen.kt:99`, the
`RoutinesListDisplayState.Empty` branch) and was private to this file, so the fix is a direct call-site swap
— `EmptyState(icon = AppIcons.Builder.Timer, message = stringResource(R.string.no_routines))` — followed by
deleting the private composable outright. Unlike the Google Tasks case in §39, this screen already sourced
its icon through `AppIcons.Builder.Timer` rather than a bare `painterResource(R.drawable.*)`, so there was no
incidental `DrawableCatalog` convention fix riding along this time.

Removing the composable made both `androidx.compose.material3.Icon` and `androidx.compose.foundation.layout.size`
(each used only inside the deleted function — `size` for its `Modifier.size(64.dp)`) genuinely unused;
removed both. `EmptyState` imported newly, placed alphabetically between the existing `AppDropdownMenu` and
`PopupMenuItem` imports.

Verified via `./gradlew :feature:feature-routine:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-routine:detekt` — the only finding is the same pre-existing `RoutineNavGraph.kt` naming
issue already confirmed pre-existing in §40/§41; nothing in `RoutinesListScreen.kt` itself was flagged.

**Not fixed**: §7's remaining findings — the `TopAppBar` color-token inconsistency spanning both Workflow and
Routines, `RoutineExecutionScreen.kt`'s `FontWeight.Bold` cluster and off-scale bottom-bar
`shadowElevation`, and `RoutinePreviewScreen.kt`'s 40dp check-toggle touch target (below the 48dp minimum).
With this, every item in §7's suggested fix order through item 3 has landed; only items 4-5 remain, both
explicitly deferred by §7's own audit text to "whichever future PR next touches this screen" rather than a
dedicated sweep.

**Suggested next step**: fold the `TopAppBar` color-token fix (cross-cutting #2) across both Workflow and
Routines into one pass, the same shape as §35's Calendar/Google Tasks fix — it's the one remaining §7 finding
that's still mechanical (no design judgment) and cross-cuts both feature modules. The `FontWeight`/elevation/
touch-target items are lower priority per §7's own guidance to fold them into a future PR that touches those
screens anyway, rather than a dedicated sweep.

## 43. Workflow/Routines `TopAppBar`s pointed at shared `TopAppbarColor` token — landed

Fixes §7's cross-cutting #2, the last mechanical (no-design-judgment) finding left in the section: 6 of the
group's 8 screens hand-rolled `TopAppBarDefaults.topAppBarColors(containerColor =
MaterialTheme.colorScheme.background)` instead of the shared `TopAppbarColor` token
(`ui-common/compose/ComponentColors.kt:28-33`), which also pairs `titleContentColor =
MaterialTheme.colorScheme.onBackground` — a value the 6 raw call sites silently fell back to
`TopAppBarDefaults`'s own default (`onSurface`) for instead, same finding shape as every prior
`TopAppbarColor` fix this session (§31, §35). `RoutineEditScreen.kt` and `RoutineExecutionScreen.kt` already
used the token correctly and needed no change.

Fixed: `WorkflowGalleryScreen.kt`, `WorkflowRulesForGroupScreen.kt`, `WorkflowRulesForReminderScreen.kt`,
`builder/WorkflowRuleBuilderScreen.kt`, `RoutinesListScreen.kt`, and `preview/RoutinePreviewScreen.kt` — same
mechanical swap in all six: `colors = TopAppBarDefaults.topAppBarColors(...)` → `colors = TopAppbarColor`,
`TopAppBarDefaults` import removed, `TopAppbarColor` imported in its place (alphabetically between
`AppTheme`/`AppIcons` and `foundation.MenuIconButton` in each file, matching this session's established
placement convention). `builder/WorkflowRuleBuilderScreen.kt`'s `colors = ...` line was `MaterialTheme`'s
*only* remaining use in that file — removing it made the `MaterialTheme` import itself genuinely unused,
so it was removed too (the other five files all still use `MaterialTheme` elsewhere — `colorScheme.primary`,
`typography.*`, etc. — and kept their import).

Verified via `./gradlew :feature:feature-workflow:compileDebugKotlin :feature:feature-routine:compileDebugKotlin :app:compileProDebugKotlin`
(clean) and detekt on both modules. All reported findings are in files/lines this fix didn't change:
`WorkflowGalleryViewModel.kt`, `WorkflowRulesForGroupViewModel.kt`, `WorkflowRulesForReminderViewModel.kt`,
`WorkflowNavGraph.kt`, and `RoutineNavGraph.kt` are untouched this session (confirmed via `git status`);
`builder/WorkflowRuleBuilderScreen.kt`'s `MaxLineLength` finding sits at the same content, shifted by this
edit's net -1 line delta, confirmed identical via `git show HEAD`. One additional finding surfaced —
`RoutinePreviewScreen.kt`'s `androidx.compose.foundation.background` import flagged as unused — but `git show
HEAD` confirms that import was *already* dead at `HEAD` (the file only ever referenced `.background` as part
of unrelated property names, `MaterialTheme.colorScheme.background`/`state.backgroundColor`, never the
`Modifier.background()` function itself); detekt simply didn't happen to surface it in this file's prior
runs this session. Left it as pre-existing, unrelated debt rather than folding an incidental cleanup into
this fix's diff.

**Not fixed**: §7's two remaining findings, both explicitly deferred by §7's own audit text to "whichever
future PR next touches this screen" rather than a dedicated sweep — `RoutineExecutionScreen.kt`'s
`FontWeight.Bold` cluster and off-scale `shadowElevation = 4.dp` bottom bar, and
`RoutinePreviewScreen.kt`'s 40dp check-toggle touch target (below the 48dp minimum). With this, §7 has no
remaining findings that don't require touching one of those two screens anyway.

**Suggested next step**: §7 is now fully closed except for the two screen-specific items §7 itself deferred
to a future touch of `RoutineExecutionScreen.kt`/`RoutinePreviewScreen.kt`. A good next step is auditing the
next screen group not yet covered in depth, or continuing the alpha-blend cross-cutting pass over groups
already audited but not yet fixed for it (per the accumulated "Not fixed" notes across §16-§27).

## 44. `LocalBackupScreen.kt`/`InsightsScreen.kt` back-button content-description fixes — landed

Every group in `docs/m3-expressive-screen-inventory.md` already has at least one landed audit (checked
directly, same as before picking Workflow/Routines for §40), so there's still no literal "unaudited" group.
The closest match this time: Backup/Insights and Onboarding/Login, part of §13's combined audit alongside
Widget Configuration — the latter already got two rounds of fixes (§14's shared-scaffold pass, §16's
`ColorSlider` accessibility pass), but `LocalBackupScreen.kt` and `InsightsScreen.kt` (and `PinLoginScreen.kt`,
though it has no bug here — see below) never had a dedicated fix land. Picked up §13's own suggested fix
order's item 1, the same cheapest-and-highest-value mechanical fix every other group started with.

Fixes §13's cross-cutting #1 for the two screens `WidgetConfigScaffold.kt`'s §14 fix didn't reach (it
resolved 7 of the 9 affected screens sharing that one scaffold; these two are the remaining 2, each with
its own standalone `Scaffold`): `LocalBackupScreen.kt:45` and `InsightsScreen.kt:59` both passed
`contentDescription = null` for their back arrow — same one-line-per-file swap to `stringResource(R.string.cd_back)`
applied to every prior group. `PinLoginScreen.kt`, this same audit's positive counter-example, already had
correct content descriptions on both its close and fingerprint buttons and needed no change.

Verified via `./gradlew :extensions:localbackup:compileDebugKotlin :feature:feature-insights:compileDebugKotlin :app:compileProDebugKotlin`
(clean) and detekt on both modules. `feature-insights` detekt reports zero findings (confirmed via a forced
`--rerun`, since the first pass showed `UP-TO-DATE` and skipped re-analysis). `extensions:localbackup`
surfaced 2 findings in `LocalBackupScreen.kt` (unused `Icons`/`ArrowBack` imports) plus a batch of
`ArgumentListWrapping`/`MaxLineLength` findings in two untouched test files — all confirmed pre-existing via
`git show HEAD` (the two unused-import lines are byte-identical to HEAD; the test files were never touched
this session).

**Not fixed**: §13's remaining items — item 2 (`WidgetConfigScaffold.kt:45`, `PinLoginScreen.kt:65,88`, and
`PinInput.kt:99,117`'s raw `painterResource(R.drawable.*)` instead of the already-cataloged `AppIcons.Fluent.*`),
item 3 (`LocalBackupScreen.kt:47`/`InsightsScreen.kt:63`'s remaining `TopAppBarDefaults.topAppBarColors(...)`
→ `TopAppbarColor`), item 4 (`InsightsScreen.kt`'s 7 `.copy(alpha = ...)` call sites → `onSurfaceVariant`),
and item 6 (baseline `CircularProgressIndicator` → the newer Loading indicator, noted as low-urgency).
`ColorSlider`'s accessibility gap (item 5) was already fixed in §16.

**Suggested next step**: item 3 — the two remaining `TopAppBar` color-token bypasses in `LocalBackupScreen.kt`/
`InsightsScreen.kt`, the same mechanical swap as §35/§43. Alternatively, item 2's `DrawableCatalog` cleanup
across `WidgetConfigScaffold.kt`/`PinLoginScreen.kt`/`PinInput.kt`, matching §33's precedent.

## 45. `LocalBackupScreen.kt`/`InsightsScreen.kt` `TopAppBar`s pointed at shared `TopAppbarColor` token — landed

Fixes item 3 of §13's suggested fix order (cross-cutting #2): both screens' standalone `TopAppBar`s called
`TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` directly instead
of the shared `TopAppbarColor` token — the same finding shape fixed for their `WidgetConfigScaffold.kt`
sibling back in §14, and the same mechanical swap already applied repeatedly this session (§31, §35, §43).
`colors = TopAppBarDefaults.topAppBarColors(...)` → `colors = TopAppbarColor` in both files;
`TopAppBarDefaults` import removed, `TopAppbarColor` imported in its place (alphabetically after `AppTheme`,
before `foundation.MenuIconButton`, matching this session's established placement). `MaterialTheme` stayed
imported in both files — each still uses it elsewhere (`LocalBackupScreen.kt`'s `typography.bodyMedium`/
`colorScheme.error`; `InsightsScreen.kt`'s `typography.titleMedium`/`colorScheme.surfaceContainer`/the
alpha-blended `.copy()` calls that are item 4's still-open finding).

Verified via `./gradlew :extensions:localbackup:compileDebugKotlin :feature:feature-insights:compileDebugKotlin :app:compileProDebugKotlin`
(clean) and detekt on both modules (`--rerun` on `feature-insights`, matching §44's finding that this task
can report a stale `UP-TO-DATE` and skip re-analysis otherwise). Findings are byte-identical to §44's
confirmed-pre-existing baseline — the 2 unused imports in `LocalBackupScreen.kt` plus the
`ArgumentListWrapping`/`MaxLineLength` findings in two untouched test files; `feature-insights` remains
fully clean.

**Not fixed**: §13's remaining items — item 2 (`WidgetConfigScaffold.kt`/`PinLoginScreen.kt`/`PinInput.kt`'s
raw `painterResource(R.drawable.*)` lookups), item 4 (`InsightsScreen.kt`'s 7 `.copy(alpha = ...)` call
sites), and item 6 (baseline `CircularProgressIndicator`, noted as low-urgency). `ColorSlider`'s
accessibility gap (item 5) was already fixed in §16.

**Suggested next step**: item 4 — `InsightsScreen.kt`'s 7 hand-blended `onSurface.copy(alpha =
0.7f/0.5f/0.3f)` call sites (`StreakCard`, `RoutineInsightCard`, `InsightsEmptyState`) → `onSurfaceVariant`,
the same role-swap already applied everywhere else this pattern has come up. Alternatively, item 2's
`DrawableCatalog` cleanup across the three remaining files, matching §33's precedent.

## 46. `InsightsScreen.kt` alpha-blend fixes — landed

Fixes item 4 of §13's suggested fix order, its 7 hand-blended `onSurface.copy(alpha = ...)` call sites:

- **`StreakCard`/`RoutineInsightCard`** (5 sites: streak-longest and fired-count/focus-time captions,
  `onSurface.copy(alpha = 0.7f)`) — straight role swap to `MaterialTheme.colorScheme.onSurfaceVariant`, no
  component change, matching every prior secondary-text alpha-blend fix this session.
- **`InsightsEmptyState`** (2 sites: icon `onSurface.copy(alpha = 0.3f)`, message `onSurface.copy(alpha =
  0.5f)`) — rather than a bare role swap, migrated the whole composable onto `ui-common`'s shared
  `EmptyState` (`EmptyState.kt:17-40`, already `onSurfaceVariant`-correct since §30), the same precedent
  applied to Google Tasks' (§39) and Routines' (§42) matching empty states — `InsightsEmptyState`'s shape
  (64dp icon, `bodyLarge` message, identical padding) is byte-for-byte the same as the shared component. Its
  one call site now reads `EmptyState(icon = AppIcons.Fluent.DataPie, message =
  stringResource(R.string.no_insights_yet))`, and the private composable was deleted.

Removing `InsightsEmptyState` made `androidx.compose.material3.Icon` and
`androidx.compose.foundation.layout.size` (each used only inside the deleted function) genuinely unused;
removed both. `EmptyState` imported newly, placed alphabetically after `foundation.MenuIconButton` (per this
session's established `foundation.MenuIconButton` < `foundation.component.*` ordering — uppercase `M` sorts
before lowercase `c`).

Verified via `./gradlew :feature:feature-insights:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-insights:detekt --rerun` — fully clean, zero findings, same as §44/§45.

**Not fixed**: §13's remaining items — item 2 (`WidgetConfigScaffold.kt`/`PinLoginScreen.kt`/`PinInput.kt`'s
raw `painterResource(R.drawable.*)` lookups) and item 6 (baseline `CircularProgressIndicator` on
`LocalBackupScreen.kt`/`InsightsScreen.kt`, noted as low-urgency). With this, every remaining mechanical item
in §13 is item 2 alone — item 6 was explicitly flagged low-urgency/visual-only in the original audit.

**Suggested next step**: item 2 — route `WidgetConfigScaffold.kt:45`, `PinLoginScreen.kt:65,88`, and
`PinInput.kt:99,117` through the already-cataloged `AppIcons.Fluent.Dismiss`/`.Fingerprint`/`.TextAsterisk`
instead of raw `painterResource(R.drawable.*)`, matching §33's `DrawableCatalog` cleanup precedent. This
closes every mechanical item §13 flagged, leaving only the low-urgency `CircularProgressIndicator` swap.

## 47. `WidgetConfigScaffold.kt`/`PinLoginScreen.kt`/`PinInput.kt` `DrawableCatalog` cleanup — landed

Fixes item 2 of §13's suggested fix order, closing every mechanical item the section flagged (item 6, the
baseline `CircularProgressIndicator` swap, was always noted as low-urgency/visual-only). All four call sites
looked up an already-cataloged drawable directly instead of through `AppIcons`/`DrawableCatalog`, the same
CLAUDE.md convention violation fixed for Groups/Tags/Places in §33:

- **`WidgetConfigScaffold.kt:44`** — `painterResource(R.drawable.ic_fluent_dismiss)` → `AppIcons.Fluent.Dismiss`.
- **`PinLoginScreen.kt:65`** (fingerprint icon) — `painterResource(R.drawable.ic_fluent_fingerprint)` →
  `AppIcons.Fluent.Fingerprint`; **`PinLoginScreen.kt:88`** (close icon) —
  `painterResource(R.drawable.ic_fluent_dismiss)` → `AppIcons.Fluent.Dismiss`.
- **`PinInput.kt:99`** (delete-key icon) — `painterResource(R.drawable.ic_fluent_dismiss)` →
  `AppIcons.Fluent.Dismiss`; **`PinInput.kt:117`** (`PinDots`' per-digit indicator) —
  `painterResource(R.drawable.ic_fluent_text_asterisk)` → `AppIcons.Fluent.TextAsterisk`.

`painterResource` became unused in all three files (each had it only at the sites just fixed) and was
removed from their imports; `AppIcons` newly imported in all three, placed alphabetically (after
`com.github.naz013.appwidgets.R` in `WidgetConfigScaffold.kt`; before `AppTheme` in the two `ui-common`
files).

Verified via `./gradlew :extensions:appwidgets:compileDebugKotlin :ui:ui-common:compileDebugKotlin
:app:compileProDebugKotlin` (clean) and detekt on both modules. `extensions:appwidgets` detekt reports 4
pre-existing findings, all in files this fix never touched (`ComposeResourceProvider.kt`,
`AppWidgetPreviewUpdaterImpl.kt`, `SingleNoteWidgetConfigScreen.kt`,
`SingleNoteWidgetConfigViewModel.kt`) — `WidgetConfigScaffold.kt` itself is clean.
`ui:ui-common:detekt` crashes outright (`IllegalStateException` analyzing `PermissionRequester.kt`, a file
this fix never touched) before it can report any findings at all — confirmed this crash is **pre-existing**
and unrelated to this fix by stashing the two `PinLoginScreen.kt`/`PinInput.kt` edits, re-running detekt
against the unmodified `HEAD` versions of those files, and reproducing the identical crash; popped the stash
to restore the fix afterward. Since the module-wide crash makes a lint pass over the two touched files
impossible right now, correctness here rests on the compile-clean result and manual review rather than a
detekt confirmation — worth flagging as a separate, pre-existing tooling gap if `ui-common` needs detekt
coverage restored.

**Not fixed**: item 6 of §13 — baseline `CircularProgressIndicator` on `LocalBackupScreen.kt`/
`InsightsScreen.kt`, explicitly noted as low-urgency (visual-only, no behavior change) since the original
audit. With this, §13 has no remaining mechanical findings.

**Suggested next step**: §13 is now fully closed except for the low-urgency `CircularProgressIndicator` swap.
A good next step is auditing the next screen group not yet covered in depth, or continuing the alpha-blend
cross-cutting pass over groups already audited but not yet fixed for it (per the accumulated "Not fixed"
notes across §16-§27).

## 48. Leftover "Audited"-only screens re-verified and promoted to "Done" — landed

At this point every group in `docs/m3-expressive-screen-inventory.md` had at least one landed fix except for
four individual rows still sitting at bare "Audited" inside otherwise-fixed groups: `ReminderHelpScreen.kt`
and `RecurHelpScreen.kt` (§6, Reminders), `ImagePreviewScreen.kt` (§8, Notes), and
`NotificationCustomizationHelpScreen.kt` (§11, Settings). Asked the user which of three options to pursue
next (leftover individual screens, the cross-cutting alpha-blend pass, or a fresh Home/Events audit); they
picked the leftover screens.

Re-reading each audit's own text (rather than assuming "Audited" meant "has an unfixed bug") showed all four
were already flagged as fully spec-compliant when originally reviewed, with explicit "nothing to flag"/
"little further Compose-layer surface area to audit" language — not screens waiting on a fix, but screens
where the audit found no gap at all. Re-verified this directly against current source (not just trusting the
audit text, since these sections are old enough that the files could have drifted since):

- **`ReminderHelpScreen.kt`** / **`RecurHelpScreen.kt`** — both correctly pass `stringResource(R.string.cd_back)`
  for the back arrow and use the shared `TopAppbarColor` token. Both are thin `Scaffold`/`TopAppBar` wrappers
  around a bundled-HTML `WebView` (`how_to_create_a_reminder.html` / `doc_rfc_5545.html`) — any further work
  would mean restyling those HTML/CSS assets, out of scope for a Compose-focused pass.
- **`ImagePreviewScreen.kt`** — correct back-button description; its `TopAppBarDefaults.topAppBarColors(containerColor
  = Color.Transparent)` is the same legitimate exception already documented for Note screens in §8
  cross-cutting #2 (tinting the whole app bar to the image's own custom background/content color via
  `state.background`/`state.content`, which a static shared token couldn't express). No Cards/shapes/
  elevation in the screen to get wrong.
- **`NotificationCustomizationHelpScreen.kt`** — same shape and same verdict as `ReminderHelpScreen.kt`:
  correct back-button description, correct `TopAppbarColor`, thin `WebView` wrapper around
  `notification_customization.html`.

No code changes made — there was nothing to fix. Promoted all four rows from "Audited" to "Done" in
`docs/m3-expressive-screen-inventory.md` (Reminders, Notes, and Settings sections respectively), since
"Done" is the status this doc's own legend defines for "screen fully reflects the `ui-common` expressive
foundation," which better reflects reality than leaving them at "Audited" indefinitely as if a fix were
still pending.

**Not fixed**: nothing — this section is a verification-and-reclassification pass, not a fix. The out-of-scope
HTML/CSS asset restyling for the three WebView-hosted screens remains explicitly out of scope unless
requested separately.

**Suggested next step**: with these four resolved, the only work left that isn't a design-judgment item or
already-deferred screen-specific gap is the cross-cutting alpha-blend pass over groups already audited but
not yet fixed for it (per the accumulated "Not fixed" notes across §16-§27), or a fresh audit of Home/Events
— the two oldest-touched screens, predating this doc's numbered-section system, that have never had a formal
§-numbered audit pass like every other group.

## 49. Home / Events screens — audit

Audit pass over Home and Agenda, the two oldest-touched screens in the app — the first ones this whole
effort landed changes on (see §2-4), but never given a formal §-numbered audit like every other group since.
**Audit only — no code changed in this pass.** Files read in full: `HomeScreen.kt` (the banner-overlay
wrapper), `ChronologicalHomeScreen.kt` (the actual Home tab content — header, nav grid, event list),
`HomeScreenState.kt`, `ResolvedEventAction.kt`, and `AgendaScreen.kt`.

### Cross-cutting patterns (found on 2+ screens — fix once, verify everywhere it repeats)

1. **Raw `R.drawable.*` instead of the `DrawableCatalog`/`AppIcons` catalog, despite every icon already
   being cataloged** — a real CLAUDE.md convention violation, the same class of finding fixed for
   Groups/Tags/Places in §33. Spans both files:
   - `HomeScreenState.kt:68-72` (`HomeEvent.EventAction.IconRes` companion object) hardcodes
     `R.drawable.ic_fluent_phone`, `ic_fluent_send` (×2), `ic_fluent_globe`, `ic_fluent_open` — notably this
     one lives in a plain state/domain class, not even inside a `@Composable`, so it can't reach for
     `AppIcons`'s `@Composable` painter getters; `DrawableCatalog.Fluent.Phone`/`.Send`/`.Globe`/`.Open`
     (plain `@DrawableRes Int` constants, no composable context needed) are the right target here.
   - `ChronologicalHomeScreen.kt:227-231` (`AddButton`'s `when` block, building `PopupMenuItem.iconRes`
     values) hardcodes `ic_fluent_alert`, `ic_fluent_food_cake`, `ic_builder_google_task_list`,
     `ic_fluent_note`, `ic_fluent_cart`.
   - `AgendaScreen.kt` has the heaviest concentration: `AgendaSelectionTopBar` (`:521`, `:529` —
     `ic_fluent_archive`, `ic_fluent_delete`), `AddMenuButton` (`:608-610` — `ic_fluent_alert`,
     `ic_fluent_cart`, `ic_fluent_food_cake`), and `OverflowMenuButton` (`:632-634` — `ic_fluent_archive`,
     `ic_fluent_group`, `ic_builder_group`, plus `:638`'s `painterResource(R.drawable.ic_fluent_more_vertical)`
     bare `Painter` lookup) — 10 bare references in one file. All are `Int`-context `PopupMenuItem.iconRes`
     values except the last, so the fix is routing through `DrawableCatalog.Fluent.*`/`.Builder.*` for the
     `Int` sites and `AppIcons.Fluent.MoreVertical` for the one `Painter` site.
2. **Scroll-triggered app-bar elevation implemented as a drop shadow, not the Expressive-recommended color
   fill** — both `ChronologicalHomeScreen.kt` (`:98-108`, `animateDpAsState` driving
   `Modifier.shadow(elevation = headerElevation, clip = false)` on the header `Column`, target 4dp when
   scrolled) and `AgendaScreen.kt` (`:100-105`/`:150`, the same `animateDpAsState` pattern driving
   `Surface(shadowElevation = headerElevation)`, target 3dp) animate a real drop shadow in on scroll. This is
   a materially more sophisticated implementation than the *static* scroll-shadow gap flagged as still-open
   for `RemindersArchiveScreen.kt`/`BirthdaysScreen.kt` elsewhere in this doc (those have no scroll-elevation
   behavior at all) — but per guidelines' app-bar component table (§9.1: "On scroll: color fill instead of
   drop shadow"), Expressive's own recommended pattern moved away from the drop-shadow-on-scroll treatment
   entirely, toward tinting the app bar to a raised surface-container tone instead. Both Home and Agenda
   independently built the *previous* generation's version of this affordance — worth flagging as its own
   item since it means the "reference pattern" this doc has been citing for the Reminders/Birthdays Archive
   gap (implicitly, "build a scroll shadow like Home/Agenda already do") is itself not the currently
   recommended approach. Separately, `ChronologicalHomeScreen.kt`'s 4dp scrolled-elevation target is also
   off the defined 0/1/3/6/8/12dp scale (guidelines §5) — `AgendaScreen.kt`'s own `HEADER_ELEVATION = 3.dp`
   constant already lands correctly on "scrolled app bar" (Level 2), and would be the more scale-correct
   value to copy if a shadow-based approach were kept rather than moving to color-fill.

### Screen-specific findings

- **`AgendaScreen.kt`** — `AgendaTopBar`'s back arrow (`:560`) passes `contentDescription = null`, the same
  exact defect class found on every other screen group audited this session (worst case: 8 of 8 in
  Workflow/Routines, §7). This is the *only* back button in this whole group — Home has none since it's a
  root tab destination, not a pushed screen. Also: `:574` uses bare `Icons.Default.FilterList` instead of
  the already-cataloged `AppIcons.Fluent.Filter`/`DrawableCatalog.Fluent.Filter` — the identical bug already
  fixed for `BirthdaysScreen.kt`'s equivalent filter icon in §27. And the filter bottom sheet's empty states
  for "no tags"/"no groups" (`:267`, `:283`) hand-blend `MaterialTheme.colorScheme.onSurface.copy(alpha =
  0.5f)` instead of `onSurfaceVariant` — the same ad hoc de-emphasis anti-pattern flagged repeatedly
  throughout this doc, though here it's inline caption text inside a bottom sheet rather than a dedicated
  empty-state composable, so no `EmptyState.kt` migration applies — just the direct role swap. On the
  positive side: `AgendaTopBar`'s `colors = TopAppBarDefaults.topAppBarColors(containerColor =
  Color.Transparent)` (`:585`) is the same legitimate pattern already confirmed equivalent for
  `CalendarScreen.kt`/`TimelineScreen.kt` in §35 — the real fill comes from the wrapping
  `Surface(color = MaterialTheme.colorScheme.background, ...)` at `:150` — though per §35's own precedent,
  swapping to the real `TopAppbarColor` token would still be worth doing for the `titleContentColor` pairing
  it also carries, which the transparent-plus-Surface approach doesn't provide. `EmptyState.kt` is already
  used correctly for the empty list state (`:197`), and `FilterChipLabel`/`UiAgendaHeader` correctly reach
  for `labelLargeEmphasized`/`titleMediumEmphasized` (real emphasized type tokens) rather than manual
  `FontWeight` overrides — no `FontWeight` gap anywhere in this file, unlike almost every other group
  audited so far.
- **`HomeScreen.kt`** — the three banner variants (`PrivacyBanner`/`LoginBanner`/`WhatsNewBanner`) animate in
  with literal `tween(BANNER_ANIMATION_DURATION_MS)` (`:32-37`) instead of `MaterialTheme.motionScheme`, the
  same recurring literal-motion pattern flagged across nearly every prior audit (§7 #4, §8 #7, etc.) — worth
  calling out specifically here because its own sibling file in the same module,
  `ChronologicalHomeScreen.kt`, already gets this right (`HeaderNavigationTile`/`TimeSectionRow` both
  correctly use `MaterialTheme.motionScheme.defaultEffectsSpec()`/`defaultSpatialSpec()`, `:317-318`/
  `:379-380`) — so the fix is a same-module copy-paste, not new research. `HomeBanner`'s
  `CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)` (`:150`) is correctly on-scale (12dp =
  Level 5, guidelines §5) despite being a literal rather than a token — not a defect, just worth noting it
  landed on a real level by design rather than luck. `primaryContainer`/`onPrimaryContainer` are correctly
  paired throughout.
- **`ChronologicalHomeScreen.kt`** — beyond cross-cutting #1/#2, this is otherwise a strong screen: real
  `MaterialTheme.motionScheme` usage for its stagger-in animations (see above), `AppShapes.tile`/`AppShapes.card`
  tokens used instead of literal `RoundedCornerShape`s, and `labelSmallEmphasized`/`titleMediumEmphasized`/
  `bodyMediumEmphasized`/`headlineMediumEmphasized` used throughout instead of manual `FontWeight` — no
  `FontWeight` gap here either. `HeaderNavigationTile`'s `item.color.copy(alpha = TILE_ICON_TINT_ALPHA)`
  (`:336`) is the same legitimate *tonal container tint* pattern already confirmed correct for
  `TaskListTile` in the Google Tasks audit (§10) — not the ad hoc text/icon de-emphasis anti-pattern, so not
  a finding. One low-confidence note: `EventCard`'s default-case `containerColor = CardDefaults.cardColors().containerColor`
  (resolves to `surfaceContainerLow`) is paired with `onContainerColor = MaterialTheme.colorScheme.onBackground`
  (`:429-432`) rather than `onSurface` — guidelines §2.2 calls for container/on-color pairs to always match;
  in practice `onBackground` and `onSurface` render identically in this app's current theme, so this is a
  token-hygiene note rather than a visible bug, worth a look if this file is touched for the fixes above.

### Suggested fix order

Cheapest-and-highest-value first: (1) `AgendaScreen.kt`'s single back-button `contentDescription = null` —
mechanical, `cd_back` already exists; (2) `AgendaScreen.kt`'s bare `Icons.Default.FilterList` →
`AppIcons.Fluent.Filter`, matching §27's exact precedent; (3) the `DrawableCatalog` cleanup across
`HomeScreenState.kt`/`ChronologicalHomeScreen.kt`/`AgendaScreen.kt` (cross-cutting #1) — the widest-reach
mechanical fix in this group; (4) `AgendaScreen.kt`'s two alpha-blend call sites → `onSurfaceVariant`; (5)
`HomeScreen.kt`'s literal `tween()` → `MaterialTheme.motionScheme`, copying the pattern already correct one
file over in `ChronologicalHomeScreen.kt`. Item 6 — the scroll-shadow-to-color-fill migration
(cross-cutting #2) — is a genuine design-judgment item, not a mechanical fix: it would mean redesigning
Home's and Agenda's scroll-elevation affordance to the currently-recommended pattern, and its outcome
should probably also inform the still-open Reminders/Birthdays Archive scroll-shadow gap this doc has been
carrying since §6, rather than being fixed in isolation here.

## 50. `AgendaScreen.kt` back-button content-description fix — landed

Fixes item 1 of §49's suggested fix order: `AgendaTopBar`'s back arrow (`:560`) passed `contentDescription =
null` — the same defect class found on every other screen group audited this session, here on the one and
only back button in the whole Home/Events group (Home has none, being a root tab destination rather than a
pushed screen). One-line swap to `stringResource(R.string.cd_back)`, same mechanical fix as every prior
group's item 1; `cd_back` already resolves via the existing `com.github.naz013.ui.common.R` import.

Verified via `./gradlew :feature:feature-agenda:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-agenda:detekt --rerun`. All 29 reported findings sit in files/lines this fix never
touched: `AgendaNavGraph.kt`, `AgendaViewModel.kt`, and `AgendaViewModelTest.kt` are untouched this session,
and every finding actually located in `AgendaScreen.kt` (lines 76, 170-172, 182-184, 248-250, 607-611, 645)
was confirmed byte-identical against `git show HEAD` at those same line numbers — this fix's edit was a
like-for-like one-line content swap with zero net line-count change, so no line-shift accounting was needed.

**Not fixed**: items 2-6 of §49 — the bare `Icons.Default.FilterList`, the `DrawableCatalog` cleanup
(cross-cutting #1), the two alpha-blend call sites, `HomeScreen.kt`'s literal `tween()`, and the
scroll-shadow-to-color-fill design-judgment item (cross-cutting #2).

**Suggested next step**: item 2 — `AgendaScreen.kt`'s bare `Icons.Default.FilterList` →
`AppIcons.Fluent.Filter`, the exact same fix already applied to `BirthdaysScreen.kt`'s equivalent icon in
§27.

## 51. `AgendaScreen.kt` bare `Icons.Default.FilterList` fix — landed

Fixes item 2 of §49's suggested fix order: `AgendaTopBar`'s filter icon (`:574`) used bare
`Icons.Default.FilterList` instead of the already-cataloged `AppIcons.Fluent.Filter`/
`DrawableCatalog.Fluent.Filter` — the identical bug, and identical fix, already applied to
`BirthdaysScreen.kt`'s equivalent filter icon in §27. `AppIcons` was already imported in this file (used
elsewhere for the back arrow, add button, and empty-state icon); `MenuIconButton`'s `Painter` overload
(`MenuIconButton.kt:50-60`, alongside the `ImageVector` one the old `Icons.Default.*` call resolved to)
covers `AppIcons.Fluent.Filter`'s return type with no other call-site changes needed.

`androidx.compose.material.icons.Icons` and `androidx.compose.material.icons.filled.FilterList` both became
unused (this was their only call site in the file) and were removed.

Verified via `./gradlew :feature:feature-agenda:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-agenda:detekt --rerun`. Same 29 pre-existing findings as §50, shifted by this edit's -2
net line delta (two import lines removed); every finding actually located in `AgendaScreen.kt` was
re-confirmed byte-identical against `git show HEAD` at the corresponding pre-shift line numbers.

**Not fixed**: items 3-6 of §49 — the `DrawableCatalog` cleanup across `HomeScreenState.kt`/
`ChronologicalHomeScreen.kt`/`AgendaScreen.kt` (cross-cutting #1), `AgendaScreen.kt`'s two alpha-blend call
sites, `HomeScreen.kt`'s literal `tween()`, and the scroll-shadow-to-color-fill design-judgment item
(cross-cutting #2).

**Suggested next step**: item 3 — the `DrawableCatalog` cleanup (cross-cutting #1), the widest-reach
mechanical fix left in this group, spanning `HomeScreenState.kt`, `ChronologicalHomeScreen.kt`, and
`AgendaScreen.kt`.

## 52. Home/Events `DrawableCatalog` cleanup — landed

Fixes item 3 of §49's suggested fix order (cross-cutting #1), the widest-reach mechanical finding in this
group — 15 bare `R.drawable.*` references across all three files, the same convention violation fixed for
Groups/Tags/Places in §33:

- **`HomeScreenState.kt:68-72`** (`HomeEvent.EventAction.IconRes` companion object, a plain state class with
  no composable context) — `R.drawable.ic_fluent_phone`/`ic_fluent_send` (×2)/`ic_fluent_globe`/`ic_fluent_open`
  → `DrawableCatalog.Fluent.Phone`/`.Send`/`.Globe`/`.Open`.
- **`ChronologicalHomeScreen.kt:227-231`** (`AddButton`'s `when` block) — `ic_fluent_alert`/`ic_fluent_food_cake`/
  `ic_builder_google_task_list`/`ic_fluent_note`/`ic_fluent_cart` → `DrawableCatalog.Fluent.Alert`/`.FoodCake`,
  `DrawableCatalog.Builder.GoogleTaskList`, `DrawableCatalog.Fluent.Note`/`.Cart`.
- **`AgendaScreen.kt`** — `AgendaSelectionTopBar` (`ic_fluent_archive`/`ic_fluent_delete` →
  `DrawableCatalog.Fluent.Archive`/`.Delete`), `AddMenuButton` (`ic_fluent_alert`/`ic_fluent_cart`/
  `ic_fluent_food_cake` → the same three `DrawableCatalog.Fluent.*` constants as above), `OverflowMenuButton`
  (`ic_fluent_archive`/`ic_fluent_group`/`ic_builder_group` → `DrawableCatalog.Fluent.Archive`/`.Group`,
  `DrawableCatalog.Builder.Tag` — the last one is the one `ic_builder_group` maps to a `Tag`-named constant,
  not `Group`, so worth double-checking against `DrawableCatalog.kt:38` if this file is touched again), and
  the one `Painter`-context site, `painterResource(R.drawable.ic_fluent_more_vertical)` →
  `AppIcons.Fluent.MoreVertical`.

`painterResource` became unused in `AgendaScreen.kt` (its only call site was the one just fixed) and was
removed; `com.github.naz013.ui.common.R` in `HomeScreenState.kt` was entirely replaceable with
`DrawableCatalog` since those five lines were its only usage in the file. `DrawableCatalog` imported newly
in all three files (`HomeScreenState.kt` swapping its `R` import outright; `ChronologicalHomeScreen.kt`/
`AgendaScreen.kt` adding it alongside their existing `R` import, which both still need for `stringResource`
calls elsewhere).

Note the preview-only code at the bottom of `ChronologicalHomeScreen.kt`
(`HeaderNavigationGridPreview`, `:762-786`) still uses bare `R.drawable.*` too — left untouched since §49's
audit scoped this finding to the two real call sites, matching this session's practice of fixing documented
findings rather than scope-creeping into unflagged preview-only code.

Verified via `./gradlew :feature:feature-home:compileDebugKotlin :feature:feature-agenda:compileDebugKotlin
:app:compileProDebugKotlin` (clean) and detekt on both modules. `HomeScreenState.kt` reports zero findings.
Every other reported finding sits in a file/line this fix never touched (`AgendaNavGraph.kt`,
`AgendaViewModel.kt`, `HomeNavGraph.kt`, various use-case files, tests) or was confirmed byte-identical
against `git show HEAD` at the corresponding pre-shift line number for the two files this fix did touch —
`AgendaScreen.kt` at zero net line-count change (every replacement was line-for-line), and
`ChronologicalHomeScreen.kt` at its +1 net delta (one import line added).

**Not fixed**: items 4-6 of §49 — `AgendaScreen.kt`'s two alpha-blend call sites, `HomeScreen.kt`'s literal
`tween()`, and the scroll-shadow-to-color-fill design-judgment item (cross-cutting #2).

**Suggested next step**: item 4 — `AgendaScreen.kt`'s two alpha-blend call sites (the filter bottom sheet's
"no tags"/"no groups" captions) → `onSurfaceVariant`, the same role-swap pattern applied throughout this
session.

## 53. `AgendaScreen.kt` alpha-blend fixes — landed

Fixes item 4 of §49's suggested fix order: the filter bottom sheet's "no tags" (`:265`) and "no groups"
(`:281`) captions both hand-blended `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)` instead of
`onSurfaceVariant` — the same ad hoc de-emphasis anti-pattern flagged and fixed everywhere else this
session. Straight role swap at both sites via one `replace_all` edit; no component migration applies here
since this is inline caption text inside `FilterSection`, not a dedicated empty-state composable.

Verified via `./gradlew :feature:feature-agenda:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-agenda:detekt --rerun`. Findings are identical to the baseline confirmed pre-existing in
§51/§52 — this edit was a zero-net-line-delta, line-for-line swap at both sites, so every line number stayed
put.

**Not fixed**: items 5-6 of §49 — `HomeScreen.kt`'s literal `tween()` and the scroll-shadow-to-color-fill
design-judgment item (cross-cutting #2). With this, every mechanical item in §49 through item 4 has landed;
only item 5 (mechanical) and item 6 (design-judgment) remain.

**Suggested next step**: item 5 — `HomeScreen.kt`'s three banner variants' literal `tween(BANNER_ANIMATION_DURATION_MS)`
→ `MaterialTheme.motionScheme`, copying the pattern its own sibling file `ChronologicalHomeScreen.kt` already
gets right.

## 54. `HomeScreen.kt` literal `tween()` fix — landed

Fixes item 5 of §49's suggested fix order, the last mechanical item in the section: all three banner
variants' `AnimatedVisibility` enter/exit transitions used literal `tween(BANNER_ANIMATION_DURATION_MS)`
(300ms) instead of `MaterialTheme.motionScheme` — the same recurring literal-motion pattern flagged across
nearly every prior audit, notable here because the sibling file in the same module,
`ChronologicalHomeScreen.kt`, already gets this right for its own stagger-in animations.

The fix wasn't a bare token swap: `bannerEnterTransition`/`bannerExitTransition` were top-level `private
val`s, computed once at class-load time — `MaterialTheme.motionScheme` is a composition-local, only
readable from inside a `@Composable`. Moved both from top-level vals into local vals inside `HomeScreen`'s
own body (which already is `@Composable`), matching how `ChronologicalHomeScreen.kt`'s equivalent
`enter =`/`exit =` expressions are built directly in-place rather than hoisted out — no `remember` needed,
consistent with that same sibling pattern. `fadeIn`/`fadeOut` map to `defaultEffectsSpec()`,
`slideInVertically`/`slideOutVertically` map to `defaultSpatialSpec()`, the identical effects/spatial spec
pairing `ChronologicalHomeScreen.kt`'s `HeaderNavigationTile`/`TimeSectionRow` already use. Deleted the
now-unused `BANNER_ANIMATION_DURATION_MS` constant and the `androidx.compose.animation.core.tween` import
(both had no other call sites in the file).

Verified via `./gradlew :feature:feature-home:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-home:detekt --rerun` — `HomeScreen.kt` itself reports zero findings; every other finding
sits in a file this fix never touched (`ChronologicalHomeScreen.kt` at the same lines confirmed pre-existing
in §52, `HomeNavGraph.kt`, `scheduleview/*` use-case files, tests).

**Not fixed**: item 6 of §49 — the scroll-shadow-to-color-fill migration (cross-cutting #2), a genuine
design-judgment item rather than a mechanical fix. With this, §49 has no remaining mechanical findings.

**Suggested next step**: §49 is now fully closed except for item 6, which needs a product/design decision on
redesigning Home's and Agenda's scroll-elevation affordance to the currently-recommended color-fill pattern
— and whose outcome should probably also inform the still-open Reminders/Birthdays Archive scroll-shadow gap
carried since §6, rather than being decided in isolation here. A good next step is auditing the next screen
group not yet covered in depth, or continuing the alpha-blend cross-cutting pass over groups already audited
but not yet fixed for it (per the accumulated "Not fixed" notes across §16-§27).

## 55. Onboarding & Splash screens — audit

Every group in `docs/m3-expressive-screen-inventory.md` already carries at least one audit, so "next
unaudited screen group" no longer has a literal referent (this is the fourth time; §48's text covers the
first three). Rather than ask again or pick another already-covered proxy target, diffed every `*Screen.kt`
file in the repo against the inventory table directly (`find . -name "*Screen.kt"` minus `build/` output,
minus `admin/*` which is out of scope per the doc's own scope note) — and this time it surfaced two screens
genuinely missing from the inventory entirely, not just unaudited within a tracked group:
`OnboardingScreen.kt` (`feature-onboarding/.../onboarding/compose/OnboardingScreen.kt`) and
`BottomNavSplashScreen.kt` (`app/.../navigation/BottomNavSplashScreen.kt`, backed by
`AppLauncherIcon.kt` in the same package). Both are real, user-navigable screens (first-run onboarding
flow; the launch splash shown while the nav shell loads) that predate or were added after this doc's last
full sweep and were simply never added as rows. (Ruled out as non-screens: `DestinationScreen.kt` in
`core:navigation-api` is a plain enum, not UI; `DynamicScreen.kt` in `ui-common` is a breakpoint-switching
layout helper, not a screen of its own — neither belongs in the inventory.)

**OnboardingScreen.kt findings**:

1. `OnboardingPageIndicator`'s inactive-dot color uses `MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha
   = 0.3f)` — the same alpha-blend de-emphasis anti-pattern fixed repeatedly elsewhere in this audit (most
   recently §53's `AgendaScreen.kt` captions), where a `.copy(alpha = X)` on a text/foreground color stands
   in for a purpose-built full-opacity token. The precedent from §39 (`CalendarScreen.kt`'s de-emphasized
   other-month day numbers, `onSurface.copy(alpha = 0.35f)` → `outlineVariant`) is the closer match here since
   this is also a de-emphasized indicator dot, not body text — `outlineVariant` is the likely target.
2. No manual `FontWeight` override anywhere in the file (checked against the established trigger for the
   `*Emphasized` typography tokens — confirmed via a repo-wide grep that every existing `*Emphasized` adoption
   in this codebase, e.g. `ChronologicalHomeScreen.kt:355`, `ReminderActionScreen.kt`, `BirthdayActionScreen.kt`,
   `AgendaScreen.kt:349`, replaced a hand-rolled `FontWeight.Bold`/`.SemiBold`/`.Medium` next to a plain
   typography style, not just any headline-sized text). `OnboardingScreen.kt`'s `headlineSmall`/`bodyLarge`/
   `labelMedium` texts all use plain, un-overridden styles — no emphasized-token finding here, unlike some
   earlier audits in this doc that over-applied that check.
3. Icons all already route through `AppIcons.Fluent.*` — no `DrawableCatalog` gap, unlike several older
   screens fixed earlier in this doc; this screen appears to postdate that convention being established.
4. No back button, no `TopAppBar`, no FAB, no off-scale corner radius (`CircleShape` usages are circular
   badges/dots, outside the 10-step scale by design) — nothing else to flag.

**Not fixed / design judgment**: `OnboardingCapabilityCaption`'s icon-chip background,
`Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))`, is a translucent chip sitting on
top of `AnimatedGradientBackground` — visually a "frosted glass over gradient" effect, not the "dim this
text/icon against an opaque surface" pattern item 1 above and every prior alpha-blend fix in this doc
targets. No shared token for that effect exists anywhere else in the codebase (checked `PinLoginScreen.kt`
and other `AnimatedGradientBackground` consumers — none use this pattern), so there's nothing established to
swap it to; left as-is rather than guessing at a fix for an intentional-looking visual effect.

**BottomNavSplashScreen.kt findings**:

5. The app-name reveal animation — `AnimatedVisibility(enter = fadeIn() + slideInVertically { it / 2 })` —
   passes no `animationSpec`, relying on Compose's own built-in defaults rather than
   `MaterialTheme.motionScheme.defaultEffectsSpec()`/`defaultSpatialSpec()`. Same migration target as §54's
   `HomeScreen.kt` fix and `ChronologicalHomeScreen.kt`'s existing pattern, just starting from implicit
   defaults instead of an explicit literal `tween()`.
6. No back button (transient splash, correctly has none), no `TopAppBar`, no alpha-blend, no
   `DrawableCatalog` gap in the screen file itself.

**Not fixed / boundary case, not a genuine gap**: `AppLauncherIcon.kt`'s
`painterResource(R.drawable.ic_launcher_foreground)` is a raw drawable lookup, which CLAUDE.md's icon rule
reads broadly enough to cover ("never referenced as a bare `R.drawable.ic_fluent_*` (or any other
drawable)"). But `DrawableCatalog`/`AppIcons` are organized strictly by UI icon family
(`.Fluent.*`/`.Builder.*`); the launcher foreground/background are mipmap adaptive-icon leaves with no family
to join, single-use by construction, and the file already carries a doc comment explaining why
`painterResource` is called directly here (Compose can't load the `<adaptive-icon>` XML format, only its leaf
drawables — this recreates the round launcher icon from those leaves by hand). Not treating this as a
catalog-convention gap.

**Docs**: added both screens to `docs/m3-expressive-screen-inventory.md` — `Onboarding` under the existing
"Onboarding / Login" section (renamed from tracking PIN Login alone), and a new `Bottom Nav Splash` row.
Both set to "Audited."

**Suggested next step**: two small, independent, low-risk mechanical fixes are ready to land — item 1
(`OnboardingScreen.kt`'s alpha-blend dot → `outlineVariant`) and item 5 (`BottomNavSplashScreen.kt`'s
implicit-default reveal animation → `motionScheme`). Both are one-file, few-line changes consistent with
every other fix landed in this doc.

## 56. `OnboardingScreen.kt` alpha-blend fix — landed

Fixes item 1 of §55: `OnboardingPageIndicator`'s inactive-dot color,
`MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)`, swapped for
`MaterialTheme.colorScheme.outlineVariant` — the same full-opacity-token replacement used for §39's
de-emphasized `CalendarScreen.kt` day numbers, the closest precedent since both are de-emphasized indicator
marks rather than body text.

Verified via `./gradlew :feature:feature-onboarding:compileDebugKotlin :app:compileProDebugKotlin` (clean)
and `:feature:feature-onboarding:detekt --rerun`. Detekt reported 11 weighted issues (5 `Indentation`
findings at lines 58-62, 5 more at 277-281, 1 `UnusedParameter` in `OnboardingNavGraph.kt`) — confirmed
pre-existing and unrelated by stashing this one-line fix and re-running detekt against unmodified HEAD,
which reproduced the identical 11 issues, then restoring the fix (`git stash pop`).

**Not fixed**: item 5 of §55 (`BottomNavSplashScreen.kt`'s implicit-default reveal animation →
`motionScheme`) — still open.

**Suggested next step**: land item 5, the other small fix identified in §55.

## 57. `BottomNavSplashScreen.kt` motion-scheme fix — landed

Fixes item 5 of §55: the app-name reveal animation passed no `animationSpec` to `fadeIn()`/
`slideInVertically()`, relying on Compose's own built-in defaults instead of `MaterialTheme.motionScheme`.

First attempt inlined the fix directly into `AnimatedVisibility`'s `enter =` argument
(`fadeIn(animationSpec = ...) + slideInVertically(animationSpec = ...) { it / 2 }` spread across two lines),
but detekt's `Indentation` rule wanted a continuation-indent shape
(first line flush with `enter =`, second at +2) that reads awkwardly for a named-argument value. Instead
restructured to match §54's `HomeScreen.kt` precedent: hoisted the expression into a local
`nameEnterTransition` val computed in the composable body (this function was already `@Composable`, so no
`remember`/composition-local complication like §54 had), then passed `enter = nameEnterTransition` as a
plain reference. Same val-then-reference shape as `HomeScreen.kt`'s `bannerEnterTransition`, and it passed
detekt cleanly with zero reformatting needed — confirming that shape, not the inline-expression shape, is
what this codebase's detekt indentation config expects for multi-line animation-spec expressions.

Verified via `./gradlew :app:compileProDebugKotlin` (clean) and `:app:detekt --rerun`: zero findings in
`BottomNavSplashScreen.kt` itself; the module's other 71 weighted issues are pre-existing and untouched by
this one-file change (this session made no other edits anywhere in `app`).

With this, both items from §55 are landed. §55 has no remaining findings except the two explicitly
low-priority/design-judgment notes (the translucent onboarding icon-chip background, and the
`AppLauncherIcon.kt` raw-drawable boundary case) — neither calls for a fix.

**Suggested next step**: no open mechanical items remain from §49 or §55. A good next step is auditing
another screen group not yet covered in depth (re-diff `*Screen.kt` files against the inventory the way §55
did, in case more screens are missing from the doc entirely), or continuing the alpha-blend cross-cutting
pass over groups already audited but not yet fixed for it (per the accumulated "Not fixed" notes across
§16-§27).

## 58. Reminders/Notes/Birthdays alpha-blend cross-cutting sweep — landed

Re-checked the "Not fixed" notes across §16-§27 (the Reminders and Notes/Birthdays groups, §6/§8) looking
for a remaining alpha-blend anti-pattern to fix. None of those notes actually name one — every alpha-blend
finding those two audits originally raised was already landed (§17/§18's `ReminderActionScreen.kt`/
`BirthdayActionScreen.kt` de-emphasis fixes, §24's `NotesEmptyState`, §27's `BirthdaysEmptyState`); what's
left open in that note trail is unrelated (orientation-based layout splits, list-row consistency, the
scroll-shadow app bar, `SelectableOptionRow`'s `FontWeight`, `OfflineOnlyRow` dedup).

So rather than trust the doc text alone, grepped the live source of all three feature modules
(`feature-reminder`, `feature-note`, `feature-birthday`) for `onSurface\.copy(alpha`/`onSurfaceVariant\.copy(alpha`/
`onBackground\.copy(alpha` directly — the same "verify current state, don't just trust the audit" approach
§55 used to find the missing Onboarding/Splash screens. `feature-note` and `feature-birthday` came back
clean (confirming §24/§26/§27's "fully closed" claims actually hold). `feature-reminder` did not: two
genuine, previously-unaudited misses.

**`RemindersArchiveScreen.kt`'s `ArchiveEmptyState`** — a private composable, never named in §6 at all,
duplicating the exact `onSurface.copy(alpha = 0.3f)` icon-tint / `alpha = 0.5f` caption shape §24 fixed for
`NotesEmptyState` and §27 fixed for `BirthdaysEmptyState` (§27 itself even called those two "almost certainly
copy-pasted from one to the other" — this is evidently a third copy nobody had traced back to this file).
Migrated onto the shared `EmptyState.kt` composable, same as those two: `icon = AppIcons.Fluent.Archive`,
`message = stringResource(R.string.archive_is_empty)`. Also removed the file's now fully-unused `Icon`
import (its only call site was inside the deleted composable) and the `size` import (only used by the
deleted composable's own `Modifier.size(64.dp)`, since the shared `EmptyState` sizes its own icon
internally).

**`ReminderActionScreen.kt`'s `TodoItemRow`** — the completed-todo-item text used
`if (item.isCompleted) onSurface.copy(alpha = 0.5f) else onSurface`, a fourth ad hoc alpha-blend site in this
exact file that §17's original 3-site sweep (contact phone, email address, email subject) missed entirely —
same file, same anti-pattern, just a different composable §17 didn't happen to look at. Fixed to
`onSurfaceVariant` for the completed case, matching every other de-emphasis fix in this doc; the active
(`else`) branch's plain `onSurface` was left untouched.

**Reviewed and NOT changed** (found during the same grep sweep, judged not to be the anti-pattern):
`MapEditorScreen.kt:98`'s `scrim.copy(alpha = scrimAlpha)` is §19's own already-correct token-based scrim
fix, not a miss. `BuilderSelectorSheet.kt`'s `SelectorItemRow` uses `contentAlpha = if (available) 1f else
0.75f` uniformly across an item's icon/title/description/subtext — this reads as "unavailable selector item"
styling (a disabled-adjacent state applied consistently to every text role in the row), not the
"de-emphasize secondary text against primary text" pattern the `onSurfaceVariant` fixes target; M3's own
disabled-content convention is itself alpha-based, so there's no obvious full-opacity token this should
become instead. Left as a boundary case rather than guessed at.

**Adjacent gap noticed but out of this pass's Reminders/Notes/Birthdays scope**:
`ManagePresetsScreen.kt`'s private `EmptyState` (same `onSurface.copy(alpha = 0.3f/0.5f)` shape, `feature-reminder`
module but a **Settings**-group screen) already has this exact fix named as item 3 of §11's own suggested fix
order and was never landed — a pre-existing gap from a different audit section, not a new find, left for a
Settings-scoped pass rather than folded in here.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-reminder:detekt --rerun`. `RemindersArchiveScreen.kt` briefly showed a new
`NoUnusedImports` hit on the `size` import after the first pass (caught and removed before the final run);
the two files' remaining `ImportOrdering` findings were confirmed pre-existing by diffing against
`git show HEAD` (`RemindersArchiveScreen.kt`'s `com.github.naz013.ui.reminder.UiReminderList` import was
already out of lexicographic order relative to `com.github.naz013.ui.common.compose.AppIcons` before this
pass touched the file; `ReminderActionScreen.kt`'s edit never touched any import line at all — a same-line
value swap with zero net line delta).

**Not fixed**: `ManagePresetsScreen.kt`'s empty state (§11, Settings group — noted above, deliberately left
for a Settings-scoped pass). Everything else in §16-§27's "Not fixed" trail remains what it already was
(orientation splits, list-row consistency, scroll-shadow app bars, `SelectableOptionRow` `FontWeight`,
`OfflineOnlyRow` dedup) — none of it alpha-blend, so out of scope for this specific sweep.

**Suggested next step**: this closes out the alpha-blend cross-cutting pass for Reminders/Notes/Birthdays —
no more grep hits for the anti-pattern in any of those three modules. `ManagePresetsScreen.kt`'s matching gap
is a small, well-scoped fix if picking up the Settings group (§11/§12) next; otherwise, re-diffing
`*Screen.kt` files against the inventory (the §55 method) for more undiscovered screens remains open too.

## 59. `ManagePresetsScreen.kt` empty-state fix — landed

Fixed the adjacent gap §58 noticed but deliberately left out of its Reminders/Notes/Birthdays scope: item 3
of §11's own suggested fix order, named there and never landed since. Same private-`EmptyState`-composable
shape as §58's `RemindersArchiveScreen.kt` fix (and §24's `NotesEmptyState`/§27's `BirthdaysEmptyState`
before that) — `onSurface.copy(alpha = 0.3f)` icon tint, `alpha = 0.5f` caption — migrated onto the shared
`EmptyState.kt` composable: `icon = AppIcons.Builder.Preset` (already cataloged), `message =
stringResource(R.string.recur_no_presets)`.

One naming wrinkle this file's fix hit that the others didn't: its private composable was itself named
`EmptyState`, shadowing the shared one — deleting it and adding the `ui-common` import resolves the call
site to the shared composable by the same name, so the diff reads as if the call just grew two new
arguments, not a rename.

Removed six imports that lost their only call site once the private composable was deleted (`Icon`,
`Arrangement`, `Alignment`, `size`, `painterResource`, `Text`, `dp` — `dp` had no remaining use anywhere in
the file either); added `AppIcons` and the shared `EmptyState` import.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-reminder:detekt --rerun`: one pre-existing `ImportOrdering` finding and a run of
`Indentation` findings on the untouched `modifier =` chain and the untouched preview's `listOf(...)` block.
Confirmed pre-existing by stashing this change and re-running detekt against unmodified HEAD: identical 945
weighted-issue total, same findings shifted by exactly the line count this fix removed, then restored via
`git stash pop`.

With this, every empty-state alpha-blend duplicate found across §24, §27, §58, and this section is closed —
`NotesEmptyState`, `BirthdaysEmptyState`, `RemindersArchiveScreen.kt`'s `ArchiveEmptyState`, and now
`ManagePresetsScreen.kt`'s own copy, all on the shared `EmptyState.kt`.

**Suggested next step**: §11's own suggested fix order named `HolidayCountryScreen.kt` alongside
`ManagePresetsScreen.kt` for this exact same alpha-blended empty-state pattern — a same-module (`feature-settings`),
same-fix candidate worth checking next if continuing the Settings group. Otherwise, re-diffing `*Screen.kt`
files against the inventory (the §55 method) for more undiscovered screens remains open.

## 60. `HolidayCountryScreen.kt` alpha-blend fix and `GeneralSettingsScreen`/`SingleChoiceDialog` dedup — landed

Two of §11's remaining suggested-fix-order items, landed together in one batch at the user's request rather
than as two separate turns.

**Item 3, second half — `HolidayCountryScreen.kt`'s alpha-blend fix**: unlike the `ManagePresetsScreen.kt`
half of this finding (§59), this screen's "empty" state is a single centered `Text` with no icon — a
"no search results" message, not an icon+caption empty-list pattern — so it doesn't fit the shared
`EmptyState.kt` composable's shape (which requires an `icon: Painter`) and was never meant to: §11's finding
#5 only ever asked for the token swap, not a component migration. One-line fix:
`MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)` → `onSurfaceVariant`. Verified via
`./gradlew :feature:feature-settings:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-settings:detekt --rerun` — zero findings in this file.

**Item 4 — `GeneralSettingsScreen`'s private `SingleChoiceDialog` deduped onto the shared `ui-common` one**:
checked for real differences first, same discipline as §23's `OfflineOnlyRow` dedup, rather than trusting
§11's "functionally equivalent" description at face value. The two were **not** interchangeable as originally
described: the shared version's dialog `Column` had no height cap or scroll, while
`GeneralSettingsScreen`'s private version wrapped its `Column` in
`.heightIn(max = 400.dp).verticalScroll(rememberScrollState())` — necessary because this dialog's option
list is the app's language picker, which runs to dozens of entries and would overflow the dialog without a
cap. Swapping to the shared component as-is would have silently dropped that behavior.

Rather than skip the dedup or fork the difference, ported the height-cap/scroll behavior into the shared
`SingleChoiceDialog.kt` itself, making it the more complete, canonical version — the same "pick the more
correct behavior as canonical" call §23 made. This is additive, not a breaking change for the dialog's other
3 existing callers (`CalendarSettingsScreen.kt`, `BirthdaySettingsScreen.kt`, `LocationSettingsScreen.kt`):
`heightIn(max = 400.dp)` is a ceiling that has no effect on option lists already shorter than that, and
`verticalScroll` on content that doesn't overflow is inert — neither changes appearance or behavior for a
short list, only enables correct behavior for a long one.

`GeneralSettingsScreen.kt`'s private `SingleChoiceDialog(dialog: GeneralSettingsDialog, ...)` composable
(45 lines) was deleted entirely; its call site now calls the shared `SingleChoiceDialog(title, options,
selectedIndex, onOptionSelected, onDismiss)` directly, unpacking the three fields off `state.dialog` instead
of passing the whole object (a real, if minor, signature difference — the shared component takes primitives,
not this screen's own dialog-state type). Removed ten imports that lost their only call site with the
private composable (`Row`, `fillMaxWidth`, `heightIn`, `selectable`, `selectableGroup`, `AlertDialog`,
`RadioButton`, `TextButton`, `Alignment`, `Role`) — `dp`, `Text`, `verticalScroll`, and `rememberScrollState`
were checked individually and kept, since each still has a real call site in the screen's own outer `Column`.

Verified via `./gradlew :ui:ui-common:compileDebugKotlin :feature:feature-settings:compileDebugKotlin
:app:compileProDebugKotlin` (all clean) and detekt on both touched modules.
`:ui:ui-common:detekt --rerun` hit the same pre-existing `PermissionRequester.kt` crash already documented
in §47 (confirmed unrelated — this change never touches that file). `:feature:feature-settings:detekt --rerun`
flagged 4 `Indentation` hits on `GeneralSettingsScreen.kt`'s untouched outer-`Column` modifier chain;
confirmed pre-existing by stashing the change and re-running against unmodified HEAD, which reported the
same 4 hits (shifted) plus 12 more inside the now-deleted private composable's own body — 425 weighted
issues at HEAD vs. 413 with this fix applied, a net *decrease* since deleting the duplicate removed its own
debt along with it. Restored via `git stash pop`.

**Not fixed**: §11's item 5 (consolidating the app's four hand-rolled seek/slider `AlertDialog`s into one
shared `SeekValueDialog`) — explicitly lower-urgency per §11's own ordering, and a larger rework than this
batch's two items.

**Suggested next step**: with items 1-4 of §11's suggested fix order now all landed, only item 5 (the
`SeekValueDialog` consolidation) remains from that list. Otherwise, re-diffing `*Screen.kt` files against the
inventory (the §55 method) for more undiscovered screens remains open.

## 61. Shared `SeekValueDialog` consolidation — landed

Fixed §11's last remaining item, item 5: four hand-rolled seek/slider `AlertDialog`s across
`RemindersSettingsScreen.kt`, `BirthdaySettingsScreen.kt` (already extracted into its own private
`SeekValueDialog` composable), `NoteSettingsScreen.kt`, and `LocationSettingsScreen.kt` consolidated onto one
new shared `SeekValueDialog` in `ui-common`, alongside `SingleChoiceDialog`/`MultiChoiceDialog`. Turned out to
be **five** instances, not four — `LocationSettingsScreen.kt` has two independent seek dialogs (Radius and
Tracker) under one `when` block, both counted as "the Location screen's dialog" by §11's original tally.

**Checked for real differences before designing the shared API**, same discipline as every prior dedup in
this doc (§23, §60). The five were not interchangeable:

- **Stepped vs. continuous slider**: Birthday's two dialogs and Location's Tracker dialog pass `steps`
  (discrete increments); Reminders', Note's, and Location's Radius dialog don't (continuous drag).
- **Haptic feedback**: Birthday and Note wire `LocalHapticFeedback` + a `hapticFeedbackEnabled` flag into the
  slider's `onValueChange`; Reminders and both Location dialogs have no haptic at all.
- **Confirm button text**: Birthday uses `R.string.save`; every other instance uses `R.string.ok`.
- **Body content shape**: four of five show one `Text` (the current value) above the `Slider`; Location's
  Tracker dialog shows *two* — an explanatory `titleSmall` sentence ("for lower battery usage, set bigger
  values") followed by the `titleLarge` value — the one dialog in the group that also uses a heavier text
  style for its value than the other four's `bodyLarge`.
- **One real, not just stylistic, inconsistency worth flagging rather than silently carrying forward**:
  Note's dialog compared the new slider value against `state.colorOpacity` (the last *confirmed* value) to
  decide whether to fire haptic feedback, while Birthday's compared against its own `value` parameter (the
  live *preview* value, i.e. what every render of the dialog already shows). Note's version meant haptic
  feedback wouldn't fire on most drag steps — only when crossing back over the last-saved value — which reads
  as an unintentional divergence, not a deliberate design choice; the other four dialogs' consistent
  "compare against the currently-displayed value" logic is what the shared component now does for everyone,
  Note's call site included.

**The shared component** (`ui-common/compose/foundation/dialog/SeekValueDialog.kt`) takes the union of what
every call site needs, defaulted to match the majority behavior: `title`, `value`, `valueText`, `valueRange`,
`onValueChange`, `onConfirm`, `onDismiss` are required; `description: String? = null` (Location Tracker's
extra sentence), `valueTextStyle: TextStyle = MaterialTheme.typography.bodyLarge` (Location Tracker passes
`titleLarge` explicitly to keep its exact existing appearance — a real, if minor, visual choice from before
consolidation, preserved rather than normalized away since nothing suggested it was accidental),
`steps: Int = 0` (continuous by default), `hapticFeedbackEnabled: Boolean = false`, and
`confirmText: String = stringResource(R.string.ok)` (Birthday passes `R.string.save` explicitly) are
optional. Haptic wiring (the `LocalHapticFeedback` lookup and the `performHapticFeedback` call) lives inside
the shared component now, not duplicated at each call site.

**Per-file changes**: `BirthdaySettingsScreen.kt`'s private `SeekValueDialog` (36 lines) deleted outright,
its two call sites gaining `steps = 4` and `confirmText = stringResource(R.string.save)`.
`RemindersSettingsScreen.kt`'s inline `AlertDialog` block replaced with a `SeekValueDialog` call using only
the required parameters (its defaults already match). `NoteSettingsScreen.kt`'s inline block replaced the
same way, dropping its own `LocalHapticFeedback.current` local val (now redundant) and passing
`hapticFeedbackEnabled = state.hapticFeedbackEnabled`. `LocationSettingsScreen.kt`'s two inline blocks
(Radius, Tracker) each replaced with their own `SeekValueDialog` call, Tracker's carrying `steps = 28`,
`description`, and `valueTextStyle = MaterialTheme.typography.titleLarge`.

Removed now-fully-unused imports across all four call-site files: `AlertDialog`, `Slider`, `TextButton`,
`Text` (where no longer used elsewhere in the file), `Column` (checked individually — still needed by each
screen's own outer layout `Column`, so kept everywhere), `fillMaxWidth` (missed on the first pass for
`BirthdaySettingsScreen.kt` — caught by `NoUnusedImports` on the first detekt run, then proactively grepped
the other three files for the same miss before rerunning, finding none), and `LocalHapticFeedback`/
`HapticFeedbackType` in `BirthdaySettingsScreen.kt`/`NoteSettingsScreen.kt` (Reminders and Location never had
these, matching their no-haptic behavior).

Verified via `./gradlew :ui:ui-common:compileDebugKotlin :feature:feature-birthday:compileDebugKotlin
:feature:feature-reminder:compileDebugKotlin :feature:feature-settings:compileDebugKotlin
:app:compileProDebugKotlin` (all clean after one fix: `TextStyle` is `androidx.compose.ui.text.TextStyle`,
not `androidx.compose.material3.TextStyle` — caught immediately by the first compile attempt). Detekt on all
four touched feature modules; every finding confirmed pre-existing via stash-and-rerun-against-HEAD for each
file (`BirthdaySettingsScreen.kt`: 95 weighted issues both before and after, same indentation shifted by the
removed lines; `RemindersSettingsScreen.kt`: 935 both before and after, including its own pre-existing
`MultiLineIfElse` finding on the unrelated `dndValueColor` line; `feature-settings` module: 409 at HEAD vs.
408 with the fix — one fewer, because deleting `NoteSettingsScreen.kt`'s old inline lambda (with its awkward
`onOpacityPreviewChange(it.toInt()) }` trailing-brace wrapping) also removed a pre-existing `Wrapping`
finding along with it, a genuine improvement, not something hidden). `ui:ui-common:detekt` hit the same
pre-existing `PermissionRequester.kt` crash from §47/§60 — this change doesn't touch that file, and the new
`SeekValueDialog.kt` was checked by hand against `SingleChoiceDialog.kt`'s established formatting (2-space
indent, trailing commas) since the module-wide crash prevents a direct per-file detekt run on it this session.

This closes out every item in §11's suggested fix order (1 through 5).

**Suggested next step**: with §11 fully closed, re-diffing `*Screen.kt` files against the inventory (the §55
method) for more undiscovered screens remains the main open thread, alongside §12's still-unaudited-for-fixes
findings (the doc has only ever audited, not fixed, most of §12's own list beyond the gradient-hero dedup in
§15) and the various design-judgment items carried since earlier sections (scroll-shadow-to-color-fill,
breakpoint-based layout splits).

## 62. §12 items 4 and 5 — `HeaderItemsSettingsScreen.kt` drag-handle a11y fix and `ProVersionScreen.kt` emphasized type — landed

Items 1-3 of §12's suggested fix order were already landed (1/2 in §14's shared-scaffold fix, 3 in §15's
gradient-hero dedup, both of which predate this doc segment). This lands items 4 and 5, batched together per
the user's request to group ready fixes rather than land them one at a time.

**Item 4 — `HeaderItemsSettingsScreen.kt`'s drag handle**: two findings, following the reference pattern
`SubTasksValueEditor.kt` (§20) already established for the identical problem on its own reorder rows.

- **48dp touch target**: the drag handle icon was sized directly at `Modifier.size(20.dp)` — both its visual
  size and its actual gesture-detection region (`detectDragGesturesAfterLongPress` is attached to that same
  modifier chain), well under the 48×48dp minimum. Wrapped it in a new `Box(DRAG_HANDLE_TOUCH_SIZE = 48.dp)`
  with the icon centered inside at its original 20dp — same "48dp interactive footprint, smaller icon
  centered inside" shape §20 used for `SubTasksValueEditor.kt`'s check/remove buttons
  (`ROW_BUTTON_SIZE`/`.fillMaxSize().padding(12.dp)`), just via an explicit `Box` here since there's no
  `IconButton` wrapper in this row to begin with. Note this is a real, deliberate layout change, not a
  behavior-preserving one: the row's leading column grows from 20dp to 48dp, shifting everything after it
  right by 28dp — an accepted, correct consequence of actually meeting the touch-target minimum, the same
  way any `IconButton` reserves its full 48dp footprint regardless of its icon's drawn size.
- **TalkBack-reachable reorder action**: the long-press-drag gesture itself has no screen-reader equivalent,
  exactly the gap `SubTasksValueEditor.kt`'s `ShopItemRow` already solves via `CustomAccessibilityAction`s
  exposing one-step move-up/move-down actions. Copied that exact pattern: the parent `items(...)` block now
  computes `onMoveUp`/`onMoveDown` (nullable lambdas, `null` at the list's start/end respectively) from
  `displayIndex` within `state.configurableItems`, calling the existing `onReorder(fromIndex, toIndex)`
  callback — mirroring `SubTasksValueEditor.kt`'s identical `displayIndex`/`onMoveUp`/`onMoveDown` shape
  verbatim. `ConfigurableHeaderItemRow` gained `onMoveUp`/`onMoveDown` parameters and a
  `Modifier.semantics { customActions = listOfNotNull(...) }` block on the drag-handle `Box`, using the same
  already-existing, already-localized `R.string.cd_move_item_up`/`cd_move_item_down` strings
  `SubTasksValueEditor.kt` uses — no new string resources needed.

One formatting miss caught by detekt on the first pass: writing each `CustomAccessibilityAction`'s lambda
body as `{ action(); true }` (semicolon-separated on one line) tripped the `Wrapping` rule ("Missing newline
after \";\""); reformatted to match `SubTasksValueEditor.kt`'s own multi-line block shape
(`action()` / `true` on separate lines) instead of copying the semicolon shorthand.

**Item 5 — `ProVersionScreen.kt`'s emphasized type**: `headlineSmall` → `headlineSmallEmphasized` (the "Pro
advantages" headline) and `titleMedium` → `titleMediumEmphasized` (each advantage line). Worth noting this
doesn't follow the narrower "only swap on a manual `FontWeight` override" trigger §58 established for most
emphasized-token fixes in this doc — there was no `FontWeight` override here, just baseline styles carrying
the screen's "hero" emphasis through color (`tertiary`) alone. This screen is the one explicit exception:
§12's own audit text named it directly as the group's single clearest hero-moment candidate and put this
swap in its suggested fix order as a deliberate, no-downside adoption ("costs nothing further to wire up"),
not a mechanical anti-pattern sweep — different trigger, same doc, done because the audit that found it
asked for it by name.

Verified via `./gradlew :feature:feature-settings:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-settings:detekt --rerun`. `ProVersionScreen.kt`: zero findings in both runs.
`HeaderItemsSettingsScreen.kt`: after the `Wrapping` fix, exactly 389 weighted issues — confirmed identical
to unmodified HEAD's own 389 (stash-and-rerun comparison), the same pre-existing indentation block on the
untouched `dragHandleModifier`/`rowModifier` `pointerInput` chain, shifted by the 17 lines this fix added.

This closes every item in §12's suggested fix order (1 through 5), same as §11's earlier full closure.

**Suggested next step**: with both Settings audits (§11, §12) now fully closed on their suggested fix orders,
the remaining threads are re-diffing `*Screen.kt` files against the inventory (the §55 method) for more
undiscovered screens, §12's lower-priority screen-specific notes that were never in its fix order
(`OtherSettingsScreen.kt`'s two icon-less rows, `HeaderItemsSettingsScreen`'s own row-composable divergence
from `SettingsItem`, `CloudBackupSettingsScreen.kt`'s `LoadingIndicator` candidacy), and the accumulated
design-judgment items (scroll-shadow-to-color-fill, breakpoint-based layout splits) carried since earlier
sections.

## 63. Screen-inventory re-diff (negative result) and `OtherSettingsScreen.kt` icon fix — landed

**Re-diff, no new screens found**: repeated §55's `*Screen.kt`-vs-inventory diff to check for any screen
added or missed since the last pass. This time the raw diff produced 3 false positives
(`BuildReminderScreen.kt`, `MapEditorScreen.kt`, `ReminderHelpScreen.kt` briefly looked "untracked" because
the first-pass `grep -v "/build/"` filter meant to exclude Gradle build output also matched these files'
own package path — `feature/reminder/build/...` is a real source package named "build", not a build
directory — so the filter incorrectly excluded genuinely-tracked files; corrected by restricting the file
search to actual `*/src/main/*` paths instead of grep-excluding a substring). After that correction, every
remaining untracked file is one already known and excluded: the `admin/cloudtestadmin` and
`admin/reviewsadmin` modules' 6 screens (debug-only, explicitly out of scope per this doc's own scope note)
and `DestinationScreen.kt`/`DynamicScreen.kt` (confirmed non-screens back in §55 — a plain enum and a
breakpoint-switching layout helper, neither a UI screen). No genuine gap this time — the inventory is
currently complete.

**`OtherSettingsScreen.kt`'s two icon-less rows** (§12 screen-specific finding, never in its numbered fix
order): "Permissions" and "Allow Permission" were the only 2 of 12 `SettingsItem` rows in this screen with no
`icon`, leaving them visually mis-aligned against every sibling row (the icon column reserves its width
whether or not one is supplied). No dedicated "permission" icon exists in `DrawableCatalog`/`AppIcons` — the
closest cataloged fits are the two already-existing lock icons — so "Permissions" got `AppIcons.Fluent.LockShield`
(already used elsewhere for the conceptually adjacent "Lock Screen Visibility" row in
`RemindersSettingsScreen.kt`) and "Allow Permission" got the plainer `AppIcons.Fluent.Lock`, keeping the two
adjacent rows visually distinct from each other rather than both using the same icon.

Verified via `./gradlew :feature:feature-settings:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-settings:detekt --rerun` — zero findings in `OtherSettingsScreen.kt`, and the module's
total weighted-issue count is unchanged from §62's 389, confirming no new debt.

**Suggested next step**: §12's two remaining screen-specific notes are both non-urgent by the audit's own
framing — `HeaderItemsSettingsScreen`'s row-composable divergence from `SettingsItem` needs a new
optional-drag-handle-slot API design, not a mechanical fix, and `CloudBackupSettingsScreen.kt`'s
`CircularProgressIndicator`→`LoadingIndicator` swap was explicitly flagged "optional, not urgent." With the
screen inventory confirmed complete and both Settings audits (§11, §12) fully worked through, the main open
threads left project-wide are the accumulated design-judgment items: the scroll-shadow-to-color-fill
migration (Home/Agenda from §49, Reminders/Birthdays Archive from §6/§27), and the Reminders/Notes/Birthdays
groups' breakpoint-based layout splits — both need a product/design decision rather than a mechanical fix.

## 64. Scroll-shadow-to-color-fill migration — landed

Fixed the scroll-shadow-on-scroll pattern flagged across §6/§27 (`RemindersArchiveScreen.kt`,
`BirthdaysScreen.kt`) and §49 (`ChronologicalHomeScreen.kt`, `AgendaScreen.kt`) — 4 screens, all growing a
drop shadow under their app bar once the list scrolls, against guidelines §9.6's explicit "App bars... On
scroll: color fill instead of drop shadow."

**Design decision made explicit**: this had been deferred since §27 specifically because a real migration
path — wiring `TopAppBarDefaults.pinnedScrollBehavior()`/`enterAlwaysScrollBehavior()` via
`Modifier.nestedScroll(...)` — changes real UX behavior beyond "add a color instead of a shadow" (§27's own
text: `enterAlwaysScrollBehavior()` would let the whole app bar slide off-screen on scroll-down, a behavior
none of these 4 screens has today). Given the user's instruction to continue with this specific migration
now, the decision made was the narrowest one that satisfies the guideline literally without also deciding a
separate, bigger, unrequested UX change: keep each screen's existing "always-pinned, gains a visual cue on
scroll" behavior exactly as-is, and swap only the visual cue itself — shadow → color fill — using the same
`isScrolled` boolean state each screen already computes. No `nestedScroll`/`scrollBehavior` wiring was
introduced anywhere; this is a token/mechanism swap on already-working scroll-state plumbing, not a
migration onto M3's built-in scrollable-app-bar machinery.

**Color choice verified against the actual library, not assumed**: extracted
`material3-android-1.5.0-alpha27-sources.jar` (the exact version on this project's classpath, same diligence
as §19/§22) and confirmed `AppBarTokens.OnScrollContainerColor = ColorSchemeKeyTokens.SurfaceContainer` —
i.e. `MaterialTheme.colorScheme.surfaceContainer` is M3's own real default for exactly this "app bar container
color once scrolled" case, not a guess. Each screen already used `MaterialTheme.colorScheme.background` as
its unscrolled color (matching this app's established `TopAppbarColor` convention elsewhere, not M3's own
`Surface` default) — kept that side as-is, since only the *scrolled* half of the pair was ever the gap.

**The fix, applied identically across all 4 files**: replaced each `animateDpAsState`-driven
`headerElevation: Dp` (which fed `shadowElevation`/`Modifier.shadow(...)`) with an `animateColorAsState`-driven
`headerContainerColor: Color` (`background` unscrolled → `surfaceContainer` scrolled), and dropped the shadow
parameter/modifier entirely rather than keeping it at 0 — the guideline says "instead of," not "in addition
to."

- **`AgendaScreen.kt`, `RemindersArchiveScreen.kt`, `BirthdaysScreen.kt`** (identical shape in all 3):
  `Surface(color = MaterialTheme.colorScheme.background, shadowElevation = headerElevation)` →
  `Surface(color = headerContainerColor)`. Removed each file's now-unused `HEADER_ELEVATION = 3.dp` constant
  and `animateDpAsState` import; added `animateColorAsState`.
- **`ChronologicalHomeScreen.kt`** (a different shape — no `TopAppBar`/`Surface`, just a plain header
  `Column`): `Modifier.shadow(elevation = headerElevation, clip = false).background(background)` →
  `Modifier.background(headerContainerColor)`. Removed the now-unused `androidx.compose.ui.draw.shadow`
  import alongside `animateDpAsState`.

Verified via `./gradlew :feature:feature-home:compileDebugKotlin :feature:feature-agenda:compileDebugKotlin
:feature:feature-reminder:compileDebugKotlin :feature:feature-birthday:compileDebugKotlin
:app:compileProDebugKotlin` (all clean) and detekt on all 4 modules, each checked against unmodified HEAD via
stash-and-rerun. All 4 files' post-fix findings were either identical to HEAD (`BirthdaysScreen.kt`: same
pre-existing `Wrapping`/`ArgumentListWrapping` on its still-unmigrated private `BirthdaysEmptyState`, just
shifted by the 3 net lines this fix added) or strictly fewer than HEAD
(`ChronologicalHomeScreen.kt`: 20→17, `AgendaScreen.kt`: 29→10, `RemindersArchiveScreen.kt`: 935→931) —
in every reduced case the file had already been externally reformatted on disk mid-session (flagged by
this session's own "changed on disk" notices for these exact 3 files), and the removed findings were
pre-existing `Indentation`/`Wrapping`/`ArgumentListWrapping` debt at lines this fix never touched, not
anything this change caused. `CyclomaticComplexMethod` findings on `EventCard`
(`ChronologicalHomeScreen.kt`) and the `AgendaScreen` composable itself (`AgendaScreen.kt`) are unchanged in
both value and relative position — confirmed pre-existing, untouched by a `Dp`-to-`Color` state swap that
adds no branching.

**Not fixed / deliberately out of scope**: whether any of these 4 app bars *should* hide on scroll
(`enterAlwaysScrollBehavior()`) rather than stay pinned remains an open, separate product question — this
fix only addressed the literal "shadow vs. color fill" gap the guideline named, not a broader scroll-behavior
redesign nobody asked for.

This was the last of the accumulated design-judgment items carried since §6/§27/§49. Combined with §63's
confirmation that the screen inventory is complete and §11/§12's full closure, every open item this doc has
tracked with a concrete, nameable fix is now landed.

**Suggested next step**: no further tracked findings remain open in this doc as mechanical or now-decided
items. A fresh full re-audit of a screen group (re-reading current source against
`m3-expressive-guidelines.md` from scratch, the way §6/§8/§9/§10 etc. originally did) would be the way to
surface anything new — the accumulated backlog from prior audits has been fully worked through.

## 65. Widget Configuration group — fresh re-audit and two fixes — landed

Every screen-inventory row now reads "In progress" or "Done" — there's no longer a literal "unaudited group"
or "not fixed" item left to point at (§64's own closing note). Per the user's request for "a fresh full
re-audit of the next screen group," picked **Widget Configuration** (the 7 `*WidgetConfigScreen.kt` screens
+ `WidgetConfigScaffold.kt` + `ColorSlider.kt`) specifically because its shared-component gaps (§13's
findings #1/#2/#3/#5) were fixed years-in-doc-time ago (§14, §16, §47) but every one of §13's *screen-specific*
findings — the ones that don't reduce to "fix the shared file once" — were never revisited. Re-read all 7
screens' current source in full against the guidelines, from scratch, rather than just re-checking §13's old
finding list against memory.

**Confirmed still holding, no drift found**: `WidgetConfigScaffold.kt` still has the real content
description, `TopAppbarColor`, and cataloged `AppIcons.Fluent.Dismiss` from §14/§47. `ColorSlider.kt` still
has full semantics (content description, `progressBarRangeInfo`, `setProgress`) from §16, and every one of
the 7 screens now correctly sizes it at `.height(48.dp)` (the `.height(36.dp)`/`.height(40.dp)` sub-target
sizes §13 flagged are gone). No alpha-blend anti-pattern anywhere in the group. The
`dimensionResource(R.dimen.home_screen_widget_corner_radius)` mock-preview corner radius and the decorative
mock-preview icons are both still correctly out of scope, matching §13's own original judgment calls on both.

**Fixed — §13's never-landed `modifier`-parameter-order finding**: `SingleNoteWidgetConfigScreen`,
`NotesWidgetConfigScreen`/`NotesWidgetMockPreview`, `CalendarWidgetConfigScreen`/`CalendarWidgetMockPreview`,
`EventsWidgetConfigScreen`/`EventsWidgetMockPreview`, `BirthdaysWidgetConfigScreen`, and
`TasksWidgetConfigScreen`/`TasksWidgetMockPreview` all put `modifier: Modifier = Modifier` last instead of
first, against CLAUDE.md's explicit convention. Confirmed every call site (`*WidgetConfigActivity.kt`,
`@Preview` composables, and mock-preview call sites) already uses named arguments before reordering any
declaration — a pure parameter reorder, zero behavior change. `CombinedWidgetConfigScreen` and
`BirthdaysWidgetMockPreview` already had it right (§13 named exactly these two as the correct examples);
left untouched.

**New finding, not in §13's original list — `EventsWidgetConfigScreen.kt`'s own hand-rolled seek dialog**:
its text-size picker is an `AlertDialog { Text + Slider }` with inline haptic-feedback wiring — the *exact*
shape §61 just consolidated four other instances of onto the shared `ui-common` `SeekValueDialog`, missed
there only because widget-config screens weren't in that pass's scope. Migrated it onto `SeekValueDialog`
the same way: `title`/`value`/`valueText`/`valueRange`/`steps`/`hapticFeedbackEnabled` map directly, and the
screen's own `LocalHapticFeedback.current` local val (now redundant — the shared component owns that
wiring) was removed along with the now-unused `AlertDialog`/`Slider`/`TextButton`/`HapticFeedbackType`/
`LocalHapticFeedback`/`roundToInt` imports.

Verified via `./gradlew :extensions:appwidgets:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:extensions:appwidgets:detekt --rerun`: 4 weighted issues total across the whole module, all one
pre-existing `ImportOrdering` finding on `SingleNoteWidgetConfigScreen.kt` (already documented as
pre-existing back in §16) — confirmed identical, not shifted, via stash-and-rerun against unmodified HEAD
(param reorders don't move import lines, so the finding landed at the exact same line before and after).

**Not fixed / still open, unchanged from §13**: `EventsWidgetConfigScreen.kt` still puts its text-size
control behind a dialog while `SingleNoteWidgetConfigScreen.kt` uses an inline `Slider` for the equivalent
controls — a cross-screen UX inconsistency, not a compliance gap on its own, and now that both use the
correct underlying primitives (real M3 `Slider`, real `SeekValueDialog` where a dialog is used) there's even
less pressure to force one screen's pattern onto the other without a product opinion on which shape is
right. `CircularProgressIndicator`→`LoadingIndicator` remains open too, but that's `LocalBackupScreen.kt`/
`InsightsScreen.kt` (Backup/Insights group), not this one.

**Suggested next step**: no further findings surfaced in this group. Continuing the same "fresh full
re-audit" approach on another group — Groups/Tags/Places or Calendar/Google Tasks are the two that have had
the fewest post-audit fix sections landed relative to their original finding counts — would be the next
place to look for anything similarly missed.

## 66. Groups/Tags/Places group — fresh re-audit, `modifier`-order fixes — landed

Picked **Groups/Tags/Places** for the next fresh full re-audit. Correction to §65's own "suggested next
step" before starting: checking §9's fix history first (not just its finding count) showed this group
actually had the *most* dedicated fix sections of any group audited so far — §29 through §33, five sections,
covering every cross-cutting item and screen-specific note in the original audit. Not the under-serviced
group the "fewest fix sections" framing suggested — worth re-auditing anyway, since a fully-closed audit
list is exactly the situation where a genuinely fresh read (not just re-checking the old list) is most
likely to catch something the original pass missed, which is what happened here.

Re-read all 8 screens, `GroupListItem.kt`/`TagListItem`/`PlaceListItemCard.kt` (list rows),
`GroupReminderRow.kt`/`TagDetailRows.kt` (dispatcher rows), and `ColorPickerCard.kt` (shared color picker)
in full, from scratch, against the guidelines — not against §9's finding list from memory.

**Confirmed every §29-§33 fix is still holding, no drift**: real back/save-button content descriptions on
all 8 screens (including `TagEditScreen.kt`'s save button, still an icon-only `MenuIconButton` rather than a
self-labeling `MenuTextButton` like its siblings — the component-choice half of that finding was explicitly
left as-is by §29, only the missing description was the actual accessibility defect). `TopAppbarColor` on
all 8. `GroupsEmptyState`/`TagsEmptyState`/`PlacesEmptyState`/`TagDetailsEmptyState` all gone, replaced by
the shared `EmptyState.kt`. `GroupListItem`/`TagListItem`/`PlaceListItemCard` all now use `titleMedium` for
their primary label. `DrawableCatalog`/`AppIcons` used everywhere checked. `ColorPickerCard.kt` still the
positive, spec-correct example §9 called out (`surfaceContainer` card, `titleMedium` + `primary` title,
`outlineVariant`-bordered selection dot) — no change since.

**New finding, fixed — the same `modifier`-parameter-order violation §65 fixed in Widget Configuration,
never checked here**: `GroupsScreen`, `TagsScreen`, `PlacesScreen` (the 3 top-level list screens) and every
list-row/dispatcher composable — `GroupListItem`, `TagListItem` (private, in `TagsScreen.kt`),
`PlaceListItemCard`, `GroupReminderRow`, `TagDetailItemRow` — all put `modifier: Modifier = Modifier` last
instead of first, against CLAUDE.md's convention. The same split pattern found in Widget Configuration
repeats here too: every screen that has a `renderAsDetailPane` toggle (`GroupDetailsScreen`,
`EditGroupScreen`, `TagEditScreen`, `TagDetailsScreen`, `EditPlaceScreen`) already had it right; only the
plain list/dispatcher composables didn't. Confirmed every call site uses named arguments before reordering
any declaration (`*NavGraph.kt` entries, `@Preview` composables, and inter-composable call sites) — a pure
parameter reorder, zero behavior change, same discipline as §65.

Note: `ColorPickerCard.kt`'s own signature (`colors, selectedIndex, onColorSelected, modifier, title, ...`)
technically also has required params before `modifier`, and none of the recently-added shared dialogs
(`SingleChoiceDialog`, `MultiChoiceDialog`, `SeekValueDialog`) expose a `modifier` parameter at all — both
are real, wider instances of the same CLAUDE.md convention gap, but pursuing either is a different, much
larger scope than "the composable already has a trailing `modifier` param, just move it" — this pass stayed
within the narrower, already-precedented fix, matching §65 rather than expanding scope mid-audit.

**New finding, NOT fixed — `EditGroupScreen.kt`'s `DelayMinutes` dialog is a third leftover hand-rolled
seek/slider `AlertDialog`** (after §61's four and §65's `EventsWidgetConfigScreen.kt`), but this one doesn't
fit the shared `SeekValueDialog`'s shape and wasn't forced onto it. Every prior `SeekValueDialog` migration
target renders its value-text-plus-`Slider` content unconditionally whenever the dialog is open;
`EditGroupScreen.kt`'s version wraps an "inherit from settings" `Switch` row (always shown) around a
value-text-plus-`Slider` section that only renders `if (dialog.isOverridden)` — the slider itself is
conditionally present, not just accompanied by optional description text the way `SeekValueDialog`'s
`description` parameter already handles. Forcing this one caller's conditional-content shape onto a
component 6 other call sites already use cleanly would mean growing `SeekValueDialog`'s API for a single
user, which risks making it harder to read for everyone else rather than easier — left as a documented,
found-but-not-migrated gap rather than guessed at. Worth reconsidering if a second dialog with this same
"optional slider section behind a toggle" shape ever turns up.

Verified via `./gradlew :feature:feature-group:compileDebugKotlin :feature:feature-tags:compileDebugKotlin
:feature:feature-places:compileDebugKotlin :app:compileProDebugKotlin` (all clean) and detekt on all three
modules, each checked against unmodified HEAD via stash-and-rerun: identical weighted-issue counts before
and after in every module (`feature-group`: 15, `feature-tags`: 30, `feature-places`: 8) — confirming the 7
parameter reorders introduced zero new findings anywhere.

**Suggested next step**: `EditGroupScreen.kt`'s `DelayMinutes` dialog remains open as a documented,
deliberately-not-forced gap. Otherwise, Calendar/Google Tasks is the next candidate for the same fresh-audit
treatment — and worth checking its fix-section count directly first this time, rather than assuming from the
finding-count framing the way this section's own opening had to correct.

## 67. Calendar/Google Tasks group — fresh re-audit, `modifier`-order and `FontWeight`→`Emphasized` fixes — landed

Checked §10's fix history first this time, per §66's own correction. Calendar/Google Tasks turned out the
same as every other group checked so far: §34 through §39, six sections, closed every item in §10's
suggested fix order (back-button, `TopAppbarColor`, deprecated baseline FAB, `TimelinePager.kt`'s off-scale
corner radius, `detailScreenContentWidth()`, alpha-blend). Re-audited anyway, on the same reasoning as §65/§66
— a fully-closed list doesn't mean a fresh read won't find something new, and this one did, on both counts
that have held for the last two groups.

Re-read all 8 screens, `TimelinePager.kt`, and `CalendarModeToggleButton.kt` in full, from scratch.

**Confirmed every §34-§39 fix still holding**: real content descriptions, `TopAppbarColor` everywhere,
`SmallExtendedFloatingActionButton` on both Google Tasks screens' FABs, `TimelinePager.kt`'s corner radii
on-scale, `detailScreenContentWidth()` on the three Google Tasks detail screens plus
`GoogleCalendarEventPreviewScreen.kt`, `GoogleTasksEmptyState` gone (migrated to shared `EmptyState.kt` in
§39). No drift.

**Fixed — the same `modifier`-parameter-order violation §65/§66 already fixed twice**: 12 composables across
`CalendarScreen.kt`, `CalendarModeToggleButton.kt`, `TimelineScreen.kt`, all 7 composables in
`TimelinePager.kt` (`TimelinePager`, `TimelinePage`, `TimelineDayHeader`, `TimelineHolidayRow`,
`HolidayChip`, `HourAxis`, `TimelineDayColumn`), `GoogleTasksScreen.kt` (plus its private `TaskListTile`/
`NotLoggedInContent`), and `TaskListScreen.kt`. Same split as the last two groups: every screen with
`renderAsDetailPane` already had it right (`GoogleCalendarEventPreviewScreen`, `PreviewGoogleTaskScreen`,
`EditGoogleTaskScreen`, `EditGoogleTaskListScreen`) — worth noting `TaskListScreen.kt` breaks that pattern
slightly, since it *has* `renderAsDetailPane` but still had `modifier` last, unlike every other
detail-pane-capable screen checked in this doc so far. Confirmed every call site (nav graphs, `@Preview`
composables, internal call sites within `TimelinePager.kt`) uses named arguments before reordering; pure
parameter reorder, zero behavior change.

**Fixed — `CalendarModeToggleButton.kt`'s manual `FontWeight` override**: `CalendarModeRow`'s selected-state
label used `fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal` on a baseline
`titleMedium` style — §10 flagged this as "worth noting" and named it as exactly the guidelines §3.1
"context" trigger for `titleMediumEmphasized`, but never put it in the suggested fix order, so it never
landed. Fixed now, following the same trigger §17/§18/§58 established elsewhere in this doc: swapped to
`style = if (selected) titleMediumEmphasized else titleMedium`, removed the `fontWeight` parameter and the
now-unused `FontWeight` import.

**New finding, NOT fixed — extensive `DrawableCatalog`/`AppIcons` bypass across the whole group**: this
audit surfaced far more raw `painterResource(R.drawable.*)`/`PopupMenuItem(iconRes = R.drawable.*)` call
sites than any group checked so far — roughly 25+ across `CalendarScreen.kt` (overflow menu, add-event
bubble rows), `CalendarModeToggleButton.kt` (its own trigger icon), `TimelineScreen.kt` (add-menu items),
`TimelinePager.kt` (`HolidayChip`'s globe icon), `GoogleCalendarEventPreviewScreen.kt` (delete icon),
`GoogleTasksScreen.kt` (list-add icon, FAB icon), `TaskListScreen.kt` (overflow icon, FAB icon),
`PreviewGoogleTaskScreen.kt` (edit/delete/complete icons, plus its `DetailRow`'s `icon: Int` parameter
threading 7 more raw drawable IDs through from its own call sites), `EditGoogleTaskScreen.kt` (move/delete
icons), and `EditGoogleTaskListScreen.kt` (delete icon). Every icon checked already exists in
`DrawableCatalog.Fluent`/`AppIcons.Fluent` (matching §33's experience doing the same cleanup for
Groups/Tags/Places — no new catalog entries needed, pure call-site migration), but the volume here is large
enough — spanning 10 files, several with a dozen-plus call sites apiece — that folding it into this section
risked losing the modifier-order and `FontWeight` fixes in a much bigger diff. Left as a clearly-scoped,
ready-to-land item for its own pass, the same way §33 and §47 each got their own dedicated section rather
than being bundled into the audit that found them.

Verified via `./gradlew :feature:feature-calendar:compileDebugKotlin :feature:feature-googletask:compileDebugKotlin
:app:compileProDebugKotlin` (all clean) and detekt on both modules, checked against unmodified HEAD via
stash-and-rerun: identical weighted-issue counts before and after in both modules (`feature-calendar`: 193,
`feature-googletask`: 6) — confirming the 12 parameter reorders and the one `FontWeight` fix introduced zero
new findings. The `feature-googletask` findings that do show (5 `NoUnusedImports` in `GoogleTasksScreen.kt`)
match §39's own already-documented pre-existing baseline exactly.

**Suggested next step**: the `DrawableCatalog`/`AppIcons` cleanup identified above is the natural next item —
large but entirely mechanical, same shape as §33's Groups/Tags/Places pass. Otherwise, Workflow/Routines is
the one remaining screen group not yet given the fresh-re-audit treatment in this doc.

## 68. Calendar/Google Tasks group — `DrawableCatalog`/`AppIcons` cleanup — landed

Landed the cleanup §67 identified but deliberately deferred: the raw `painterResource(R.drawable.*)` and
`PopupMenuItem(iconRes = R.drawable.*)` bypass spanning all 10 files in the Calendar/Google Tasks group.

Confirmed by re-grepping the group from scratch (not just the files §67 named) that every occurrence was
already accounted for — no additional call sites turned up. Verified every drawable referenced already has
a `DrawableCatalog.Fluent`/`DrawableCatalog.Builder` entry (and, where a `Painter` was needed, a matching
`AppIcons.Fluent`/`AppIcons.Builder` wrapper) before touching any call site — same check §33 and §47 both
did first, so no new catalog entries were needed here either.

**Fixed — 19 call sites across 10 files**:
- `CalendarModeToggleButton.kt`: the mode-switcher trigger icon, the selected-row checkmark.
- `TimelinePager.kt`: `HolidayChip`'s globe icon.
- `GoogleCalendarEventPreviewScreen.kt`: the delete action icon.
- `CalendarScreen.kt`: the add-event bubble's two rows (`AddEventRow` itself keeps its generic `iconRes: Int`
  parameter — it's shared by both, and now always receives a `DrawableCatalog` constant instead of a bare
  resource ID), the overflow menu icon, and its one settings `PopupMenuItem`.
- `TimelineScreen.kt`: the add-menu's two `PopupMenuItem`s (reformatted to multi-line — the constant name is
  a few characters longer than the raw resource reference, which pushed both lines past the line-length
  limit).
- `GoogleTasksScreen.kt`: the new-list-icon and the FAB icon.
- `TaskListScreen.kt`: the overflow-menu icon and the FAB icon.
- `PreviewGoogleTaskScreen.kt`: the edit/delete top-bar icons, the complete-FAB icon, the tags-row icon, and
  all 7 raw drawable IDs threaded through `DetailRow`'s call sites (`DetailRow` itself keeps its generic
  `icon: Int` parameter, same reasoning as `AddEventRow` above — it's now always fed a `DrawableCatalog`
  constant, never a bare `R.drawable.*`).
- `EditGoogleTaskScreen.kt`: the move and delete top-bar icons.
- `EditGoogleTaskListScreen.kt`: the delete top-bar icon.

Every fix is a pure call-site substitution — `painterResource(R.drawable.ic_fluent_x)` →
`AppIcons.Fluent.X` (or `AppIcons.Builder.X`) where a `Painter` was expected, `R.drawable.ic_fluent_x` →
`DrawableCatalog.Fluent.X` (or `.Builder.X`) where a raw `@DrawableRes Int` was expected — plus removing the
now-unused `painterResource` import from the 8 files where it had no remaining use (it stays imported in
`CalendarScreen.kt` and `PreviewGoogleTaskScreen.kt`, where `AddEventRow`/`DetailRow` still call it
internally on their generic `Int` parameter) and adding a `DrawableCatalog` import to the 4 files that
needed one for the first time. No behavior change.

Verified via `./gradlew :feature:feature-calendar:compileDebugKotlin :feature:feature-googletask:compileDebugKotlin`
(both clean) and detekt on both modules, checked against unmodified HEAD via stash-and-rerun: identical
weighted-issue counts before and after in both modules (`feature-calendar`: 193, `feature-googletask`: 6) —
confirming zero new findings from the 19 substitutions and the import changes.

**Suggested next step**: Workflow/Routines remains the one screen group never given the fresh-re-audit
treatment used for the last three groups.

## 69. Workflow/Routines group — fresh re-audit, `modifier`-order, `FontWeight`→`Emphasized`, drop-shadow, touch-target, and motion-spec fixes — landed

Fresh full re-audit of the last screen group never given this treatment. Re-read all 8 screens from
scratch — `WorkflowGalleryScreen.kt`, `WorkflowRulesForGroupScreen.kt`, `WorkflowRulesForReminderScreen.kt`,
`builder/WorkflowRuleBuilderScreen.kt`, `WorkflowRuleRow.kt`, `WorkflowTemplateCard.kt`,
`RoutinesListScreen.kt`, `RoutineEditScreen.kt`, `RoutinePreviewScreen.kt`, `RoutineExecutionScreen.kt` —
plus `RoutineNavGraph.kt` to confirm call-site argument style before reordering any signature.

**Confirmed still holding from §7/§40/§43**: back-button content descriptions fixed on all 8 screens,
`TopAppbarColor` on all 8, no raw `R.drawable.*`/`painterResource(R.drawable.*)` anywhere in either module
(both were already fully on `DrawableCatalog`/`AppIcons` — nothing left for a §68-style cleanup here).

**New finding, fixed — `modifier`-parameter-order violation, but only in `feature-routine`**: unlike the
last three groups, `feature-workflow`'s 6 composables (`WorkflowGalleryScreen`, `WorkflowRulesForGroupScreen`,
`WorkflowRulesForReminderScreen`, `WorkflowRuleBuilderScreen`, `WorkflowRuleRow`, `WorkflowTemplateCard`)
already had `modifier` first — no fix needed there. `feature-routine` was the opposite: every single
screen had `modifier` as its *last* parameter, the mirror-image of the bug this doc has fixed three groups
running. Fixed all 4 public screens (`RoutinesListScreen`, `RoutineEditScreen`, `RoutinePreviewScreen`,
`RoutineExecutionScreen`) plus 6 private composables that had the same shape
(`RecurrenceOptionPicker`, `WeekdaySelector`, `DayOfMonthPicker`, `RoutineStepRow` in `RoutineEditScreen.kt`;
`RunningContent`, `FinishedContent` in `RoutineExecutionScreen.kt`). Confirmed every call site — all 4 in
`RoutineNavGraph.kt`, plus every internal call site within `RoutineEditScreen.kt`/`RoutineExecutionScreen.kt`
— uses named arguments and never passes `modifier` explicitly (all rely on the default), so the reorder is
behavior-free.

**Fixed — `RoutineExecutionScreen.kt`'s `FontWeight.Bold` cluster**, the last unfixed item from §7's
cross-cutting finding #3: the "Complete step" button label (`titleMedium` → `titleMediumEmphasized`), the
"Step X of N" counter (`labelLarge` → `labelLargeEmphasized`), the step title (`headlineSmall` →
`headlineSmallEmphasized`), and the finished-state headline (`headlineSmall` → `headlineSmallEmphasized`).
Verified all three `*Emphasized` tokens are real properties on `androidx.compose.material3.Typography` by
extracting `material3-android-1.5.0-alpha27-sources.jar` (this project's exact classpath version, same
diligence as §19/§22/§61) rather than assuming the names — confirmed `headlineSmallEmphasized` and
`labelLargeEmphasized` exist alongside the already-used `titleMediumEmphasized`. Removed the now-unused
`FontWeight` import.

**Fixed — `RoutineExecutionScreen.kt`'s bottom bar drop shadow**, the last unfixed item from §7's
screen-specific findings: `Surface(shadowElevation = 4.dp)` wrapping the "Complete step" button was already
called out by §7 itself as "an M2-style drop-shadow-on-surface treatment rather than the M3 color-fill/tonal
approach" — the exact same principle §64 applied to 4 scrolling top app bars. This bar isn't scroll-linked
(it's simply present whenever `RoutineExecutionState.Running`), so there's no unscrolled/scrolled pair to
animate between here — the fix is a direct swap: `shadowElevation = 4.dp` → `color =
MaterialTheme.colorScheme.surfaceContainer`, dropping the shadow parameter entirely rather than keeping it
at 0, matching §64's "instead of, not in addition to" reading of the guideline.

**Fixed — `RoutinePreviewScreen.kt`'s 40dp check-toggle touch target**, the item §7 and the screen-inventory
doc both left open pending wherever `SubTasksValueEditor.kt` landed its own fix for the identical
compact-checklist-row trade-off. That fix has since landed — `SubTasksValueEditor.kt`'s row-action buttons
now use a named `ROW_BUTTON_SIZE = 48.dp` constant — so this screen's `IconButton.size(40.dp)` was bumped to
`48.dp` to match, closing the gap.

**Fixed — `RoutinePreviewScreen.kt`'s literal `tween()` motion**, §7's cross-cutting finding #4, deferred at
the time as "not urgent... a natural fix alongside item 3" (item 3 was `RoutinesListScreen`'s empty state,
landed since in §42). The 8 `tween(CHECK_ANIMATION_MS)` calls across the check-toggle's two
`AnimatedVisibility` blocks (scale+fade in each direction, ×2 for the checked/unchecked icons) were swapped
for `MaterialTheme.motionScheme.defaultSpatialSpec()` (the `scaleIn`/`scaleOut` calls) and
`.defaultEffectsSpec()` (the `fadeIn`/`fadeOut` calls) — the same spatial/effects pairing already established
in `ChronologicalHomeScreen.kt` for an identical scale+fade `AnimatedVisibility`. Removed the now-unused
`CHECK_ANIMATION_MS` constant and `androidx.compose.animation.core.tween` import.

Verified via `./gradlew :feature:feature-routine:compileDebugKotlin :feature:feature-workflow:compileDebugKotlin
:app:compileProDebugKotlin` (all clean) and detekt on both modules, checked against unmodified HEAD via
stash-and-rerun: identical weighted-issue counts before and after in both modules (`feature-routine`: 2,
`feature-workflow`: 5) — confirming zero new findings from the 10 modifier reorders, the 4 typography-token
swaps, the shadow-to-color-fill swap, the touch-target bump, and the 8 motion-spec swaps combined. The one
finding that does show in `feature-routine` (`NoUnusedImports` on `RoutinePreviewScreen.kt`'s pre-existing
unused `androidx.compose.foundation.background` import) is confirmed pre-existing via the stash comparison,
untouched by any of this section's edits.

This closes out every item §7 ever flagged for this group, mechanical or otherwise — the only findings left
unactioned from that audit are `RoutineEditScreen.kt`'s indirect up-chevron-via-180°-rotation implementation
and `WorkflowTemplateCard.kt`'s `titleSmall`-for-description type-role mismatch, both explicitly logged in §7
as low-severity/non-defects rather than open work.

**Suggested next step**: every screen group in the inventory has now had a fresh full re-audit. No
group-level work remains queued; future sections should pick up from whatever the user's next request is,
rather than another audit pass.

## 70. Home — full adoption pass, promoted to "Done"

The user asked to promote Home (`HomeScreen.kt` + `ChronologicalHomeScreen.kt`) to "Done" — the screen-
inventory doc's own legend defines that as "fully reflects the `ui-common` expressive foundation," a higher
bar than "no known defects." Home had already received the deepest build-out of any screen in this doc
(§3/§4's original pass, plus §50-§54/§64 touching the sibling `AgendaScreen.kt`/`HomeScreen.kt`), so this
pass re-verified every prior claim against current source before deciding what, if anything, was still
missing — rather than trusting the doc's own "landed" labels at face value.

**Discrepancy found and worth flagging plainly**: two fixes the doc records as "landed" for this exact file
were not actually present in current source:
- §52's `DrawableCatalog` cleanup never reached `ChronologicalHomeScreen.kt`'s `AddButton` — it still had 5
  raw `R.drawable.*` references in its `when` block (`HomeScreenState.kt`'s half of that same §52 fix, the
  `EventAction.IconRes` companion, *was* correctly on `DrawableCatalog` — only the `AddButton` half was
  missing).
- §64's scroll-shadow-to-color-fill migration, which the doc explicitly lists as covering 4 files including
  `ChronologicalHomeScreen.kt`, was never applied here — the header was still driving `animateDpAsState` into
  `Modifier.shadow(elevation = headerElevation, clip = false)`, the exact pre-Expressive pattern §64 says it
  replaced.

No theory for how this happened is confirmed — `git log` shows only one commit ever touching this file
(`faf8e86af Migrate to M3 Expressive`) plus this session's own step commits, so the gap predates this
session. Both are now fixed as part of this pass (see below) rather than left as an open question, but future
sessions should treat this doc's "landed" labels as claims to spot-check against current source before
building on them, not as verified fact — this is the first time in ~70 sections that a claim didn't hold up.

**Re-verified as genuinely landed** (matches doc claims, confirmed against current source): Greeting →
`headlineMediumEmphasized`; header nav tiles → `AppShapes.tile`, tonal `item.color.copy(alpha = 0.16f)` icon
chip, `labelSmallEmphasized`/`titleMediumEmphasized` title/subtitle; `EventCard` → `AppShapes.card`,
`isSelected`→`isOverdue`→`Birthday`→default color priority, `bodyMediumEmphasized`/`bodySmallEmphasized` text;
`TimeSectionRow` time label → `bodyMediumEmphasized`; stagger animations (tiles + rows) →
`MaterialTheme.motionScheme`; `HomeScreen.kt`'s banner transitions → `MaterialTheme.motionScheme` (§54);
`GetNavigationItemsUseCase.kt`'s per-section themed colors (§5's `Color.Green` stub resolution) → real
`ThemeProvider.themedColor` values, one per section. No `FontWeight` overrides, no literal `RoundedCornerShape`,
no literal `tween()` anywhere left in either file.

**Fixed — the two missing "landed" items above**, plus two new findings from this pass's own fresh line-by-line
read (never flagged in §49 or anywhere else):
- `AddButton`'s 5 raw `R.drawable.*` references → `DrawableCatalog.Fluent.Alert`/`.FoodCake`/`.Note`/`.Cart`
  and `DrawableCatalog.Builder.GoogleTaskList`.
- Scroll header: `animateDpAsState(0.dp → 4.dp)` driving `Modifier.shadow(...)` → `animateColorAsState`
  driving `Modifier.background(...)` (`background` unscrolled → `surfaceContainer` scrolled), following §64's
  exact pattern (color fill instead of drop shadow, target removed entirely rather than kept at 0). Removed
  the now-unused `androidx.compose.ui.draw.shadow` import; swapped `animateDpAsState` for `animateColorAsState`.
- **New**: `EventCard`'s per-row action `MenuIconButton` was sized `Modifier.size(36.dp)` — below the 48dp
  minimum touch target (guidelines §8). Verified against the real `IconButtonImpl` source
  (`material3-android-1.5.0-alpha27-sources.jar`, same diligence as §61/§64/§69) that an outer `.size()`
  modifier passed into `IconButton` constrains the box *before* its internal
  `.minimumInteractiveComponentSize()` call gets a chance to expand it — so 36dp really did cap the tappable
  area, it wasn't just a visual icon-scale choice. Bumped to 48dp, matching the same fix already applied to
  `SubTasksValueEditor.kt`'s and `RoutinePreviewScreen.kt`'s equivalent compact-row action buttons this
  session (§69).
- **New**: `EventCard`'s default (non-selected/non-overdue/non-birthday) case paired `containerColor =
  CardDefaults.cardColors().containerColor` (resolves to `surfaceContainerLow`) with `onContainerColor =
  MaterialTheme.colorScheme.onBackground` instead of `onSurface` — §49 flagged this exact pairing as a
  "token-hygiene note... worth a look if this file is touched," never acted on. Fixed now that the file is
  genuinely being touched: `onBackground` → `onSurface`.

**Deliberately not touched, and not required for "Done"**: `HomeEvent.color` stays unread by `EventCard` (an
explicit, still-open design question from §5 about whether a third color signal on the card would conflict
with the new overdue/birthday container colors — a product decision, not a foundation-adoption gap); shape
morphing (FAB open/close, loading indicators) stays out of scope per §5's own reasoning that it's the
highest-effort, most novel piece of Expressive and isn't what any other "Done" screen in this doc has needed
either.

Verified via `./gradlew :feature:feature-home:compileDebugKotlin :feature:feature-home:testDebugUnitTest
:app:compileProDebugKotlin` (compiles clean, all existing tests pass unchanged — no test touched this
session, since none of these fixes changed observable state/logic) and detekt, checked against unmodified
HEAD via stash-and-rerun: baseline carried 20 weighted issues including 3 `Indentation` findings on the exact
shadow-modifier block this fix replaced; this fix's version carries 17, with those 3 gone as a side effect
and every other finding identical (two location-only, modulo the net +1 line this fix added, no new
categories). Zero new findings.

**Promoted "Home" from "In progress" to "Done"** in `docs/m3-expressive-screen-inventory.md` — the first
screen in this doc's Home/Events-through-Workflow/Routines arc to reach that status (as opposed to the 4
earlier "Done" screens, all thin WebView/static wrappers promoted in §48 where reaching the bar was closer to
automatic).

**Suggested next step**: `AgendaScreen.kt` — sibling screen in the same Home/Events group. Spot-checked its
scroll-shadow state while writing this section: it has the identical gap just fixed for Home — `Surface(color
= MaterialTheme.colorScheme.background, shadowElevation = headerElevation)` driven by `animateDpAsState` is
still there, so §64's "landed" claim doesn't hold for this file either. A full re-verification pass on
Agenda (not just this one spot-check) is the natural next step before assuming "Done" is as close as the doc
currently implies.

## 71. Agenda — full adoption pass, promoted to "Done"

Continuation of §70's pattern, applied to `AgendaScreen.kt` and every shared component it renders through.
Re-read the whole screen fresh and re-verified every §49-§54/§64 claim against current source before deciding
what was actually still open — the spot-check at the end of §70 already predicted this would matter.

**The discrepancy is worse here than for Home — worth stating plainly**: every single one of §50, §51, §52,
§53, and §64's claimed Agenda-specific fixes turned out to still be unlanded in current source, including a
genuine accessibility defect (not just a foundation-adoption gap):
- §50 claimed `AgendaTopBar`'s back arrow got a real `contentDescription`. Current source: still `null`.
- §51 claimed the bare `Icons.Default.FilterList` was swapped for `AppIcons.Fluent.Filter`. Current source:
  still `Icons.Default.FilterList`, `androidx.compose.material.icons.*` still imported.
- §52 claimed the `DrawableCatalog` cleanup covered `AgendaScreen.kt` too. Current source: 9 raw
  `R.drawable.*`/`painterResource(R.drawable.*)` references remained, matching §49's original "10 bare
  references in one file" finding almost exactly (one fewer, since `AgendaSelectionTopBar` — a later addition
  from the multiselect feature — hadn't existed yet when §49 counted).
- §53 claimed the two alpha-blended filter-sheet captions were fixed. Current source: both still
  `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)`.
- §64 claimed the scroll-shadow-to-color-fill migration covered this file. Current source: still
  `animateDpAsState` driving `Surface(shadowElevation = ...)`, the exact pattern §64 says it removed.

Checked `git log` for `AgendaScreen.kt`: the only commits since `feature: extract agenda feature to the
feature module` are `Migrate to M3 Expressive` and a later `Add multiselect to the Agenda and Birthdays list
screens`. Diffed the multiselect commit specifically in case it had reverted something — it didn't; every one
of the defects above was already present in the multiselect commit's *parent*, meaning none of §50-§53/§64
ever reached this file's committed history at all, going back further than this whole doc's step-commit arc.
Same conclusion as §70: no evidence of a revert, the fixes simply never landed, and this doc's "landed"
labels are claims worth spot-checking, not verified fact — second time now, and worse than the first.

**Fixed — all five items above**, in `AgendaScreen.kt`:
- Back arrow → `stringResource(R.string.cd_back)`.
- `Icons.Default.FilterList` → `AppIcons.Fluent.Filter`; removed the now-unused
  `androidx.compose.material.icons.Icons`/`.filled.FilterList` imports.
- 9 raw drawable references (`AgendaSelectionTopBar`'s archive/delete actions, `AddMenuButton`'s 3 add-type
  icons, `OverflowMenuButton`'s 3 menu-item icons plus its own trigger icon) → `DrawableCatalog.Fluent.*`/
  `.Builder.Tag` / `AppIcons.Fluent.MoreVertical`. Removed the now-unused `painterResource` import.
- Two `onSurface.copy(alpha = 0.5f)` captions ("no tags"/"no groups" in the filter sheet) → `onSurfaceVariant`.
- Scroll header: `animateDpAsState` + `Surface(shadowElevation = ...)` → `animateColorAsState` +
  `Surface(color = headerContainerColor)`, identical pattern to §70's Home fix (`background` unscrolled →
  `surfaceContainer` scrolled). Removed the now-unused `HEADER_ELEVATION` constant.

**New finding, fixed**: `AgendaTopBar`'s `TopAppBarDefaults.topAppBarColors(containerColor =
Color.Transparent)` was missing the `titleContentColor` pairing `TopAppbarColor` normally carries — flagged
as "worth doing" back in §49 but never actioned. Could **not** just swap to the full `TopAppbarColor` token
the way every other screen in this doc has, though: that token's `containerColor` is opaque
(`MaterialTheme.colorScheme.background`), and this screen deliberately keeps its own app bar transparent so
the *wrapping* `Surface` — the same one the scroll-color-fill fix above just touched — shows through and
carries the scroll-driven tint. Making the app bar itself opaque would paint over that tint and silently
defeat the fix two bullets up. Landed the narrower, non-conflicting half instead: added `titleContentColor =
MaterialTheme.colorScheme.onBackground` to the existing transparent `topAppBarColors(...)` call, leaving
`containerColor = Color.Transparent` untouched.

**Fixed — shared components `AgendaScreen.kt` renders through**, found on the same fresh read (never flagged
by §49, since that audit only read `AgendaScreen.kt` itself, not its dependencies) — in scope here because
they're exactly what "full adoption" for this screen renders through, with the side effect of also fixing
Groups, Reminders Archive, Notes, Birthdays, Tags, and every multiselect screen that shares them:
- `ui-common/.../AgendaListItem.kt` (shared row scaffold behind both `ReminderAgendaRow`/`BirthdayAgendaRow`,
  and — per its own docstring — Groups' and Reminders Archive's rows too): its "more options" icon was
  `painterResource(R.drawable.ic_fluent_more_vertical)` → `AppIcons.Fluent.MoreVertical`; its decorative
  `AgendaChip`'s `RoundedCornerShape(8.dp)` literal → `MaterialTheme.shapes.small` (verified against the real
  M3 1.5.0-alpha27 `ShapeTokens.kt` that `CornerSmall = 8.dp` exactly — same value, now a token, matching
  guidelines §4.1's "corner radii come from the shape scale... not a literal `Ndp` value").
- `ui-common/.../SelectionTopBar.kt` (the shared multiselect top bar used by every screen with bulk-select —
  see `docs/multiselect.md`): same raw `painterResource(R.drawable.ic_fluent_more_vertical)` →
  `AppIcons.Fluent.MoreVertical`; its `TopAppBarDefaults.topAppBarColors(containerColor =
  MaterialTheme.colorScheme.background)` had no wrapping-`Surface` reason to stay split like `AgendaTopBar`
  above, so swapped to the full `TopAppbarColor` token outright, picking up the `titleContentColor` pairing
  for every screen using this bar, not just Agenda's.
- `ui-agenda/.../ReminderAgendaRow.kt` and `BirthdayAgendaRow.kt`: 8 more raw `R.drawable.*` references
  across both files' `AgendaMenuAction.iconResOrNull()` (open/edit/archive/delete/skip/turn-off) →
  `DrawableCatalog.Fluent.*`. Both files already had `PIN`/`UNPIN` correctly on `DrawableCatalog` — only the
  older actions were still raw.

**Confirmed already correct, no fix needed**: `FilterChipLabel` and `UiAgendaHeader` already use
`labelLargeEmphasized`/`titleMediumEmphasized` real tokens (§49 noted this already held); no `FontWeight`
override anywhere in `AgendaScreen.kt` or its dependencies; `AgendaListItem.kt`'s main-text `titleMedium` is
intentionally *not* emphasized — this doc's established trigger for `xxxEmphasized` has consistently been
"replacing an existing manual `FontWeight` override" (§58/§62/§67/§69), and there's no such override here to
replace, so adding emphasis would be scope creep beyond what any other shared row component in this doc has
received; no touch-target gaps (every `MenuIconButton` in this group uses its default size, none pass an
explicit sub-48dp `Modifier.size(...)`, unlike Home's `EventCard` action button in §70).

Verified via `./gradlew :feature:feature-agenda:compileDebugKotlin :ui:ui-agenda:compileDebugKotlin
:ui:ui-common:compileDebugKotlin :feature:feature-group:compileDebugKotlin
:feature:feature-tags:compileDebugKotlin :feature:feature-birthday:compileDebugKotlin
:feature:feature-note:compileDebugKotlin :feature:feature-reminder:compileDebugKotlin
:app:compileProDebugKotlin` (all clean — the last 5 modules are the shared-component consumers, checked
since `AgendaListItem.kt`/`SelectionTopBar.kt` changed) and
`:feature:feature-agenda:testDebugUnitTest :ui:ui-agenda:testDebugUnitTest` (pass, unchanged — no fix here
touched observable state/logic). Detekt on all 3 directly-edited modules, checked against unmodified HEAD via
stash-and-rerun: `ui-agenda` and `ui-common` came back with weighted-issue counts identical to baseline (zero
new findings, `ui-common` reports zero issues in both versions). `feature-agenda` came back higher — 33 vs.
baseline's 29 — but every one of those 4 extra weighted points is the *same* `Indentation` finding
(`items =` followed by a `listOf(...)` continuation indented 2 spaces deeper than detekt expects) already
present, unfixed, at 3 other untouched locations in this exact file in *both* versions; reformatting
`AddMenuButton`'s birthday `PopupMenuItem` onto multiple lines (needed to keep it under the line-length limit
after the `DrawableCatalog` swap) extended that same pre-existing, already-2-off indentation onto more lines
of the same block, and a byte-for-byte-unchanged block elsewhere in the file (`AgendaFilterBottomSheet`'s
`Column` modifier, confirmed via `git diff` to carry zero changes) started being flagged too — ktlint's
indentation rule tracks nesting via a stateful pass through the whole file rather than pure per-node AST
recursion, so a line-count shift earlier in the file can change what it reports for untouched code later on.
Confirmed by eye: every newly-reported line matches the identical "actual = expected + 2" signature already
accepted as pre-existing debt at the 3 other occurrences of this same pattern in this file — not a new
violation type, not code this pass touched in substance.

**Promoted "Agenda" from "In progress" to "Done"** in `docs/m3-expressive-screen-inventory.md` — closing out
the Home/Events group entirely (both rows now "Done").

**Suggested next step**: given two consecutive "full adoption" passes both found the doc's own "landed"
claims didn't hold for the specific file they focused on, it's worth treating every other still-"In progress"
screen's claimed fixes the same way — spot-check against current source before trusting the doc — rather than
assuming this was isolated to Home/Events. No specific screen is queued next; that's a call for whoever picks
this up.

## 72. Reminders group — re-verification of §17/§19-§23/§28/§48/§58's claims

Re-verified every "landed" claim across 9 sections (§17, §19, §20, §21, §22, §23, §28, §48, §58) touching
this group's 9 files, against current source rather than trusting the doc text — the pattern §71 closed on.
Read `ReminderActionScreen.kt`, `MapEditorScreen.kt`, `SubTasksValueEditor.kt`, `SelectApplicationScreen.kt`,
`PreviewReminderScreen.kt`, `ReminderFullscreenMapScreen.kt`, `TodoEditScreen.kt`, `BuildReminderScreen.kt`
(spot-checked for the `OfflineOnlyRow` import), `RemindersArchiveScreen.kt`, `ReminderHelpScreen.kt`, and
`RecurHelpScreen.kt` fresh.

**Much better news than §70/§71**: the great majority of this group's claims hold up. Confirmed genuinely
landed, matching the doc exactly: §17's 9 `FontWeight`→emphasized swaps and 3 alpha-blend→`onSurfaceVariant`
sites and the off-scale card-elevation removal on `ReminderActionScreen.kt`; §19's `tween`→
`defaultSpatialSpec()`, 640dp max-width, and scrim-token fixes on `MapEditorScreen.kt`; §20's
`tween`→fast-tier `motionScheme` specs and 40dp→`ROW_BUTTON_SIZE = 48.dp` on `SubTasksValueEditor.kt`; §21's
`Card`-own-`onClick` fix on `SelectApplicationScreen.kt` and its surface-container-role/`AttachmentRow`
grouping fixes on `PreviewReminderScreen.kt`; §22's FAB swap on `ReminderFullscreenMapScreen.kt`; §23's
`OfflineOnlyRow` extraction (the shared file exists, both screens import it); §58's `TodoItemRow`
alpha-blend fix on `ReminderActionScreen.kt`; §28's back-button fix on `RemindersArchiveScreen.kt`; and §48's
"Done" verdict for `ReminderHelpScreen.kt`/`RecurHelpScreen.kt`.

**Real gaps found and fixed anyway** — two different shapes:

1. **Three back-button `contentDescription = null` bugs, never claimed fixed by any section, exactly as §6
   originally found them** — not a "doc said X but it's false" case like §70/§71, more a "the fix-order
   sequence moved on to type/motion/elevation work across §17-§23 and never came back to close every
   instance of item 1" gap. §6's cross-cutting #1 named 4 files with this bug
   (`SelectApplicationScreen.kt:72`, `PreviewReminderScreen.kt:112`, `RemindersArchiveScreen.kt:146`,
   `TodoEditScreen.kt:70`); only the third ever got a dedicated fix (§28). The other three were still
   exactly as broken as §6 first found them. Fixed all three now:
   `SelectApplicationScreen.kt`'s sole back arrow → `stringResource(R.string.cd_back)`;
   `PreviewReminderScreen.kt`'s non-detail-pane branch (the detail-pane/close branch was already correct) →
   same, reformatted to a multi-line `if`/`else` matching the shape §29 established for this exact split
   elsewhere; `TodoEditScreen.kt`'s sole back arrow → same.
2. **`RemindersArchiveScreen.kt` had 2 of its 3 claimed fixes not actually landed** — the same
   doc-vs-reality gap §70/§71 found, on the one file in this group that had multiple claims. §64 claims this
   file's scroll-shadow was migrated to color-fill; current source still had `animateDpAsState` driving
   `Surface(shadowElevation = ...)`, untouched. §58 claims `ArchiveEmptyState` was migrated onto the shared
   `EmptyState.kt`; it was still a private composable with the exact `onSurface.copy(alpha = 0.3f/0.5f)`
   icon/caption pattern §58 says it removed. Only §28's back-button fix (confirmed above) actually landed
   for this file. Fixed both now, identically to the pattern §70/§71 already established:
   `animateDpAsState`/`shadowElevation` → `animateColorAsState`/`Surface(color = headerContainerColor)`
   (`background` unscrolled → `surfaceContainer` scrolled); `ArchiveEmptyState` deleted and replaced with
   `EmptyState(icon = AppIcons.Fluent.Archive, message = stringResource(R.string.archive_is_empty))` at its
   one call site. Removed the now-unused `HEADER_ELEVATION` constant, `animateDpAsState`/`Icon`/`size`
   imports; added `animateColorAsState`/`EmptyState` imports.

Verified via `./gradlew :feature:feature-reminder:compileDebugKotlin :app:compileProDebugKotlin
:feature:feature-reminder:testDebugUnitTest` (all clean/passing) and detekt, checked against unmodified HEAD
via stash-and-rerun on the 4 edited files: `SelectApplicationScreen.kt` and `TodoEditScreen.kt` identical to
baseline (same-line-count content swaps). `PreviewReminderScreen.kt` identical modulo the expected +4-line
shift from the back-button's `if`/`else` reformat. `RemindersArchiveScreen.kt` came back *lower* than
baseline (931 vs. 935 module-wide, isolated to 4 fewer findings on this file) — the old
`ArchiveEmptyState(modifier = Modifier\n  .fillMaxSize()\n  .weight(1f))` call had pre-existing
`Wrapping`/`ArgumentListWrapping` debt that the `EmptyState(...)` replacement's cleaner formatting
incidentally cleared. Zero new findings anywhere.

**Not fixed, left as-is**: everything else the doc already listed as explicitly deferred (orientation-based
layout splits on the two alarm screens, `SelectableOptionRow`'s `FontWeight` in Notes, `ManagePresetsScreen.kt`'s
empty state in Settings) remains genuinely open and out of scope for this group.

**Suggested next step**: continue the same re-verification approach on the next screen group in the
inventory — Notes/Birthdays (§8, §18, §24-§27, plus the shared §58 sweep already partially covers it) — since
this group scored much better than Home/Agenda but still wasn't 100% clean, there's no reason to assume any
other "In progress" group is either.

## 73. Notes & Birthdays group — re-verification of §24-§27/§58's claims, plus a stale deferred item resolved

Re-verified every "landed" claim across §24 (`NotesScreen.kt`), §25 (`NoteEditFloatingBar.kt`), §26
(`PreviewNoteReminderRow.kt`), §27 (`BirthdaysScreen.kt`/`EditBirthdayScreen.kt`/`PreviewBirthdayScreen.kt`),
and §58's Notes/Birthdays-relevant claims (`RemindersArchiveScreen.kt`'s `ArchiveEmptyState` — already
confirmed fixed in §72 — and `ReminderActionScreen.kt`'s `TodoItemRow`), against current source. Read all six
Notes/Birthdays files fresh.

**Best result yet for §24-§27/§58's own claims**: every claim made *by those sections themselves* held up
exactly as documented — back buttons, `TopAppbarColor` token swaps, `onSurfaceVariant` alpha-blend fixes, the
bare `Icons.Default.FilterList` swap, and every motion fix (`NoteEditFloatingBar.kt`'s elevation/spring/tween
replacements, `PreviewNoteReminderRow.kt`'s stagger motion, `PreviewBirthdayScreen.kt`'s
`AnimatedDetailRow`/`AnimatedAvatar` motion) all matched the doc precisely. `ReminderActionScreen.kt`'s
`TodoItemRow` also confirmed already on `onSurfaceVariant`, no `onSurface.copy(alpha` left in the file.
`NotesScreen.kt:568`'s state-driven `SelectableOptionRow` `FontWeight` — the one item §24 itself deliberately
deferred — was confirmed still genuinely untouched, exactly as the doc says. The one miss found in this pass
came from a *different*, later section (§64) overclaiming about this group's files — see below.

**One false "landed" claim found — §64 didn't actually touch this file**: §64 (`Scroll-shadow-to-color-fill
migration`) explicitly lists `BirthdaysScreen.kt` alongside `AgendaScreen.kt`/`RemindersArchiveScreen.kt` as one
of "3 identical-shape screens" fixed in that pass. Current source before this section's edit still had the
original `animateDpAsState`-driven `HEADER_ELEVATION`/`Surface(shadowElevation = ...)` — byte-for-byte the
pre-§64 pattern. So §64's claim for this specific file was false, the same doc-vs-reality gap this whole
re-verification series keeps finding (§70/§71/§72), just discovered from the opposite direction this time — a
later section (§64) overclaiming a fix for a file an *earlier* section (§27) had correctly logged as
deliberately deferred, and nothing after §64 ever circled back to check whether that particular file actually
got touched. §27's own deferral reasoning ("same open product question as `RemindersArchiveScreen.kt`'s
identical case") is itself now moot regardless: §72 resolved that "identical case" using the simpler,
already-established shadow→color-fill pattern, which doesn't touch scroll behavior or `nestedScroll` at all, so
it never actually collides with the harder UX question §27 was avoiding in the first place. Applied §64's
documented fix for real this time: `animateDpAsState`/`Surface(shadowElevation = ...)` →
`animateColorAsState`/`Surface(color = headerContainerColor)` (`background` unscrolled → `surfaceContainer`
scrolled), removed the now-unused `HEADER_ELEVATION` constant and `animateDpAsState` import, added
`animateColorAsState`. Also added the missing `titleContentColor = MaterialTheme.colorScheme.onBackground` to
`BirthdaysTopBar`'s `TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)` — the container stays
transparent deliberately (the wrapping `Surface` now carries the scroll-tint, same reasoning as `AgendaTopBar`
in §71) but the paired title-color half of the token was missing, same gap already fixed for the group's other
three screens in §24/§27.

Verified via `./gradlew :feature:feature-birthday:compileDebugKotlin :app:compileProDebugKotlin` (clean) and
`:feature:feature-birthday:testDebugUnitTest` (passing). Detekt reported 91 weighted issues both with and
without this file's changes (stash-compare) — the two new-looking `ArgumentListWrapping`/`Wrapping` hits on the
already-badly-wrapped `BirthdaysEmptyState(modifier = Modifier\n  .fillMaxSize()\n  .weight(1f))` call confirmed
via `git show HEAD` to be byte-identical pre-existing code, just re-surfaced at shifted line numbers by the
`+6`-line net change earlier in the file (the same stateful-`Indentation`-pass behavior observed in §71/§72) —
zero new findings.

This closes every remaining item from §8's Notes/Birthdays audit except the one genuinely-judgment-requiring
deferral: `NotesScreen.kt`'s `SelectableOptionRow` `FontWeight` swap (state-driven "context" trigger, correctly
left for a full type-token pass per §24's own reasoning, still valid).

**Suggested next step**: continue the same re-verification approach on the remaining "In progress" groups —
Groups/Tags/Places (§9, §29-§33, §66), Calendar/Google Tasks (§10, §34-§39, §67-§68), Workflow/Routines (§7,
§40-§43, §69), Settings (§11-§12, §59-§63), or Backup/Insights/Onboarding/Widget Config (§13, §44-§47, §55-§57,
§65) — none of which have had a dedicated post-hoc re-verification pass yet, so the same doc-vs-reality risk
flagged repeatedly in this series (§70-§73) remains unchecked for all of them.

## 74. Groups/Tags/Places group — re-verification of §29-§33/§66's claims

Re-verified every "landed" claim across §29 (back/save-button content descriptions), §30 (empty states onto
shared `EmptyState.kt`), §31 (`TopAppbarColor` token), §32 (list-row `titleMedium` unification), §33
(`DrawableCatalog`/`AppIcons` cleanup), and §66 (fresh re-audit + `modifier`-order fixes), against current
source. Read all 14 relevant files fresh: `GroupsScreen.kt`, `GroupListItem.kt`, `GroupDetailsScreen.kt`,
`GroupReminderRow.kt`, `EditGroupScreen.kt` (`feature-group`); `TagsScreen.kt` (incl. its private
`TagListItem`), `TagEditScreen.kt`, `TagDetailsScreen.kt`, `TagDetailRows.kt` (`feature-tags`);
`PlacesScreen.kt`, `PlaceListItemCard.kt`, `EditPlaceScreen.kt` (`feature-places`); `ColorPickerCard.kt`,
`EmptyState.kt` (`ui-common`).

**First group in this re-verification series with zero gaps found, on either side of the ledger** — no false
"landed" claims (unlike Home/Agenda/Reminders/Birthdays' §64 miss) and no stray unclaimed defects either
(unlike §73's fresh `BirthdaysScreen.kt` finding). Every specific claim held up exactly:

- All 8 screens' back/close/save buttons use real `stringResource` content descriptions, including
  `TagEditScreen.kt`'s icon-only save button (`AppIcons.Fluent.Checkmark` + `stringResource(...save)`,
  component choice deliberately left as `MenuIconButton` rather than a self-labeling `MenuTextButton`, exactly
  as §29 scoped it — only the missing description was ever the actual defect).
- All 8 `TopAppBar`s use `colors = TopAppbarColor`.
- `GroupsEmptyState`/`TagsEmptyState`/`PlacesEmptyState`/`TagDetailsEmptyState` are gone; all four call sites
  now go through shared `EmptyState.kt`, which itself uses `onSurfaceVariant` (not the alpha-blend the four
  duplicates originally had) — confirming §30's "fixed the shared component first" claim.
- `GroupListItem`/`TagListItem`/`PlaceListItemCard` all use `titleMedium` for their primary label.
- `AppIcons`/`DrawableCatalog` used at every call site checked — no bare `painterResource(R.drawable.ic_fluent_*)`
  left in any of these 14 files.
- `modifier: Modifier = Modifier` is the first parameter everywhere, including the 7 list/dispatcher
  composables §66 specifically fixed (`GroupsScreen`, `TagsScreen`, `PlacesScreen`, `GroupListItem`,
  `TagListItem`, `PlaceListItemCard`, `GroupReminderRow`, `TagDetailItemRow`).
- Every item §9/§66 deliberately left open is still open, exactly as scoped: `GroupListItem.kt`'s
  `DefaultChip` still uses a literal `RoundedCornerShape(8.dp)` (token-hygiene note, never claimed fixed);
  `GroupListItem`'s plain default container color vs. `TagListItem`'s explicit `surfaceContainer` still
  diverge (§9's cross-cutting #5, explicitly lower-priority); `EditPlaceScreen.kt` still places delete behind
  an overflow menu vs. `EditGroupScreen.kt`/`TagEditScreen.kt`'s direct app-bar icon (also explicitly
  lower-priority); `EditGroupScreen.kt`'s `DelayMinutes` dialog is still a hand-rolled `AlertDialog` with a
  conditional `Switch`-gated `Slider` section, not `SeekValueDialog` (§66's own "doesn't fit the shared
  component's shape without growing its API for one caller" reasoning, still valid); `ColorPickerCard.kt`
  still has required params before `modifier` (§66's own "wider, larger-scope gap" note, correctly left
  alone).

No code changes were needed in this pass — nothing to verify via compile/detekt/tests since nothing was
edited.

This closes out Groups/Tags/Places' re-verification with a clean bill of health: every §29-§33/§66 claim is
real, and no drift or oversights surfaced.

**Suggested next step**: continue the same re-verification approach on the remaining "In progress" groups —
Calendar/Google Tasks (§10, §34-§39, §67-§68), Workflow/Routines (§7, §40-§43, §69), Settings (§11-§12,
§59-§63), or Backup/Insights/Onboarding/Widget Config (§13, §44-§47, §55-§57, §65) — none of which have had a
dedicated post-hoc re-verification pass yet.

## 75. Calendar/Google Tasks group — re-verification of §34-§39/§67-§68's claims, plus 3 missed `modifier`-order gaps

Re-verified every "landed" claim across §34 (back-button descriptions), §35 (`TopAppbarColor`), §36
(deprecated baseline FAB), §37 (`TimelinePager.kt`'s off-scale corner radius), §38
(`detailScreenContentWidth()`), §39 (alpha-blend de-emphasis), and §67-§68 (fresh re-audit,
`modifier`-order, `FontWeight`→`Emphasized`, `DrawableCatalog`/`AppIcons` cleanup) against current source.
Read all 10 files fresh: `CalendarScreen.kt`, `TimelinePager.kt`, `TimelineScreen.kt`,
`GoogleCalendarEventPreviewScreen.kt`, `CalendarModeToggleButton.kt` (`feature-calendar`); `GoogleTasksScreen.kt`,
`TaskListScreen.kt`, `PreviewGoogleTaskScreen.kt`, `EditGoogleTaskScreen.kt`, `EditGoogleTaskListScreen.kt`
(`feature-googletask`).

**Every specific claim held up**: real back-button descriptions on all 8 screens, `TopAppbarColor`
everywhere (including the `Color.Transparent`→token swap on the 2 Calendar screens),
`SmallExtendedFloatingActionButton` on all 3 former baseline-FAB call sites, `TimelinePager.kt`'s
`HolidayChip`/`TimelineEventBlock` on `MaterialTheme.shapes.extraSmall` (not the old off-scale 6dp literal),
`detailScreenContentWidth()` on the 4 detail/preview screens, `GoogleTasksEmptyState` gone (both
`GoogleTasksScreen.kt` and `TaskListScreen.kt` call the shared `EmptyState.kt` directly),
`CalendarModeToggleButton.kt`'s selected-row label on `titleMediumEmphasized`, and the full 19-site
`DrawableCatalog`/`AppIcons` migration (no bare `painterResource(R.drawable.ic_fluent_*)`/`iconRes =
R.drawable.*` left anywhere checked, aside from `AddEventRow`/`DetailRow`'s deliberately-generic `icon: Int`
parameters, exactly as §68 documented).

**3 `modifier`-parameter-order gaps found that §67's own sweep missed**, despite §67 explicitly enumerating
"12 composables across CalendarScreen.kt, CalendarModeToggleButton.kt, TimelineScreen.kt, all 7 composables
in TimelinePager.kt..., GoogleTasksScreen.kt (plus its private TaskListTile/NotLoggedInContent), and
TaskListScreen.kt" as fixed. §67's own count and file list check out for everywhere it actually reached
(confirmed all 7 `TimelinePager.kt` composables, `GoogleTasksScreen.kt`'s 3, `CalendarModeToggleButton.kt`,
`TimelineScreen.kt`, `TaskListScreen.kt`, and `CalendarScreen.kt`'s own top-level composable are all
modifier-first) — but three private helper composables the sweep apparently never reached still had
`modifier` last or mid-list:

- **`CalendarScreen.kt`**: `WeekdayHeaderRow`, `MonthPage`, and `MonthDayCell` (all three take an incoming
  `modifier` from their call sites — `WeekdayHeaderRow`'s app-bar-row padding, `MonthPage`'s
  `Modifier.fillMaxSize()` from the pager, `MonthDayCell`'s `Modifier.weight(1f).fillMaxSize()` from the grid
  row) all had `modifier: Modifier = Modifier` as their last parameter.
- **`PreviewGoogleTaskScreen.kt`**: `DetailRow` had `modifier` third, after the two required `icon`/`text`
  params — not last, but still not first.
- **`EditGoogleTaskScreen.kt`**: `FieldCard` had `modifier` last, after `label`/`value`/`enabled`/`onClick`.

None of these three files were named in §67's per-file breakdown for anything beyond what's confirmed above
(`PreviewGoogleTaskScreen.kt`/`EditGoogleTaskScreen.kt` weren't named in §67's modifier-order list at all —
their top-level screen composables already had it right from §38's earlier `detailScreenContentWidth()` work,
so the pass apparently never descended into their private helpers). Fixed all 5 the same way as
§65/§66/§67 — confirmed every call site (`WeekdayHeaderRow`/`MonthPage`/`MonthDayCell` from
`CalendarScreen`'s own body, `DetailRow` from `PreviewGoogleTaskScreen`'s 7 call sites, `FieldCard` from
`EditGoogleTaskScreen`'s 3 call sites) already uses named arguments before reordering — pure parameter
reorder, zero behavior change.

Verified via `./gradlew :feature:feature-calendar:compileDebugKotlin :feature:feature-googletask:compileDebugKotlin
:app:compileProDebugKotlin` (all clean) and
`:feature:feature-calendar:testDebugUnitTest :feature:feature-googletask:testDebugUnitTest` (passing), plus
detekt on both modules checked against unmodified HEAD via stash-and-rerun: identical weighted-issue counts
before and after in both modules (`feature-calendar`: 116, `feature-googletask`: 6 — the latter matching
§67/§68's own documented baseline exactly) — confirming the 5 parameter reorders introduced zero new
findings.

This closes out Calendar/Google Tasks' re-verification: every §34-§39/§67-§68 claim is real, and the one gap
found (a mechanical sweep that reached 8 of 10 files' private composables but not all of 3) is now closed too.

**Suggested next step**: continue the same re-verification approach on the remaining "In progress" groups —
Workflow/Routines (§7, §40-§43, §69), Settings (§11-§12, §59-§63), or Backup/Insights/Onboarding/Widget
Config (§13, §44-§47, §55-§57, §65) — none of which have had a dedicated post-hoc re-verification pass yet.

## 76. Workflow/Routines group — re-verification of §7/§40-§43/§69's claims

Re-verified every "landed" claim across §40 (back-button descriptions), §41 (`RoutinePreviewScreen.kt`'s
deprecated FAB), §42 (`RoutinesListScreen.kt`'s `EmptyState` migration), §43 (`TopAppbarColor`), and §69
(fresh re-audit, `modifier`-order, `FontWeight`→`Emphasized`, drop-shadow, touch-target, motion-spec fixes)
against current source. Read all 10 files fresh: `WorkflowGalleryScreen.kt`,
`WorkflowRulesForGroupScreen.kt`, `WorkflowRulesForReminderScreen.kt`,
`builder/WorkflowRuleBuilderScreen.kt`, `WorkflowRuleRow.kt`, `WorkflowTemplateCard.kt`
(`feature-workflow`); `RoutinesListScreen.kt`, `RoutineEditScreen.kt`, `RoutinePreviewScreen.kt`,
`RoutineExecutionScreen.kt` (`feature-routine`).

**Every claim held up, on both sides of the ledger — the third clean re-verification in this series** (after
Groups/Tags/Places §74; Notes/Birthdays §73 needed one small fix but had zero false claims):

- All 8 screens' back buttons (or close/back split) use real `stringResource` content descriptions.
- All 8 `TopAppBar`s use `colors = TopAppbarColor` — including `RoutineEditScreen.kt`/`RoutineExecutionScreen.kt`,
  which already had it correct before §43 and needed no change.
- `RoutinePreviewScreen.kt` uses `SmallExtendedFloatingActionButton`, not the deprecated baseline variant.
- `RoutinesEmptyState` is gone; `RoutinesListScreen.kt` calls the shared `EmptyState.kt` directly with
  `AppIcons.Builder.Timer`.
- `modifier: Modifier = Modifier` is first everywhere it should be: all 6 `feature-workflow` composables
  (confirmed already correct pre-§69, exactly as that section claimed — no fix was ever needed there), and
  in `feature-routine`, all 4 public screens plus the 6 private composables §69 named
  (`RecurrenceOptionPicker`, `WeekdaySelector`, `DayOfMonthPicker`, `RoutineStepRow` in
  `RoutineEditScreen.kt`; `RunningContent`, `FinishedContent` in `RoutineExecutionScreen.kt`).
- `RoutineExecutionScreen.kt`'s 4 `FontWeight.Bold` overrides are gone, replaced with
  `titleMediumEmphasized`/`labelLargeEmphasized`/`headlineSmallEmphasized` (×2) — no `FontWeight` import
  left in the file.
- `RoutineExecutionScreen.kt`'s bottom bar is `Surface(color = MaterialTheme.colorScheme.surfaceContainer)`
  — no `shadowElevation` parameter at all, confirming the drop-shadow-to-color-fill swap.
- `RoutinePreviewScreen.kt`'s check-toggle `IconButton` is `Modifier.size(48.dp)` (not 40dp), and its 8
  `AnimatedVisibility` scale/fade calls all use `MaterialTheme.motionScheme.defaultSpatialSpec()`/
  `.defaultEffectsSpec()` — no `tween()` or `CHECK_ANIMATION_MS` constant left.
- Every item §7 deliberately left open as low-severity/non-defect is still open, exactly as scoped:
  `RoutineEditScreen.kt`'s "move up" control still rotates a chevron-down icon 180° rather than using a
  distinct icon; `WorkflowTemplateCard.kt`'s description text still uses `titleSmall`;
  `RoutinePreviewScreen.kt`'s pre-existing unused `androidx.compose.foundation.background` import (explicitly
  left alone by §43 as unrelated debt) is still there, untouched.

No code changes were needed in this pass — nothing to verify via compile/detekt/tests since nothing was
edited.

This closes out Workflow/Routines' re-verification with a clean bill of health, matching Groups/Tags/Places'
result: every §7/§40-§43/§69 claim is real, and no drift or oversights surfaced.

**Suggested next step**: continue the same re-verification approach on the two remaining "In progress"
groups — Settings (§11-§12, §59-§63) or Backup/Insights/Onboarding/Widget Config (§13, §44-§47, §55-§57,
§65) — neither of which has had a dedicated post-hoc re-verification pass yet. Once both are done, every
screen group in the inventory will have gone through this re-verification pass at least once.

## 77. Settings group — re-verification of §11-§12/§14-§15/§59-§63's claims

Re-verified every "landed" claim across §14 (shared-scaffold back-button/`TopAppbarColor` fix), §15
(gradient-hero header/card dedup), §59 (`ManagePresetsScreen.kt` empty state), §60
(`HolidayCountryScreen.kt` alpha-blend + `GeneralSettingsScreen`/`SingleChoiceDialog` dedup), §61 (shared
`SeekValueDialog` consolidation), §62 (`HeaderItemsSettingsScreen.kt` drag-handle a11y +
`ProVersionScreen.kt` emphasized type), and §63 (`OtherSettingsScreen.kt` icon fix) against current source —
the largest group re-verified in this series (29 screens across §11/§12). Read every file with a specific
"landed" claim: `SettingsScaffold.kt`, `HolidayCountryScreen.kt`, `GradientScreenHeader.kt`,
`GradientHeroCard.kt`, `CloudServicesScreen.kt`, `WhatsNewScreen.kt`, `ProVersionScreen.kt`,
`ManagePresetsScreen.kt`, `GeneralSettingsScreen.kt`, `SingleChoiceDialog.kt`, `SeekValueDialog.kt`,
`RemindersSettingsScreen.kt`, `BirthdaySettingsScreen.kt`, `NoteSettingsScreen.kt`,
`LocationSettingsScreen.kt`, `HeaderItemsSettingsScreen.kt`, `OtherSettingsScreen.kt` — 17 files with a
specific claim to check, spanning `feature-settings`, `feature-reminder`, `feature-birthday`, and
`ui-common`.

**Every claim held up — the fourth clean re-verification in this series** (after Groups/Tags/Places §74,
Workflow/Routines §76; Notes/Birthdays §73 needed one small fix but had zero false claims):

- `SettingsScaffold.kt` passes a real `navigationContentDescription` (defaulting to `cd_back`, with
  `settingsNavigationIcon`/`settingsNavigationContentDescription` correctly pairing the close/back icon and
  its description for the same inputs) and uses `colors = TopAppbarColor` — the single fix covering ~24 of
  this group's screens automatically.
- `HolidayCountryScreen.kt` now renders through `SettingsScaffold` entirely (no hand-rolled `TopAppBar` left)
  and its "no results" text uses `onSurfaceVariant`, not an alpha blend.
- `GradientScreenHeader.kt`/`GradientHeroCard.kt` exist as shared `ui-common` composables (no 40dp
  `IconButton` override — the default 48dp footprint is preserved; `AppShapes.largeIncreased` instead of a
  bare `RoundedCornerShape(20.dp)` literal) and all three former hand-rollers —
  `CloudServicesScreen.kt`/`WhatsNewScreen.kt`/`ProVersionScreen.kt` — call them instead of duplicating the
  header/card inline. `ProVersionScreen.kt` additionally uses `headlineSmallEmphasized`/
  `titleMediumEmphasized` for its hero headline/advantage lines.
- `ManagePresetsScreen.kt` calls the shared `EmptyState.kt` (`AppIcons.Builder.Preset` +
  `recur_no_presets`) — no private `EmptyState` composable left shadowing the shared one.
- `GeneralSettingsScreen.kt` calls the shared `SingleChoiceDialog` directly (no private copy), and the
  shared `SingleChoiceDialog.kt` itself carries the ported `heightIn(max = 400.dp).verticalScroll(...)`
  behavior the language picker needs.
- The shared `SeekValueDialog.kt` exists with exactly the parameter union §61 designed (`description`,
  `valueTextStyle`, `steps`, `hapticFeedbackEnabled`, `confirmText` all optional with the documented
  defaults), and all 5 former call sites use it correctly: `RemindersSettingsScreen.kt`/
  `LocationSettingsScreen.kt`'s Radius dialog (required params only), `NoteSettingsScreen.kt`
  (`hapticFeedbackEnabled`), `BirthdaySettingsScreen.kt`'s two dialogs (`steps = 4`, `confirmText =
  ...save`), and `LocationSettingsScreen.kt`'s Tracker dialog (`steps = 28`, `description`,
  `valueTextStyle = titleLarge`) — no inline `AlertDialog`/`Slider` block left in any of the four files, and
  `RemindersSettingsScreen.kt`'s `dndValueColor` still correctly uses the shared `DisabledAlpha` constant
  rather than a magic-number alpha.
- `HeaderItemsSettingsScreen.kt`'s drag handle is a 48dp `Box` (`DRAG_HANDLE_TOUCH_SIZE`) with the 20dp icon
  centered inside, and `ConfigurableHeaderItemRow` carries `onMoveUp`/`onMoveDown`-driven
  `CustomAccessibilityAction`s using the existing `cd_move_item_up`/`cd_move_item_down` strings, formatted
  as multi-line action bodies (not the semicolon-joined shape the first detekt pass caught).
- `OtherSettingsScreen.kt`'s "Permissions" and "Allow Permission" rows use `AppIcons.Fluent.LockShield`/
  `AppIcons.Fluent.Lock` respectively — no longer the two icon-less outliers among 12 sibling rows.

**Grep-verified the ~20 screens that only ever inherit the `SettingsScaffold`/`GradientScreenHeader` fixes
automatically** (never read individually, since their claim is purely mechanical — "uses the shared
component, therefore gets the fix") rather than assuming: `grep -rn "contentDescription = null"` across all
of `feature-settings` returned 9 hits, every one confirmed by context to be a legitimate decorative icon
next to visible text (a banner icon, a battery-optimization card icon, a map-style preview thumbnail, a
selected-country checkmark, the header-item row icons) — none a back/navigation button. `grep -rn
"TopAppBarDefaults.topAppBarColors"` across `feature-settings` and `feature-reminder`'s `settings` package
returned zero hits — no screen anywhere in the group still hand-rolls the bypassed color call.

No code changes were needed in this pass — nothing to verify via compile/detekt/tests since nothing was
edited.

This closes out Settings' re-verification with a clean bill of health, the largest group in this series to
come back with zero gaps.

**Suggested next step**: one group remains for this re-verification pass — Backup/Insights/Onboarding/Widget
Config (§13, §44-§47, §55-§57, §65). Once it's done, every screen group in the inventory will have gone
through this re-verification pass at least once.

## 78. Backup/Insights/Onboarding/Widget Configuration group — re-verification of §13/§16/§44-§47/§55-§57/§65's claims, plus 12 newly-found `modifier`-order gaps

Re-verified every "landed" claim across §16 (`ColorSlider` accessibility), §44 (back-button descriptions),
§45 (`TopAppbarColor`), §46 (`InsightsScreen.kt` alpha-blend), §47 (`DrawableCatalog` cleanup), §55
(Onboarding/Splash audit — including adding the two missing screens to the inventory), §56
(`OnboardingScreen.kt` alpha-blend), §57 (`BottomNavSplashScreen.kt` motion-scheme), and §65 (Widget
Configuration fresh re-audit) against current source — the last group in this re-verification series. Read
every file with a specific claim: `LocalBackupScreen.kt`, `InsightsScreen.kt`, `PinLoginScreen.kt`,
`PinInput.kt`, `WidgetConfigScaffold.kt`, `ColorSlider.kt`, `OnboardingScreen.kt`,
`BottomNavSplashScreen.kt`, and all 7 `*WidgetConfigScreen.kt` screens — 18 files across 6 modules
(`extensions:localbackup`, `feature-insights`, `feature-onboarding`, `extensions:appwidgets`, `ui-common`,
`app`).

**Every specific claim held up**: real back-button descriptions on `LocalBackupScreen.kt`/`InsightsScreen.kt`
(`PinLoginScreen.kt` already correct, as claimed); `TopAppbarColor` on both standalone screens plus
`WidgetConfigScaffold.kt`; `InsightsScreen.kt`'s `StreakCard`/`RoutineInsightCard` on `onSurfaceVariant` and
its empty state migrated onto shared `EmptyState.kt`; `WidgetConfigScaffold.kt`/`PinLoginScreen.kt`/
`PinInput.kt` all routed through `AppIcons.Fluent.Dismiss`/`.Fingerprint`/`.TextAsterisk`; `ColorSlider.kt`
carries full semantics (`contentDescription`, `progressBarRangeInfo`, `setProgress`) and every one of its 14
call sites (7 widget-config screens + `ColorPickerCard.kt`/`ColorPickerDialog.kt`/`MapPickerCards.kt`/4
sites in `NoteEditPanels.kt`) passes a real or sensibly-defaulted description; every widget-config screen
sizes `ColorSlider` at `.height(48.dp)`, not the old 36dp/40dp; `EventsWidgetConfigScreen.kt`'s text-size
picker uses the shared `SeekValueDialog` (`steps = 12`, `hapticFeedbackEnabled`), not a hand-rolled
`AlertDialog`; `OnboardingScreen.kt`'s inactive-dot indicator uses `outlineVariant`, and its translucent
icon-chip alpha blend (a deliberate frosted-glass-over-gradient effect, not the de-emphasis anti-pattern) is
still correctly left alone; `BottomNavSplashScreen.kt`'s name-reveal animation uses the hoisted
`nameEnterTransition` val built from `MaterialTheme.motionScheme.defaultEffectsSpec()`/
`.defaultSpatialSpec()`, matching §57's own documented `HomeScreen.kt`-precedent shape exactly. The two
screens §55 found missing from the inventory (`OnboardingScreen.kt`, `BottomNavSplashScreen.kt`) are both
still present and correctly described.

**12 unclaimed `modifier`-parameter-order violations found and fixed** — this group never received a
dedicated modifier-order sweep the way Groups/Tags/Places (§66), Calendar/Google Tasks (§67), and
Workflow/Routines (§69) each did; only Widget Configuration's own 7 screens (+ their private
`*WidgetMockPreview` composables) got one, in §65. Everything else in the group — the shared components
`ColorSlider`/`WidgetConfigScaffold`/`PinInput`/`PinLoginScreen`, and the standalone screens
`LocalBackupScreen`/`InsightsScreen`/`OnboardingScreen` plus their private sub-composables — was never
checked. Confirmed every call site uses named arguments before reordering any signature (`grep`-verified for
`ColorSlider`'s and `WidgetConfigScaffold`'s call sites specifically, since those are the two shared
components with 14 and 7 call sites respectively spanning multiple modules — every one already named its
arguments):

- **`ColorSlider.kt`** (`ui-common`) — `modifier` was 4th, after 3 required params. This is the single
  highest-reach fix in this batch: the component backs all 7 widget-config screens plus `ColorPickerCard.kt`
  (Groups/Tags/Places, `RoutineColorPicker.kt`), `ColorPickerDialog.kt`, `MapPickerCards.kt`, and 4 sites in
  `NoteEditPanels.kt` — 14 call sites across 6+ feature modules, all confirmed compiling clean afterward.
- **`WidgetConfigScaffold.kt`** (`extensions:appwidgets`) — `modifier` was 4th, after `title`/`onBackClick`/
  `onSaveClick`.
- **`PinLoginScreen.kt`**/**`PinInput.kt`** (`ui-common`) — both had `modifier` last.
- **`LocalBackupScreen.kt`** (`extensions:localbackup`) — `modifier` was last.
- **`InsightsScreen.kt`** (`feature-insights`) — the top-level screen plus its three private card
  composables (`WeeklyTrendCard`, `StreakCard`, `RoutineInsightCard`) all had `modifier` last.
- **`OnboardingScreen.kt`** (`feature-onboarding`) — the top-level screen plus four private composables
  (`OnboardingCapabilityCaption`, `OnboardingGetStartedPage`, `OnboardingPageContent`,
  `OnboardingPageIndicator`) had `modifier` last; `OnboardingWelcomePage`/`OnboardingCapabilitiesPage`
  already had it first and needed no change.

Deliberately left `ColorPickerCard.kt` untouched despite it sharing the identical "modifier after required
params" shape: §66 already found and explicitly declined to fix this exact gap during the Groups/Tags/Places
re-audit, reasoning it (along with `SingleChoiceDialog`/`MultiChoiceDialog`/`SeekValueDialog`, which don't
expose `modifier` at all) was a "wider instance of the same CLAUDE.md convention gap" than that pass's own
narrow scope — and `ColorPickerCard.kt` belongs to a different, already-closed screen group (Groups/Tags/
Places, re-confirmed clean in §74), not this one. Respecting that prior, reasoned deferral rather than
silently overriding it.

Verified via `./gradlew :extensions:appwidgets:compileDebugKotlin :ui:ui-common:compileDebugKotlin
:extensions:localbackup:compileDebugKotlin :feature:feature-insights:compileDebugKotlin
:feature:feature-onboarding:compileDebugKotlin :app:compileProDebugKotlin` (all clean) — this also
transitively compiled every downstream consumer of the two widely-shared components (`ui-map`, `ui-routine`,
`feature-note`, `feature-settings`, `feature-birthday`, `feature-workflow`, `feature-reminder`,
`feature-googletask`, `feature-places`), confirming none of `ColorSlider`'s 14 call sites broke. Ran
`testDebugUnitTest` on all 4 non-`ui-common` touched modules (passing; `feature-onboarding` has no test
source). Detekt via stash-and-rerun on `extensions:appwidgets`/`extensions:localbackup`/
`feature-insights`/`feature-onboarding`: identical finding counts before and after in all four (4/17/0/1) —
zero new findings. `ui:ui-common:detekt` reproduces the same pre-existing `PermissionRequester.kt` crash
already documented in §47 (confirmed unrelated — this pass never touches that file); correctness there rests
on the compile-clean result plus manual review, the same limitation §47 already flagged as an open tooling
gap.

This closes out the re-verification series: every screen group in the inventory has now had this
doc-vs-reality check at least once. Final tally across all eight groups (§70-§78): three fully clean on both
claims and fresh findings (Groups/Tags/Places, Workflow/Routines, Settings), two with a small fix and zero
false claims (Notes/Birthdays, this section), two with a handful of false "landed" claims corrected (Home,
Reminders), one with extensive false claims plus a real accessibility bug (Agenda), and one with a
modifier-order gap the sweep didn't fully reach (Calendar/Google Tasks) — plus, in this final section, the
first case of a *systemic* gap (modifier-order) that no prior pass had ever attempted for this specific
group at all, found only because a full fresh read was in scope rather than spot-checking the existing
claim list.

**Suggested next step**: no group-level re-verification work remains queued. Future sections should pick up
from whatever the user's next request is.

## 79. Groups/Tags/Places — full adoption pass, promoted to "Done"

The user asked to move on from re-verification and start promoting screen groups that already came back
clean to "Done" — the screen-inventory doc's own bar for that status ("fully reflects the `ui-common`
expressive foundation," higher than "no known defects"). Picked Groups/Tags/Places first: it's the smallest
of the three groups that came back with zero gaps in this session's re-verification series (§74), and it had
already been through two independent clean checks (§66's fresh re-audit, §74's re-verification), so it
needed the least additional work to actually clear the higher bar.

Went through the screen-inventory checklist (`m3-expressive-guidelines.md` §10) item by item against all 8
screens plus their shared components (`ColorPickerCard.kt`/`ColorSlider.kt`), rather than re-deriving new
findings from scratch — §66/§74 already did that work twice with nothing left uncovered mechanically, so
this pass's job was checking the *unmechanical* checklist items (emphasized type, hero moments, breakpoint
adaptation, shape/elevation defaults) that a compliance-bug sweep doesn't naturally surface.

**Typography** — the checklist's own phrasing is "emphasized type used (or *deliberately not* used) for
selection/primary-action/unread moments." `GroupListItem`/`TagsScreen`'s `TagListItem` both drive their
`isHighlighted` selected state through color + border only (`primaryContainer` fill, `primary`-colored
1dp border) with no `FontWeight` distinction between selected and unselected title text at all — unlike
`CalendarModeToggleButton.kt`'s selected-row label (§67), there's no existing manual weight override to
swap for the emphasized token here; adding one now would be introducing new visual behavior, not a
token-hygiene fix. Left as-is: this is the checklist's own explicitly-sanctioned "deliberately not used"
branch, matching §9's original audit judgment that this was "worth adopting," never "a compliance gap."

**Shape** — found and fixed the one remaining literal corner-radius value in the group: `GroupListItem.kt`'s
`DefaultChip` used `RoundedCornerShape(8.dp)` instead of `MaterialTheme.shapes.small` (8dp is exactly the
"small" step on the M3 shape scale, confirmed against real `ShapeTokens.kt` back in §37 — this is a pure
token-hygiene swap, zero visual change). §9's original audit named this exact line as "fixable... with zero
visual change" but it was never in any suggested fix order, so it never landed. `RoundedCornerShape` became
fully unused in the file afterward and was removed from imports.

**Layout/breakpoints** — none of the 8 screens add a `detailScreenContentWidth()`-style max-width cap; §9's
own audit explicitly judged this "lower priority... these screens are generally used inside the app's
existing two-pane `renderAsDetailPane` shell (already width-bounded by the pane)... not a confirmed defect,"
and the guidelines checklist itself allows skipping breakpoint logic where it doesn't apply. Left as-is,
consistent with that judgment and with Home/Agenda's own "Done" promotions not having added anything here
either.

**Elevation/Components/States** — re-confirmed no off-scale elevation, no deprecated components, and no
custom-highlight-instead-of-state-layer anywhere in the group (all interaction feedback comes from
`Card`/`MenuIconButton`/`FilterChip`'s built-in ripple/state-layer handling) — matches §9's original
"cleanest baseline of any group audited" finding, still true.

**Deliberately left open, not required for "Done"**: the three individually-legitimate role-choice
divergences §9/§33 already named and judged non-defects — `GroupListItem`'s plain default container color
vs. `TagListItem`'s explicit `surfaceContainer`, `EditPlaceScreen.kt`'s overflow-menu delete placement vs.
`EditGroupScreen.kt`/`TagEditScreen.kt`'s direct app-bar icon, and `TagEditScreen.kt`'s icon-only save button
component choice vs. its siblings' `MenuTextButton`. Also unchanged: `ColorPickerCard.kt`'s `modifier`
position (a wider convention gap §66 explicitly declined to fix, reaffirmed in §78) and
`EditGroupScreen.kt`'s hand-rolled `DelayMinutes` dialog (§66's "doesn't fit `SeekValueDialog`'s shape
without growing its API for one caller," still valid).

Verified via `./gradlew :feature:feature-group:compileDebugKotlin :app:compileProDebugKotlin
:feature:feature-group:testDebugUnitTest` (all clean/passing) and detekt, checked against unmodified HEAD
via stash-and-rerun: identical finding count (8) before and after — zero new findings from the one-line
shape swap.

**Promoted "Groups / Tags / Places" from "In progress" to "Done"** in `docs/m3-expressive-screen-inventory.md`
— all 8 rows (Groups List, Group Details, Group Editor, Tags Manage, Tag Editor, Tag Details, Places List,
Place Editor).

**Suggested next step**: Workflow/Routines or Settings are the other two groups that came back clean in the
re-verification series (§76, §77) and are the next candidates for this same "full adoption pass" treatment —
Workflow/Routines is the smaller of the two (10 files vs. Settings' 29 screens), so a natural next pick.

## 80. Workflow/Routines — full adoption pass, promoted to "Done"

Second group in the "promote clean groups to Done" arc (after Groups/Tags/Places, §79), per the user's
explicit request to continue with this group next. Same starting position: §76's re-verification had already
confirmed every §7/§40-§43/§69 claim held with zero gaps, so this pass's job was the same as §79's — work the
non-mechanical items in the guidelines' §10 screen-audit checklist, and this time also read the supporting
`ui-routine` module components no prior audit of this group had ever examined directly (`RoutineCard.kt`,
`CircularStepTimer.kt`, `RoutineColorPicker.kt`, `RoutineIconPicker.kt`) — every prior pass scoped to
`feature-workflow`/`feature-routine` only, never the shared UI library backing Routines' cards/pickers.

**Typography** — same checklist item as §79 ("emphasized type used, or *deliberately not* used, for
selection/primary-action moments"), same conclusion reached differently: `CircularStepTimer.kt`'s countdown
text (`displaySmall`, confirmed `displaySmallEmphasized` is a real `Typography` property via the actual
`material3-android-1.5.0-alpha27-sources.jar`, same diligence as §37/§69/§61) is arguably this group's
strongest hero-moment candidate — the single largest, most-watched piece of content on the Routine Execution
running screen. Considered and deliberately left as-is: `RoutineExecutionScreen.kt` already has its
one designated hero element from §69 (the step title, `headlineSmallEmphasized`), and per guidelines'
own "1-2 hero moments, don't make everything loud" principle, promoting a second element on the same screen
risks diluting rather than reinforcing that hierarchy — a product call, not a mechanical gap, so left open
rather than forced in.

**Accessibility — new finding, fixed**: `RoutineIconPicker.kt`'s circular trigger bubble (`BUBBLE_SIZE`) was
sized `44.dp` — under the 48×48dp minimum touch target (guidelines §8), never previously flagged since no
audit of this group had read `ui-routine` before. Bumped to `48.dp`, a simple constant change since the
bubble is a single standalone control, not a dense grid (unlike the picker's own `IconOption` grid tiles,
`40.dp` each across a 6-column/33-option layout inside a `DropdownMenu` — reviewed and deliberately left
open: bumping every grid cell to 48dp would meaningfully widen the whole grid, a real layout trade-off
needing design input, the same category of judgment call `TimelinePager.kt`'s sub-48dp event blocks were
left with in the Calendar audit, not a same-shape mechanical fix like the bubble).

**`modifier`-parameter-order — new finding, fixed**: `BuilderListItemCard.kt` (`ui-common`, both overloads)
had `modifier` as its 6th parameter, after 5 required ones — the same convention violation this doc's been
finding and fixing in shared components throughout the "Done"-promotion and re-verification passes (§78's
`ColorSlider.kt`/`WidgetConfigScaffold.kt`/etc.). This component is shared between `WorkflowRuleBuilderScreen.kt`
(this group) and `BuildReminderScreen.kt` (Reminders, already "Done"-adjacent) — confirmed all of both
files' call sites use named arguments (`BuildReminderScreen.kt` passes `modifier = modifier` explicitly at
both its call sites) before reordering, a pure parameter reorder with zero behavior change.

**Re-confirmed clean, no changes needed**: `RoutineCard.kt` (token-driven shape, default elevation, no
manual `FontWeight`, correct `Button` component, badge tint is a legitimate translucent-fill-on-brand-color
use rather than the de-emphasis anti-pattern); `RoutineColorPicker.kt` (thin, correctly-typed wrapper over
the already-spec-correct `ColorPickerCard`); `RoutineIconSet.kt` (already fully `DrawableCatalog`-sourced,
32 cataloged icons, no raw drawable bypass). No breakpoint/max-width work added to any of the 8 screens,
matching §79's identical reasoning: these are simple list/form screens that don't need it, and the
guidelines checklist itself permits skipping breakpoint logic where it doesn't apply.

**Deliberately left open, not required for "Done"**: `RoutineEditScreen.kt`'s indirect "move up" control
(rotating a chevron-down icon 180° instead of using a distinct icon, §7) and `WorkflowTemplateCard.kt`'s
`titleSmall`-for-description type-role mismatch (§7) — both already explicitly logged as low-severity,
non-defect notes, not open work.

Verified via `./gradlew :feature:feature-workflow:compileDebugKotlin :feature:feature-routine:compileDebugKotlin
:ui:ui-routine:compileDebugKotlin :ui:ui-common:compileDebugKotlin :feature:feature-reminder:compileDebugKotlin
:app:compileProDebugKotlin` (all clean — confirms `BuilderListItemCard.kt`'s reorder didn't break either
consumer module) and `testDebugUnitTest` on all touched modules with test source (`feature-workflow`,
`feature-routine`, `feature-reminder`; `ui-routine` has none). `:ui:ui-routine:detekt --rerun` reports one
finding, on `RoutineColorPicker.kt`'s `routineColorSliderTestTag` naming — already documented pre-existing
in §16, a file this pass never touched. `:ui:ui-common:detekt` reproduces the same pre-existing
`PermissionRequester.kt` crash already documented in §47/§78 (confirmed unrelated); correctness for
`BuilderListItemCard.kt` rests on the compile-clean result plus manual review, the same limitation those
sections already flagged as an open tooling gap.

**Promoted "Workflow (automation rules)" and "Routines" from "In progress" to "Done"** in
`docs/m3-expressive-screen-inventory.md` — all 8 rows (Workflow Gallery, Workflow Rules for Group, Workflow
Rules for Reminder, Workflow Rule Builder, Routines List, Routine Editor, Routine Preview, Routine
Execution).

**Suggested next step**: Settings is the last group that came back clean in the re-verification series
(§77) and hasn't had this "full adoption pass" treatment yet — the largest at 29 screens, so likely the
longest of the three, but the same methodology applies directly.

## 81. Settings — full adoption pass, promoted to "Done"

Third and last group in the "promote clean groups to Done" arc, per the user's explicit request. By far the
largest of the three (29 screens across `feature-settings`, plus `RemindersSettingsScreen.kt`/
`ManagePresetsScreen.kt` in `feature-reminder` and `BirthdaySettingsScreen.kt` in `feature-birthday`), and it
surfaced far more than §79/§80 did — this group had never had a dedicated `modifier`-order sweep or a
systematic `DrawableCatalog` re-check the way Groups/Tags/Places (§66/§33) and Calendar/Google Tasks (§67/§68)
each got, so both gaps had accumulated widely and silently across nearly every screen.

**`modifier`-parameter-order — the largest fix in this whole "Done"-promotion arc**: read every screen with
its own `modifier` parameter and reordered it to first wherever it wasn't, after confirming every call site
uses named arguments (verified exhaustively for the two highest-reach shared components specifically, since
a mistake there would ripple furthest):

- **`SettingsScaffold.kt`** — the shared scaffold behind ~24 of this group's 29 screens. Grepped all 26 real
  call sites across `SettingsNavGraph.kt`, `SecurityNavGraph.kt`, `OtherNavGraph.kt`, `LocationNavGraph.kt`,
  `ExportNavGraph.kt`, `HolidayCountryScreen.kt`, and `app`'s `SettingsCrossFeatureEntries.kt` — every one
  uses named arguments (`title =`, `onBackClick =`, `navigationIcon =`, etc.), so this was safe to reorder.
- **20 individual screen composables** — `SettingsHubScreen.kt` (+ its private `SettingsBanner`/
  `SettingsSearchResults`), `BackupSettingsScreen.kt`, `CalendarSettingsScreen.kt`, `MapStyleScreen.kt`,
  `SecuritySettingsScreen.kt`, `AddPinScreen.kt`, `ChangePinScreen.kt`, `DisablePinScreen.kt`,
  `CloudBackupSettingsScreen.kt`, `DigestSettingsScreen.kt`, `TroubleshootingScreen.kt`,
  `GeneralSettingsScreen.kt`, `HeaderItemsSettingsScreen.kt` (+ its private `PinnedHeaderItemRow`),
  `OtherSettingsScreen.kt`, `HolidayCountryScreen.kt` (+ its private `CountryListItem`),
  `NoteSettingsScreen.kt`, `LocationSettingsScreen.kt`, `OtherNavGraph.kt`'s private `SettingsWebView`,
  `RemindersSettingsScreen.kt` (`feature-reminder`), `ManagePresetsScreen.kt` (`feature-reminder`), and
  `BirthdaySettingsScreen.kt` (`feature-birthday`) — each checked individually against its own (usually
  single) call site before reordering.
- **Deliberately not touched**: `SettingsItem.kt`/`SettingsSwitchItem`/`SettingsCheckboxItem`/
  `SettingsSectionHeader` (`ui-common`) — the single most-used shared component in this whole app (26+ files
  across `feature-googletask`, `feature-birthday`, `feature-reminder`, `feature-workflow`, `feature-group`,
  and every `feature-settings` screen). Unlike every other fix in this arc, many call sites pass the row's
  `title` as a single, unnamed positional argument (e.g. `SettingsSectionHeader(stringResource(...))`) —
  reordering `modifier` to first would silently require touching upwards of 100 individual call sites across
  those same 26+ files just to add `title = ` everywhere, a fundamentally different scope than "the
  composable already has a trailing `modifier` param, just move it." Matches §66's own established
  distinction for exactly this shape of gap (`ColorPickerCard.kt`, reaffirmed in §79) — deferred, not fixed.
  `DeveloperScreen.kt`/`ObjectExportScreen.kt` (`feature-settings/debug`) also still have the same gap but
  are explicitly out of scope per the screen-inventory doc's own "debug-only" note — left untouched.

**`DrawableCatalog`/`AppIcons` convention — the second-largest fix, ~35 call sites across 8 files**: this
group had never had a dedicated cleanup pass the way Groups/Tags/Places (§33) or Calendar/Google Tasks (§68)
did. Every icon found was already cataloged (checked `DrawableCatalog.kt` before touching any call site, same
discipline as §33/§47/§68 — zero new catalog entries needed):

- **`SettingsHubScreen.kt`** (8 sites) — every category row (`ic_fluent_system`, `ic_builder_by_monthday`,
  `ic_fluent_clock_alarm`, `ic_fluent_food_cake`, `ic_fluent_lock`, `ic_fluent_note`,
  `ic_fluent_launcher_settings`) plus the Do Not Disturb banner icon (`ic_moon`) — the file's other icon
  (`AppIcons.Fluent.CloudSyncComplete`) was already correct, making the inconsistency visible in the file
  itself.
- **`GeneralSettingsScreen.kt`** (7 sites) and **`RemindersSettingsScreen.kt`** (9 sites) — the latter mixed
  raw `painterResource` and correct `AppIcons` calls side by side throughout the same file.
- **`CalendarSettingsScreen.kt`** (9 sites, one file with zero `AppIcons` usage at all beforehand),
  **`SecuritySettingsScreen.kt`** (4 sites, same), **`CloudBackupSettingsScreen.kt`** (4 sites, same),
  **`BirthdaySettingsScreen.kt`** (7 sites, same), **`LocationSettingsScreen.kt`** (5 real icon sites — its
  two `ic_fluent_place` sites shared one `replace_all`; its map-style-preview thumbnail correctly stayed on
  bare `painterResource`, the same "decorative mock-preview pixel" exception §13 already established for
  Widget Configuration's own previews), **`NoteSettingsScreen.kt`** (2 sites in an otherwise-mixed file),
  and **`TroubleshootingScreen.kt`** (2 sites, one an `Icon`'s `painter =`, one a `SettingsItem`'s `icon =`).

**Motion — `SettingsItem.kt`'s literal `tween()`, flagged by §11 itself but never landed**: the shared
component's search-highlight flash (`animateColorAsState` between `Color.Transparent` and
`tertiaryContainer`) used `tween(durationMillis = if (isFlashing) 150 else 900)` — an internal
implementation detail with zero public-API surface, so unlike the `modifier`-order gap this carried none of
that fix's ripple risk. Replaced with two `MaterialTheme.motionScheme` specs selected by direction: the
150ms attention-grabbing flash-in now resolves `fastEffectsSpec()`, the 900ms settle-back-to-normal now
resolves `defaultEffectsSpec()` — both small-list-row-scale color changes, not partial-screen surfaces, so
"fast"/"default" rather than any slower tier. Both are `Color`-typed `FiniteAnimationSpec`s resolved once
per composition and switched between via the same `if (isFlashing)` condition the original code already
used to pick a duration.

**Re-confirmed clean, nothing to fix**: every §77-verified claim (back buttons, `TopAppbarColor`,
`GradientScreenHeader`/`GradientHeroCard`, `EmptyState`/`SingleChoiceDialog`/`SeekValueDialog` consolidations,
the drag-handle a11y fix, `ProVersionScreen.kt`'s emphasized type, `OtherSettingsScreen.kt`'s icon fix) —
none of this pass's edits touched any of that surface. `NotificationCustomizationHelpScreen.kt` has no
`modifier` parameter at all (already "Done" via §48) and needed no change. `CloudServicesScreen.kt`/
`WhatsNewScreen.kt`/`ProVersionScreen.kt` likewise have no `modifier` parameter (their layout is driven
entirely by `AnimatedGradientBackground`'s own sizing) and were already fully migrated onto
`GradientScreenHeader`/`GradientHeroCard` — no raw drawables, no gaps.

Verified via `./gradlew :feature:feature-settings:compileDebugKotlin :feature:feature-reminder:compileDebugKotlin
:feature:feature-birthday:compileDebugKotlin :ui:ui-common:compileDebugKotlin :app:compileProDebugKotlin`
(all clean — confirms the `SettingsScaffold`/`SettingsItem` reorders and every icon-catalog swap compile
across every consumer module) and `testDebugUnitTest` on all three touched feature modules (all passing).
Detekt via stash-and-rerun on `feature-settings`/`feature-reminder`/`feature-birthday`: identical weighted-
issue counts before and after in all three (352/931/91) — zero new findings across the entire batch.
`:ui:ui-common:detekt` reproduces the same pre-existing `PermissionRequester.kt` crash already documented in
§47/§78/§80 (confirmed unrelated — this pass's `ui-common` edits, `SettingsItem.kt`'s internal motion fix,
never touch that file); correctness there rests on the compile-clean result plus manual review, the same
established limitation.

**Promoted all 29 "Settings" rows from "In progress" to "Done"** in `docs/m3-expressive-screen-inventory.md`
— every row in the Settings section's tables, spanning Hub, General, Backup (Cloud/Local Export/Import),
Reminders, Manage Presets, Calendar, Select Holiday Country, Birthdays, Notes, Location, Map Style, Security,
Add/Change/Disable PIN, Notification Customization Help (already "Done" via §48), Cloud Backup, Other
Settings, Permissions, Open Source Licenses, Privacy Policy, Terms, Gemini Functions, AI Digest, Header
Items, Troubleshooting, Cloud Services, What's New, and Pro Version. `Developer`/`Object Export` remain
explicitly out of scope (debug-only), unchanged.

This closes out the "promote clean groups to Done" arc the user asked for across all three candidates
(Groups/Tags/Places §79, Workflow/Routines §80, Settings §81) — every group that came back fully clean in
the earlier re-verification series (§74/§76/§77) has now also cleared the higher "fully reflects the
`ui-common` expressive foundation" bar.

**Suggested next step**: no group-level promotion work remains queued from this arc. The screen-inventory
doc's remaining "In progress" rows are the four groups that had real gaps corrected during re-verification
rather than a clean pass (Notes/Birthdays, Home/Events already-Done-via-§70/§71 aside, Reminders,
Calendar/Google Tasks, Backup/Insights/Onboarding/Widget Config) — each is a candidate for this same
adoption-pass treatment if the user wants to continue promoting groups to "Done," or a different request
entirely.
