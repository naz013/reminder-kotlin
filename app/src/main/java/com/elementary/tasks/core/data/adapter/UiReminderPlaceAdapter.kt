package com.elementary.tasks.core.data.adapter

import com.github.naz013.domain.Place
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace

class UiReminderPlaceAdapter : UiAdapter<Place, UiReminderPlace> {

  override fun create(data: Place): UiReminderPlace {
    return UiReminderPlace(
      marker = data.marker,
      latitude = data.latitude,
      longitude = data.longitude,
      radius = data.radius,
      address = data.address
    )
  }
}
