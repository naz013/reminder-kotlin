package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.VersionTag

internal class VersionTagParser : TagParserInterface<VersionTag> {

  override fun parse(line: String): VersionTag? {
    return if (line.startsWith(TagType.VERSION.value)) {
      val parts = line.split(":")
      runCatching { parts[1] }.getOrNull()?.let { VersionTag(it) }
    } else {
      null
    }
  }
}
