package com.elementary.tasks.core.app_widgets.notes

import android.content.Intent
import android.widget.RemoteViewsService

class NotesService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NotesFactory(applicationContext)
    }
}
