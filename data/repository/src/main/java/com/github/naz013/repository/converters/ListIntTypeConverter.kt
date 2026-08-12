package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class ListIntTypeConverter {

  @TypeConverter
  fun toJson(list: List<Int>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String?): List<Int> {
    if (json == null) {
      return emptyList()
    }
    return runCatching {
      Gson().fromJson<List<Int>>(json, object : TypeToken<List<Int>>() {}.type)
    }.getOrNull() ?: emptyList()
  }
}
