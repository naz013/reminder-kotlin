package com.elementary.tasks.home.scheduleview.data

import com.elementary.tasks.R
import com.github.naz013.domain.Birthday
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class UiBirthdayScheduleListAdapter(
  private val unitsConverter: UnitsConverter,
  private val colorProvider: ColorProvider,
  private val textProvider: TextProvider,
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager
) {

  fun create(
    data: Birthday,
    nowDateTime: LocalDateTime = LocalDateTime.now()
  ): UiBirthdayScheduleList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(data.date) ?: LocalDate.now()
    val futureBirthdayDateTime = dateTimeManager.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      nowDateTime = nowDateTime,
      birthday = data
    )
    val remainingTime = dateTimeManager.getBirthdayRemaining(
      futureBirthdayDateTime = futureBirthdayDateTime,
      ignoreYear = data.ignoreYear,
      nowDateTime = nowDateTime
    )
    return UiBirthdayScheduleList(
      id = data.uuId,
      dueDateTime = futureBirthdayDateTime,
      mainText = UiTextElement(
        text = data.name,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(16f),
          textStyle = UiTextStyle.NORMAL,
          textColor = colorProvider.getColorOnSurface()
        )
      ),
      secondaryText = createSecondaryText(data),
      timeText = UiTextElement(
        text = dateTimeManager.getTime(futureBirthdayDateTime.toLocalTime()),
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(18f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnSurface()
        )
      ),
      tags = listOfNotNull(
        createTypeBadge(),
        createRemainingBadge(remainingTime)
      )
    )
  }

  private fun createTypeBadge(): UiTextElement {
    return UiTextElement(
      text = textProvider.getText(R.string.birthday),
      textFormat = UiTextFormat(
        fontSize = unitsConverter.spToPx(12f),
        textStyle = UiTextStyle.BOLD,
        textColor = colorProvider.getColorOnSecondaryContainer()
      )
    )
  }

  private fun createRemainingBadge(
    remaining: String?
  ): UiTextElement? {
    return remaining?.let {
      UiTextElement(
        text = it,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(12f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnSecondaryContainer()
        )
      )
    }
  }

  private fun createSecondaryText(
    birthday: Birthday
  ): UiTextElement? {
    return birthday.takeIf { it.contactId > 0 || it.number.isNotEmpty() }?.let {
      val contactName = contactsReader.getNameFromNumber(it.number) ?: it.number
      UiTextElement(
        text = contactName,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(14f),
          textStyle = UiTextStyle.ITALIC,
          textColor = colorProvider.getColorOnSurface()
        )
      )
    }
  }
}
