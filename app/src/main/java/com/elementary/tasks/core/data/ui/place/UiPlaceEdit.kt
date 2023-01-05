package com.elementary.tasks.core.data.ui.place

data class UiPlaceEdit(
  val id: String,
  val marker: Int,
  val name: String,
  val lat: Double,
  val lng: Double,
  val radius: Int
)
