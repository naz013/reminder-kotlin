package com.elementary.tasks.core.app_widgets.calendar

import android.content.Intent
import android.widget.RemoteViewsService

class CalendarMonthService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CalendarMonthFactory(applicationContext, intent)
    }
}
