package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

class UiBirthdayShowAdapter(
  private val contactsReader: ContactsReader,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) {

  fun convert(birthday: Birthday): UiBirthdayShow {
    val ageFormatted = modelDateTimeFormatter.getAgeFormatted(birthday.date)
      .takeIf { !birthday.ignoreYear }
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
