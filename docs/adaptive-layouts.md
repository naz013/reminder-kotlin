# Adaptive Layouts (Tablet, Desktop, Foldable)

Reference for the tablet/desktop/foldable adoption effort. See `docs/architecture.md` for the
general module map - this file only covers the adaptive-layout pieces added on top of it.

## Status

**Phase 0 (done):** foundation only - dependencies, window/fold state, a nav-rail scaffold, the
two-pane wiring hook, and preview tooling all live in `ui-common`.

**Phase 1 (done):** Home adopts the nav rail via `PersistentNavRailSceneDecoratorStrategy`, a
`SceneDecoratorStrategy` registered in `AppNavGraph.kt` (see below) - no manual per-screen
branching.

Planned next: 2-3 List→Preview flows (Notes, Google Tasks, Agenda/Reminders) adopt two-pane
(Phase 2), fold-posture-aware refinements and the remaining screens (Phase 3).

## Breakpoint reference

[`DeviceScreenConfiguration`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/DeviceScreenConfiguration.kt)
classifies the window into six buckets off `androidx.window.core.layout.WindowSizeClass`'s
canonical breakpoints (width 600/840dp, height 480/900dp):

| Bucket | Width | Height |
|---|---|---|
| `MobilePortrait` | < 600 | any (or falls through below) |
| `MobileLandscape` | >= 600 | < 480 |
| `TabletPortrait` | >= 600 | >= 900 |
| `TabletLandscape` | >= 840 | 480-899 |
| `DesktopSmall` | 600-839 | 480-899 |
| `DesktopNormal` | >= 840 | >= 900 |

Branch order matters in `fromWindowSizeClass` - `DesktopSmall`'s bounds are a superset of the two
Tablet buckets', so those must be checked first. Covered by
`DeviceScreenConfigurationTest`.

Read it via `deviceScreenConfiguration()` (or the `isTabletScreen()`/`isDesktopScreen()`/etc.
booleans) in any composable. `dynamicParameter { ... }` / `DynamicScreen { ... }` branch a value or
a whole composable per bucket - reach for these first before hand-rolling a `when` on
`deviceScreenConfiguration()`.

For fold posture (independent of size - a device can be `TabletLandscape` sized and still have a
hinge splitting it), use `rememberAppWindowState()` /
[`FoldPosture`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/AppWindowState.kt)
(`Flat` / `Book` / `TableTop`). Nothing in the app reads this yet - it's here for Phase 3.

## Deciding what a screen needs

1. **Nothing** - most edit/preview/settings-detail screens are fine staying single-column; don't
   add adaptive machinery to a screen just because it's now possible.
2. **Top-level navigation chrome** (one per app, not per screen) →
   [`AppNavigationScaffold`](../ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/navigation/AppNavigationScaffold.kt).
3. **A List → Preview/Edit flow with a genuinely useful side-by-side view** (Notes, Google Tasks,
   Reminders/Agenda, Settings hub) → tag the two `NavKey` entries with `ListDetailSceneStrategy`
   metadata (below). Don't reach for this on flows where the "detail" is a full-screen editor that
   doesn't make sense shrunk into a side pane.

## Nav rail / bottom bar: `AppNavigationScaffold` + `PersistentNavRailSceneDecoratorStrategy`

`AppNavigationScaffold` (`ui-common`) wraps Material3's `NavigationSuiteScaffold`: a bottom
`NavigationBar` on Compact width, a `NavigationRail` on Medium+ - decided automatically from the
window size class, not from our own `DeviceScreenConfiguration` enum (the library's calculation is
the source of truth here so we stay aligned with Material3's own guidance instead of re-deriving
it). `ui-common` only knows the generic `AppDestination<T>` model (icon, label, badge, a caller-
supplied key) - it has no idea what a `NavKey` is, per the module dependency rules in
`architecture.md`.

