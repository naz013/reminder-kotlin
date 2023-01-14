package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.UiReminderListHeader
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalDate
import java.util.UUID

class UiReminderListsAdapter(
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider
) {

  fun convert(data: List<Reminder>): List<UiReminderList> {
    val list = data.map { uiReminderListAdapter.create(it) }
    val result = mutableListOf<UiReminderList>()

    val todayDate = dateTimeManager.getHeaderDateFormatted(LocalDate.now())
    val tomorrowDate = dateTimeManager.getHeaderDateFormatted(LocalDate.now().plusDays(1))

    var previousHeader: String? = null

    for (i in list.indices) {
      val current = list[i]

      val header = getHeaderText(current, todayDate, tomorrowDate)

      if (header != previousHeader) {
        result.add(createHeader(header))
      }
      result.add(current)

      previousHeader = header
    }
    return result
  }

  private fun createHeader(text: String): UiReminderListHeader {
    return UiReminderListHeader(UUID.randomUUID().toString(), text)
  }

  private fun getHeaderText(data: UiReminderListData, today: String, tomorrow: String): String {
    val date = data.due?.localDateTime?.toLocalDate()
    return when {
      date != null -> {
        when (val formattedDate = dateTimeManager.getHeaderDateFormatted(date)) {
          today -> textProvider.getText(R.string.today)
          tomorrow -> textProvider.getText(R.string.tomorrow)
          else -> formattedDate
        }
      }
      data.status.active -> textProvider.getText(R.string.permanent)
      else -> textProvider.getText(R.string.disabled)
    }
  }
}
