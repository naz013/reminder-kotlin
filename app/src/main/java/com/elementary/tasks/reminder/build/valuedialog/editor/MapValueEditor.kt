package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.naz013.feature.reminder.build.BuilderItem
import com.elementary.tasks.simplemap.MapMarker
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.google.android.gms.maps.model.LatLng

data class ReminderMapMarker(
  override val latLng: LatLng,
  override val radius: Int,
  override val style: Int,
  override val title: String
) : MapMarker

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
  val markers = remember(builderItem) {
    builderItem.modifier.getValue()?.let { place ->
      listOf(
        ReminderMapMarker(
          latLng = LatLng(place.latitude, place.longitude),
          style = place.marker,
          radius = place.radius,
          title = place.name,
        ),
      )
    } ?: emptyList()
  }

  SimpleMapView(
    mapParams = MapParams(),
    markers = markers,
    onLocationSelected = { markerState ->
      val current = builderItem.modifier.getValue() ?: Place(syncState = SyncState.WaitingForUpload)
      val updated = current.copy(
        latitude = markerState.latLng.latitude,
        longitude = markerState.latLng.longitude,
        radius = markerState.radius,
        marker = markerState.styleIndex,
        address = markerState.address,
        name = markerState.title,
        dateTime = dateTimeManager.getNowGmtDateTime(),
      )
      builderItem.modifier.update(updated)
      onValueChange(builderItem)
    },
    modifier = modifier,
  )
}
