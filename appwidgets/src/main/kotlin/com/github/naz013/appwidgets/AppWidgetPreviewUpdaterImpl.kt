package com.github.naz013.appwidgets

import android.content.Context
import com.github.naz013.logging.Logger

class AppWidgetPreviewUpdaterImpl(
  private val context: Context
) : AppWidgetPreviewUpdater {

  override suspend fun updateEventsWidgetPreview() {
    Logger.d("AppWidgetPreviewUpdater", "Updating events widget preview")
//    if (Module.is15) {
//      AppWidgetManager.getInstance(context).setWidgetPreview(
//        ComponentName(
//          context,
//          EventsGlanceAppWidgetReceiver::class.java
//        ),
//        AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
//        EventsGlanceAppWidget().compose(context = context)
//      )
//    }
  }
}
