package com.github.naz013.feature.reminder.build.preset

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.ICalByDayBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.preset.primitive.PrimitiveProtocol
import com.google.gson.Gson

internal class BiValueToBuilderSchemeValue(
  private val primitiveProtocol: PrimitiveProtocol,
) {
  operator fun invoke(builderItem: BuilderItem<*>): String {
    if (!builderItem.modifier.isCorrect()) {
      return ""
    }
    val value = builderItem.modifier.getValue() ?: return ""
    return when (builderItem) {
      is SubTasksBuilderItem -> Gson().toJson(value)
      is ICalByDayBuilderItem -> Gson().toJson(value)
      else -> primitiveProtocol.asString(value)
    }
  }
}
