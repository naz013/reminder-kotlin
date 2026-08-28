# Navigation 3 Setup — `NavDisplay`, Scene Strategies & Decorators (Portable Guide)

This document describes **how this repo wires up Jetpack Navigation 3** (`androidx.navigation3`)
as a standalone recipe, so the same setup can be reproduced in another multi-module Compose
project. It repeats some material from `docs/architecture.md` / `docs/adaptive-layouts.md` but
frames it as a generic "how to build this from scratch" guide rather than a description of this
app's specific screens.

For *why* specific features look the way they do (which screens are two-pane, why the rail is
static), see `docs/adaptive-layouts.md` and `docs/home-two-pane-design.md`. This doc is about the
*mechanics*: dependencies, the single shared backstack, per-feature entry registration,
`NavDisplay`, scene strategies, and entry/scene decorators.

---

## 1. The core idea: one shared backstack, contributed to by every feature module

There is exactly **one** `NavBackStack` and exactly **one** `NavDisplay` in the whole app, owned by
the top-level Activity/Composable. Every feature module does **not** own its own `NavHost` —
instead each feature exposes a plain Kotlin extension function that registers its screens' entries
into the shared `entryProvider { }` block, using the shared backstack it's handed as a parameter.

This matters for a multi-module project because Nav3's `NavKey` is just a serializable marker
interface — there's no `NavController` object a feature module would need to depend on `app` to
obtain. A feature module only needs:
- its own `sealed interface XyzNavKey : NavKey` (one destination per subtype)
- an `EntryProviderScope<NavKey>.xyzEntries(backStack: MutableList<NavKey>, ...callbacks)`
  extension function
- its own `KoinModule.kt` for ViewModels

...and the top-level module (`app` here) wires all of these together in one place.

```
app  (owns NavDisplay + the one NavBackStack)
 ├── feature-home       → homeEntries(backStack, ...)
 ├── feature-note       → notesEntries(backStack, ...)
 ├── feature-reminder   → buildReminderEntries(...), reminderPreviewEntries(...)
 ├── feature-workflow   → workflowEntries(backStack)
 └── ...
```

No `feature-*` module depends on another `feature-*` module or on `app` — cross-feature navigation
either goes through a plain callback lambda threaded in from `app` (e.g. `onOpenNote: (id) -> Unit`
passed into `xyzEntries(...)`), or through a small Koin-injected bridge (`AppNavBridge`, §6) for
screens that are several entries deep and don't want every intermediate screen to thread a
callback just to relay it upward.

---

## 2. Dependencies

Version catalog entries (`gradle/libs.versions.toml`):

```toml
[versions]
androidx-navigation3 = "1.1.6"
androidx-lifecycle-viewmodel-navigation3 = "2.11.0"
androidx-compose-material3-adaptive-navigation3 = "1.3.0"

[libraries]
androidx-navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "androidx-navigation3" }
androidx-navigation3-ui      = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "androidx-navigation3" }
androidx-lifecycle-viewmodel-navigation3 = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-navigation3", version.ref = "androidx-lifecycle-viewmodel-navigation3" }
compose-material3-adaptive-navigation3   = { group = "androidx.compose.material3.adaptive", name = "adaptive-navigation3", version.ref = "androidx-compose-material3-adaptive-navigation3" }
koin-compose-navigation3 = { group = "io.insert-koin", name = "koin-compose-navigation3", version.ref = "koin" }
```

Any module that defines `NavKey`s and registers entries (every `feature-*` module, plus `app`)
needs, in its `build.gradle.kts`:

```kotlin
dependencies {
  implementation(libs.androidx.navigation3.runtime)   // NavKey, EntryProviderScope, entryProvider{}
  implementation(libs.androidx.navigation3.ui)         // NavDisplay
  implementation(libs.androidx.lifecycle.viewmodel.navigation3) // rememberViewModelStoreNavEntryDecorator
  implementation(libs.koin.compose.navigation3)        // koinViewModel() inside entry content
}
```

