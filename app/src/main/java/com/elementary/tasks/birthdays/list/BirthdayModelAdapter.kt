package com.elementary.tasks.birthdays.list

import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil

class BirthdayModelAdapter(
  private val currentStateHolder: CurrentStateHolder
) {

  private val prefs = currentStateHolder.preferences

  fun convert(birthday: Birthday): BirthdayListItem {
    val birthTime = TimeUtil.getBirthdayTime(prefs.birthdayTime)
    val language = prefs.appLanguage

    val birthDate = TimeUtil.getReadableBirthDate(birthday.date, language)
    val dateItem = TimeUtil.getFutureBirthdayDate(birthTime, birthday.date)
    val ageFormatted = if (dateItem != null) {
      TimeUtil.getAgeFormatted(currentStateHolder.context, dateItem.year, dateItem.millis, language)
    } else ""
    val nextBirthdayDateTime = if (dateItem != null) {
      TimeUtil.getFullDateTime(dateItem.millis, prefs.is24HourFormat, language)
    } else ""
    val remainingTime = if (dateItem != null) {
      TimeCount.getRemaining(currentStateHolder.context, dateItem.millis, language)
    } else ""

    return BirthdayListItem(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      birthdayDate = birthDate,
      ageFormatted = ageFormatted,
      remainingTimeFormatted = remainingTime,
      nextBirthdayDateFormatted = nextBirthdayDateTime,
      nextBirthdayDate = TimeUtil.getFutureBirthdayDate(birthTime, birthday.date)?.millis ?: 0L,
    )
  }
}
