package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.DurationTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.DurationParser

class DurationTagParser : TagParserInterface<DurationTag> {

  private val durationParser = DurationParser()

  override fun parse(line: String): DurationTag? {
    return if (line.startsWith(TagType.DURATION.value)) {
      val parts = line.split(":")
      runCatching { durationParser.parse(parts[1]) }.getOrNull()?.let { DurationTag(it) }
    } else {
      null
    }
  }
}
