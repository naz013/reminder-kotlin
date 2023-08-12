package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalTime

class UiBirthdayListAdapter(private val dateTimeManager: DateTimeManager) {

  fun convert(birthday: Birthday): UiBirthdayList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthDate = dateTimeManager.getReadableBirthDate(birthday.date, birthday.ignoreYear)
    val dateItem = dateTimeManager.getFutureBirthdayDate(birthTime, birthday.date)
    val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date)
      .takeIf { !birthday.ignoreYear } ?: ""
    val nextBirthdayDateTime = dateTimeManager.getFullDateTime(dateItem.dateTime)
    val remainingTime = dateTimeManager.parseBirthdayDate(birthday.date)?.let {
      dateTimeManager.getBirthdayRemaining(dateItem.dateTime, it)
    } ?: dateTimeManager.getRemaining(dateItem.dateTime)

    return UiBirthdayList(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      birthdayDate = birthDate,
      ageFormatted = ageFormatted,
      remainingTimeFormatted = remainingTime,
      nextBirthdayDateFormatted = nextBirthdayDateTime,
      nextBirthdayDate = dateTimeManager.toMillis(dateItem.dateTime)
    )
  }
}
