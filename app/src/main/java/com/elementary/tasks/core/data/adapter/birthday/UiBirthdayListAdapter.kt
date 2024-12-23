package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class UiBirthdayListAdapter(
  private val dateTimeManager: DateTimeManager,
  private val themeProvider: ThemeProvider,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
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
    val futureBirthdayDateTime = modelDateTimeFormatter.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      nowDateTime = nowDateTime,
      birthday = birthday
    )
    val ageFormatted = modelDateTimeFormatter.getAgeFormatted(
      date = birthday.date,
      nowDate = nowDateTime.toLocalDate()
    )
      .takeIf { !birthday.ignoreYear } ?: ""
    val nextBirthdayDateTime = dateTimeManager.getFullDateTime(futureBirthdayDateTime)
    val remainingTime = modelDateTimeFormatter.getBirthdayRemaining(
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
