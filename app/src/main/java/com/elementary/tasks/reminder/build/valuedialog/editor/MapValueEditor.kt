package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.simplemap.MapCallerEvent
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.MapViewModel
import com.elementary.tasks.simplemap.MarkerState
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.google.android.gms.maps.model.LatLng
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

/** Location picker for the Arriving/Leaving coordinates editor. */
@Composable
fun MapValueEditor(
  builderItem: BuilderItem<Place>,
  dateTimeManager: DateTimeManager,
  onValueChange: (BuilderItem<*>) -> Unit,
  modifier: Modifier = Modifier
    .fillMaxWidth()
    .aspectRatio(1f),
) {
  // Arriving/Leaving each reuse this same composable, and the sheet fully leaves composition
  // between opens (see BuildReminderNavGraph's `editingItem?.let { ... }`) - a fresh key here
  // means each open gets its own MapViewModel instance rather than Koin's viewmodel-scoping
  // silently reusing whichever one was first created for this NavEntry.
  val mapViewModelKey = remember { UUID.randomUUID().toString() }
  val mapViewModel = koinViewModel<MapViewModel>(key = mapViewModelKey) { parametersOf(MapParams()) }

  mapViewModel.callerEvent.ObserveEvent { event ->
    if (event is MapCallerEvent.LocationSelected) {
      val markerState = event.markerState
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

  val markers = remember(builderItem) {
    builderItem.modifier.getValue()?.let { place ->
      listOf(
        MarkerState(
          latLng = LatLng(place.latitude, place.longitude),
          style = place.marker,
          radius = place.radius,
          title = place.name,
        ),
      )
    } ?: emptyList()
  }

  SimpleMapView(
    viewModel = mapViewModel,
    markers = markers,
    modifier = modifier,
  )
}