Wiring which destinations get the rail uses Nav3's
[scene decorators](https://developer.android.com/guide/navigation/navigation-3/scenes/scene-decorators)
rather than a boolean threaded through screen composables - same idea as `ListDetailSceneStrategy`
above: a `SceneDecoratorStrategy` registered once in `AppNavGraph.kt`, entries opt in via metadata.
`PersistentNavRailSceneDecoratorStrategy` (`app`) checks whether the scene's top entry carries
`PersistentNavRail.metadata()` and, if so and the width is Medium+, wraps `scene.content()` in
`AppNavigationScaffold`:

```kotlin
// AppNavGraph.kt
val persistentNavRailStrategy = PersistentNavRailSceneDecoratorStrategy(
  destinations = homeRailDestinations(),
  onNavigate = { backStack.add(it) },
)
NavDisplay(
  // ...
  sceneDecoratorStrategies = listOf(persistentNavRailStrategy),
)

// HomeNavGraph.kt
entry<HomeNavKey.Main>(metadata = PersistentNavRail.metadata()) { HomeEntry(backStack) }
```

**Why the rail's destinations are static (icon/label, no live badge counts):** a
`SceneDecoratorStrategy` runs *outside* the Nav3 entry-scoped `ViewModelStoreOwner` -
`rememberViewModelStoreNavEntryDecorator()` wraps each entry's own `content`, not the chrome a
decorator adds around it. So the decorator can't safely resolve `ScheduleHomeViewModel` a second
time without either getting a different instance than the one `HomeEntry` uses, or duplicating its
data fetch. Since navigation targets are just `NavKey`s, routing doesn't need the ViewModel at all
- only the live subtitle counts Home's header grid shows would - so the rail intentionally drops
those rather than risk a second ViewModel instance. If live counts on the rail matter later, fetch
them independently (not via `ScheduleHomeViewModel`) in the decorated `Scene`'s `content`, e.g. via
a plain Koin-injected use case rather than a ViewModel.

Because the wrapping happens outside the screen, `ChronologicalHomeScreen` doesn't take a `showXxx`
boolean either - it independently calls `isTabletScreen() || isDesktopScreen()` to decide whether
to still render its own header grid/row. Both sides read the same ambient window-size state, so
they can't drift out of sync without either needing to know about the other.

## Two-pane list-detail: `ListDetailSceneStrategy`

We use Nav3's official [Scenes](https://developer.android.com/guide/navigation/navigation-3/scenes)
mechanism (`androidx.compose.material3.adaptive:adaptive-navigation3`) rather than a bespoke
scaffold. It reads the *existing* single `NavKey` backstack - there is no parallel navigation state
to keep in sync.

`AppNavGraph.kt` already registers the strategy:

```kotlin
val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
NavDisplay(
  // ...
  sceneStrategies = listOf(listDetailSceneStrategy),
)
```

This is a no-op today - `ListDetailSceneStrategy` only activates for an entry pair explicitly
tagged with its metadata, and nothing is tagged yet. To adopt it for a feature (Phase 2 work), tag
that feature's `entry<>()` calls in its own `XyzNavGraph.kt`:

```kotlin
entry<NotesNavKey.List>(
  metadata = ListDetailSceneStrategy.listPane(
    detailPlaceholder = {
      DetailPanePlaceholder(text = stringResource(R.string.select_a_note), icon = AppIcons.Fluent.Text)
    },
  ),
) { NotesListEntry(backStack) }

entry<NotesNavKey.Preview>(
  metadata = ListDetailSceneStrategy.detailPane(),
) { NotesPreviewEntry(backStack, it.id) }
```

On Medium+ width, `List` and `Preview` render side-by-side with `detailPlaceholder` shown before a
preview is selected; on Compact width, it falls back to the existing push/pop behavior
automatically. `DetailPanePlaceholder` (`ui-common`) is the shared empty-state composable for the
placeholder slot, so every feature doesn't invent its own.

A three-pane flow (list + detail + a supplementary pane) can add `ListDetailSceneStrategy.extraPane()`
to a third entry.

## Foldable

`FoldPosture` (`Flat` / `Book` / `TableTop`) is derived from `androidx.window`'s
`WindowInfoTracker`/`FoldingFeature` and exposed via `rememberAppWindowState()`. Nothing consumes it
yet. When a screen needs fold-aware placement (Phase 3): prefer `TableTop` to force two-pane
regardless of width (content above/below the hinge), and avoid centering content across a `Book`
hinge the same way you'd avoid it for `TabletLandscape`.

## Preview tooling

`@AppScreenSizePreviews` (`ui-common`) renders a `@Composable` once per `DeviceScreenConfiguration`
bucket, at dimensions chosen to land on the correct side of its breakpoints. Apply it to preview
functions for any new shared adaptive component instead of a single `@Preview`, so tablet/desktop
regressions show up before the component ships:

```kotlin
@AppScreenSizePreviews
@Composable
private fun MyComponentPreview() { ... }
```
