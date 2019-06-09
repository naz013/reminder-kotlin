package com.elementary.tasks.core.data.converters

import com.elementary.tasks.core.data.models.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import androidx.room.TypeConverter

class PlacesTypeConverter {

    @TypeConverter
    fun toJson(list: List<Place>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<Place>? {
        return Gson().fromJson<List<Place>>(json, object : TypeToken<List<Place>>() {}.type)
    }
}
