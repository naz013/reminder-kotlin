package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.Tag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.LinesParser

class TagParser {

  private val linesParser = LinesParser()

  private val versionTagParser = VersionTagParser()
  private val repeatTagParser = RepeatTagParser()
  private val durationTagParser = DurationTagParser()
  private val exceptionsDateTimeTagParser = ExceptionsDateTimeTagParser()
  private val recurrenceDateTimeTagParser = RecurrenceDateTimeTagParser()
  private val recurrenceRuleTagParser = RecurrenceRuleTagParser()
  private val dateTimeTagParser = DateTimeTagParser()

  fun parse(input: String): List<Tag> {
    val lines = linesParser.parse(input)
    if (lines.isEmpty()) return emptyList()
    return lines.mapNotNull { findTag(it) }
  }

  private fun findTag(line: String): Tag? {
    return when {
      line.startsWith(TagType.VERSION.value) -> versionTagParser.parse(line)
      line.startsWith(TagType.REPEAT.value) -> repeatTagParser.parse(line)
      line.startsWith(TagType.DURATION.value) -> durationTagParser.parse(line)
      line.startsWith(TagType.EXDATE.value) -> exceptionsDateTimeTagParser.parse(line)
      line.startsWith(TagType.RDATE.value) -> recurrenceDateTimeTagParser.parse(line)
      line.startsWith(TagType.RRULE.value) -> recurrenceRuleTagParser.parse(line)
      else -> dateTimeTagParser.parse(line)
    }
  }
}
