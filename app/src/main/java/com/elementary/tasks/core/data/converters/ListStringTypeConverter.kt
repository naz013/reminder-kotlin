package com.elementary.tasks.core.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListStringTypeConverter {

    @TypeConverter
    fun toJson(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<String> {
        System.gc()
        return Gson().fromJson<List<String>>(json, object : TypeToken<List<String>>() {}.type)
    }
}
