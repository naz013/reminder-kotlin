package com.elementary.tasks.places.create

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.datetime.DateTimeManager

class CreatePlaceViewModel(
  dateTimeManager: DateTimeManager
) : ViewModel(), DefaultLifecycleObserver {
  var place: Place = Place(
    dateTime = dateTimeManager.getNowGmtDateTime()
  )
  var isPlaceEdited = false
  var isFromFile: Boolean = false
  var isLogged = false
}
