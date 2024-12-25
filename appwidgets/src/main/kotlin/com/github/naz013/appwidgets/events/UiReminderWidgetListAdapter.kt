package com.github.naz013.appwidgets.events

import androidx.annotation.ColorInt
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.events.data.DateSorted
import com.github.naz013.appwidgets.events.data.UiReminderDueData
import com.github.naz013.appwidgets.events.data.UiReminderWidgetList
import com.github.naz013.appwidgets.events.data.UiReminderWidgetShopList
import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.view.ViewUtils
import java.util.Locale

internal class UiReminderWidgetListAdapter(
  private val contextProvider: ContextProvider,
  private val dateTimeManager: DateTimeManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) {

  fun create(data: Reminder, @ColorInt textColor: Int): DateSorted {
    return when {
      Reminder.isKind(data.type, Reminder.SHOPPING) -> {
        val due = getDue(data)
        val checkedIcon = ViewUtils.createIcon(
          contextProvider.themedContext,
          R.drawable.ic_fluent_checkbox_checked,
          textColor
        )
        val unCheckedIcon = ViewUtils.createIcon(
          contextProvider.themedContext,
          R.drawable.ic_fluent_checkbox_unchecked,
          textColor
        )
        UiReminderWidgetShopList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = due.dateTime,
          millis = due.millis.takeIf { it != 0L } ?: Long.MAX_VALUE,
          items = data.shoppings.map {
            UiShopListWidget(
              icon = if (it.isChecked) {
                checkedIcon
              } else {
                unCheckedIcon
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
          millis = Long.MAX_VALUE,
          remainingTimeFormatted = ""
        )
      }

      else -> {
        val due = getDue(data)
        UiReminderWidgetList(
          uuId = data.uuId,
          text = data.summary,
          dateTime = due.dateTime ?: "",
          millis = due.millis,
          remainingTimeFormatted = due.remaining ?: ""
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
      remaining = getRemaining(data),
      millis = dueMillis
    )
  }

  private fun getRemaining(reminder: Reminder): String {
    return modelDateTimeFormatter.getRemaining(reminder.eventTime, reminder.delay)
  }
}
