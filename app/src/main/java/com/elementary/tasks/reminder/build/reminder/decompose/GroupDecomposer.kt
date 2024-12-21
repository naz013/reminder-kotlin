package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.github.naz013.domain.Reminder
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.repository.ReminderGroupRepository

class GroupDecomposer(
  private val biFactory: BiFactory,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val uiGroupListAdapter: UiGroupListAdapter
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val group = reminder.groupUuId.takeIf { it.isNotEmpty() }
      ?.let { reminderGroupRepository.getById(it) }
      ?.let { uiGroupListAdapter.convert(it) }
      ?.let { biFactory.createWithValue(BiType.GROUP, it, GroupBuilderItem::class.java) }
    return listOfNotNull(group)
  }
}
