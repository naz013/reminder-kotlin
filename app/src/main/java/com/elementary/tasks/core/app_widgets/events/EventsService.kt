package com.elementary.tasks.core.app_widgets.events

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

class EventsService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return EventsFactory(applicationContext, intent, get(), get())
  }
}
