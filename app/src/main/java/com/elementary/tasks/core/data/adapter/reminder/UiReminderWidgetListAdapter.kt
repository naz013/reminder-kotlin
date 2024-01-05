package com.elementary.tasks.core.data.adapter.reminder

import androidx.annotation.ColorInt
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.widget.UiReminderWidgetList
import com.elementary.tasks.core.data.ui.reminder.widget.UiReminderWidgetShopList
import com.elementary.tasks.core.data.ui.reminder.widget.UiShopListWidget
import com.elementary.tasks.core.data.ui.widget.DateSorted
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
import java.util.Locale

class UiReminderWidgetListAdapter(
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val contextProvider: ContextProvider
) {

  fun create(data: Reminder, @ColorInt textColor: Int): DateSorted {
    val type = UiReminderType(data.type)
    return when {
      type.isSubTasks() -> {
        val due = uiReminderCommonAdapter.getDue(data, type)
        val checkedIcon = ViewUtils.createIcon(
          contextProvider.context,
          R.drawable.ic_fluent_checkbox_checked,
          textColor
        )
        val unCheckedIcon = ViewUtils.createIcon(
          contextProvider.context,
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

      type.isGpsType() -> {
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
        val due = uiReminderCommonAdapter.getDue(data, type)
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
}
