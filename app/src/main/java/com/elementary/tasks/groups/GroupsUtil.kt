package com.elementary.tasks.groups

import com.elementary.tasks.R
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.TextProvider
import java.util.Random

class GroupsUtil(
  private val textProvider: TextProvider,
  private val reminderGroupDao: ReminderGroupDao
) {

  private val random = Random()

  fun initDefaultIfEmpty() {
    if (reminderGroupDao.all().isEmpty()) {
      initDefault()
    }
  }

  fun initDefault(): String {
    val def = ReminderGroup(
      groupTitle = textProvider.getText(R.string.general),
      groupColor = random.nextInt(16)
    )
    def.isDefaultGroup = true
    runCatching {
      reminderGroupDao.insert(def)
      reminderGroupDao.insert(
        ReminderGroup(
          groupTitle = textProvider.getText(R.string.work),
          groupColor = random.nextInt(16)
        )
      )
      reminderGroupDao.insert(
        ReminderGroup(
          groupTitle = textProvider.getText(R.string.personal),
          groupColor = random.nextInt(16)
        )
      )
    }
    return def.groupUuId
  }

  fun mapAll(): Map<String, ReminderGroup> {
    val list = reminderGroupDao.all()
    val map = mutableMapOf<String, ReminderGroup>()
    for (group in list) map[group.groupUuId] = group
    return map
  }
}
