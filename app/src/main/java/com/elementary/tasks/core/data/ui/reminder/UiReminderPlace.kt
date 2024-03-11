package com.elementary.tasks.core.data.ui.reminder

import com.google.android.gms.maps.model.LatLng

data class UiReminderPlace(
  val marker: Int,
  val latitude: Double,
  val longitude: Double,
  val radius: Int,
  val address: String
) {
  fun latLng(): LatLng = LatLng(latitude, longitude)
}
