package com.github.naz013.icalendar

import com.github.naz013.icalendar.builder.RuleBuilder
import com.github.naz013.icalendar.parser.TagParser
import com.github.naz013.logging.Logger
import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.iterable.RecurrenceSet
import org.dmfs.rfc5545.iterable.instanceiterable.RuleInstances
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.TimeZone

internal class ICalendarApiImpl(
  private val ruleBuilder: RuleBuilder,
  private val tagParser: TagParser
) : ICalendarApi {

  override fun parseObject(obj: String?): RuleMap? {
    if (obj == null) return null

    val map = mutableMapOf<TagType, Tag>()
    tagParser.parse(obj).forEach {
      map[it.tagType] = it
    }

    Logger.d("parseObject: map = $map")

    return RuleMap(map)
  }

  override fun createObject(ruleMap: RuleMap): String? {
    requireNotNull(ruleMap.map[TagType.RRULE])
    requireNotNull(ruleMap.map[TagType.DTSTART])

    if ((ruleMap.map[TagType.RRULE] as? RecurrenceRuleTag)?.hasCountParam() != true) {
      throw IllegalArgumentException("Count should be present in RRULE")
    }

    val newRuleMap = ruleMap.copy(
      map = ruleMap.map.toMutableMap().apply {
        put(
          TagType.RDATE,
          RecurrenceDateTimeTag(ValueParam(ParamValueType.DATE_TIME), generate(ruleMap))
        )
      }
    )

    return ruleBuilder.buildString(newRuleMap.map.values.toList())
  }

  override fun generate(ruleMap: RuleMap): List<UtcDateTime> {
    val startDateTime = (ruleMap.map[TagType.DTSTART] as? DateTimeStartTag)?.value?.dateTime
      ?.let { toMillis(it) } ?: return emptyList()
    val rrule = (ruleMap.map[TagType.RRULE] as? RecurrenceRuleTag) ?: return emptyList()

    // UTC date time
    val dateTime = DateTime(TimeZone.getDefault(), startDateTime)

    val rule = RecurrenceRule(rrule.buildValueString())
    val set = RecurrenceSet(dateTime, RuleInstances(rule))
    val iterator = set.iterator()

    var count = 0
    val resultList = mutableListOf<UtcDateTime>()
    while (iterator.hasNext()) {
      val next = iterator.next()
      next?.shiftTimeZone(TimeZone.getDefault())?.toLocalDateTime()?.also {
        resultList.add(UtcDateTime(it))
      }
      count++
    }

    return resultList
  }

  private fun DateTime.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(
      LocalDate.of(year, month + 1, dayOfMonth),
      LocalTime.of(hours, minutes, seconds)
    )
  }

  private fun toMillis(localDateTime: LocalDateTime): Long {
    return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli()
  }
}
