package com.elementary.tasks.core.data.converters

import androidx.room.TypeConverter
import com.elementary.tasks.core.data.models.PresetBuilderScheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PresetBuilderSchemeTypeConverter {

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
