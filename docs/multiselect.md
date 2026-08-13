# Multiselect

Reference for the long-press-driven bulk-selection pattern used on list screens (delete/archive/
move/etc. several items at once). The Notes list is the reference implementation - copy its shape
rather than inventing a new one per screen.

## The three pieces

1. The list item's UI model implements
   [`Selectable<T>`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/selection/Selectable.kt)
   (`ui-common`).
2. The screen's state carries a single `selectedCount: Int` - no separate `Set<String>` of
   selected ids, no separate "is selecting" boolean.
3. UI chrome comes from two shared `ui-common` composables -
   [`SelectionTopBar`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/SelectionTopBar.kt)
   and
   [`SelectionOverlay`](../ui/ui-common/src/main/kotlin/com/github/naz013/ui/common/compose/foundation/SelectionOverlay.kt)
   - plus a `BackHandler` the screen wires itself.

Full reference implementation: [`feature/feature-note/.../list/`](../feature/feature-note/src/main/kotlin/com/github/naz013/feature/note/list)
(`NotesScreen.kt`, `NotesViewModel.kt`, `NotesScreenState.kt`) and its test,
[`NotesViewModelTest.kt`](../feature/feature-note/src/test/kotlin/com/github/naz013/feature/note/list/NotesViewModelTest.kt).

## 1. Make the list item selectable

```kotlin
data class UiXListItem(
  override val id: String,
  // ...existing fields...
  override val isSelected: Boolean = false,
) : Selectable<UiXListItem> {
  override fun withSelected(selected: Boolean) = copy(isSelected = selected)
}
```

A default value on `isSelected` keeps every existing producer/consumer of the model source-
compatible. Implementing `Selectable<T>` unlocks these `List<T>` extensions
(`com.github.naz013.ui.common.selection`):

| Extension | Effect |
|---|---|
| `select(id)` | marks one item selected, leaves the rest alone |
| `toggleSelection(id)` | flips one item's `isSelected` |
| `clearSelection()` | deselects everything |
| `selectedCount()` | `count { it.isSelected }` |
| `selectedIds()` | the `id`s of every selected item, as a `Set<String>` |

## 2. Screen state

Add one field to the screen's state data class:

```kotlin
data class XScreenState(
  // ...
  val selectedCount: Int = 0,
)
```

Selection mode is *derived*, not stored: `val isSelectionMode = state.selectedCount > 0`.
Deselecting the last item this way naturally exits selection mode on its own - the same behavior
as Files/Gmail-style multiselect - with no extra state to keep in sync.

Don't thread a `Set<String>` of selected ids down into the Composable. Selection lives on the item
itself (`item.isSelected`), so list rendering never needs a membership lookup - it just reads the
field off the item it's already rendering.

## 3. ViewModel recipe

All selection mutation goes through one helper that maps the current ready list and recomputes the
count in the same state update:

```kotlin
private fun updateSelection(transform: (List<UiXListItem>) -> List<UiXListItem>) {
  _state.update { state ->
    val listState = state.listState
    if (listState !is ListState.Ready) return@update state
    val items = transform(listState.items)
    state.copy(listState = ListState.Ready(items), selectedCount = items.selectedCount())
  }
}

fun onItemClick(id: String) {
  if (_state.value.selectedCount > 0) {
    updateSelection { it.toggleSelection(id) }
  } else {
    // normal single-item navigation
  }
}

fun onItemLongClick(id: String) {
  updateSelection { it.select(id) }
}

fun onSelectionCancel() {
  updateSelection { it.clearSelection() }
}

private fun selectedIds(): Set<String> =
  (_state.value.listState as? ListState.Ready)?.items.orEmpty().selectedIds()
```

Bulk actions loop `selectedIds()` through the *existing* single-item use case, then cancel the
selection and refresh:

