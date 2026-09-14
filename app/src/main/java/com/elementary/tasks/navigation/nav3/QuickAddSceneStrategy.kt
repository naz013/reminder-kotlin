package com.elementary.tasks.navigation.nav3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.github.naz013.ui.common.compose.foundation.navigation.isQuickAddOverlay

/**
 * Renders quick-add's sheet entry (tagged [com.github.naz013.ui.common.compose.foundation.navigation.quickAddOverlay]
 * in `QuickAddNavGraph.kt`) as an overlay on top of whatever entry is already on the backstack
 * (Home, in practice), instead of replacing it the way a plain push does. Quick add is a
 * lightweight capture surface meant to float over the screen it was opened from, not a destination
 * in its own right - without this, [androidx.navigation3.ui.NavDisplay]'s default [Scene] disposes
 * the previous entry, leaving the sheet's own scrim rendered over a flat background instead of the
 * actual screen behind it.
 *
 * Modeled on [SidePanelSceneStrategy], but simpler: `AppModalBottomSheet` (inside
 * `QuickAddScreen`) already supplies its own scrim/Popup/show-hide animation via Material3's
 * `ModalBottomSheet`, so this strategy only needs to keep the previous entry composed underneath -
 * it needs no progress animation of its own. `NavEntry.key` isn't public, so - like
 * `SidePanelSceneStrategy` - this matches via metadata rather than checking the entry's key type.
 */
class QuickAddSceneStrategy : SceneStrategy<NavKey> {
  override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
    val sheetEntry = entries.lastOrNull() ?: return null
    if (!isQuickAddOverlay(sheetEntry.metadata)) return null
    return QuickAddOverlayScene(sheetEntry = sheetEntry, previousEntries = entries.dropLast(1))
  }
}

private class QuickAddOverlayScene(
  private val sheetEntry: NavEntry<NavKey>,
  override val previousEntries: List<NavEntry<NavKey>>,
) : OverlayScene<NavKey> {
  override val key: Any = QuickAddOverlayScene::class to sheetEntry.contentKey
  override val entries: List<NavEntry<NavKey>> = listOf(sheetEntry)
  override val overlaidEntries: List<NavEntry<NavKey>> = previousEntries
  override val content: @Composable () -> Unit = { sheetEntry.Content() }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as QuickAddOverlayScene
    return key == other.key && previousEntries == other.previousEntries && entries == other.entries
  }

  override fun hashCode(): Int =
    key.hashCode() * 31 + previousEntries.hashCode() * 31 + entries.hashCode()
}
