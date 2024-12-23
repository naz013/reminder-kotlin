package com.github.naz013.appwidgets.googletasks

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

internal class TasksService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return TasksFactory(applicationContext, intent, get(), get())
  }
}
