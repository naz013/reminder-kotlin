package com.elementary.tasks.core.os.datapicker.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberMultipleUriPicker(): (onResult: (List<Uri>) -> Unit) -> Unit {
  val context = LocalContext.current
  val pendingCallback = remember { PendingCallback<List<Uri>>() }
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val fileUri = result.data?.data
        val clipData = result.data?.clipData
        val uris = mutableListOf<Uri>()
        if (fileUri != null) {
          uris.add(fileUri)
        } else if (clipData != null) {
          for (i in 0 until clipData.itemCount) {
            uris.add(clipData.getItemAt(i).uri)
          }
        }
        uris.forEach { uri ->
          context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
          )
        }
        if (uris.isNotEmpty()) pendingCallback.value?.invoke(uris)
      }
      pendingCallback.value = null
    }
  return remember(launcher) {
    { onResult ->
      pendingCallback.value = onResult
      val intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
          addCategory(Intent.CATEGORY_OPENABLE)
          type = "*/*"
          putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
      launcher.launch(intent)
    }
  }
}