```kotlin
fun onArchiveSelectedClick() {
  val ids = selectedIds()
  if (ids.isEmpty()) return
  viewModelScope.launch(dispatcherProvider.default()) {
    ids.forEach { changeArchiveStateUseCase(it, true) }
    withContext(dispatcherProvider.main()) { onSelectionCancel() }
    refresh()
  }
}
```

**Confirmation convention:** match whatever the single-item version of that action already does.
Delete is destructive, so bulk delete asks for confirmation too - reuse `DialogDispatcher`, with a
pluralized title built via `TextProvider.getText(res, ids.size)` and passed through the nav event
(see `NotesViewModel.NavigationEvent.ConfirmDeleteSelected`). Non-destructive actions (archive,
move, recolor) skip confirmation, same as their single-item menu entries do today. This is
judgment, not a hard rule - follow what the screen already does for one item.

## 4. UI recipe

```kotlin
val isSelectionMode = state.selectedCount > 0
BackHandler(enabled = isSelectionMode) { onSelectionCancel() }

Scaffold(
  topBar = {
    if (isSelectionMode) {
      SelectionTopBar(
        title = pluralStringResource(R.plurals.notes_selected_count, state.selectedCount, state.selectedCount),
        onCancelClick = onSelectionCancel,
        actions = selectionMenuItems(/* ...whatever the screen's actions need... */),
        onActionClick = { id -> /* map id back to a callback, see below */ },
      )
    } else {
      NormalTopBar(/* ... */)
    }
  },
) { /* ... */ }
```

`SelectionTopBar` takes the title as a plain `String`, not a count - it doesn't assume "N selected"
is the right wording, and it doesn't localize anything itself.

**All of a screen's bulk actions collapse into a single three-dot menu**, not individual icon
buttons in the bar - so every screen's selection bar looks and behaves identically (X, title,
one overflow button) no matter how many actions it offers, the same way the app's per-item
overflow menus already read as one consistent pattern. `actions` is a `List<PopupMenuItem>`
(`ui-common`, the same type the per-item overflow menus already build) and `onActionClick` gets
the clicked item's `id`. Define a private enum for the action ids and a `@Composable` builder
function, mirroring how the per-item menu (`NoteMenuAction`/`noteMenuItems`) is already built:

```kotlin
private enum class XSelectionAction { DELETE, ARCHIVE }

@Composable
private fun XSelectionMenuItems(isArchived: Boolean): List<PopupMenuItem> =
  buildList {
    add(
      PopupMenuItem(
        id = XSelectionAction.ARCHIVE.ordinal,
        title = stringResource(if (isArchived) R.string.unarchive else R.string.archive),
        iconRes = R.drawable.ic_fluent_archive,
      )
    )
    add(
      PopupMenuItem(
        id = XSelectionAction.DELETE.ordinal,
        title = stringResource(R.string.delete),
        iconRes = R.drawable.ic_fluent_delete,
      )
    )
  }

// in the screen composable:
SelectionTopBar(
  title = /* ... */,
  onCancelClick = onSelectionCancel,
  actions = XSelectionMenuItems(state.isArchived),
  onActionClick = { id ->
    when (XSelectionAction.entries[id]) {
      XSelectionAction.ARCHIVE -> onArchiveSelectedClick()
      XSelectionAction.DELETE -> onDeleteSelectedClick()
    }
  },
)
```

**Whenever the title names a noun** ("N notes selected", "N groups selected"), use
`pluralStringResource` backed by an Android `<plurals>` resource, not a plain formatted
`stringResource` - "1 note" vs "2 notes" needs real pluralization once a noun is involved, and most
non-English languages need it even for phrasing English doesn't (Russian/Polish/Arabic/Czech all
distinguish more than "one vs. many"). Follow the `notes_selected_count` example set up for Notes:

