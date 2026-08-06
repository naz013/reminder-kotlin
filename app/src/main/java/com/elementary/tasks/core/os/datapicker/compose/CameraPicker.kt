package com.elementary.tasks.core.os.datapicker.compose

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.BuildConfig
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.logging.Logger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun rememberCameraPicker(onPhotoTaken: (Uri) -> Unit): () -> Unit {
  val context = LocalContext.current
  var pendingUri by remember { mutableStateOf<Uri?>(null) }

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
      val uri = pendingUri
      pendingUri = null
      Logger.d(TAG, "dispatchResult: $success, $uri")
      if (success && uri != null) {
        onPhotoTaken(uri)
      }
    }

  return remember(launcher, context) {
    {
      val photoFile = createImageFile(context)
      val uri = UriUtil.getUri(context, photoFile, BuildConfig.APPLICATION_ID)
      if (uri != null) {
        pendingUri = uri
        runCatching { launcher.launch(uri) }
      }
    }
  }
}

private fun createImageFile(context: Context): File {
  val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
  val imageFileName = "IMG_" + timeStamp + "_.jpg"
  val storageDir = File(context.externalCacheDir, "Reminder")
  if (!storageDir.exists()) storageDir.mkdirs()
  return File(storageDir, imageFileName)
}

private const val TAG = "CameraPicker"
