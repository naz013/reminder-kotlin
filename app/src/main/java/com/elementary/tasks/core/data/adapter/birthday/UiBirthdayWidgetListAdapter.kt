package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayWidgetList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalTime

class UiBirthdayWidgetListAdapter(private val dateTimeManager: DateTimeManager) {

  fun convert(birthday: Birthday): UiBirthdayWidgetList {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthDate = dateTimeManager.getReadableBirthDate(birthday.date)
    val dateItem = dateTimeManager.getFutureBirthdayDate(birthTime, birthday.date)
    val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date)
    val remainingTime = dateTimeManager.parseBirthdayDate(birthday.date)?.let {
      dateTimeManager.getBirthdayRemaining(dateItem.dateTime, it)
    } ?: dateTimeManager.getRemaining(dateItem.dateTime)

    return UiBirthdayWidgetList(
      uuId = birthday.uuId,
      name = birthday.name,
      remainingTimeFormatted = remainingTime,
      millis = dateTimeManager.toMillis(dateItem.dateTime),
      ageFormattedAndBirthdayDate = "$ageFormatted ($birthDate)"
    )
  }
}
