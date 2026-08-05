# M3 Expressive Adoption Plan

Research + gap analysis for adopting Material 3 Expressive, starting with `HomeScreen`
(`ChronologicalHomeScreen`) and `EventsScreen`. This is a planning document, not a change log — no
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

Compose BOM is `2026.06.01` and `androidx.compose.material3:material3` is pinned to **1.4.0**
([`gradle/libs.versions.toml:39,42`](../gradle/libs.versions.toml)) — this is a stable release that carries
the Expressive APIs (`MaterialExpressiveTheme`, emphasized `Typography` fields, `MaterialShapes`,
`MaterialTheme.motionScheme`, `ButtonGroup`, `LoadingIndicator`, floating toolbars, etc., some still gated
behind `@ExperimentalMaterial3ExpressiveApi`). No version bump is needed to start — the gap is entirely in
how `ui-common` and the screens use the library today.

### `ui-common` foundation audit

- **Color** — [`compose/Color.kt`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Color.kt)
  and [`compose/Theme.kt`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Theme.kt)
  already define the *full* M3 role set for light/dark, including the surface container ramp
  (`surfaceContainerLowest` → `surfaceContainerHighest`) and the theme-independent **Fixed** roles
  (`primaryFixed`/`primaryFixedDim`/etc. — explicitly commented as Expressive roles). This part is already
  expressive-ready; the gap is that screens barely touch secondary/tertiary or the fixed roles today (see
  §3).
- **Typography** — [`compose/Type.kt`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/Type.kt)
  is just `internal val AppTypography = Typography()` — the stock baseline scale, no custom sizes, no
  emphasized styles wired up at all. This is the single biggest gap: there is currently no way for a screen
  to opt into an emphasized style even if it wanted to.
- **Shape** — `Theme.kt`'s `MaterialTheme(...)` call never passes a `shapes` parameter, so the app runs on
  default M3 `Shapes()`. There's no `Shape.kt` / shared shape tokens file in `ui-common` at all. Corner
  radii are hardcoded ad hoc per call site instead: `12.dp` for header tiles
  ([`ChronologicalHomeScreen.kt:288`](../app/src/main/java/com/elementary/tasks/home/ChronologicalHomeScreen.kt)),
  `16.dp` default in [`SplitButton.kt`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/SplitButton.kt),
  `28.dp` in [`SearchBar.kt`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/component/SearchBar.kt),
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
- Header navigation tiles ([`ChronologicalHomeScreen.kt:263-328`](../app/src/main/java/com/elementary/tasks/home/ChronologicalHomeScreen.kt))
  are the closest thing to a "hero" element on the screen (colored icon chip + count), but currently use a
  flat `12.dp` `RoundedCornerShape` and only `surfaceContainer`/`secondaryContainer` — no tertiary, no fixed
  roles, no shape variety versus the event cards below them.
  - **Note**: `HeaderNavigationItem.color` ([`HomeScreenState.kt:18-24`](../app/src/main/java/com/elementary/tasks/home/HomeScreenState.kt))
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

### `EventsScreen` audit

- Top bar, search bar, filter chips, and bottom sheet are all stock M3 components used correctly, but with
  no emphasis differentiation: selected `FilterChip`s, the active filter `Badge`, and section headers
  (`titleMedium`) are exactly where the spec calls out emphasized type as the recommended pattern (selected
  chips/menu items, badges) and are not using it.
- `EventsList` renders headers and rows with uniform `8.dp` spacing and no shape distinction between a
  `UiEventReminder` and `UiEventBirthday` row — same gap as Home: type is conveyed only through icon/text,
  not through shape or color-role variety.
- Empty state (`EventsEmptyState`) and the equivalent on Home (`EmptyEventsState`) are near-duplicates that
  belong in `ui-common` as a shared composable regardless of the Expressive work — worth doing opportunistically
  during this migration since both screens will already be touched.

## 3. Proposed `ui-common` foundation work

Do this once, in `ui-common`, before touching either screen — otherwise Home and Events end up with two
divergent hand-rolled interpretations of "expressive."

