package com.elementary.tasks.core.appwidgets.notes

import android.content.Intent
import android.widget.RemoteViewsService
import org.koin.android.ext.android.get

class NotesService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return NotesFactory(applicationContext, get(), get())
  }
}
