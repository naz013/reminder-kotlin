package com.elementary.tasks.reminder.build.preset

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.ICalByDayBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.preset.primitive.PrimitiveProtocol
import com.google.gson.Gson

class BiValueToBuilderSchemeValue(
  private val primitiveProtocol: PrimitiveProtocol
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
