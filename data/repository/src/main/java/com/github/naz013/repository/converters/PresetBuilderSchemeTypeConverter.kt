package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.domain.PresetBuilderScheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class PresetBuilderSchemeTypeConverter {

  @TypeConverter
  fun toJson(list: List<PresetBuilderScheme>): String {
    if (list.isEmpty()) {
      return ""
    }
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<PresetBuilderScheme> {
    if (json.isEmpty()) {
      return emptyList()
    }
    return runCatching {
      Gson().fromJson<List<PresetBuilderScheme>>(
        json,
        object : TypeToken<List<PresetBuilderScheme>>() {}.type
      )
    }.getOrNull() ?: emptyList()
  }
}
