package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.repository.entity.PlaceEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class PlacesTypeConverter {

  @TypeConverter
  fun toJson(list: List<PlaceEntity>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<PlaceEntity>? {
    if (json.isEmpty()) {
      return null
    }
    return runCatching {
      Gson().fromJson<List<PlaceEntity>>(json, object : TypeToken<List<PlaceEntity>>() {}.type)
    }.getOrNull()
  }
}
