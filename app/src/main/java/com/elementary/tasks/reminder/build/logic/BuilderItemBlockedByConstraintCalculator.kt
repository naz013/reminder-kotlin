package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems

class BuilderItemBlockedByConstraintCalculator {

  operator fun invoke(
    item: BuilderItemConstraints,
    processedBuilderItems: ProcessedBuilderItems
  ): List<BiType> {
    if (item.blockedBy.isEmpty()) {
      return emptyList()
    }

    val blockedByTypes = item.blockedByType.filter {
      processedBuilderItems.typeMap.containsKey(it.value)
    }.map { it.value }

    val blockedByGroupTypes = item.blockedByGroup.filter {
      processedBuilderItems.groupMap.containsKey(it.value)
    }.map { it.value.types }
      .flatten()
      .filter { processedBuilderItems.typeMap.containsKey(it) }

    return (blockedByTypes + blockedByGroupTypes).toHashSet().toList()
  }
}
