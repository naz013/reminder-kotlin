package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.RepeatTag
import com.github.naz013.icalendar.TagType

internal class RepeatTagParser : TagParserInterface<RepeatTag> {

  override fun parse(line: String): RepeatTag? {
    return if (line.startsWith(TagType.REPEAT.value)) {
      val parts = line.split(":")
      runCatching { parts[1].toInt() }.getOrNull()?.let { RepeatTag(it) }
    } else {
      null
    }
  }
}
