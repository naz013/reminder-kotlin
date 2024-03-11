package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractValueAndTypeController

class BeforeTimeController(
  builderItem: BeforeTimeBuilderItem,
  private val dateTimeManager: DateTimeManager
) : AbstractValueAndTypeController<Long>(builderItem) {

  override fun convertToValue(textValue: String, typeIndex: Int): Long? {
    val long = runCatching { textValue.toLong() }.getOrNull() ?: return null
    return long * getMultiplier(typeIndex)
  }

  override fun parseValueAndType(t: Long?): ValueAndType {
    if (t == null) {
      return ValueAndType("0", 0)
    }
    val beforeTime = dateTimeManager.parseBeforeTime(t)
    return ValueAndType(beforeTime.value.toString(), beforeTime.type.index)
  }

  override fun getSelectionItems(): List<String> {
    return getContext().resources.getStringArray(R.array.before_times).toList()
  }

  private fun getMultiplier(index: Int): Long {
    return when (index) {
      DateTimeManager.MultiplierType.MINUTE.index -> DateTimeManager.MINUTE
      DateTimeManager.MultiplierType.HOUR.index -> DateTimeManager.HOUR
      DateTimeManager.MultiplierType.DAY.index -> DateTimeManager.DAY
      DateTimeManager.MultiplierType.WEEK.index -> DateTimeManager.DAY * 7
      DateTimeManager.MultiplierType.MONTH.index -> DateTimeManager.DAY * 30
      else -> DateTimeManager.SECOND
    }
  }
}
