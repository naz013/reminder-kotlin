package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.R
import com.github.naz013.domain.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.isColorDark
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class UiBirthdayListAdapter(
  private val dateTimeManager: DateTimeManager,
  private val themeProvider: ThemeProvider
) {

  fun convert(
    birthday: Birthday,
    nowDateTime: LocalDateTime = LocalDateTime.now()
  ): UiBirthdayList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
    val birthDateFormatted = dateTimeManager.getReadableBirthDate(
      dateOfBirth = birthdayDate,
      ignoreYear = birthday.ignoreYear
    )
    val futureBirthdayDateTime = dateTimeManager.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      nowDateTime = nowDateTime,
      birthday = birthday
    )
    val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date, nowDateTime.toLocalDate())
      .takeIf { !birthday.ignoreYear } ?: ""
    val nextBirthdayDateTime = dateTimeManager.getFullDateTime(futureBirthdayDateTime)
    val remainingTime = dateTimeManager.getBirthdayRemaining(
      futureBirthdayDateTime = futureBirthdayDateTime,
      ignoreYear = birthday.ignoreYear,
      nowDateTime = nowDateTime
    )

    val color = themeProvider.colorBirthdayCalendar()
    return UiBirthdayList(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      birthdayDate = birthdayDate,
      birthdayDateFormatted = birthDateFormatted,
      ageFormatted = ageFormatted,
      remainingTimeFormatted = remainingTime,
      nextBirthdayDateFormatted = nextBirthdayDateTime,
      nextBirthdayDateMillis = dateTimeManager.toMillis(futureBirthdayDateTime),
      nextBirthdayDate = futureBirthdayDateTime,
      nextBirthdayTimeFormatted = dateTimeManager.getTime(futureBirthdayDateTime.toLocalTime()),
      color = color,
      contrastColor = getContrastColor(color)
    )
  }

  private fun getContrastColor(color: Int): Int {
    return if (color.isColorDark()) {
      themeProvider.getColor(R.color.whitePrimary)
    } else {
      themeProvider.getColor(R.color.pureBlack)
    }
  }
}
