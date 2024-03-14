package com.elementary.tasks.core.data.converters

import androidx.room.TypeConverter
import com.elementary.tasks.core.data.models.BuilderSchemeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BuilderSchemeItemsTypeConverter {

  @TypeConverter
  fun toJson(list: List<BuilderSchemeItem>?): String? {
    if (list.isNullOrEmpty()) {
      return null
    }
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String?): List<BuilderSchemeItem>? {
    if (json == null) return null
    return runCatching {
      Gson().fromJson<List<BuilderSchemeItem>>(
        json,
        object : TypeToken<List<BuilderSchemeItem>>() {}.type
      )
    }.getOrNull() ?: null
  }
}
