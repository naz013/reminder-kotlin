package com.github.naz013.appwidgets.events

import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.events.data.DateSorted
import com.github.naz013.appwidgets.events.data.UiReminderDueData
import com.github.naz013.appwidgets.events.data.UiReminderWidgetList
import com.github.naz013.appwidgets.events.data.UiReminderWidgetShopList
import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime
import java.util.Locale

internal class UiReminderWidgetListAdapter(
  private val dateTimeManager: DateTimeManager
) {

  fun createV2(data: ReminderV2): DateSorted {
    return when {
      data.action is ReminderAction.Shopping -> {
        val due = getDueV2(data.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) })
        UiReminderWidgetShopList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = due.dateTime,
          millis = due.millis.takeIf { it != 0L } ?: Long.MAX_VALUE,
          items = data.shoppingItems.map {
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

      data.location != null -> {
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
        val due = getDueV2(data.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) })
        UiReminderWidgetList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = due.dateTime ?: "",
          millis = due.millis
        )
      }
    }
  }

  /** [dateTime] must already be converted to local time (via [DateTimeManager.utcToLocal]) -
   * `ReminderV2.schedule.eventDateTime` is stored UTC-zoned. */
  private fun getDueV2(dateTime: LocalDateTime?): UiReminderDueData {
    val due = dateTime?.let { dateTimeManager.getFullDateTime(it) }
    val dueMillis = dateTime?.let { dateTimeManager.toMillis(it) } ?: 0L
    return UiReminderDueData(
      dateTime = due,
      millis = dueMillis
    )
  }
}
