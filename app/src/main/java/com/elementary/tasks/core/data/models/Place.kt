package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.elementary.tasks.core.data.converters.ListStringTypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@TypeConverters(ListStringTypeConverter::class)
@Keep
@Parcelize
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
  var dateTime: String = "",
  @SerializedName("tags")
  var tags: List<String> = listOf()
) : Parcelable {

  fun latLng(): LatLng = LatLng(latitude, longitude)

  fun hasLatLng(): Boolean = latitude != 0.0 && longitude != 0.0
}
