package com.github.naz013.domain

import com.github.naz013.domain.place.LatLng
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.UUID

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
  var id: String = UUID.randomUUID().toString(),
  @SerializedName("address")
  var address: String = "",
  @SerializedName("dateTime")
  var dateTime: String = "",
  @SerializedName("tags")
  var tags: List<String> = listOf()
) : Serializable {

  fun latLng(): LatLng = LatLng(latitude, longitude)
}
