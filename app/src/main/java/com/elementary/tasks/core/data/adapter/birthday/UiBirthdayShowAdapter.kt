package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager

class UiBirthdayShowAdapter(
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader
) {

  fun convert(birthday: Birthday): UiBirthdayShow {
    val ageFormatted = dateTimeManager.getAgeFormatted(birthday.date)
    val number = if (birthday.contactId == 0L || birthday.number.isEmpty()) {
      contactsReader.getNumber(birthday.name)
    } else {
      birthday.number
    }
    val contactId = contactsReader.getIdFromNumber(number)

    return UiBirthdayShow(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      ageFormatted = ageFormatted,
      photo = contactsReader.getPhotoBitmap(contactId),
      uniqueId = birthday.uniqueId
    )
  }
}
