package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.RepeatTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType

class RepeatTagParser : TagParserInterface<RepeatTag> {

  override fun parse(line: String): RepeatTag? {
    return if (line.startsWith(TagType.REPEAT.value)) {
      val parts = line.split(":")
      runCatching { parts[1].toInt() }.getOrNull()?.let { RepeatTag(it) }
    } else {
      null
    }
  }
}
