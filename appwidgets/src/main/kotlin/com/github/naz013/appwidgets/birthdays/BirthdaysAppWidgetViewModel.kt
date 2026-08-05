package com.github.naz013.appwidgets.birthdays

import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase

internal class BirthdaysAppWidgetViewModel(
  private val prefsProvider: BirthdaysWidgetPrefsProvider,
  private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
  private val uiBirthdayWidgetListAdapter: UiBirthdayWidgetListAdapter,
) {

  suspend fun getState(): BirthdaysAppWidgetState {
    return BirthdaysAppWidgetState(
      widgetId = prefsProvider.widgetId,
      backgroundColor = prefsProvider.getWidgetBackground(),
      items = getAllBirthdaysUseCase()
        .map { uiBirthdayWidgetListAdapter.convert(it) }
        .sortedBy { it.millis }
    )
  }
}
