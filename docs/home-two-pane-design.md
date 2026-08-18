# Home Two-Pane (List-Detail) Design

Design proposal for adopting `ListDetailSceneStrategy` (see `docs/adaptive-layouts.md` § "Two-pane
list-detail") on the Home screen — the first concrete adopter of that mechanism. Nothing described
here is implemented yet; this is the plan to review before writing code.

## Goal

On Medium+ width (tablet/desktop, same breakpoint the nav rail already uses), Home renders as two
panes side by side:

- **List pane (left):** today's `ChronologicalHomeScreen`, unchanged.
- **Detail pane (right):** the reminder or birthday preview screen for whichever item was last
  tapped in the list. Before anything is tapped, it shows an empty-state placeholder.

Tapping a list item selects it (visually highlighted) and swaps the detail pane's content — no
push/pop, no full-screen navigation. A **Close (X)** button in the detail pane's app bar closes it
and clears the selection, returning to the empty-state placeholder. On Compact width (phone),
behavior is unchanged: tapping a list item pushes the existing preview screen full-screen, and the
app bar keeps its normal back arrow, which pops back to the list.

## Current state (as of this doc)

- `HomeNavKey` has a single destination, `HomeNavKey.Main` — no `List`/`Preview` split.
- `ChronologicalHomeScreen` has no `TopAppBar`; its "app bar" is a custom `Header` row (greeting +
  `AddButton` dropdown + a settings icon that's hidden on Medium+ since Settings becomes a rail
  item). No FAB — `AddButton` is the create-entry point.
- Tapping a list row (`EventCard`, shared by both reminders and birthdays, differentiated only by
  `HomeEvent.type`) calls `ScheduleHomeViewModel.onEventClicked`, which emits
  `OpenReminderDetails`/`OpenBirthdayDetails`. `HomeNavGraph.kt`'s `homeEntries(...)` maps those to
  caller-supplied callbacks; `AppNavGraph.kt` wires them to
  `backStack.add(ReminderPreviewNavKey.Preview(id))` / `backStack.add(BirthdaysNavKey.Preview(id))`.
- `PreviewReminderScreen` and `PreviewBirthdayScreen` (in `feature-reminder`/`feature-birthday`)
  each own a full `Scaffold { topBar = TopAppBar(title = "Details", navigationIcon = back arrow,
  actions = [edit, share/copy, delete]) }` — neither has any pane-awareness today.
- `rememberListDetailSceneStrategy<NavKey>()` is already registered in `AppNavGraph.kt`'s
  `NavDisplay`, but is a **no-op**: no entry anywhere (Home, Notes, or otherwise) is tagged with
  `ListDetailSceneStrategy.listPane()`/`.detailPane()` yet, despite `docs/adaptive-layouts.md`'s
  Notes example reading as if it's already in place — it's prescriptive, not descriptive. Home
  would be the first real adopter, with no in-repo precedent beyond that pseudocode.
- **Important:** `ReminderPreviewNavKey.Preview` and `BirthdaysNavKey.Preview` aren't Home-exclusive
  — Calendar, Agenda, and Groups all push the same keys from their own list screens. Any metadata
  tagging on those keys is graph-wide, not Home-scoped (see "Scope of the detail-pane tag" below).

## Layout behavior

| Width | List pane | Detail pane |
|---|---|---|
| Compact (phone) | Full-screen `ChronologicalHomeScreen`, as today | Not shown; tapping an item pushes the existing full-screen preview, back pops it. No change from current behavior. |
| Medium+ (tablet/desktop) | `ChronologicalHomeScreen`, fixed-ish width per `ListDetailSceneStrategy` defaults, selected row highlighted | Reminder/birthday preview rendered inline; empty-state placeholder before any selection |

This sits *inside* the nav rail's content area — `PersistentNavRailSceneDecoratorStrategy` wraps
the whole scene (rail on the far left), and the list+detail pair fills the remaining width. The two
mechanisms are independent (`SceneStrategy` vs. `SceneDecoratorStrategy`) and should compose without
special-casing, but this is the first time they'll apply to the same backstack entries
simultaneously in this codebase — worth an explicit smoke test early (see "Risks").

## Navigation wiring

Tag three entries with `ListDetailSceneStrategy` metadata:

```kotlin
// HomeNavGraph.kt
entry<HomeNavKey.Main>(
  metadata = ListDetailSceneStrategy.listPane(
    detailPlaceholder = {
      DetailPanePlaceholder(
        text = stringResource(R.string.select_an_item_to_see_details),
        icon = painterResource(R.drawable.ic_fluent_clipboard_task), // or similar existing icon
      )
    },
  ),
) { HomeEntry(...) }
```

```kotlin
// AppNavGraph.kt, inside reminderPreviewEntries(...) / birthdaysEntries(...) wiring
entry<ReminderPreviewNavKey.Preview>(metadata = ListDetailSceneStrategy.detailPane()) { ... }
entry<BirthdaysNavKey.Preview>(metadata = ListDetailSceneStrategy.detailPane()) { ... }
```

### Scope of the detail-pane tag

`ListDetailSceneStrategy` pairs panes by reading the backstack itself: when the entry immediately
below the top is tagged `listPane()` and the top entry is tagged `detailPane()`, it renders them
side by side; otherwise a tagged `detailPane()` entry with no matching `listPane()` predecessor just
falls back to the normal single-pane scene. Concretely:

- `[HomeNavKey.Main, ReminderPreviewNavKey.Preview(id)]` → `HomeNavKey.Main` is tagged `listPane()`
  → two-pane forms. This is what we want.
- `[CalendarNavKey.Month, ReminderPreviewNavKey.Preview(id)]` → `CalendarNavKey.Month` is **not**
  tagged → falls back to today's full-screen push, unaffected by this change. Same for Agenda and
  Groups.

So tagging the shared preview keys is safe for those other flows today, and is in fact the intended
reuse model (a `detailPane()`-tagged entry is meant to be a landing spot any sufficiently-tagged
list can pair with) — not a hack. If Calendar/Agenda/Groups later want their own two-pane view, they
tag their own list entry the same way and get it for free. Flag this for a manual regression pass
during QA (open a reminder from Calendar/Agenda/Groups on a tablet, confirm it still pushes
full-screen as before) since it's inference from the strategy's documented pairing rule, not
something exercised anywhere in this codebase yet.

**Edge case to note, not resolve up front:** `reminderPreviewEntries` can navigate *beyond* the
paired detail entry (e.g. tapping a linked note pushes `NotesNavKey.List` + `NotesNavKey.Preview`
onto the same flat backstack via `AppNavBridge`). Those keys aren't tagged, so the chain breaks and
the scene falls back to single-pane/full-screen for that push — acceptable default behavior, but
worth a manual check that it doesn't look jarring (list pane disappearing mid-flow) during QA.

## Selection state

Feature modules never depend on each other (`feature-home` can't import
`ReminderPreviewNavKey`/`BirthdaysNavKey` to inspect the backstack itself), but `AppNavGraph.kt`
(in `app`) already sees every concrete `NavKey` type. Derive the selected id there and thread it
into `homeEntries(...)` as a plain `String?`, the same pattern already used for the ~18 `onOpenXxx`
callbacks:

```kotlin
// AppNavGraph.kt
val selectedEventId = backStack.lastOrNull()?.let { key ->
  when (key) {
    is ReminderPreviewNavKey.Preview -> key.id
    is BirthdaysNavKey.Preview -> key.id
    else -> null
  }
}
homeEntries(
  backStack = backStack,
  selectedEventId = selectedEventId,
  ...
)
```

`ChronologicalHomeScreen`/`EventCard` uses it purely for a visual highlight (tonal background, e.g.
`MaterialTheme.colorScheme.secondaryContainer`) when `event.id == selectedEventId` — no behavior
change, so it's safe to always pass through and simply won't visibly do anything on Compact width
(no selection is ever visible there since the list and detail are never on screen together).

Re-tapping the already-selected row is a no-op (detail pane already shows it) — no toggle-to-deselect
via the row itself; deselecting is the detail pane's Close button's job (see next section).

**Edge case:** if the currently-selected reminder/birthday is deleted from the detail pane, the
detail pane has nothing left to show. Simplest handling: pop back to `HomeNavKey.Main` (same as
today's single-pane delete-then-back behavior, and the same pop the Close button itself triggers —
see below), which naturally clears `selectedEventId` and falls back to the placeholder — no new
logic needed beyond confirming the existing delete flow already pops.

## Detail pane app bar: Close (X) instead of back

This was the open research question. Findings below, then the decision: the detail pane's app bar
shows an explicit **Close (X)** icon rather than either a back arrow or no icon at all — a deliberate
product call that diverges from the "no explicit deselect" pattern the research below found
elsewhere, made because a reminder/birthday detail pane can land in a state (just marked complete,
just created) where the user actively wants to confirm "done looking at this" and reset the list to
neutral, rather than the detail pane silently sitting there tied to a stale selection.

### What Material 3 / Android guidance says

- Each pane should carry its own top app bar with its own title and actions — not one shared bar
  ([Android Developers: Build a list-detail layout](https://developer.android.com/develop/adaptive-apps/guides/list-detail),
  [m3.material.io canonical layouts](https://m3.material.io/foundations/layout/canonical-layouts/list-detail)).
  This matches what we already have: Home's own header row for the list pane, and the preview
  screens' existing `TopAppBar` for the detail pane — no new bar structure needed, just conditional
  back-icon behavior.
- Google's own `NavigableListDetailPaneScaffold` / `ListDetailSceneStrategy` docs describe back
  behavior via a `BackNavigationBehavior` enum (default `PopUntilScaffoldValueChange`): in
  multi-pane, changing the detail pane's content is **not** a "back-worthy" layout change (there's
  nothing to revert to — the list is already showing); in single-pane, back pops from detail back to
  list, same as today.
- Android's canonical-layouts guidance and Google's `nav3-recipes` sample both converge on the same
  rule for the **back icon inside the detail screen's own app bar**: hidden in two-pane (list is
  already visible alongside, so there's nothing to "go back" to within the screen itself), shown and
  wired to pop in single-pane. The sample ties this to a `LocalBackButtonVisibility`-style
  composition local set by the scaffold — but that's sample-authored plumbing, not confirmed to be
  built into the exact `androidx.compose.material3.adaptive:adaptive-navigation3:1.3.0` version this
  repo pins. Don't assume the library exposes it for free; verify against the actual API surface
  before relying on it.

### Prior art in shipped apps

- **Gmail** (tablet/foldable dual-pane, rolled out 2025): inbox and open conversation are visually
  separate panes with an adjustable divider; each pane carries its own header content.
  ([9to5Google](https://9to5google.com/2025/04/25/gmail-adjustable-layout/))
- No well-documented, citable breakdown of the exact app-bar-architecture in Slack/Telegram/WhatsApp
  desktop or the Android Settings app's tablet mode was found — treat those as directionally
  consistent (per-pane bar, no back arrow when both panes visible) but not independently confirmed
  sources.
- No app examined offers an explicit "deselect" affordance in two-pane mode — selecting a different
  list item just replaces the detail pane's content; there's no back/close icon whose only job is
  clearing the selection. This design deliberately does add one (see above).

### Recommendation

Don't derive "which icon to show" from window-size class alone (`isTabletScreen() ||
isDesktopScreen()`) — that's the same ambient check `ChronologicalHomeScreen` already uses for its
own header, but reusing it here has a real bug: on a tablet, opening a reminder from **Calendar**
(not tagged `listPane()`, so no two-pane forms) would still evaluate "is tablet" as true and show
the Close icon on what is, in that flow, a genuine full-screen push — closing it would have nothing
sensible to do (there's no paired list pane to reveal, and no selection to clear).

Instead, compute an explicit `renderAsDetailPane: Boolean` in `AppNavGraph.kt`, mirroring the exact
condition `ListDetailSceneStrategy` itself uses to pair panes — width is Medium+ **and** the entry
immediately below the top of the backstack is `HomeNavKey.Main`:

```kotlin
val renderAsDetailPane = (isTabletScreen() || isDesktopScreen()) &&
  backStack.getOrNull(backStack.lastIndex - 1) == HomeNavKey.Main
```

Thread this into `reminderPreviewEntries`/`birthdaysEntries` as a parameter, forwarded to
`PreviewReminderScreen`/`PreviewBirthdayScreen` to pick the icon. The handler stays the **same
`onBackClick` callback** either way — both icons just pop the current entry off the backstack
(`backStack.removeLastOrNull()`); only the glyph changes. That single pop is also what clears the
selection: `selectedEventId` (see "Selection state" above) is derived from the backstack's top
entry, so once the pop removes `ReminderPreviewNavKey.Preview`/`BirthdaysNavKey.Preview`, the next
recomposition already reads `selectedEventId == null` — no separate "clear selection" call needed.

```kotlin
TopAppBar(
  title = { Text(stringResource(R.string.details)) },
  navigationIcon = {
    MenuIconButton(
      icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Fluent.ArrowLeft,
      onClick = onBackClick, // unchanged handler - backStack.removeLastOrNull()
    )
  },
  actions = { /* edit / share / delete, unchanged */ },
)
```

Title and actions (edit/share/delete) stay exactly as they are today. This keeps the decision
explicit and testable without relying on unconfirmed library internals, and consistent with how
this codebase already threads booleans/callbacks through `XyzNavGraph.kt` functions rather than
reaching for ambient composition locals.

### Empty-detail placeholder

No `TopAppBar` in the placeholder state — just `DetailPanePlaceholder` (already exists in
`ui-common`, exactly the shape needed: centered icon + text, no bar) centered in the pane. Adding an
app bar with no title/actions to fill against would look like a broken/unstyled screen for no
benefit.

## Rollout plan

1. Tag `HomeNavKey.Main` (`listPane`) and `ReminderPreviewNavKey.Preview`/`BirthdaysNavKey.Preview`
   (`detailPane`); verify two-pane forms on a tablet-width preview/emulator with the placeholder
   showing before any selection.
2. Compute `selectedEventId` and `renderAsDetailPane` in `AppNavGraph.kt`; thread both through.
3. Add the selected-row highlight to `EventCard`.
4. Swap the detail screens' `navigationIcon` between arrow-back and Close(X) based on
   `renderAsDetailPane`, both wired to the existing `onBackClick` handler unchanged.
5. Regression pass: Compact width unaffected (push/pop as today, arrow-back only); Calendar/Agenda/
   Groups still push `ReminderPreviewNavKey.Preview`/`BirthdaysNavKey.Preview` full-screen on tablet
   width with the arrow-back icon (not Close), unaffected by the new tags; tapping Close on the
   detail pane clears both the pane and the list's highlighted row in one action; delete-while-
   selected falls back to placeholder correctly; rotation/resize between Compact and Medium+
   mid-flow doesn't strand the user without a way back.
6. Manual QA on a tablet emulator with the nav rail visible (rail + list + detail simultaneously),
   since that three-way composition (`SceneDecoratorStrategy` + `SceneStrategy` + Home's own
   `showHeaderNavigation` logic) has no precedent in this codebase yet.

## Open questions for review

- Confirm the exact detail-pane empty-state icon/copy (placeholder above uses a guess).
- Confirm selected-row highlight color/treatment against the current M3 Expressive theme tokens
  (see `docs/m3-expressive-adoption.md`) rather than inventing one ad hoc.
- Decide whether this doc's scope should also fold in Notes' Phase-2 adoption (mentioned as
  "planned next" in `docs/adaptive-layouts.md`) or stay Home-only for now — this doc assumes
  Home-only, matching what was actually asked.
