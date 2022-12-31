package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiShowBirthday
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager

class UiShowBirthdayAdapter(
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader
) {

  fun convert(birthday: Birthday): UiShowBirthday {
    val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date)
    val number = if (birthday.contactId == 0L || birthday.number.isEmpty()) {
      contactsReader.getNumber(birthday.name)
    } else {
      birthday.number
    }
    val contactId = contactsReader.getIdFromNumber(number)

    return UiShowBirthday(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      ageFormatted = ageFormatted,
      photo = contactsReader.getPhoto(contactId),
      uniqueId = birthday.uniqueId,
    )
  }
}
