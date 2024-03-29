package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.ICalStartDateBuilderItem
import com.elementary.tasks.reminder.build.ICalStartTimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.preset.RecurParamsToBiAdapter

class ICalDecomposer(
  private val biFactory: BiFactory,
  private val recurrenceManager: RecurrenceManager,
  private val recurParamsToBiAdapter: RecurParamsToBiAdapter
) {

  operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val rules = runCatching {
      recurrenceManager.parseObject(reminder.recurDataObject)
    }.getOrNull() ?: return emptyList()

    val list = mutableListOf<BuilderItem<*>>()

    rules.map.values.forEach { tag ->
      when (tag) {
        is RecurrenceRuleTag -> {
          recurParamsToBiAdapter(tag.params).also {
            list.addAll(it)
          }
        }

        is DateTimeStartTag -> {
          tag.value.dateTime?.also { dateTime ->
            biFactory.createWithValue(
              BiType.ICAL_START_DATE,
              dateTime.toLocalDate(),
              ICalStartDateBuilderItem::class.java
            )?.also {
              list.add(it)
            }
            biFactory.createWithValue(
              BiType.ICAL_START_TIME,
              dateTime.toLocalTime(),
              ICalStartTimeBuilderItem::class.java
            )?.also {
              list.add(it)
            }
          }
        }

        else -> {}
      }
    }

    return list
  }
}
