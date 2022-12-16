package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.os.data.ContactData
import com.elementary.tasks.core.utils.readString

class ContactPicker(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (ContactData) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

  constructor(activity: ComponentActivity, resultCallback: (ContactData) -> Unit) : this(
    ActivityLauncherCreator(activity),
    resultCallback
  )

  constructor(fragment: Fragment, resultCallback: (ContactData) -> Unit) : this(
    FragmentLauncherCreator(fragment),
    resultCallback
  )

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      result.data?.data?.also { readResults(it) }
    }
  }

  fun pickContact() {
    launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
  }

  private fun readResults(uri: Uri) {
    val cursor = getActivity().contentResolver.query(uri, null, null, null, null)
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
