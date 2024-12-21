package com.elementary.tasks.core.appwidgets.googletasks

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

class TasksService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return TasksFactory(applicationContext, intent, get(), get())
  }
}
