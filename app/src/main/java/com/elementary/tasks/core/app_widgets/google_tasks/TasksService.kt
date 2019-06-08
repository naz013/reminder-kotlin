package com.elementary.tasks.core.app_widgets.google_tasks

import android.content.Intent
import android.widget.RemoteViewsService

class TasksService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksFactory(applicationContext, intent)
    }
}
