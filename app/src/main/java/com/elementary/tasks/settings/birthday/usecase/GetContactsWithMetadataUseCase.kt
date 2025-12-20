package com.elementary.tasks.settings.birthday.usecase

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger

class GetContactsWithMetadataUseCase(
  private val context: Context,
) {

  /**
   * Retrieves all contacts from the device that have a birthday, along with their
   * phone numbers if available.
   *
   * @return List of contacts with their metadata (id, name, phone number, birthday)
   */
  operator fun invoke(): List<Contact> {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      Logger.e(TAG, "No READ_CONTACTS permission!")
      return emptyList()
    }

    val contentResolver = context.contentResolver
    val contactsMap = mutableMapOf<Long, Contact>()

    // First, query for contacts with birthdays
    queryContactsWithBirthdays(contentResolver, contactsMap)

    // Then, add phone numbers to the contacts
    addPhoneNumbersToContacts(contentResolver, contactsMap)

    return contactsMap.values.toList().also {
      Logger.i(TAG, "Total contacts retrieved with metadata: ${it.size}")
    }
  }

  /**
   * Queries the ContactsContract for contacts that have birthday information.
   *
   * @param contentResolver ContentResolver to query the contacts database
   * @param contactsMap Map to store contacts by their ID
   */
  private fun queryContactsWithBirthdays(
    contentResolver: ContentResolver,
    contactsMap: MutableMap<Long, Contact>
  ) {
    val projection = arrayOf(
      ContactsContract.Data.CONTACT_ID,
      ContactsContract.Data.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Event.START_DATE
    )

    val selection = "${ContactsContract.Data.MIMETYPE} = ? AND " +
      "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"

    val selectionArgs = arrayOf(
      ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
      ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
    )

    try {
      contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        ContactsContract.Data.DISPLAY_NAME + " ASC"
      )?.use { cursor ->
        val contactIdIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val displayNameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
        val birthdayIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)

        if (contactIdIndex == -1 || displayNameIndex == -1 || birthdayIndex == -1) {
          Logger.e(TAG, "Failed to get column indices for contact data")
          return
        }

        while (cursor.moveToNext()) {
          val contactId = cursor.getLong(contactIdIndex)
          val displayName = cursor.getString(displayNameIndex) ?: "Unknown"
          val birthday = cursor.getString(birthdayIndex)

          if (birthday != null && contactId > 0) {
            contactsMap[contactId] = Contact(
              id = contactId,
              name = displayName,
              number = null,
              birthday = birthday
            )
          }
        }
      }
      Logger.i(TAG, "Found ${contactsMap.size} contacts with birthdays")
    } catch (e: Exception) {
      Logger.e(TAG, "Error querying contacts with birthdays", e)
    }
  }

  /**
   * Adds phone numbers to contacts that were previously queried.
   *
   * @param contentResolver ContentResolver to query the contacts database
   * @param contactsMap Map of contacts to update with phone numbers
   */
  private fun addPhoneNumbersToContacts(
    contentResolver: ContentResolver,
    contactsMap: MutableMap<Long, Contact>
  ) {
    if (contactsMap.isEmpty()) {
      return
    }

    val projection = arrayOf(
      ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
      ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} IN " +
      "(${contactsMap.keys.joinToString(",")})"

    try {
      contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        selection,
        null,
        null
      )?.use { cursor ->
        val contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        if (contactIdIndex == -1 || numberIndex == -1) {
          Logger.e(TAG, "Failed to get column indices for phone data")
          return
        }

        while (cursor.moveToNext()) {
          val contactId = cursor.getLong(contactIdIndex)
          val phoneNumber = cursor.getString(numberIndex)

          contactsMap[contactId]?.let { contact ->
            // Update with first phone number found (or you could collect all numbers)
            if (contact.number == null && phoneNumber != null) {
              contactsMap[contactId] = contact.copy(number = phoneNumber)
            }
          }
        }
      }
      Logger.d(TAG, "Added phone numbers to contacts")
    } catch (e: Exception) {
      Logger.e(TAG, "Error querying phone numbers", e)
    }
  }

  data class Contact(
    val id: Long,
    val name: String,
    val number: String?,
    val birthday: String?
  )

  companion object {
    private const val TAG = "GetContactUseCase"
  }
}
