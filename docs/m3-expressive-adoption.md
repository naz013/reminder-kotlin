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
