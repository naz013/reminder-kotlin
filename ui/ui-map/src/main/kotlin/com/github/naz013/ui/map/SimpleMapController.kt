package com.github.naz013.ui.map

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng

/**
 * Handed to callers via `SimpleMapView`'s `onControllerReady` callback - the only way to drive the
 * map imperatively from outside, since `SimpleMapView` owns its `MapViewModel` internally and
 * never exposes it.
 */
@Stable
class SimpleMapController internal constructor(
  private val viewModel: SimpleMapViewViewModel,
) {
  fun moveCamera(latLng: LatLng) = viewModel.moveCamera(latLng)

  /** Returns `true` if the caller should proceed with its own back-navigation. */
  fun onBackPressed(): Boolean = viewModel.onBackPressed()
}
