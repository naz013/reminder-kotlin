package com.github.naz013.ui.common.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.github.naz013.ui.common.compose.findActivity

/** Fold posture of the current window, independent of the width/height size buckets in
 *  [DeviceScreenConfiguration]. A device can be [DeviceScreenConfiguration.TabletLandscape] and
 *  still have a hinge splitting that space in two - layouts that care should check both. */
enum class FoldPosture {
  /** No hinge reported - a non-foldable device, or a foldable that's fully open or closed. */
  Flat,

  /** Half-opened with a vertical hinge (book-like) - content either side of the hinge sits
   *  side-by-side. */
  Book,

  /** Half-opened with a horizontal hinge (tabletop-like) - content either side of the hinge
   *  sits top/bottom. */
  TableTop,
}

/** Combines the existing width/height based [DeviceScreenConfiguration] with fold posture, so
 *  adaptive layouts can react to both "how much space do I have" and "is content straddling a
 *  hinge" from a single state object instead of querying two unrelated systems. */
data class AppWindowState(
  val screenConfiguration: DeviceScreenConfiguration,
  val foldPosture: FoldPosture,
)

@Composable
fun rememberAppWindowState(): AppWindowState {
  val screenConfiguration = deviceScreenConfiguration()
  val foldPosture = rememberFoldPosture()
  return remember(screenConfiguration, foldPosture) {
    AppWindowState(screenConfiguration, foldPosture)
  }
}

@Composable
fun rememberFoldPosture(): FoldPosture {
  val activity = LocalContext.current.findActivity() ?: return FoldPosture.Flat
  val windowInfoTracker = remember(activity) { WindowInfoTracker.getOrCreate(activity) }
  val layoutInfo by remember(windowInfoTracker, activity) {
    windowInfoTracker.windowLayoutInfo(activity)
  }.collectAsState(initial = null)

  return layoutInfo.toFoldPosture()
}

private fun WindowLayoutInfo?.toFoldPosture(): FoldPosture {
  val foldingFeature = this?.displayFeatures?.filterIsInstance<FoldingFeature>()?.firstOrNull()
  return when {
    foldingFeature == null -> FoldPosture.Flat
    foldingFeature.state != FoldingFeature.State.HALF_OPENED -> FoldPosture.Flat
    foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL -> FoldPosture.Book
    foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL -> FoldPosture.TableTop
    else -> FoldPosture.Flat
  }
}
