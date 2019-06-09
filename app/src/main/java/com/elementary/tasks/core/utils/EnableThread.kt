package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb

object EnableThread {

    fun run(context: Context) {
        launchDefault {
            for (item in AppDb.getAppDatabase(context).reminderDao().getAll(active = true, removed = false)) {
                EventControlFactory.getController(item).start()
            }
        }
    }
}
