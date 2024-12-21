package com.elementary.tasks.core.appwidgets.singlenote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.AppWidgetActionActivity
import com.elementary.tasks.core.appwidgets.Direction
import com.elementary.tasks.core.appwidgets.WidgetIntentProtocol
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.data.adapter.note.UiNoteWidgetAdapter
import com.elementary.tasks.core.data.invokeSuspend
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.github.naz013.feature.common.android.dp2px
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.github.naz013.feature.common.android.adjustAlpha
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SingleNoteWidget : AppWidgetProvider(), KoinComponent {

  private val uiNoteWidgetAdapter by inject<UiNoteWidgetAdapter>()
  private val noteRepository by inject<NoteRepository>()

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    super.onUpdate(context, appWidgetManager, appWidgetIds)
    for (widgetId in appWidgetIds) {
      val prefsProvider = SingleNoteWidgetPrefsProvider(context, widgetId)
      updateWidget(
        context = context,
        appWidgetManager = appWidgetManager,
        prefsProvider = prefsProvider,
        uiNoteWidgetAdapter = uiNoteWidgetAdapter,
        noteWithImages = prefsProvider.getNoteId()?.let {
          invokeSuspend { noteRepository.getById(it) }
        }
      )
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    for (widgetId in appWidgetIds) {
      SingleNoteWidgetPrefsProvider(context, widgetId).clear()
    }
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: SingleNoteWidgetPrefsProvider,
      uiNoteWidgetAdapter: UiNoteWidgetAdapter,
      noteWithImages: NoteWithImages?
    ) {
      val rv = RemoteViews(context.packageName, R.layout.widget_single_note_text_only)

      if (noteWithImages != null) {
        val options = appWidgetManager.getAppWidgetOptions(prefsProvider.widgetId)
        val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        val size = maxOf(width, height) * 2
        val baseSize = context.dp2px(156).toFloat()
        val baseMargin = context.dp2px(8).toFloat()

        val sizeScale = size / baseSize
        val fontScale = sizeScale * 0.85f

        val textColor = ThemeProvider.themedColor(
          context = context,
          code = prefsProvider.getTextColorPosition()
        ).adjustAlpha(prefsProvider.getTextColorOpacity().toInt())

        val overlayColor = ThemeProvider.themedColor(
          context = context,
          code = prefsProvider.getOverlayColorPosition()
        ).adjustAlpha(prefsProvider.getOverlayColorOpacity().toInt())

        val uiNoteWidget = uiNoteWidgetAdapter.convert(
          noteWithImages = noteWithImages,
          size = size,
          fontSize = prefsProvider.getTextSize() * fontScale,
          textColor = textColor,
          horizontalAlignment = prefsProvider.getHorizontalAlignment(),
          verticalAlignment = prefsProvider.getVerticalAlignment(),
          margin = baseMargin * sizeScale,
          overlayColor = overlayColor
        )

        rv.setImageViewBitmap(R.id.note_image, uiNoteWidget.bitmap)

        WidgetUtils.initButton(
          context,
          rv,
          R.id.btn_settings,
          uiNoteWidget.settingsIcon,
          SingleNoteWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }

        val data = WidgetIntentProtocol(
          mapOf<String, Any?>(
            Pair(Constants.INTENT_ID, uiNoteWidget.id)
          )
        )

        val intent = AppWidgetActionActivity.createIntent(context)
        intent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.NOTE)
        intent.putExtra(AppWidgetActionActivity.DATA, data)
        val pendingIntent = PendingIntentWrapper.getActivity(
          context,
          uiNoteWidget.uniqueId,
          intent,
          PendingIntent.FLAG_CANCEL_CURRENT
        )
        rv.setOnClickPendingIntent(R.id.note_image, pendingIntent)
      }

      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
    }
  }
}
