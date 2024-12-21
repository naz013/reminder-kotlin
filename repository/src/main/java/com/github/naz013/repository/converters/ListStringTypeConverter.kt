package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class ListStringTypeConverter {

  @TypeConverter
  fun toJson(list: List<String>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<String> {
    if (json.isEmpty()) {
      return emptyList()
    }
    return runCatching {
      Gson().fromJson<List<String>>(json, object : TypeToken<List<String>>() {}.type)
    }.getOrNull() ?: emptyList()
  }
}