Only the module that actually hosts `NavDisplay` and any `ListDetailSceneStrategy`/scene-decorator
code (`app`, plus any `ui-common`-style module owning the reusable nav-rail scaffold) needs
`compose-material3-adaptive-navigation3`.

If a feature's `NavKey`s carry non-primitive fields, add
`alias(libs.plugins.kotlin.serialization)` to that module's plugin block — `@Serializable` is
required on every `NavKey` subtype (see §3).

---

## 3. Defining a feature's `NavKey`s

One `sealed interface` per feature, one `@Serializable` subtype per destination:

```kotlin
package com.github.naz013.feature.workflow

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface WorkflowNavKey : NavKey {
  @Serializable
  data object Gallery : WorkflowNavKey

  @Serializable
  data class RulesForGroup(val groupId: String) : WorkflowNavKey

  // Primitive-only fields — NavKey needs to stay simple/serializable (survives process death via
  // rememberNavBackStack's SavedStateHandle), not carry a raw sealed domain type.
  @Serializable
  data class Builder(
    val scopeType: String,
    val scopeId: String? = null,
    val editingRuleId: String? = null,
  ) : WorkflowNavKey
}
```

Rule of thumb: keep `NavKey` fields to primitives/`String`/nullable IDs. If a destination needs a
richer type, pass its *id* and have the destination's ViewModel look the full object up — the same
discipline as passing IDs through an old-style `Intent`/`Bundle`.

---

## 4. Registering a feature's entries

Each feature exposes exactly one `EntryProviderScope<NavKey>` extension function per feature
(not per screen), taking the shared backstack plus whatever cross-feature callbacks it needs:

```kotlin
package com.github.naz013.feature.workflow

fun EntryProviderScope<NavKey>.workflowEntries(backStack: MutableList<NavKey>) {
  entry<WorkflowNavKey.Gallery> { WorkflowGalleryEntry(backStack) }
  entry<WorkflowNavKey.RulesForGroup> { key -> WorkflowRulesForGroupEntry(key, backStack) }
  entry<WorkflowNavKey.Builder> { key -> WorkflowBuilderEntry(key, backStack) }
}

@Composable
private fun WorkflowGalleryEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<WorkflowGalleryViewModel>()
  val state by viewModel.state.collectAsState()
  WorkflowGalleryScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onCreateRuleClick = { backStack.add(WorkflowNavKey.Builder(scopeType = WorkflowScopeType.GLOBAL.name)) },
  )
}
```

Conventions worth keeping:
- **Back press is always `if (backStack.size > 1) backStack.removeLastOrNull()`** — never pop
  below the graph's single start destination.
- **A screen resolves its own ViewModel** with `koinViewModel<T>()` (optionally
  `{ parametersOf(...) }` to forward `NavKey` fields as constructor params) inside its private
  `@Composable` entry function — not in the `entry<>` lambda itself, and not passed in from the
  caller.
- **Screen composables stay navigation-agnostic** — `WorkflowGalleryScreen` takes plain lambdas
  (`onBackClick`, `onCreateRuleClick`), not the backstack. Only the private `*Entry` composable in
  the `XyzNavGraph.kt` file touches `backStack` directly. This keeps the screen testable/previewable
  without a real Nav3 graph.
- **A completion side-effect that should pop** (e.g. "saved successfully, go back") is a
  `LaunchedEffect` keyed on a state flag, not something threaded through the ViewModel's event
  channel as a navigation command:
  ```kotlin
  LaunchedEffect(state.didSave) {
    if (state.didSave && backStack.size > 1) backStack.removeLastOrNull()
  }
  ```

---

## 5. The root `NavDisplay`

Owned by one Composable in the top module (here, `AppNavGraph.kt`, hosted directly by the
Activity):

