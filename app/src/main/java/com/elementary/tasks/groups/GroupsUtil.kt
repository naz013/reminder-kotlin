package com.elementary.tasks.groups

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.ReminderGroup
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object GroupsUtil {

    fun initDefault(context: Context): String {
        val random = Random()
        val def = ReminderGroup(context.getString(R.string.general), random.nextInt(16))
        def.isDefaultGroup = true
        val dao = AppDb.getAppDatabase(context).reminderGroupDao()
        dao.insert(def)
        dao.insert(ReminderGroup(context.getString(R.string.work), random.nextInt(16)))
        dao.insert(ReminderGroup(context.getString(R.string.personal), random.nextInt(16)))
        return def.groupUuId
    }

    fun mapAll(appDb: AppDb): Map<String, ReminderGroup> {
        val list = appDb.reminderGroupDao().all()
        val map = mutableMapOf<String, ReminderGroup>()
        for (group in list) map[group.groupUuId] = group
        return map
    }
}
