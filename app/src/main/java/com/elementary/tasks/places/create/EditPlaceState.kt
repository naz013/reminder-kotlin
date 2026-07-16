package com.elementary.tasks.places.create

import com.elementary.tasks.simplemap.MapMarker
import com.google.android.gms.maps.model.LatLng

data class EditPlaceState(
  val name: String = "",
  val nameError: Boolean = false,
  val canDelete: Boolean = false,
  val markers: List<EditMarker> = emptyList(),
)

data class EditMarker(
  override val latLng: LatLng,
  override val style: Int,
  override val radius: Int,
  override val title: String
) : MapMarker

sealed interface EditPlaceEvent {
  data object Saved : EditPlaceEvent

  data object Deleted : EditPlaceEvent

  data object NoLocationSelected : EditPlaceEvent

  data object ConfirmDelete : EditPlaceEvent

  data object AskCopySaving : EditPlaceEvent
}
