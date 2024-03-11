package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.core.utils.datetime.recurrence.RuleMap
import com.elementary.tasks.core.utils.datetime.recurrence.Tag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.UntilRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.ICalBuilderItem
import com.elementary.tasks.reminder.build.ICalStartDateBuilderItem
import com.elementary.tasks.reminder.build.ICalStartTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilDateBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilTimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.create.fragments.recur.EventData
import org.threeten.bp.LocalDateTime

class ICalDateTimeCalculator(
  private val recurrenceManager: RecurrenceManager,
  private val dateTimeManager: DateTimeManager
) {

  operator fun invoke(processedBuilderItems: ProcessedBuilderItems): EventData? {
    Traces.d(TAG, "invoke: $processedBuilderItems")

    val iCalParams = processedBuilderItems.groupMap[BiGroup.ICAL]
      ?.takeIf { it.isNotEmpty() }
      ?.associateBy { it.biType }
      ?: return null

    Traces.d(TAG, "invoke: iCalParams = $iCalParams")

    val startDate = iCalParams.readValue(
      BiType.ICAL_START_DATE,
      ICalStartDateBuilderItem::class.java
    ) ?: return null
    val startTime = iCalParams.readValue(
      BiType.ICAL_START_TIME,
      ICalStartTimeBuilderItem::class.java
    ) ?: return null

    val startDateTime = LocalDateTime.of(startDate, startTime)

    Traces.d(TAG, "invoke: startDateTime = $startDateTime")

    val ruleMap = createRuleMap(startDateTime, iCalParams)

    Traces.d(TAG, "invoke: ruleMap = $ruleMap")

    val recurObject = runCatching {
      recurrenceManager.createObject(ruleMap)
    }.getOrNull() ?: return null

    Traces.d(TAG, "invoke: recurObject = $recurObject")

    val dates = runCatching { recurrenceManager.generate(ruleMap) }.getOrNull() ?: emptyList()

    Traces.d(TAG, "invoke: dates = $dates")

    val position = findPosition(dates)

    Traces.d(TAG, "invoke: position = $position")

    return dates[position].dateTime?.let {
      EventData(
        startDateTime = it,
        recurObject = recurObject
      )
    }
  }

  private fun createRuleMap(
    startDateTime: LocalDateTime,
    params: Map<BiType, BuilderItem<*>>
  ): RuleMap {
    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
    }

    createRruleTag(params)?.also {
      map[it.tagType] = it
    }

    return RuleMap(map)
  }

  private fun createRruleTag(
    params: Map<BiType, BuilderItem<*>>
  ): RecurrenceRuleTag? {
    if (params.isEmpty()) return null

    val recurParams = mutableListOf<RecurParam>()

    val untilDate = params.readValue(
      BiType.ICAL_UNTIL_DATE,
      ICalUntilDateBuilderItem::class.java
    )
    val untilTime = params.readValue(
      BiType.ICAL_UNTIL_TIME,
      ICalUntilTimeBuilderItem::class.java
    )

    val untilDateTime = if (untilDate != null && untilTime != null) {
      LocalDateTime.of(untilDate, untilTime)
    } else {
      null
    }

    Traces.d(TAG, "invoke: untilDateTime = $untilDateTime")

    untilDateTime?.also {
      recurParams.add(UntilRecurParam(UtcDateTime(untilDateTime)))
    }

    params.map { it.value }.mapNotNull { item ->
      if (item is ICalBuilderItem<*>) {
        item.getRecurParam()
      } else {
        null
      }
    }.forEach { recurParams.add(it) }

    return RecurrenceRuleTag(recurParams)
  }

  private fun findPosition(generated: List<UtcDateTime>): Int {
    if (generated.isEmpty()) return -1

    val nowDateTime = dateTimeManager.getCurrentDateTime().withNano(0)

    var nowSelected = false
    var position = -1

    generated.forEachIndexed { index, utcDateTime ->
      val dateTime = utcDateTime.dateTime
      if (dateTime != null) {
        if (!nowSelected) {
          if (dateTime.isEqual(nowDateTime) || dateTime.isAfter(nowDateTime)) {
            position = index
            nowSelected = true
          }
        }
      }
    }

    return position
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T, B : BuilderItem<T>> Map<BiType, BuilderItem<*>>.readValue(
    type: BiType,
    clazz: Class<B>
  ): T? {
    return get(type)?.takeIf { it::class.java == clazz }
      ?.let { it as? B }
      ?.modifier
      ?.getValue()
  }

  companion object {
    private const val TAG = "ICalDateTimeCalculator"
  }
}
