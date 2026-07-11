package com.elementary.tasks.reminder.build.valuedialog.editor

import android.view.View
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState

/**
 * Location picker: hosts the existing `SimpleMapFragment` (Google Maps SDK, radius circle,
 * geocoding, custom buttons - all unchanged) inside an `AndroidView`-wrapped
 * `FragmentContainerView`, rather than rewriting the 800+ line map fragment itself. Replaces
 * `MapController`.
 *
 * The fragment transaction runs from [AndroidView]'s `update` block (not a plain
 * `DisposableEffect`) since that's the point at which the container view is guaranteed to already
 * be attached to the composition's view hierarchy - `childFragmentManager` needs to resolve the
 * container by id, which fails if attempted too early. A `remember`-backed flag keeps the
 * transaction to a one-time "attach" per time this editor enters composition.
 */
@Composable
fun MapValueEditor(
  builderItem: BuilderItem<Place>,
  parentFragment: Fragment,
  dateTimeManager: DateTimeManager,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val containerId = remember(builderItem) { View.generateViewId() }
  var fragmentAttached by remember(builderItem) { mutableStateOf(false) }

  AndroidView(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f),
    factory = { context -> FragmentContainerView(context).apply { id = containerId } },
    update = {
      if (fragmentAttached) return@AndroidView
      fragmentAttached = true

      val simpleMapFragment = SimpleMapFragment.newInstance(SimpleMapFragment.MapParams())
      simpleMapFragment.mapCallback = object : SimpleMapFragment.MapCallback {
        override fun onMapReady() {
          builderItem.modifier.getValue()?.also { place ->
            simpleMapFragment.addMarker(
              latLng = place.latLng(),
              title = place.name,
              markerStyle = place.marker,
              radius = place.radius,
              clear = true,
              animate = true,
            )
          }
        }

        override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
          val current = builderItem.modifier.getValue() ?: Place(syncState = SyncState.WaitingForUpload)
          val updated = current.copy(
            latitude = markerState.latLng.latitude,
            longitude = markerState.latLng.longitude,
            radius = markerState.radius,
            marker = markerState.style,
            address = markerState.address,
            name = markerState.title,
            dateTime = dateTimeManager.getNowGmtDateTime(),
          )
          builderItem.modifier.update(updated)
          onValueChange(builderItem)
        }
      }

      parentFragment.childFragmentManager
        .beginTransaction()
        .replace(containerId, simpleMapFragment)
        .commitNowAllowingStateLoss()
    },
  )

  DisposableEffect(builderItem) {
    onDispose {
      val fragmentManager = parentFragment.childFragmentManager
      if (!fragmentManager.isDestroyed) {
        fragmentManager.findFragmentById(containerId)?.also { existing ->
          fragmentManager.beginTransaction().remove(existing).commitNowAllowingStateLoss()
        }
      }
    }
  }
}
