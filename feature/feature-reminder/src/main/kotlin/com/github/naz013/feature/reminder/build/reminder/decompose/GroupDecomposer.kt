package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.GroupBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.GroupV2Repository

class GroupDecomposer(
  private val biFactory: BiFactory,
  private val groupV2Repository: GroupV2Repository,
  private val uiGroupListAdapter: UiGroupListAdapter,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val group =
      reminder.groupId
        ?.let { groupV2Repository.getById(it) }
        ?.let { uiGroupListAdapter.convert(it) }
        ?.let { biFactory.createWithValue(BiType.GROUP, it, GroupBuilderItem::class.java) }
    return listOfNotNull(group)
  }
}
