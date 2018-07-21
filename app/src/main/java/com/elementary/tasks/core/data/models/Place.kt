package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.elementary.tasks.core.data.converters.ListStringTypeConverter
import com.elementary.tasks.core.utils.SuperUtil
import com.google.gson.annotations.SerializedName
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
@TypeConverters(
        ListStringTypeConverter::class
)
class Place {

    @SerializedName("radius")
    var radius: Int = 0
    @SerializedName("marker")
    var marker: Int = 0
    @SerializedName("latitude")
    var latitude: Double = 0.toDouble()
    @SerializedName("longitude")
    var longitude: Double = 0.toDouble()
    @SerializedName("name")
    var name: String = ""
    @SerializedName("id")
    @PrimaryKey
    var id: String = ""
    @SerializedName("address")
    var address: String = ""
    @SerializedName("tags")
    var tags: List<String> = listOf()

    constructor()

    @Ignore
    constructor(radius: Int, marker: Int, latitude: Double, longitude: Double, name: String, address: String, tags: List<String>) {
        this.radius = radius
        this.marker = marker
        this.latitude = latitude
        this.longitude = longitude
        this.name = name
        this.id = UUID.randomUUID().toString()
        this.address = address
        this.tags = tags
    }

    override fun toString(): String {
        return SuperUtil.getObjectPrint(this, Place::class.java)
    }
}