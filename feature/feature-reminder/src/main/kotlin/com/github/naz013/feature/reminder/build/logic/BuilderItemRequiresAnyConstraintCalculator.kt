package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.feature.reminder.build.bi.BuilderItemConstraints
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.domain.reminder.BiType

internal class BuilderItemRequiresAnyConstraintCalculator {
  operator fun invoke(
    item: BuilderItemConstraints,
    processedBuilderItems: ProcessedBuilderItems,
  ): List<BiType> {
    if (item.requiresAny.isEmpty()) {
      return emptyList()
    }
    val containsRequired =
      (
        item.requiresAnyType.isNotEmpty() &&
          item.requiresAnyType.any { processedBuilderItems.typeMap.containsKey(it.value) }
      ) ||
        (
          item.requiresAnyGroup.isNotEmpty() &&
            item.requiresAnyGroup.any { processedBuilderItems.groupMap.containsKey(it.value) }
        )

    if (containsRequired) {
      return emptyList()
    }
    val requiresAnyTypes =
      item.requiresAnyType
        .filterNot { processedBuilderItems.typeMap.containsKey(it.value) }
        .map { it.value }
    val requiresAnyGroupTypes =
      item.requiresAnyGroup
        .filterNot {
          processedBuilderItems.groupMap.containsKey(it.value)
        }.map { it.value.types }
        .flatten()
    return (requiresAnyTypes + requiresAnyGroupTypes).toHashSet().toList()
  }
}
