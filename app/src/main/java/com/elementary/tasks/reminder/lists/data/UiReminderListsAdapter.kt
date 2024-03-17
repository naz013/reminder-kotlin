package com.elementary.tasks.reminder.lists.data

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class UiReminderListsAdapter(
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val unitsConverter: UnitsConverter,
  private val colorProvider: ColorProvider
) {

  fun convert(data: List<Reminder>): List<UiReminderEventsList> {
    val result = mutableListOf<UiReminderEventsList>()

    val todayDate = dateTimeManager.getHeaderDateFormatted(LocalDate.now())
    val tomorrowDate = dateTimeManager.getHeaderDateFormatted(LocalDate.now().plusDays(1))

    var previousHeader: String? = null

    data.map { uiReminderListAdapter.create(it) }.forEach { current ->
      val header = getHeaderText(
        dueDate = current.dueDateTime,
        isActive = current.state.isActive,
        today = todayDate,
        tomorrow = tomorrowDate
      )
      if (header != previousHeader) {
        result.add(createHeader(header))
      }
      result.add(current)
      previousHeader = header
    }
    return result
  }

  private fun createHeader(text: String): UiReminderListHeader {
    return UiReminderListHeader(
      mainText = UiTextElement(
        text = text,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(18f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnBackground()
        )
      )
    )
  }

  private fun getHeaderText(
    dueDate: LocalDateTime?,
    isActive: Boolean,
    today: String,
    tomorrow: String
  ): String {
    val date = dueDate?.toLocalDate()
    return when {
      date != null && isActive -> {
        when (val formattedDate = dateTimeManager.getHeaderDateFormatted(date)) {
          today -> textProvider.getText(R.string.today)
          tomorrow -> textProvider.getText(R.string.tomorrow)
          else -> formattedDate
        }
      }
      isActive -> textProvider.getText(R.string.permanent)
      else -> textProvider.getText(R.string.disabled)
    }
  }
}
