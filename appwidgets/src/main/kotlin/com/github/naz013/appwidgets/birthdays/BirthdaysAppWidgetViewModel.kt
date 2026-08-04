package com.github.naz013.appwidgets.birthdays

import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase

internal class BirthdaysAppWidgetViewModel(
  private val prefsProvider: BirthdaysWidgetPrefsProvider,
  private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
  private val uiBirthdayWidgetListAdapter: UiBirthdayWidgetListAdapter
) {

  suspend fun getState(): BirthdaysAppWidgetState {
    val headerBackgroundColor = prefsProvider.getHeaderBackground()
    val itemBackgroundColor = prefsProvider.getItemBackground()
    return BirthdaysAppWidgetState(
      widgetId = prefsProvider.widgetId,
      headerBackgroundColor = headerBackgroundColor,
      headerContrastColor = WidgetUtils.getContrastColor(headerBackgroundColor),
      itemBackgroundColor = itemBackgroundColor,
      itemContrastColor = WidgetUtils.getContrastColor(itemBackgroundColor),
      items = getAllBirthdaysUseCase()
        .map { uiBirthdayWidgetListAdapter.convert(it) }
        .sortedBy { it.millis }
    )
  }
}
