package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.bi.BiComparator
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFilter
import com.elementary.tasks.reminder.build.reminder.decompose.ActionDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ExtrasDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.GroupDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.NoteDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.TypeDecomposer

class ReminderToBiDecomposer(
  private val biFactory: BiFactory,
  private val typeDecomposer: TypeDecomposer,
  private val actionDecomposer: ActionDecomposer,
  private val extrasDecomposer: ExtrasDecomposer,
  private val groupDecomposer: GroupDecomposer,
  private val biFilter: BiFilter,
  private val noteDecomposer: NoteDecomposer
) {

  operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val items = mutableListOf<BuilderItem<*>>()

    items.addAll(extrasDecomposer(reminder))
    items.addAll(typeDecomposer(reminder))
    items.addAll(actionDecomposer(reminder))
    items.addAll(groupDecomposer(reminder))
    items.addAll(noteDecomposer(reminder))

    val itemsMap = items.associateBy { it.biType }
    val builderScheme = reminder.builderScheme?.sortedBy { it.position }

    return if (builderScheme.isNullOrEmpty()) {
      items.filter { biFilter(it) }.sortedWith(BiComparator())
    } else {
      builderScheme.map {
        if (itemsMap.containsKey(it.type)) {
          itemsMap[it.type] ?: biFactory.create(it.type)
        } else {
          biFactory.create(it.type)
        }
      }.filter { biFilter(it) }
    }
  }
}
