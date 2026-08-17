package com.github.naz013.feature.reminder.build.bi

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.domain.reminder.BiType

internal data class ProcessedBuilderItems(
  val typeMap: Map<BiType, BuilderItem<*>>,
  val groupMap: Map<BiGroup, List<BuilderItem<*>>>,
) {
  constructor(items: List<BuilderItem<*>>) : this(
    typeMap = items.associateBy { it.biType },
    groupMap = items.groupBy { it.biGroup },
  )
}
