package com.elementary.tasks.core.app_widgets.calendar

import android.content.Intent
import android.widget.RemoteViewsService

class CalendarWeekdayService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return CalendarWeekdayFactory(applicationContext, intent)
  }
}
