package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.bi.BiComparator
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFilter
import com.elementary.tasks.reminder.build.reminder.decompose.ActionDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ExtrasDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.GroupDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.NoteDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.TypeDecomposer
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderV2

class ReminderToBiDecomposer(
  private val biFactory: BiFactory,
  private val typeDecomposer: TypeDecomposer,
  private val actionDecomposer: ActionDecomposer,
  private val extrasDecomposer: ExtrasDecomposer,
  private val groupDecomposer: GroupDecomposer,
  private val biFilter: BiFilter,
  private val noteDecomposer: NoteDecomposer,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val items = mutableListOf<BuilderItem<*>>()

    items.addAll(extrasDecomposer(reminder))
    items.addAll(typeDecomposer(reminder))
    items.addAll(actionDecomposer(reminder))
    items.addAll(groupDecomposer(reminder))
    items.addAll(noteDecomposer(reminder))

    val itemsMap = items.associateBy { it.biType }
    val builderScheme =
      reminder.builderScheme
        ?.sortedBy { it.position }
        ?.mapNotNull { scheme -> BiType.entries.getOrNull(scheme.type) }

    return if (builderScheme.isNullOrEmpty()) {
      items.filter { biFilter(it) }.sortedWith(BiComparator())
    } else {
      builderScheme
        .map {
          if (itemsMap.containsKey(it)) {
            itemsMap[it] ?: biFactory.create(it)
          } else {
            biFactory.create(it)
          }
        }.filter { biFilter(it) }
    }
  }
}
