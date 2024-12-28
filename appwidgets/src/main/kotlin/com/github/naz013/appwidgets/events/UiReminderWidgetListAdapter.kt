package com.github.naz013.appwidgets.events

import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.events.data.DateSorted
import com.github.naz013.appwidgets.events.data.UiReminderDueData
import com.github.naz013.appwidgets.events.data.UiReminderWidgetList
import com.github.naz013.appwidgets.events.data.UiReminderWidgetShopList
import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import java.util.Locale

internal class UiReminderWidgetListAdapter(
  private val dateTimeManager: DateTimeManager
) {

  fun create(data: Reminder): DateSorted {
    return when {
      Reminder.isKind(data.type, Reminder.SHOPPING) -> {
        val due = getDue(data)
        UiReminderWidgetShopList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = due.dateTime,
          millis = due.millis.takeIf { it != 0L } ?: Long.MAX_VALUE,
          items = data.shoppings.map {
            UiShopListWidget(
              iconRes = if (it.isChecked) {
                R.drawable.ic_fluent_checkbox_checked
              } else {
                R.drawable.ic_fluent_checkbox_unchecked
              },
              text = it.summary
            )
          }
        )
      }

      Reminder.isBase(data.type, Reminder.BY_LOCATION) ||
        Reminder.isBase(data.type, Reminder.BY_OUT) ||
        Reminder.isBase(data.type, Reminder.BY_PLACES) -> {
        val place = data.places.firstOrNull()?.let {
          String.format(Locale.getDefault(), "%.5f", it.latitude) + " " +
            String.format(Locale.getDefault(), "%.5f", it.longitude)
        }
        UiReminderWidgetList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = place ?: "",
          millis = Long.MAX_VALUE
        )
      }

      else -> {
        val due = getDue(data)
        UiReminderWidgetList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = due.dateTime ?: "",
          millis = due.millis
        )
      }
    }
  }

  private fun getDue(data: Reminder): UiReminderDueData {
    val dateTime = dateTimeManager.fromGmtToLocal(data.eventTime)
    val dueMillis = dateTimeManager.toMillis(data.eventTime)
    val due = dateTime?.let { dateTimeManager.getFullDateTime(it) }
    return UiReminderDueData(
      dateTime = due,
      millis = dueMillis
    )
  }
}