```kotlin
@Composable
fun AppNavGraph(initialKeys: List<NavKey> = emptyList()) {
  val backStack = rememberNavBackStack(HomeNavKey.Main, *initialKeys.toTypedArray())
  val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
  val persistentNavRailStrategy = PersistentNavRailSceneDecoratorStrategy(/* ... */)

  NavDisplay(
    backStack = backStack,
    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
    sceneStrategies = listOf(listDetailSceneStrategy),
    sceneDecoratorStrategies = listOf(persistentNavRailStrategy),
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    transitionSpec = { /* fade + scale, see below */ },
    popTransitionSpec = { /* mirrored */ },
    predictivePopTransitionSpec = { /* mirrored */ },
    entryProvider = entryProvider {
      homeEntries(backStack = backStack, /* callbacks */)
      notesEntries(backStack = backStack, /* callbacks */)
      workflowEntries(backStack = backStack)
      // ... one call per feature module
    },
  )
}
```

Key points:
- **`rememberNavBackStack(start, *initial)`** creates the single `NavBackStack` (a
  `SnapshotStateList<NavKey>` under the hood) — `start` is the graph's permanent start destination
  (`HomeNavKey.Main` here), `initial` lets a caller (e.g. resolving a deep link or notification tap
  at process start) push additional destinations on top before first composition.
- **`entryProvider { }`** is just a builder that flattens every feature's `xyzEntries(...)` calls
  into one `Map<KClass<out NavKey>, NavEntryProvider>` under the hood — there's no registration
  order requirement beyond "don't register the same `NavKey` subtype twice."
- **`onBack`** is Nav3's system-back-gesture hook — wire it to the same one-line guard every
  feature's own `onBackClick` uses.
- Values derived from the *shape* of the backstack that only the top module can compute (e.g.
  "is the currently-open detail screen being rendered as a side-pane or full-screen") are computed
  here in `AppNavGraph.kt`, not inside a feature module, because only this module imports every
  concrete `NavKey` type. Thread the result down as a plain callback/boolean, e.g.:
  ```kotlin
  val isRenderedAsDetailPane: (NavKey) -> Boolean = { key ->
    isMediumOrWiderWidth &&
      backStack.lastOrNull() == key &&
      backStack.getOrNull(backStack.lastIndex - 1) == HomeNavKey.Main
  }
  ```

### Custom navigation helpers on the backstack

Plain extension functions on `MutableList<NavKey>`, defined next to `AppNavGraph.kt`, encode
backstack-shape rules that would otherwise be duplicated at every call site:

```kotlin
// Tab-like navigation: pop back to an existing instance instead of stacking a duplicate.
private fun MutableList<NavKey>.navigateToRailDestination(key: NavKey) {
  val existingIndex = indexOf(key)
  if (existingIndex >= 0) {
    while (size > existingIndex + 1) removeLastOrNull()
  } else {
    add(key)
  }
}

// Two-pane detail navigation: replace the current detail entry instead of stacking a second one,
// since ListDetailSceneStrategy expects at most one entry per pane role.
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  val top = lastOrNull()
  if (top is ReminderPreviewNavKey.Preview || top is BirthdaysNavKey.Preview) {
    removeLastOrNull()
  }
  add(key)
}
```

---

## 6. Cross-feature navigation without a direct module dependency

Two mechanisms, chosen by how deep the caller is:

**A. Plain callback threading** — the default. `app`'s `entryProvider { }` block passes a lambda
into a feature's `xyzEntries(...)` call; that lambda pushes onto `backStack` using a *different*
feature's `NavKey` (which only `app` is allowed to import):

```kotlin
notesEntries(
  backStack = backStack,
  onEditReminder = { id -> backStack.add(BuildReminderNavKey.Main(id = id)) },
  // ...
)
```

**B. `AppNavBridge`** — a Koin singleton holding a nullable reference to the *same* backstack, for
a screen several `NavEntry`s deep that needs to reach a destination outside its own immediate
caller chain (so it doesn't have to thread a callback through every intermediate screen just to
relay it upward):

