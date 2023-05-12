package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalTime

class UiBirthdayPreviewAdapter(
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager
) {

  fun convert(birthday: Birthday): UiBirthdayPreview {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val dateOfBirth = dateTimeManager.parseBirthdayDate(birthday.date)
    val dateOfBirthFormatted = dateOfBirth?.let { dateTimeManager.getDate(it) }
    val futureBirthday = dateTimeManager.getFutureBirthdayDate(birthTime, birthday.date)
    val nextBirthdayDate = dateTimeManager.getFullDateTime(futureBirthday.dateTime)
    val contactId = if (birthday.number.isNotEmpty()) {
      contactsReader.getIdFromNumber(birthday.number)
    } else {
      null
    }

    return UiBirthdayPreview(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      photo = contactId?.let { contactsReader.getPhotoBitmap(it) },
      contactName = contactId?.let { contactsReader.getNameFromNumber(birthday.number) },
      dateOfBirth = dateOfBirthFormatted,
      nextBirthdayDate = nextBirthdayDate,
      ageFormatted = dateTimeManager.getAgeFormatted(birthday.date),
      hasBirthdayToday = dateOfBirth?.let { dateTimeManager.isSameDay(it) } ?: false
    )
  }
}
