package com.elementary.tasks.navigation.nav3

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.github.naz013.ui.common.compose.foundation.isDesktopScreen
import com.github.naz013.ui.common.compose.foundation.isTabletScreen
import com.github.naz013.ui.common.compose.foundation.navigation.AppDestination
import com.github.naz013.ui.common.compose.foundation.navigation.AppNavigationScaffold

/**
 * Wraps every [Scene] in [AppNavigationScaffold] on Medium+ width - a persistent navigation rail
 * shown regardless of which screen is open, not just Home (see docs/adaptive-layouts.md). On
 * Compact width every scene is returned unchanged.
 *
 * [destinations] are the app's top-level sections (Home, Calendar, Notes, ...); each is a plain
 * [NavKey] rather than something dynamic, so clicking one behaves like switching a tab: if that
 * key is already somewhere on [backStack] it pops back to it, otherwise it's pushed. The selected
 * item is derived the same way - the *deepest* entry on [backStack] that matches one of
 * [destinations], so drilling into e.g. a note preview keeps "Notes" highlighted rather than
 * losing selection. [HomeNavKey.Main][com.elementary.tasks.home.HomeNavKey] is always present at
 * the bottom of [backStack] (the graph's start destination), so there is always a fallback match.
 *
 * This decorator is applied outside the Nav3 entry-scoped `ViewModelStoreOwner`
 * ([androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator] wraps each
 * entry's own content, not the chrome a [SceneDecoratorStrategy] adds around it), so
 * [destinations] can't safely carry per-screen ViewModel data like live badge counts - only
 * NavKey-based navigation is safe here.
 *
 * [railState] must be `remember`ed once above `NavDisplay` (see `AppNavGraph.kt`), not created
 * fresh per scene: `NavDisplay` disposes and recreates each scene's composition on navigation, so
 * a rail state `remember`ed inside the decorated content would reset to collapsed on every
 * navigation instead of persisting the user's expanded/collapsed choice.
 */
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
  // Derived from the wrapped scene's own key (class + key) so scene identity - and NavDisplay's
  // transition animations - stay stable across recompositions of this decorator.
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
        modifier = Modifier.fillMaxSize(),
        content = scene.content,
      )
    } else {
      scene.content()
    }
  }
}
