package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.domain.reminder.ShopItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class ShopItemsTypeConverter {

  @TypeConverter
  fun toJson(list: List<ShopItem>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<ShopItem> {
    if (json.isEmpty()) {
      return emptyList()
    }
    return runCatching {
      Gson().fromJson<List<ShopItem>>(json, object : TypeToken<List<ShopItem>>() {}.type)
    }.getOrNull() ?: emptyList()
  }
}
