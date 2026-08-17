package com.github.naz013.feature.places.list

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class PlacesScreenState(
  val listState: ListState = ListState.Loading,
  val searchQuery: String = "",
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val places: List<PlaceState>,
  ) : ListState

  data object Empty : ListState
}

data class PlaceState(
  val id: String,
  val markerColor: Color,
  val name: String,
  val latLng: LatLng,
  val formattedDate: String?,
)

enum class PlaceMenuAction {
  EDIT,
  SHARE,
  DELETE,
}
