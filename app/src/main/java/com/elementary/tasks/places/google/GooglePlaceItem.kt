package com.elementary.tasks.places.google

import com.google.android.gms.maps.model.LatLng

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
