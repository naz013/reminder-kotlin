package com.elementary.tasks.navigation.nav3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.github.naz013.ui.common.compose.foundation.navigation.SidePanelMaxWidth
import com.github.naz013.ui.common.compose.foundation.navigation.SidePanelSurface
import com.github.naz013.ui.common.compose.foundation.navigation.isSidePanelHost
import com.github.naz013.ui.common.compose.foundation.navigation.isSidePanelSupporting

private const val SidePanelAnimationDurationMillis = 300

/**
 * A minimal, hand-rolled [SceneStrategy] for the Calendar side-sheet flow, built as an
 * [OverlayScene] rather than a regular [Scene] (and deliberately not built on
 * [androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy] - see git history
 * for why that library strategy doesn't work for this: it funnels through
 * `calculateThreePaneScaffoldValue`, whose priority-fill algorithm phantom-expands roles that have
 * no backing entry).
 *
 * Rendering the host (Calendar) entry is deliberately left to whatever [Scene] the rest of the
 * [SceneStrategy] chain computes for [OverlayScene.overlaidEntries] - `SceneState`'s calculation
 * (`rememberSceneState` in navigation3-ui) treats the *last non-overlay* scene as the "current"
 * scene driving `NavDisplay`'s `AnimatedContent`/`SeekableTransitionState`, while every
 * [OverlayScene] on top is rendered through a separate code path with no involvement in that
 * transition at all. Concretely: Calendar's own scene is calculated identically (same key) whether
 * or not this side panel is open, so opening/closing the panel never triggers a scene-level
 * crossfade of Calendar - only the panel's own self-driven [progress] animation runs. Earlier
 * versions of this strategy rendered the host entry directly inside a plain (non-overlay) [Scene],
 * which meant the whole screen (host + panel) participated in `NavDisplay`'s default
 * crossfade-on-scene-change transition, causing a visible flash on both open and close.
 */
class SidePanelSceneStrategy(
  private val isMediumOrWiderWidth: () -> Boolean,
) : SceneStrategy<NavKey> {
  override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
    if (!isMediumOrWiderWidth()) return null
    val panelEntry = entries.lastOrNull() ?: return null
    if (!isSidePanelSupporting(panelEntry.metadata)) return null
    val hostEntry = entries.getOrNull(entries.lastIndex - 1) ?: return null
    if (!isSidePanelHost(hostEntry.metadata)) return null
    return SidePanelScene(
      panelEntry = panelEntry,
      previousEntries = entries.dropLast(1),
      overlaidEntries = entries.dropLast(1),
      onBack = onBack,
    )
  }
}

private class SidePanelScene(
  private val panelEntry: NavEntry<NavKey>,
  override val previousEntries: List<NavEntry<NavKey>>,
  override val overlaidEntries: List<NavEntry<NavKey>>,
  private val onBack: () -> Unit,
) : OverlayScene<NavKey> {
  override val key: Any = SidePanelScene::class to panelEntry.contentKey
  override val entries: List<NavEntry<NavKey>> = listOf(panelEntry)

  // Drives the panel's own slide+fade in/out - not part of NavDisplay's scene transition, since
  // this Scene is an OverlayScene. 0f = fully hidden (off-screen, transparent scrim), 1f = fully
  // shown. Retained across recompositions: `NavDisplay` only adds a newly-calculated OverlayScene
  // instance to its rendered set once per distinct `key`, so this same instance (and its live
  // animation state) keeps being reused for as long as the panel stays open.
  private val progress = Animatable(0f)

  override val content: @Composable () -> Unit = {
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(SidePanelAnimationDurationMillis)) }
    val panelWidthPx = with(LocalDensity.current) { SidePanelMaxWidth.toPx() }
    Box(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer { alpha = progress.value }
          .background(Color.Black.copy(alpha = 0.32f))
          .clearAndSetSemantics {}
          .clickable(interactionSource = null, indication = null, onClick = onBack),
      )
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        SidePanelSurface(
          modifier = Modifier.graphicsLayer {
            translationX = (1f - progress.value) * panelWidthPx
            alpha = progress.value
          },
        ) {
          panelEntry.Content()
        }
      }
    }
  }

  // Runs the exit (slide+fade out) animation before this Scene leaves composition - see
  // `OverlayScene.onRemove`'s contract in navigation3-ui.
  override suspend fun onRemove() {
    progress.animateTo(0f, tween(SidePanelAnimationDurationMillis))
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as SidePanelScene
    return key == other.key &&
      previousEntries == other.previousEntries &&
      overlaidEntries == other.overlaidEntries &&
      entries == other.entries
  }

  override fun hashCode(): Int {
    return key.hashCode() * 31 +
      previousEntries.hashCode() * 31 +
      overlaidEntries.hashCode() * 31 +
      entries.hashCode() * 31
  }
}
