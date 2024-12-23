package com.elementary.tasks.voice

import com.backdoor.engine.misc.ContactsInterface
import com.github.naz013.common.contacts.ContactsReader

class ContactsHelper(
  private val contactsReader: ContactsReader
) : ContactsInterface {

  override fun findEmail(input: String?): String? {
    return contactsReader.findEmail(input)
  }

  override fun findNumber(input: String?): String? {
    return contactsReader.findNumber(input)
  }
}
