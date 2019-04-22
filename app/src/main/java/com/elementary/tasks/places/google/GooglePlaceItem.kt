package com.elementary.tasks.places.google

import com.google.android.gms.maps.model.LatLng

class GooglePlaceItem {
    var name: String = ""
    var id: String = ""
    var icon: String = ""
    var address: String = ""
    var position: LatLng? = null
    var isSelected: Boolean = false
    var types: List<String> = listOf()
    val latitude: Double
        get() {
            return position?.latitude ?: 0.0
        }
    val longitude: Double
        get() {
            return position?.longitude ?: 0.0
        }

    constructor() {
        isSelected = false
    }

    constructor(name: String, id: String, icon: String, address: String,
                position: LatLng?, types: List<String>, selected: Boolean) {
        this.name = name
        this.id = id
        this.icon = icon
        this.address = address
        this.position = position
        this.types = types
        this.isSelected = selected
    }
}
