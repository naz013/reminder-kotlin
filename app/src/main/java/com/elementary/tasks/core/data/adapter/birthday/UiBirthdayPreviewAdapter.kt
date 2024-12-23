package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalTime

class UiBirthdayPreviewAdapter(
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) {

  fun convert(birthday: Birthday): UiBirthdayPreview {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val dateOfBirth = dateTimeManager.parseBirthdayDate(birthday.date)
    val dateOfBirthFormatted = dateOfBirth?.let {
      dateTimeManager.formatBirthdayDateForUi(it, birthday.ignoreYear)
    }
    val futureBirthday = dateOfBirth?.let {
      modelDateTimeFormatter.getFutureBirthdayDate(
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
      ageFormatted = modelDateTimeFormatter.getAgeFormatted(birthday.date)
        .takeIf { !birthday.ignoreYear },
      hasBirthdayToday = dateOfBirth?.let { dateTimeManager.isSameDay(it) } ?: false
    )
  }
}
