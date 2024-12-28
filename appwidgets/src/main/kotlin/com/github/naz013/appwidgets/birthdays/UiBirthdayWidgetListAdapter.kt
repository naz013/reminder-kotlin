package com.github.naz013.appwidgets.birthdays

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal class UiBirthdayWidgetListAdapter(
  private val dateTimeManager: DateTimeManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) {

  fun convert(birthday: Birthday): UiBirthdayWidgetList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
    val birthDate = dateTimeManager.getReadableBirthDate(birthdayDate, birthday.ignoreYear)
    val futureBirthday = modelDateTimeFormatter.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      birthday = birthday
    )
    val ageAndBirthdayDate = if (birthday.ignoreYear) {
      birthDate
    } else {
      val ageFormatted = modelDateTimeFormatter.getAgeFormatted(birthday.date)
      "$ageFormatted ($birthDate)"
    }

    return UiBirthdayWidgetList(
      uuId = birthday.uuId,
      name = birthday.name,
      millis = dateTimeManager.toMillis(futureBirthday),
      ageFormattedAndBirthdayDate = ageAndBirthdayDate
    )
  }
}
