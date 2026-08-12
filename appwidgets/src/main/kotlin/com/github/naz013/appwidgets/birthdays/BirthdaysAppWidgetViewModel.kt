package com.github.naz013.appwidgets.birthdays

import com.github.naz013.repository.BirthdayRepository

internal class BirthdaysAppWidgetViewModel(
  private val prefsProvider: BirthdaysWidgetPrefsProvider,
  private val birthdayRepository: BirthdayRepository,
  private val uiBirthdayWidgetListAdapter: UiBirthdayWidgetListAdapter,
) {

  suspend fun getState(): BirthdaysAppWidgetState {
    return BirthdaysAppWidgetState(
      widgetId = prefsProvider.widgetId,
      backgroundColor = prefsProvider.getWidgetBackground(),
      items = birthdayRepository.getAll()
        .map { uiBirthdayWidgetListAdapter.convert(it) }
        .sortedBy { it.millis }
    )
  }
}
