package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.converters.ListStringTypeConverter
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "Place")
@TypeConverters(ListStringTypeConverter::class)
@Keep
internal data class PlaceEntity(
  @SerializedName("radius")
  val radius: Int = 0,
  @SerializedName("marker")
  val marker: Int = 0,
  @SerializedName("latitude")
  val latitude: Double = 0.0,
  @SerializedName("longitude")
  val longitude: Double = 0.0,
  @SerializedName("name")
  val name: String = "",
  @SerializedName("id")
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("address")
  val address: String = "",
  @SerializedName("dateTime")
  val dateTime: String = "",
  @SerializedName("tags")
  val tags: List<String> = listOf(),
  @SerializedName("version")
  val version: Long = 0L,
  @SerializedName("syncState")
  val syncState: String = SyncState.WaitingForUpload.name
) {

  constructor(place: Place) : this(
    radius = place.radius,
    marker = place.marker,
    latitude = place.latitude,
    longitude = place.longitude,
    name = place.name,
    id = place.id,
    address = place.address,
    dateTime = place.dateTime,
    tags = place.tags,
    version = place.version,
    syncState = place.syncState.name
  )

  fun toDomain(): Place = Place(
    radius = radius,
    marker = marker,
    latitude = latitude,
    longitude = longitude,
    name = name,
    id = id,
    address = address,
    dateTime = dateTime,
    tags = tags,
    version = version,
    syncState = SyncState.valueOf(syncState)
  )
}