1. Two plain strings per locale, `values*/strings.xml` - `xxx_count_one` for quantity 1,
   `xxx_count_other` for everything else (this codebase's plural sets only define these two
   buckets; see `x_attachments`/`group_x_reminders` in
   [`plurals.xml`](../ui/ui-common/src/main/res/values/plurals.xml) for other examples of the same
   pattern):
   ```xml
   <!-- values/strings.xml -->
   <string name="note_selected_count_one">%1$d note selected</string>
   <string name="note_selected_count_other">%1$d notes selected</string>
   ```
   Translate both into every `values-*` locale the app ships (`ui/ui-common/src/main/res/values-*/`)
   - reuse whatever noun that screen already uses elsewhere (e.g. however `"Notes"` or
     `"Groups"` is already translated in that locale) rather than inventing new vocabulary.
2. One `<plurals>` entry referencing those two strings, **declared once in the base
   `values/plurals.xml` only** - not per locale. `@string/...` references inside it still resolve
   against whichever locale is active at runtime, so this one block is enough:
   ```xml
   <!-- values/plurals.xml -->
   <plurals name="notes_selected_count">
     <item quantity="one">@string/note_selected_count_one</item>
     <item quantity="other">@string/note_selected_count_other</item>
   </plurals>
   ```
3. Read it with `pluralStringResource(R.plurals.notes_selected_count, count, count)` (the count is
   passed twice - once to pick the quantity bucket, once as the `%1$d` format argument).

If the title genuinely has no noun and "N selected" alone is fine, the shared
`R.string.selected_count` (`"%1$d selected"`, `ui-common`) covers that case without a `<plurals>`
resource:

```kotlin
title = stringResource(R.string.selected_count, state.selectedCount)
```

The list item's card needs long-press (`combinedClickable`, not plain `clickable`), and its
trailing content swaps between a checkbox and its normal per-item menu via `SelectionOverlay`:

```kotlin
Card(modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
  // ...
  Box(Modifier.align(Alignment.TopEnd)) {
    SelectionOverlay(
      isSelectionMode = isSelectionMode,
      isSelected = item.isSelected,
      onToggleSelected = { onItemClick(item.id) },
    ) {
      ItemOverflowMenu(/* the screen's existing per-item menu */)
    }
  }
}
```

Route any other trailing-content taps (e.g. an image thumbnail that normally opens a preview)
through `onItemClick(item.id)` instead of their normal handler while `isSelectionMode` is true, so
every tap on the card toggles selection consistently.

## Testing pitfall: refresh-on-collection

Several ViewModels expose state as `someFlow.stateIn(...).onStart { refresh() }`, so that every
*fresh* collection re-fetches from the repository (`NotesViewModel.notesScreenState` does this).
That's fine for production (`collectAsStateWithLifecycle()` subscribes once), but it's a trap in
tests: calling `.first()` twice in a row is two fresh collections, and the second one's refresh
will silently discard any in-memory selection mutation you made between the two calls - the
assertion ends up checking freshly-refetched data, not what you just did.

Subscribe once with a long-lived collector instead of repeated `.first()` calls when a test needs
to observe selection state after mutating it:

```kotlin
private fun TestScope.readyViewModel(ids: List<String>): Pair<XViewModel, () -> XScreenState> {
  // ...stub the repository...
  val viewModel = createViewModel()
  var latest = XScreenState()
  backgroundScope.launch(Dispatchers.Unconfined) { viewModel.state.collect { latest = it } }
  return viewModel to { latest }
}
```

`Dispatchers.Unconfined` matters here: it makes the collector observe each mutation synchronously,
so no `runCurrent()`/`advanceUntilIdle()` juggling is needed (this repo's `BaseTest` already calls
`Dispatchers.setMain(UnconfinedTestDispatcher())`, and `mockDispatcherProvider()` backs
`DispatcherProvider` with `Dispatchers.Unconfined` too). See
`NotesViewModelTest.readyViewModel` for the full version, including bulk-action tests that
*don't* need this (checking `selectedCount == 0` after a bulk action is safe via `.first()`, since
the action already zeroed it out via `onSelectionCancel()` before its own `refresh()` runs).
