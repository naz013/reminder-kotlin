package com.elementary.tasks.reminder.build.bi

import com.elementary.tasks.reminder.build.BuilderItem

data class ProcessedBuilderItems(
  val typeMap: Map<BiType, BuilderItem<*>>,
  val groupMap: Map<BiGroup, List<BuilderItem<*>>>
) {

  constructor(items: List<BuilderItem<*>>) : this(
    typeMap = items.associateBy { it.biType },
    groupMap = items.groupBy { it.biGroup }
  )
}
