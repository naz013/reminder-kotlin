package com.github.naz013.appwidgets.singlenote

import android.appwidget.AppWidgetManager
import android.content.Context
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.ui.common.context.dp2px
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.usecase.notes.GetNoteByIdUseCase

internal class SingleNoteAppWidgetViewModel(
  private val context: Context,
  private val prefsProvider: SingleNoteWidgetPrefsProvider,
  private val getNoteByIdUseCase: GetNoteByIdUseCase,
  private val uiNoteWidgetAdapter: UiNoteWidgetAdapter
) {

  suspend fun getState(): SingleNoteAppWidgetState {
    val noteId = prefsProvider.getNoteId()
    val noteWithImages = noteId?.let { getNoteByIdUseCase(it) }
      ?: return SingleNoteAppWidgetState(prefsProvider.widgetId, null, null)

    val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(prefsProvider.widgetId)
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

    return SingleNoteAppWidgetState(
      widgetId = prefsProvider.widgetId,
      noteId = uiNoteWidget.id,
      bitmap = uiNoteWidget.bitmap
    )
  }
}
