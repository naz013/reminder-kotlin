package com.github.naz013.appwidgets

object GlanceAppWidgetIdExtractor {

  fun extract(fileKey: String): Int {
    return fileKey.replace("appWidget-", "").toIntOrNull() ?: WidgetId.NO_ID
  }
}
