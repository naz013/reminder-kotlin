package com.elementary.tasks.core.os

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.elementary.tasks.core.os.data.ContactData
import com.elementary.tasks.core.utils.readString

class ContactPicker(
  private val activity: FragmentActivity,
  private val resultCallback: (ContactData) -> Unit
) {
  private val launcher =
    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        result.data?.data?.also { readResults(it) }
      }
    }

  fun pickContact() {
    launcher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
  }

  private fun readResults(uri: Uri) {
    val cursor = activity.contentResolver.query(uri, null, null, null, null)
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        runCatching {
          val phoneNumber = cursor.readString(ContactsContract.CommonDataKinds.Phone.NUMBER)
          val name = cursor.readString(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
          if (phoneNumber != null) {
            resultCallback.invoke(ContactData(name ?: "", phoneNumber))
          }
        }
      }
      cursor.close()
    }
  }
}
