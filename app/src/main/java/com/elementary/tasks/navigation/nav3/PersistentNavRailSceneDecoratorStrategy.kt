package com.elementary.tasks.navigation.nav3

import androidx.compose.foundation.layout.fillMaxSize
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
 * Marks an `entry<>()` as eligible for the persistent nav rail applied by
 * [PersistentNavRailSceneDecoratorStrategy] - see docs/adaptive-layouts.md.
 */
object PersistentNavRail {
  private const val METADATA_KEY = "persistentNavRail"

  fun metadata(): Map<String, Any> = mapOf(METADATA_KEY to true)

  fun isEligible(scene: Scene<NavKey>): Boolean =
    scene.entries.lastOrNull()?.metadata?.get(METADATA_KEY) == true
}

/**
 * Wraps a [Scene] whose top entry carries [PersistentNavRail.metadata] in
 * [AppNavigationScaffold] on Medium+ width, so that destination gets a persistent navigation
 * rail instead of whatever compact-width navigation UI it renders itself (e.g. Home's header
 * grid). On Compact width the scene is returned unchanged.
 *
 * [destinations] are static (icon/label only) and navigate via [onNavigate] directly - this
 * decorator is applied outside the Nav3 entry-scoped `ViewModelStoreOwner`
 * ([androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator] wraps each
 * entry's own content, not the chrome a [SceneDecoratorStrategy] adds around it), so it can't
 * safely reach into a screen's ViewModel for live data like badge counts - only NavKey-based
 * navigation is safe here.
 */
class PersistentNavRailSceneDecoratorStrategy(
  private val destinations: List<AppDestination<NavKey>>,
  private val onNavigate: (NavKey) -> Unit,
) : SceneDecoratorStrategy<NavKey> {
  override fun SceneDecoratorStrategyScope<NavKey>.decorateScene(scene: Scene<NavKey>): Scene<NavKey> {
    if (!PersistentNavRail.isEligible(scene)) return scene
    return NavRailDecoratedScene(scene, destinations, onNavigate)
  }
}

private class NavRailDecoratedScene(
  private val scene: Scene<NavKey>,
  private val destinations: List<AppDestination<NavKey>>,
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
        selectedKey = null,
        onDestinationSelected = onNavigate,
        modifier = Modifier.fillMaxSize(),
        content = scene.content,
      )
    } else {
      scene.content()
    }
  }
}
