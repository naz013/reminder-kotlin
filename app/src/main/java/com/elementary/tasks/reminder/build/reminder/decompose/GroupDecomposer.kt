package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory

class GroupDecomposer(
  private val biFactory: BiFactory,
  private val groupDao: ReminderGroupDao,
  private val uiGroupListAdapter: UiGroupListAdapter
) {

  operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val group = reminder.groupUuId.takeIf { it.isNotEmpty() }
      ?.let { groupDao.getById(it) }
      ?.let { uiGroupListAdapter.convert(it) }
      ?.let { biFactory.createWithValue(BiType.GROUP, it, GroupBuilderItem::class.java) }
    return listOfNotNull(group)
  }
}
