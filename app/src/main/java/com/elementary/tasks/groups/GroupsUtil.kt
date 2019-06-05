package com.elementary.tasks.groups

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.ReminderGroup
import java.lang.Exception
import java.util.*

object GroupsUtil {

    fun initDefault(context: Context): String {
        val random = Random()
        val def = ReminderGroup(context.getString(R.string.general), random.nextInt(16))
        def.isDefaultGroup = true
        try {
            val appDb = AppDb.getAppDatabase(context)
            appDb.reminderGroupDao().insert(def)
            appDb.reminderGroupDao().insert(ReminderGroup(context.getString(R.string.work), random.nextInt(16)))
            appDb.reminderGroupDao().insert(ReminderGroup(context.getString(R.string.personal), random.nextInt(16)))
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
