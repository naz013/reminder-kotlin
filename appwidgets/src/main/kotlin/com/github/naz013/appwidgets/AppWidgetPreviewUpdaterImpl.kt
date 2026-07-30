package com.github.naz013.appwidgets

import com.github.naz013.logging.Logger

class AppWidgetPreviewUpdaterImpl : AppWidgetPreviewUpdater {

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
