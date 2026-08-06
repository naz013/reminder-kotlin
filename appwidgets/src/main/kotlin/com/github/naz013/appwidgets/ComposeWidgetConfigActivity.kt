package com.github.naz013.appwidgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.github.naz013.ui.common.compose.ComposeActivity

/**
 * Shared shell for widget-configuration Activities: extracts the widget ID, sets the pessimistic
 * `RESULT_CANCELED` up front (matching the `APPWIDGET_CONFIGURE` contract), and finishes
 * immediately if the ID is missing. Subclasses call [finishWithResult] once the user saves.
 */
internal abstract class ComposeWidgetConfigActivity : ComposeActivity() {

  protected var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private set

  override fun onCreate(savedInstanceState: Bundle?) {
    widgetId = intent.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    super.onCreate(savedInstanceState)
    setResult(RESULT_CANCELED, resultIntent())
    if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
  }

  protected fun finishWithResult() {
    setResult(RESULT_OK, resultIntent())
    finish()
  }

  private fun resultIntent(): Intent =
    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
}
