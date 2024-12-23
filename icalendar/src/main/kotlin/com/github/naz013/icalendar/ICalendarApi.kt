package com.github.naz013.icalendar

interface ICalendarApi {
  fun parseObject(obj: String?): RuleMap?
  fun createObject(ruleMap: RuleMap): String?
  fun generate(ruleMap: RuleMap): List<UtcDateTime>
}
