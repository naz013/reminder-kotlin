package com.elementary.tasks.core.data.adapter.missedcall

import android.graphics.drawable.Drawable
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.ui.missedcall.UiMissedCallShow
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BitmapUtils

@Deprecated("After S")
class UiMissedCallShowAdapter(
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader
) {

  fun convert(missedCall: MissedCall): UiMissedCallShow {
    val contactId = contactsReader.getIdFromNumber(missedCall.number)
    val name = contactsReader.getNameFromNumber(missedCall.number)
    val photo = contactsReader.getPhoto(contactId)
    val avatar: Drawable? = if (photo == null) {
      BitmapUtils.imageFromName(name)
    } else {
      null
    }

    return UiMissedCallShow(
      photo = photo,
      name = name,
      number = missedCall.number,
      formattedTime = dateTimeManager.getTime(
        dateTimeManager.fromMillis(missedCall.dateTime).toLocalTime()
      ),
      uniqueId = missedCall.uniqueId,
      avatar = avatar
    )
  }
}
