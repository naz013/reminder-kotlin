package com.elementary.tasks.core.data.converters

import com.elementary.tasks.core.data.models.ShopItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import androidx.room.TypeConverter

class ShopItemsTypeConverter {

  @TypeConverter
  fun toJson(list: List<ShopItem>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<ShopItem>? {
    return Gson().fromJson<List<ShopItem>>(json, object : TypeToken<List<ShopItem>>() {}.type)
  }
}
