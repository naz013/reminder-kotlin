package com.elementary.tasks.core.os.datapicker

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.utils.UriUtil
import com.github.naz013.logging.Logger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraPhotoPicker private constructor(
  launcherCreator: LauncherCreator<Uri, Boolean>,
  private val resultCallback: (Uri) -> Unit
) : IntentPicker<Uri, Boolean>(
  ActivityResultContracts.TakePicture(),
  launcherCreator
) {

  private var imageUri: Uri? = null

  constructor(
    activity: ComponentActivity,
    resultCallback: (Uri) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (Uri) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun takePhoto() {
    val photoFile = createImageFile()
    UriUtil.getUri(getActivity(), photoFile)?.also {
      imageUri = it
      runCatching {
        launch(it)
      }
    }
  }

  override fun dispatchResult(result: Boolean) {
    Logger.d("dispatchResult: $result, $imageUri")
    if (result) {
      val uri = imageUri ?: return
      resultCallback.invoke(uri)
    }
  }

  private fun createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "IMG_" + timeStamp + "_.jpg"
    val storageDir = getExternalFilesDir()
    if (!storageDir.exists()) storageDir.mkdirs()
    return File(storageDir, imageFileName)
  }

  private fun getExternalFilesDir(): File {
    return File(getActivity().externalCacheDir, "Reminder")
  }
}
