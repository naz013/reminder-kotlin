package com.github.naz013.appwidgets.events

import androidx.compose.ui.unit.sp
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetList
import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetListAdapter
import com.github.naz013.appwidgets.events.data.DateSorted
import com.github.naz013.appwidgets.events.data.EventsAppWidgetState
import com.github.naz013.appwidgets.events.data.UiReminderWidgetList
import com.github.naz013.appwidgets.events.data.UiReminderWidgetShopList
import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.logging.Logger
import com.github.naz013.usecase.birthdays.GetBirthdaysByDayMonthUseCase
import com.github.naz013.usecase.reminders.GetActiveRemindersUseCase
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.UUID

internal class EventsAppWidgetViewModel(
  private val prefsProvider: EventsWidgetPrefsProvider,
  private val dateTimeManager: DateTimeManager,
  private val getActiveRemindersUseCase: GetActiveRemindersUseCase,
  private val getBirthdaysByDayMonthUseCase: GetBirthdaysByDayMonthUseCase,
  private val uiReminderWidgetListAdapter: UiReminderWidgetListAdapter,
  private val uiBirthdayWidgetListAdapter: UiBirthdayWidgetListAdapter,
  private val appWidgetPreferences: AppWidgetPreferences
) {

  suspend fun getState(): EventsAppWidgetState {
    Logger.d(TAG, "Get state: ${prefsProvider.widgetId}")
    return if (prefsProvider.widgetId <= -2) {
      getPreviewState()
    } else {
      EventsAppWidgetState(
        widgetId = prefsProvider.widgetId,
        headerBackgroundColor = prefsProvider.getHeaderBackground(),
        headerContrastColor = WidgetUtils.getContrastColor(prefsProvider.getHeaderBackground()),
        headerText = getCurrentDateFormatted(),
        itemBackgroundColor = prefsProvider.getItemBackground(),
        itemContrastColor = WidgetUtils.getContrastColor(prefsProvider.getItemBackground()),
        itemTextSize = prefsProvider.getTextSize().toInt().sp,
        items = getEvents()
      )
    }
  }

  private fun getCurrentDateFormatted(): String {
    val cal = GregorianCalendar()
    val dateFormat = SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault())
    dateFormat.calendar = cal
    return dateFormat.format(cal.time)
  }

  private suspend fun getEvents(): List<DateSorted> {
    val events = getActiveRemindersUseCase()
      .map { uiReminderWidgetListAdapter.create(it) }
      .toMutableList()

    if (appWidgetPreferences.isBirthdayInWidgetEnabled) {
      val dateTime = dateTimeManager.getCurrentDateTime()
      getBirthdaysByDayMonthUseCase(
        dateTime.dayOfMonth,
        dateTime.monthValue - 1
      )
        .map { uiBirthdayWidgetListAdapter.convert(it) }
        .also { events.addAll(it) }
    }

    return events.sortedWith(compareBy { it.millis })
  }

  private fun getPreviewState(): EventsAppWidgetState {
    val backgroundCode = 10
    Logger.d(TAG, "Get state for preview")
    return EventsAppWidgetState(
      widgetId = prefsProvider.widgetId,
      headerBackgroundColor = backgroundCode,
      headerContrastColor = WidgetUtils.getContrastColor(backgroundCode),
      headerText = getCurrentDateFormatted(),
      itemBackgroundColor = backgroundCode,
      itemContrastColor = WidgetUtils.getContrastColor(backgroundCode),
      itemTextSize = 15.sp,
      items = getPreviewEvents()
    )
  }

  private fun getPreviewEvents(): List<DateSorted> {
    return listOf(
      UiReminderWidgetList(
        uuId = UUID.randomUUID().toString(),
        text = "Call to Person",
        dateTime = getCurrentDateFormatted(),
        millis = System.currentTimeMillis()
      ),
      UiBirthdayWidgetList(
        uuId = UUID.randomUUID().toString(),
        name = "Some Person",
        ageFormattedAndBirthdayDate = "25 years (25 December 2000)",
        millis = System.currentTimeMillis()
      ),
      UiReminderWidgetShopList(
        uuId = UUID.randomUUID().toString(),
        text = "Buy some goods",
        dateTime = null,
        millis = System.currentTimeMillis(),
        items = listOf(
          UiShopListWidget(
            iconRes = R.drawable.ic_fluent_checkbox_unchecked,
            text = "Apples"
          ),
          UiShopListWidget(
            iconRes = R.drawable.ic_fluent_checkbox_checked,
            text = "Butter"
          ),
          UiShopListWidget(
            iconRes = R.drawable.ic_fluent_checkbox_checked,
            text = "Flour"
          ),
          UiShopListWidget(
            iconRes = R.drawable.ic_fluent_checkbox_unchecked,
            text = "Milk"
          )
        )
      )
    )
  }

  companion object {
    private const val TAG = "EventsAppWidgetViewModel"
  }
}
