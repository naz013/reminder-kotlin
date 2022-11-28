package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.ui.UiReminderPlace

class UiReminderPlaceAdapter : UiAdapter<Place, UiReminderPlace> {

  override fun create(data: Place): UiReminderPlace {
    return UiReminderPlace(
      marker = data.marker,
      latitude = data.latitude,
      longitude = data.longitude,
      radius = data.radius
    )
  }
}