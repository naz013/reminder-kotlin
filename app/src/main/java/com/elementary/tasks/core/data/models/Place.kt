package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
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

@Entity
@TypeConverters(ListStringTypeConverter::class)
@Keep
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