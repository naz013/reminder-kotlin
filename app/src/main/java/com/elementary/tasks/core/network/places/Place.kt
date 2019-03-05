package com.elementary.tasks.core.network.places

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Copyright 2017 Nazar Suhovich
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

class Place {

    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null
    @SerializedName("icon")
    @Expose
    var icon: String? = ""
    @SerializedName("id")
    @Expose
    var id: String = ""
    @SerializedName("name")
    @Expose
    var name: String? = ""
    @SerializedName("photos")
    @Expose
    var photos: List<Photo> = listOf()
    @SerializedName("place_id")
    @Expose
    var placeId: String = ""
    @SerializedName("scope")
    @Expose
    var scope: String = ""
    @SerializedName("reference")
    @Expose
    var reference: String = ""
    @SerializedName("types")
    @Expose
    var types: List<String> = listOf()
    @SerializedName("formatted_address")
    @Expose
    var formattedAddress: String? = ""
}
