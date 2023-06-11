package com.elementary.tasks.core.utils.datetime.recurrence.builder

import com.elementary.tasks.core.utils.datetime.recurrence.Buildable

class RuleBuilder {

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
