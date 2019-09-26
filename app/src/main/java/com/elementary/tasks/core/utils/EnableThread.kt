package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder

object EnableThread {

    fun run(context: Context) {
        launchDefault {
            val items: List<Reminder> = try {
                AppDb.getAppDatabase(context).reminderDao().getAll(active = true, removed = false)
            } catch (e: Exception) {
                listOf()
            }
            for (item in items) {
                EventControlFactory.getController(item).start()
            }
        }
    }
}
