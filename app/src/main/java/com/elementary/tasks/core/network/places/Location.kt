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

class Location {

    @SerializedName("lat")
    @Expose
    var lat: Double = 0.toDouble()
    @SerializedName("lng")
    @Expose
    var lng: Double = 0.toDouble()
}