```kotlin
class AppNavBridge {
  private var outerBackStack: MutableList<NavKey>? = null

  fun attachOuterBackStack(backStack: MutableList<NavKey>) { outerBackStack = backStack }
  fun detachOuterBackStack(backStack: MutableList<NavKey>) {
    if (outerBackStack === backStack) outerBackStack = null
  }
  fun navigate(vararg keys: NavKey) { outerBackStack?.let { stack -> keys.forEach { stack.add(it) } } }
}

@Composable
fun rememberAppNavBridge(): AppNavBridge = koinInject()
```

Registered as a plain Koin `single { AppNavBridge() }`, and attached/detached from
`AppNavGraph.kt` via a `DisposableEffect` keyed on `backStack`:

```kotlin
DisposableEffect(backStack) {
  appNavBridge.attachOuterBackStack(backStack)
  onDispose { appNavBridge.detachOuterBackStack(backStack) }
}
```

A feature module only needs `koinInject<AppNavBridge>()` (or an injected constructor param) to
call `.navigate(SomeOtherFeatureNavKey.Whatever)` — it never needs a compile dependency on the
feature that owns that `NavKey`... except it does need to *know the type*, so in practice this is
still called from `app`-level glue code (a callback passed down), not from inside a `feature-*`
module directly. Its real value is letting a deeply-nested screen reach the *outer* backstack
without every intermediate entry threading the call through, not bypassing the module boundary.

---

## 7. Entry decorators: `entryDecorators`

`NavDisplay(entryDecorators = listOf(...))` controls what wraps *every* entry's content — state
that must survive recomposition/navigation but is scoped to that specific backstack entry:

```kotlin
entryDecorators = listOf(
  rememberSaveableStateHolderNavEntryDecorator(),
  rememberViewModelStoreNavEntryDecorator(),
)
```

