package com.elementary.tasks.core.app_widgets.calendar

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

class CalendarWeekdayService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return CalendarWeekdayFactory(applicationContext, intent, get(), get(), get())
  }
}
