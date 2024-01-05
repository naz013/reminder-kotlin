package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems

class BuilderItemMandatoryIfConstraintCalculator {

  operator fun invoke(
    item: BuilderItemConstraints,
    processedBuilderItems: ProcessedBuilderItems
  ): List<BiType> {
    if (item.mandatoryIf.isEmpty()) {
      return emptyList()
    }
    val containsRequired = (
      item.mandatoryIfType.isNotEmpty() &&
        item.mandatoryIfType.any { processedBuilderItems.typeMap.containsKey(it.value) }
      ) || (
      item.mandatoryIfGroup.isNotEmpty() &&
        item.mandatoryIfGroup.any { processedBuilderItems.groupMap.containsKey(it.value) }
      )

    if (containsRequired) {
      return emptyList()
    }
    val mandatoryIfTypes =
      item.mandatoryIfType.filterNot { processedBuilderItems.typeMap.containsKey(it.value) }
        .map { it.value }
    val mandatoryIfGroupTypes = item.mandatoryIfGroup.filterNot {
      processedBuilderItems.groupMap.containsKey(it.value)
    }.map { it.value.types }.flatten()

    return (mandatoryIfTypes + mandatoryIfGroupTypes).toHashSet().toList()
  }
}
