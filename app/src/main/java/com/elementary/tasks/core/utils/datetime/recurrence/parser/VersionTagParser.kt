package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.VersionTag

class VersionTagParser : TagParserInterface<VersionTag> {

  override fun parse(line: String): VersionTag? {
    return if (line.startsWith(TagType.VERSION.value)) {
      val parts = line.split(":")
      runCatching { parts[1] }.getOrNull()?.let { VersionTag(it) }
    } else {
      null
    }
  }
}
