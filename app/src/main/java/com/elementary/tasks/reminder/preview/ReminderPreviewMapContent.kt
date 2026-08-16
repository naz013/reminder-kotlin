package com.elementary.tasks.reminder.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.valuedialog.editor.ReminderMapMarker
import com.elementary.tasks.simplemap.MapCustomButton
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.SimpleMapController
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.preview.FullScreenMapViewModel
import com.github.naz013.feature.reminder.preview.ReminderFullscreenMapScreen
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.ui.reminder.UiReminderPlace
import com.google.android.gms.maps.model.LatLng
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The `embeddedMapContent`/`fullscreenMapEntryContent` slots threaded into
 * [com.github.naz013.feature.reminder.preview.reminderPreviewEntries] - `feature-reminder` can't
 * depend on `simplemap` (shared with the separate `places` feature), so the actual embedded map
 * rendering stays here, matching how `MapEditorScreen`/`MapValueEditor` stayed in `app` for the
 * reminder builder.
 */
@Composable
fun EmbeddedMap(
  places: List<UiReminderPlace>,
  onMapClick: () -> Unit,
) {
  SimpleMapView(
    mapParams = MapParams(
      isTouch = false,
      isSearch = false,
      isRadius = false,
      isPlaces = false,
      isStyles = false,
      isLayers = false,
    ),
    markers = places.map {
      ReminderMapMarker(
        latLng = it.latLng(),
        style = it.marker,
        radius = it.radius,
        title = it.address
      )
    },
    onMapClick = onMapClick,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun FullscreenEmbeddedMap(
  reminder: ReminderV2,
  onBackClick: () -> Unit,
  onControllerReady: (SimpleMapController) -> Unit,
) {
  SimpleMapView(
    mapParams = MapParams(
      isPlaces = false,
      isStyles = false,
      isRadius = false,
      isSearch = false,
      isTouch = false,
      customButtons = listOf(MapCustomButton(R.drawable.ic_builder_arrow_left, id = 0)),
    ),
    markers = reminder.places.map { place ->
      ReminderMapMarker(
        latLng = LatLng(place.latitude, place.longitude),
        style = place.marker,
        radius = place.radius,
        title = place.name.takeIf { it.isNotEmpty() }
          ?: place.address.takeIf { it.isNotEmpty() }
          ?: reminder.summary,
      )
    },
    onCustomButtonClick = { id -> if (id == 0) onBackClick() },
    onControllerReady = onControllerReady,
    edgeToEdge = true,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
fun FullscreenMapEntry(
  key: ReminderPreviewNavKey.FullscreenMap,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<FullScreenMapViewModel> { parametersOf(key.id) }
  var mapController by remember { mutableStateOf<SimpleMapController?>(null) }

  BackHandler {
    if (mapController?.onBackPressed() != false && backStack.size > 1) backStack.removeLastOrNull()
  }

  val reminder by viewModel.reminder.collectAsState()
  ReminderFullscreenMapScreen(
    isLoading = reminder == null,
    onMoveToPlaceClick = {
      val currentReminder = viewModel.reminder.value ?: return@ReminderFullscreenMapScreen
      if (currentReminder.places.isEmpty()) return@ReminderFullscreenMapScreen
      viewModel.placeIndex =
        if (viewModel.placeIndex < currentReminder.places.size - 1) viewModel.placeIndex + 1 else 0
      val place = currentReminder.places[viewModel.placeIndex]
      mapController?.moveCamera(LatLng(place.latitude, place.longitude))
    },
    mapContent = {
      reminder?.let {
        FullscreenEmbeddedMap(
          reminder = it,
          onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
          onControllerReady = { mapController = it },
        )
      }
    },
  )
}
