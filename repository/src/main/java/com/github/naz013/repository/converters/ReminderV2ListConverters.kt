package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.domain.reminder.v2.BuilderSchemeItemV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class ReminderV2ShopItemsConverter {

  @TypeConverter
  fun toJson(list: List<ShopItemV2>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<ShopItemV2> {
    if (json.isEmpty()) {
      return emptyList()
    }
    return runCatching {
      Gson().fromJson<List<ShopItemV2>>(json, object : TypeToken<List<ShopItemV2>>() {}.type)
    }.getOrNull() ?: emptyList()
  }
}

internal class ReminderV2BuilderSchemeConverter {

  @TypeConverter
  fun toJson(list: List<BuilderSchemeItemV2>?): String? {
    if (list.isNullOrEmpty()) {
      return null
    }
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String?): List<BuilderSchemeItemV2>? {
    if (json == null) return null
    return runCatching {
      Gson().fromJson<List<BuilderSchemeItemV2>>(
        json,
        object : TypeToken<List<BuilderSchemeItemV2>>() {}.type
      )
    }.getOrNull()
  }
}