1. **Typography** — replace `Type.kt`'s bare `Typography()` with an explicit `AppTypography` that defines
   both baseline and emphasized styles (Material's default emphasized values are a reasonable starting
   point; only deviate if a specific screen needs it). Expose an easy way for a composable to reach the
   emphasized variant of whatever baseline style it's already using, rather than requiring call sites to
   hardcode `fontWeight = FontWeight.Bold`/`.Medium` overrides the way `ChronologicalHomeScreen.kt` does
   today (see `TimeSectionRow`, `EventCard`, `HeaderNavigationTile`) — those ad hoc weight bumps are exactly
   what emphasized tokens are meant to replace.
2. **Shape** — add a `Shape.kt` in `ui-common/compose` with a small named scale (e.g. `tile`, `card`,
   `pill`) built from `MaterialShapes`/the corner-radius scale, and pass it into `MaterialTheme`/
   `MaterialExpressiveTheme` in `Theme.kt` instead of leaving `shapes` as the default. Migrate the
   hardcoded `12.dp`/`16.dp`/`28.dp` call sites listed in §2 onto these tokens as they're touched, rather
   than in one big sweep.
3. **Motion** — add a shared `MotionScheme` (or a small set of named spring specs if the full
   `MaterialTheme.motionScheme` API isn't ready to adopt yet) so `BANNER_ANIMATION_DURATION_MS`-style
   per-file constants can be replaced with one source of truth. `SearchBar.kt`'s existing
   `spring(dampingRatio = Spring.DampingRatioMediumBouncy)` is a decent reference point for the "bouncy but
   not silly" feel this app should land on.
4. **Theme entry point** — evaluate switching `AppTheme` in `Theme.kt` from `MaterialTheme(...)` to
   `MaterialExpressiveTheme(...)` (guarded by `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` if that's
   still the gate in 1.4.0) once 1–3 land, since some expressive component defaults key off it.
5. **Shared empty state** — extract one `EmptyState` composable (icon + message, the pattern duplicated in
   `EmptyEventsState` and `EventsEmptyState`) into `ui-common/compose/foundation/component/`.

## 4. Screen-by-screen plan (Home + Events)

Sequence: land the `ui-common` foundation (§3) first behind no visible change, then apply tactics to each
screen incrementally so every step stays reviewable and shippable on its own.

**Home (`ChronologicalHomeScreen`)**
- Greeting → emphasized `headlineMedium` (or `headlineLarge` if it should read as more of a hero moment —
  this screen only really has one candidate for a "hero," so it should probably be this).
- Header navigation tiles → adopt the new shape scale (differentiate from event card shape), decide the
  `HeaderNavigationItem.color` question above, consider fixed-color roles for the icon chip background
  instead of a flat `secondaryContainer` for all tiles.
- Event rows → introduce a shape/color distinction for overdue vs. upcoming vs. birthday, using
  secondary/tertiary containers per tactic #2 rather than relying solely on `onBackground` text everywhere.
- Add button → candidate for the FAB menu component once available, replacing the current
  `MenuIconButton` + `AppDropdownMenu` pair.
- Stagger animations → migrate onto the shared motion scheme from §3.3.

**Events (`EventsScreen`)**
- Section headers, active-filter badge, selected filter chips → swap to emphasized type per the spec's own
  "where emphasized styles can be used" guidance (badges, selected chips) — this is the most direct,
  lowest-risk win on this screen.
- Event list rows → same shape/color differentiation as Home for reminder vs. birthday rows, so the two
  screens read as one system rather than two independent implementations.
- Filter bottom sheet → low priority; already uses `AppModalBottomSheet` + stock chips correctly, revisit
  after the above land.

## 5. Open questions

- Should `MaterialExpressiveTheme` be adopted app-wide in one PR, or should `ui-common` support both and let
  screens opt in gradually? Given `app` is the only module wiring DI/theme today, an app-wide switch is
  probably lower-risk than a dual-theme period, but it means Home + Events would ship alongside whatever
  else renders through `AppTheme` at the same time — worth confirming with whoever owns rollout risk here.
- `HeaderNavigationItem.color` (unused today) needs a decision before the tile redesign, not during it.
- No decision yet on how far to take shape morphing (loading indicators, FAB menu open/close) versus just
  adopting the static shape scale — morphing is the highest-effort, highest-novelty piece of Expressive and
  probably shouldn't block the first pass on Home/Events.
