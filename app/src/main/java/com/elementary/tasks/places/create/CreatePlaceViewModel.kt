package com.elementary.tasks.places.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Place

class CreatePlaceViewModel : ViewModel(), LifecycleObserver {

    var place: Place = Place()
    var isPlaceEdited = false

}