- **`rememberSaveableStateHolderNavEntryDecorator()`** — gives each entry its own
  `SaveableStateHolder` slot, so `rememberSaveable` state inside a screen survives that screen
  being navigated away from and back to (as long as it isn't popped off the stack).
- **`rememberViewModelStoreNavEntryDecorator()`** (from `lifecycle-viewmodel-navigation3`) — gives
  each entry its own `ViewModelStoreOwner`, so `koinViewModel<T>()` calls inside that entry's
  content resolve a ViewModel scoped to *that* backstack entry, cleared when the entry is popped —
  the Nav3 equivalent of a `NavHost` destination's ViewModel scoping.

Order matters only in that both must be present for screens to get both behaviors — there's no
dependency between the two here. Add more decorators (e.g. a custom analytics-screen-view
decorator) by prepending/appending to this list; each decorator wraps every entry's content in
composition order.

**Important scoping gotcha**: entry decorators wrap each entry's *own* content — not any chrome a
`SceneDecoratorStrategy` (§9) adds around a whole scene. A `SceneDecoratorStrategy` runs *outside*
this list, so it cannot safely call `koinViewModel<T>()` a second time for the same ViewModel type
without either getting a distinct instance or duplicating that ViewModel's side effects. Keep
scene-decorator content limited to data that doesn't need its own ViewModel (navigation keys,
static icons/labels — see §9's nav-rail example).

---

## 8. Scene strategies: two-pane list-detail with `ListDetailSceneStrategy`

`ListDetailSceneStrategy` (from `adaptive-navigation3`) reads the *existing* single `NavKey`
backstack — no parallel navigation state to keep in sync. Register it once:

```kotlin
val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
NavDisplay(
  // ...
  sceneStrategies = listOf(listDetailSceneStrategy),
)
```

This is a no-op on its own — it only activates for an entry pair explicitly tagged with its
metadata. A feature opts in inside its own `XyzNavGraph.kt`:

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

On Medium+ width, `List` and `Preview` render side-by-side, with `detailPlaceholder` shown until
something is selected; on Compact width it falls back to normal push/pop automatically — no
per-screen `if (isTablet)` branching needed. `DetailPanePlaceholder` is a small shared
`ui-common` composable (icon + text, centered) so every feature reuses the same empty state instead
of inventing its own:

```kotlin
@Composable
fun DetailPanePlaceholder(text: String, modifier: Modifier = Modifier, icon: Painter? = null) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      icon?.let { Icon(painter = it, contentDescription = null) }
      Text(text = text)
    }
  }
}
```

A three-pane flow (list + detail + a supplementary pane) tags a third entry with
`ListDetailSceneStrategy.extraPane()`.

**Deciding what needs this**: most edit/preview/settings-detail screens are fine staying
single-column — don't add two-pane machinery to a screen just because it's now possible. Reach for
`ListDetailSceneStrategy` only for a genuinely useful List → Preview/Edit flow; skip it where the
"detail" is a full-screen editor that wouldn't make sense shrunk into a side pane.

---

## 9. Scene decorators: chrome around every scene (e.g. a persistent nav rail)

Where `entryDecorators` wrap one entry's content and `sceneStrategies` combine specific *tagged*
entries into a shared scene, a `SceneDecoratorStrategy` wraps **every** scene uniformly (chrome
that should surround whatever's currently showing, not something each screen opts into
individually) — e.g. a persistent side nav rail on tablet/desktop width:

```kotlin
class PersistentNavRailSceneDecoratorStrategy(
  private val destinations: List<AppDestination<NavKey>>,
  private val backStack: List<NavKey>,
  private val railState: WideNavigationRailState,
  private val onNavigate: (NavKey) -> Unit,
) : SceneDecoratorStrategy<NavKey> {
  private val destinationKeys = destinations.map { it.key }.toSet()

  override fun SceneDecoratorStrategyScope<NavKey>.decorateScene(scene: Scene<NavKey>): Scene<NavKey> {
    val selectedKey = backStack.lastOrNull { it in destinationKeys }
    return NavRailDecoratedScene(scene, destinations, selectedKey, railState, onNavigate)
  }
}

private class NavRailDecoratedScene(
  private val scene: Scene<NavKey>,
  private val destinations: List<AppDestination<NavKey>>,
  private val selectedKey: NavKey?,
  private val railState: WideNavigationRailState,
  private val onNavigate: (NavKey) -> Unit,
) : Scene<NavKey> {
  // Derived from the wrapped scene's own key so scene identity - and NavDisplay's transition
  // animations - stay stable across recompositions of this decorator.
  override val key: Any = NavRailDecoratedScene::class to scene.key
  override val entries = scene.entries
  override val previousEntries = scene.previousEntries
  override val metadata = scene.metadata
  override val content: @Composable () -> Unit = {
    if (isTabletScreen() || isDesktopScreen()) {
      AppNavigationScaffold(
        destinations = destinations,
        selectedKey = selectedKey,
        onDestinationSelected = onNavigate,
        railState = railState,
        content = scene.content,
      )
    } else {
      scene.content()
    }
  }
}
```

Registered the same way as a scene strategy:

```kotlin
NavDisplay(
  // ...
  sceneDecoratorStrategies = listOf(persistentNavRailStrategy),
)
```

Points worth carrying over to another project:

- **`override val key: Any = ThisDecorator::class to scene.key`** — always derive the decorated
  scene's key from the wrapped scene's own key, not a fresh identity per recomposition. `NavDisplay`
  uses scene `key` to decide whether a scene is "the same scene, just recomposed" (keep its
  transition state) vs. "a different scene" (run enter/exit transitions) — get this wrong and every
  recomposition of the decorator replays a transition.
- **State that must survive navigation (like the rail's expanded/collapsed toggle) must be
  `remember`ed *above* `NavDisplay`**, not inside the decorated content — `NavDisplay` disposes and
  recreates each scene's composition on navigation, so state `remember`ed inside would silently
  reset on every nav action:
  ```kotlin
  // AppNavGraph.kt, not inside PersistentNavRailSceneDecoratorStrategy's content
  val navRailState = rememberWideNavigationRailState()
  ```
- **A `SceneDecoratorStrategy` runs outside the entry-scoped `ViewModelStoreOwner`** (§7's
  gotcha) — this is why the rail's `destinations` here are static icon/label data computed by the
  caller, not a live ViewModel-backed badge count. If per-destination live data is ever needed on
  the decorator, fetch it independently (e.g. a plain Koin-injected use case, not a ViewModel shared
  with the screen), rather than trying to reach into an entry's own ViewModel instance from outside
  its scope.
- **This decorator wraps unconditionally** (checking only window-size-class booleans internally,
  not per-entry opt-in metadata) — a project could equally make it conditional on a metadata tag if
  only some screens should show the rail; this app's version applies it to every scene because the
  rail is meant to be persistent chrome, not a per-screen opt-in.

`AppDestination<T>` is the generic (nav-agnostic) model this reads:

```kotlin
data class AppDestination<T>(
  val key: T,
  val icon: Painter,
  val labelRes: Int,
  val selectedIcon: Painter = icon,
  val badgeCount: Int? = null,
)
```

Kept generic over `T` (not `NavKey`) so the composable owning the actual chrome
(`AppNavigationScaffold`, wrapping Material3's `NavigationSuiteScaffold` — bottom bar on Compact,
rail on Medium+) can live in a shared UI module with **no dependency on the navigation library at
all** — only the top module's decorator strategy needs to know these keys are `NavKey`s.

---

## 10. Hosting `NavDisplay` from the Activity

```kotlin
class BottomNavActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    setContent {
      AppTheme {
        AppNavGraph(initialKeys = resolveInitialNavKeys())
      }
    }
  }

  // Deep links / notification taps / shortcuts resolve to a list of NavKey to seed the backstack
  // with, so tapping a reminder notification opens straight into its preview screen (with Home
  // still underneath it for back navigation) instead of just landing on Home.
  private fun resolveInitialNavKeys(): List<NavKey> { /* ... */ }
}
```

Nav3's system back button handling is internal to `NavDisplay` (its `onBack` param, §5) — the
Activity doesn't need its own `OnBackPressedCallback` for in-graph navigation.

---

## 11. Porting this to a new project — step by step

1. Add the four/five dependencies from §2 to whichever modules define screens/`NavKey`s, and to
   the one module that will host `NavDisplay`.
2. Pick your top-level start destination and create its `NavKey` — every backstack starts from one
   permanent entry (`HomeNavKey.Main` here).
3. For each feature module: define a `sealed interface XyzNavKey : NavKey` (§3) and one
   `EntryProviderScope<NavKey>.xyzEntries(backStack, ...callbacks)` function (§4). Keep screen
   composables free of `backStack`/`NavKey` — only the private `*Entry` wrapper touches it.
4. In the top module, write the single `AppNavGraph`-equivalent Composable: `rememberNavBackStack`,
   `NavDisplay` with `entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(),
   rememberViewModelStoreNavEntryDecorator())`, and an `entryProvider { }` block calling every
   feature's `xyzEntries(...)`.
5. Add cross-feature navigation as plain callback params threaded through `xyzEntries(...)` calls
   (§6-A) by default. Only add a bridge singleton like `AppNavBridge` (§6-B) once you actually hit a
   screen nested several entries deep that needs to reach the outer backstack without every
   intermediate screen relaying a callback.
6. Add `ListDetailSceneStrategy` (§8) only for flows that earn a two-pane layout — tag exactly two
   (or three, with `extraPane()`) `entry<>()` calls per flow with its metadata; everything else
   stays single-pane with zero extra code.
7. Add a `SceneDecoratorStrategy` (§9) only for chrome that should wrap *every* screen uniformly
   (a persistent rail/bottom bar, a global banner, etc.) — remember any state it needs to survive
   navigation *above* `NavDisplay`, and keep its content limited to data that doesn't need its own
   entry-scoped ViewModel.
8. Host it all from one Activity/Composable (§10), seeding `initialKeys` from whatever
   deep-link/notification/shortcut resolution your app needs.

Everything above is independent of this project's specific screens — it's a general recipe for
"single shared Nav3 backstack + per-feature entry registration + scene strategies/decorators for
adaptive chrome" that scales to any number of feature modules.
