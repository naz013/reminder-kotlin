package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalTime

class UiBirthdayListAdapter(private val dateTimeManager: DateTimeManager) {

  fun convert(birthday: Birthday): UiBirthdayList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthDateFormatted = dateTimeManager.getReadableBirthDate(
      birthday.date,
      birthday.ignoreYear
    )
    val dateItem = dateTimeManager.getFutureBirthdayDate(birthTime, birthday.date)
    val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date)
      .takeIf { !birthday.ignoreYear } ?: ""
    val nextBirthdayDateTime = dateTimeManager.getFullDateTime(dateItem.nextBirthdayDateTime)
    val remainingTime = dateTimeManager.parseBirthdayDate(birthday.date)?.let {
      dateTimeManager.getBirthdayRemaining(dateItem.nextBirthdayDateTime, it)
    }?.takeIf { !birthday.ignoreYear }
      ?: dateTimeManager.getRemaining(dateItem.nextBirthdayDateTime)

    return UiBirthdayList(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      birthdayDate = birthday.date,
      birthdayDateFormatted = birthDateFormatted,
      ageFormatted = ageFormatted,
      remainingTimeFormatted = remainingTime,
      nextBirthdayDateFormatted = nextBirthdayDateTime,
      nextBirthdayDate = dateTimeManager.toMillis(dateItem.nextBirthdayDateTime)
    )
  }
}
