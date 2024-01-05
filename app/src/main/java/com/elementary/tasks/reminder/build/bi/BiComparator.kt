package com.elementary.tasks.reminder.build.bi

import com.elementary.tasks.reminder.build.BuilderItem

class BiComparator : Comparator<BuilderItem<*>> {

  override fun compare(o1: BuilderItem<*>?, o2: BuilderItem<*>?): Int {
    if (o1 == null && o2 == null) return 0
    if (o1 == null) return 1
    if (o2 == null) return -1
    val groupCompare = o1.biGroup.ordinal.compareTo(o2.biGroup.ordinal)
    if (groupCompare != 0) {
      return groupCompare
    }
    val typeCompare = o1.biType.ordinal.compareTo(o2.biType.ordinal)
    if (typeCompare != 0) {
      return typeCompare
    }
    return o1.title.compareTo(o2.title)
  }
}
