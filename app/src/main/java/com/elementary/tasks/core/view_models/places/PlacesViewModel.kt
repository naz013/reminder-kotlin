package com.elementary.tasks.core.view_models.places

class PlacesViewModel : BasePlacesViewModel() {
    val places = appDb.placesDao().loadAll()
}
