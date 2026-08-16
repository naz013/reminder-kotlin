package com.github.naz013.feature.reminder

import com.github.naz013.ui.reminder.UiReminderPlace
import com.github.naz013.domain.Place

class UiReminderPlaceAdapter : UiAdapter<Place, UiReminderPlace> {
  override fun create(data: Place): UiReminderPlace =
    UiReminderPlace(
      marker = data.marker,
      latitude = data.latitude,
      longitude = data.longitude,
      radius = data.radius,
      address = data.address,
    )
}
