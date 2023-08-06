package com.elementary.tasks.core.appwidgets.birthdays

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

class BirthdaysService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return BirthdaysFactory(applicationContext, intent, get(), get())
  }
}
