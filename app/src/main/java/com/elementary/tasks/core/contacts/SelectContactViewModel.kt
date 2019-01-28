package com.elementary.tasks.core.contacts

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job

class SelectContactViewModel : ViewModel(), LifecycleObserver {

    var contacts: MutableLiveData<List<ContactItem>> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var contentResolver: ContentResolver? = null
    private var job: Job? = null

    fun loadContacts() {
        val contentResolver = contentResolver ?: return
        isLoading.postValue(true)
        job?.cancel()
        job = launchDefault {
            val list = mutableListOf<ContactItem>()
            val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                    null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC")
            list.clear()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                    val uri = Contacts.getPhoto(id.toLong())
                    var photo: String? = null
                    if (uri != null) {
                        photo = uri.toString()
                    }
                    hasPhone = if (hasPhone.equals("1", ignoreCase = true)) {
                        "true"
                    } else {
                        "false"
                    }
                    if (name != null && java.lang.Boolean.parseBoolean(hasPhone)) {
                        val data = ContactItem(name, photo, id)
                        val pos = getPosition(name, list)
                        if (pos == -1) {
                            list.add(data)
                        } else {
                            list.add(pos, data)
                        }
                    }
                }
                cursor.close()
            }
            withUIContext {
                isLoading.postValue(false)
                contacts.postValue(list)
            }
            job = null
        }
    }

    private fun getPosition(name: String, list: List<ContactItem>): Int {
        if (list.isEmpty()) {
            return 0
        }
        var position = -1
        for (data in list) {
            val comp = name.compareTo(data.name)
            if (comp <= 0) {
                position = list.indexOf(data)
                break
            }
        }
        return position
    }
}