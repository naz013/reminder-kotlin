package com.github.naz013.icalendar.builder

import com.github.naz013.icalendar.Buildable

internal class RuleBuilder {

  private val lineNormalizer = LineNormalizer()
  private val lineComposer = LineComposer()

  fun buildString(tags: List<Buildable>): String? {
    if (tags.isEmpty()) return null
    val lines = tags.mapNotNull { buildable ->
      buildable.buildString().takeIf { it.isNotEmpty() }
    }
    return if (lines.isEmpty()) {
      null
    } else {
      lineComposer.compose(lineNormalizer.normalize(lines))
    }
  }
}
