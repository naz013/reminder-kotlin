package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.Tag

internal interface TagParserInterface<T : Tag> {
  fun parse(line: String): T?
}
