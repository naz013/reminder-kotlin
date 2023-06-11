package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.datetime.recurrence.ValueParam
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.ArrayParser
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.UtcDateTimeParser
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.ParamParser

class RecurrenceDateTimeTagParser : TagParserInterface<RecurrenceDateTimeTag> {

  private val arrayParser = ArrayParser()
  private val utcDateTimeParser = UtcDateTimeParser()
  private val paramParser = ParamParser()

  override fun parse(line: String): RecurrenceDateTimeTag? {
    return if (line.startsWith(TagType.RDATE.value)) {
      val parts = line.split(":")
      val param = runCatching { parseParam(parts[0]) }.getOrNull()
      runCatching { parseDateTimes(parts[1]) }.getOrNull()?.let { RecurrenceDateTimeTag(param, it) }
    } else {
      null
    }
  }

  private fun parseParam(input: String): ValueParam? {
    return paramParser.parse(input).firstOrNull()
      ?.takeIf { it is ValueParam }
      ?.let { it as? ValueParam }
  }

  private fun parseDateTimes(input: String): List<UtcDateTime> {
    return arrayParser.parse(input).mapNotNull { utcDateTimeParser.parse(it) }
  }
}
