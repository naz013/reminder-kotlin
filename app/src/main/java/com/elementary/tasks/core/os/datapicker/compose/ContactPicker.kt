package com.elementary.tasks.core.os.datapicker.compose

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.R
import com.github.naz013.feature.birthday.ContactData
import com.github.naz013.feature.common.readString

@Composable
fun rememberContactPicker(onContactPicked: (ContactData) -> Unit): () -> Unit {
  val context = LocalContext.current
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
      val uri = result.data?.data ?: return@rememberLauncherForActivityResult
      val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return@rememberLauncherForActivityResult
      cursor.use {
        if (it.moveToFirst()) {
          runCatching {
            val phoneNumber = it.readString(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val name = it.readString(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            if (phoneNumber != null) onContactPicked(ContactData(name ?: "", phoneNumber))
          }
        }
      }
    }
  return remember(launcher) {
    {
      try {
        launcher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
      } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
      }
    }
  }
}

/**
 * Same picker as [rememberContactPicker], but shaped for callers that need a per-call result
 * callback rather than one fixed at remember-time - matches
 * [com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet]'s
 * `onPickContact: (onResult: (phone: String) -> Unit) -> Unit`.
 */
@Composable
fun rememberContactPhonePicker(): (onResult: (String) -> Unit) -> Unit {
  val context = LocalContext.current
  val pendingCallback = remember { PendingCallback<String>() }
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val uri = result.data?.data
        val cursor = uri?.let { context.contentResolver.query(it, null, null, null, null) }
        cursor?.use {
          if (it.moveToFirst()) {
            runCatching {
              it.readString(ContactsContract.CommonDataKinds.Phone.NUMBER)
            }.getOrNull()?.also { phoneNumber -> pendingCallback.value?.invoke(phoneNumber) }
          }
        }
      }
      pendingCallback.value = null
    }
  return remember(launcher) {
    { onResult ->
      pendingCallback.value = onResult
      try {
        launcher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
      } catch (_: ActivityNotFoundException) {
        pendingCallback.value = null
        Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
      }
    }
  }
}
