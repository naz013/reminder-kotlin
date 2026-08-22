package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.domain.routine.RoutineStep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class RoutineStepsTypeConverter {

  @TypeConverter
  fun toJson(list: List<RoutineStep>): String {
    return Gson().toJson(list)
  }

  @TypeConverter
  fun toList(json: String): List<RoutineStep> {
    if (json.isEmpty()) {
      return emptyList()
    }
    return runCatching {
      Gson().fromJson<List<RoutineStep>>(json, object : TypeToken<List<RoutineStep>>() {}.type)
    }.getOrNull() ?: emptyList()
  }
}
