package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.threeten.bp.LocalTime

class UiBirthdayPreviewAdapter(
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager
) {

  fun convert(birthday: Birthday): UiBirthdayPreview {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val dateOfBirth = dateTimeManager.parseBirthdayDate(birthday.date)
    val dateOfBirthFormatted = dateOfBirth?.let {
      dateTimeManager.formatBirthdayDateForUi(it, birthday.ignoreYear)
    }
    val futureBirthday = dateOfBirth?.let {
      dateTimeManager.getFutureBirthdayDate(
        birthdayTime = birthTime,
        birthdayDate = it,
        birthday = birthday
      )
    }
    val nextBirthdayDate = futureBirthday?.let { dateTimeManager.getFullDateTime(it) }
    val contactId = if (birthday.number.isNotEmpty()) {
      contactsReader.getIdFromNumber(birthday.number)
    } else {
      null
    }

    return UiBirthdayPreview(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number.takeIf { it.isNotEmpty() },
      photo = contactId?.let { contactsReader.getPhotoBitmap(it) },
      contactName = contactId?.let { contactsReader.getNameFromNumber(birthday.number) },
      dateOfBirth = dateOfBirthFormatted,
      nextBirthdayDate = nextBirthdayDate,
      ageFormatted = dateTimeManager.getAgeFormatted(birthday.date).takeIf { !birthday.ignoreYear },
      hasBirthdayToday = dateOfBirth?.let { dateTimeManager.isSameDay(it) } ?: false
    )
  }
}
