package com.elementary.tasks.groups

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.ReminderGroup
import java.util.*

object GroupsUtil {

  fun initDefault(context: Context, appDb: AppDb): String {
    val random = Random()
    val def = ReminderGroup(groupTitle = context.getString(R.string.general), groupColor = random.nextInt(16))
    def.isDefaultGroup = true
    try {
      appDb.reminderGroupDao().insert(def)
      appDb.reminderGroupDao().insert(ReminderGroup(groupTitle = context.getString(R.string.work), groupColor = random.nextInt(16)))
      appDb.reminderGroupDao().insert(ReminderGroup(groupTitle = context.getString(R.string.personal), groupColor = random.nextInt(16)))
    } catch (e: Exception) {
    }
    return def.groupUuId
  }

  fun mapAll(appDb: AppDb): Map<String, ReminderGroup> {
    val list = appDb.reminderGroupDao().all()
    val map = mutableMapOf<String, ReminderGroup>()
    for (group in list) map[group.groupUuId] = group
    return map
  }
}
