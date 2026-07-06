package com.elementary.tasks.places.create

data class EditPlaceState(
  val name: String = "",
  val nameError: Boolean = false,
  val canDelete: Boolean = false,
)

sealed interface EditPlaceEvent {
  data object Saved : EditPlaceEvent

  data object Deleted : EditPlaceEvent

  data object NoLocationSelected : EditPlaceEvent

  data object ConfirmDelete : EditPlaceEvent

  data object AskCopySaving : EditPlaceEvent
}
