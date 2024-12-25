package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.parser.common.RecurParamParser

internal class RecurrenceRuleTagParser : TagParserInterface<RecurrenceRuleTag> {

  private val recurParamParser = RecurParamParser()

  override fun parse(line: String): RecurrenceRuleTag? {
    return if (line.startsWith(TagType.RRULE.value)) {
      val parts = line.split(":")
      runCatching { recurParamParser.parse(parts[1]) }.getOrNull()?.let { RecurrenceRuleTag(it) }
    } else {
      null
    }
  }
}
