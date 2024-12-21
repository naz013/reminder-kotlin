package com.elementary.tasks.core.data.adapter.birthday

import com.github.naz013.domain.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayWidgetList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class UiBirthdayWidgetListAdapter(private val dateTimeManager: DateTimeManager) {

  fun convert(birthday: Birthday): UiBirthdayWidgetList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
    val birthDate = dateTimeManager.getReadableBirthDate(birthdayDate, birthday.ignoreYear)
    val futureBirthday = dateTimeManager.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      birthday = birthday
    )
    val remainingTime = dateTimeManager.getBirthdayRemaining(
      futureBirthdayDateTime = futureBirthday,
      ignoreYear = birthday.ignoreYear
    )
    val ageAndBirthdayDate = if (birthday.ignoreYear) {
      birthDate
    } else {
      val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date)
      "$ageFormatted ($birthDate)"
    }

    return UiBirthdayWidgetList(
      uuId = birthday.uuId,
      name = birthday.name,
      remainingTimeFormatted = remainingTime,
      millis = dateTimeManager.toMillis(futureBirthday),
      ageFormattedAndBirthdayDate = ageAndBirthdayDate
    )
  }
}
