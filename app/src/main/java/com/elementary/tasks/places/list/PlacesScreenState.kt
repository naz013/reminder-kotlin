package com.elementary.tasks.places.list

import com.elementary.tasks.core.data.ui.place.UiPlaceList

data class PlacesScreenState(
  val listState: ListState = ListState.Loading,
  val searchQuery: String = "",
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(val places: List<UiPlaceList>) : ListState

  data object Empty : ListState
}

enum class PlaceMenuAction {
  EDIT,
  SHARE,
  DELETE,
}
