package com.elementary.tasks.core.os.datapicker.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.R

@Composable
fun rememberGalleryPicker(onPicturesPicked: (List<Uri>) -> Unit): () -> Unit {
  val context = LocalContext.current
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
      val data = result.data ?: return@rememberLauncherForActivityResult
      val imageUri = data.data
      val clipData = data.clipData
      if (imageUri != null) {
        onPicturesPicked(listOf(imageUri))
      } else if (clipData != null) {
        val uris = (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
        onPicturesPicked(uris)
      }
    }
  return remember(launcher, context) {
    {
      val intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
          addCategory(Intent.CATEGORY_OPENABLE)
          type = "image/*"
          putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
      launcher.launch(Intent.createChooser(intent, context.getString(R.string.gallery)))
    }
  }
}
