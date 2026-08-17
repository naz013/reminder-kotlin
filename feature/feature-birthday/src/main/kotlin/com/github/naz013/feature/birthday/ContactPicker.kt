package com.github.naz013.feature.birthday

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
import com.github.naz013.ui.common.R
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
