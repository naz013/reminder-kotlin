package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.DurationTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.parser.common.DurationParser

internal class DurationTagParser : TagParserInterface<DurationTag> {

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
