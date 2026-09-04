# M3 Expressive Adoption Plan

Research + gap analysis for adopting Material 3 Expressive, starting with `HomeScreen`
(`ChronologicalHomeScreen`) and `AgendaScreen`. This is a planning document, not a change log — no
production code was modified while writing it.

Source: [m3.material.io/blog/building-with-m3-expressive](https://m3.material.io/blog/building-with-m3-expressive)
(May 2025), plus the linked Typography and Shape spec pages.

For the full list of app screens and their per-screen migration status, see
[`m3-expressive-screen-inventory.md`](m3-expressive-screen-inventory.md).

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
