package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.elementary.tasks.core.data.converters.ListStringTypeConverter
import com.elementary.tasks.core.utils.TimeUtil
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

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
@Entity
@TypeConverters(ListStringTypeConverter::class)
data class Place(
        @SerializedName("radius")
        var radius: Int = 0,
        @SerializedName("marker")
        var marker: Int = 0,
        @SerializedName("latitude")
        var latitude: Double = 0.0,
        @SerializedName("longitude")
        var longitude: Double = 0.0,
        @SerializedName("name")
        var name: String = "",
        @SerializedName("id")
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),
        @SerializedName("address")
        var address: String = "",
        @SerializedName("dateTime")
        var dateTime: String = TimeUtil.gmtDateTime,
        @SerializedName("tags")
        var tags: List<String> = listOf()
) : Serializable {

    @Ignore
    constructor(radius: Int, marker: Int, latitude: Double, longitude: Double, name: String, address: String, tags: List<String>): this() {
        this.radius = radius
        this.marker = marker
        this.latitude = latitude
        this.longitude = longitude
        this.name = name
        this.id = UUID.randomUUID().toString()
        this.address = address
        this.tags = tags
    }

    fun latLng(): LatLng = LatLng(latitude, longitude)

    fun hasLatLng(): Boolean = latitude != 0.0 && longitude != 0.0
}