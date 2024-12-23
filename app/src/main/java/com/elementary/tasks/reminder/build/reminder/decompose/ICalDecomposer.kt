package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.ICalStartDateBuilderItem
import com.elementary.tasks.reminder.build.ICalStartTimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.preset.RecurParamsToBiAdapter

class ICalDecomposer(
  private val biFactory: BiFactory,
  private val ICalendarApi: ICalendarApi,
  private val recurParamsToBiAdapter: RecurParamsToBiAdapter
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val rules = runCatching {
      ICalendarApi.parseObject(reminder.recurDataObject)
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
