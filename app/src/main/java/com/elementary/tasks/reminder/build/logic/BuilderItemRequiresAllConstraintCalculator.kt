package com.elementary.tasks.reminder.build.logic

import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems

class BuilderItemRequiresAllConstraintCalculator {

  operator fun invoke(
    item: BuilderItemConstraints,
    processedBuilderItems: ProcessedBuilderItems
  ): List<BiType> {
    if (item.requiresAll.isEmpty()) {
      return emptyList()
    }

    val requiredAllTypes = item.requiresAllType.filterNot {
      processedBuilderItems.typeMap.containsKey(it.value)
    }.map { it.value }

    val requiredAllGroupTypes = item.requiresAllGroup.asSequence().filterNot {
      processedBuilderItems.groupMap.containsKey(it.value)
    }.map { it.value }
      .map { it.types }
      .flatten()
      .filterNot { processedBuilderItems.typeMap.containsKey(it) }
      .toList()

    return (requiredAllTypes + requiredAllGroupTypes).toHashSet().toList()
  }
}
