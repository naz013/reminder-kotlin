package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.RecurParamParser

class RecurrenceRuleTagParser : TagParserInterface<RecurrenceRuleTag> {

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
