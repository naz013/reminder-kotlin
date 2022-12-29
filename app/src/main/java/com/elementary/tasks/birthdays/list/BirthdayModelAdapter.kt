package com.elementary.tasks.birthdays.list

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalTime

class BirthdayModelAdapter(private val dateTimeManager: DateTimeManager) {

  fun convert(birthday: Birthday): UiBirthdayList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthDate = dateTimeManager.getReadableBirthDate(birthday.date)
    val dateItem = dateTimeManager.getFutureBirthdayDate(birthTime, birthday.date)
    val ageFormatted = dateTimeManager.getAgeFormatted(dateItem.year, dateItem.dateTime)
    val nextBirthdayDateTime = dateTimeManager.getFullDateTime(dateItem.dateTime)
    val remainingTime =  dateTimeManager.getRemaining(dateItem.dateTime)

    return UiBirthdayList(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      birthdayDate = birthDate,
      ageFormatted = ageFormatted,
      remainingTimeFormatted = remainingTime,
      nextBirthdayDateFormatted = nextBirthdayDateTime,
      nextBirthdayDate = dateTimeManager.toMillis(dateItem.dateTime),
    )
  }
}
