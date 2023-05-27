package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.Tag

interface TagParserInterface<T : Tag> {
  fun parse(line: String): T?
}
