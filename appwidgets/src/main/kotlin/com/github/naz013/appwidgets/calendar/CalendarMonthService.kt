package com.github.naz013.appwidgets.calendar

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

internal class CalendarMonthService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return CalendarMonthFactory(intent, applicationContext, get(), get(), get(), get())
  }
}
