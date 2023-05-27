package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.ExceptionsDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.ArrayParser
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.UtcDateTimeParser

class ExceptionsDateTimeTagParser : TagParserInterface<ExceptionsDateTimeTag> {

  private val arrayParser = ArrayParser()
  private val utcDateTimeParser = UtcDateTimeParser()

  override fun parse(line: String): ExceptionsDateTimeTag? {
    return if (line.startsWith(TagType.EXDATE.value)) {
      val parts = line.split(":")
      runCatching { parseDateTimes(parts[1]) }.getOrNull()?.let { ExceptionsDateTimeTag(it) }
    } else {
      null
    }
  }

  private fun parseDateTimes(input: String): List<UtcDateTime> {
    return arrayParser.parse(input).mapNotNull { utcDateTimeParser.parse(it) }
  }
}
