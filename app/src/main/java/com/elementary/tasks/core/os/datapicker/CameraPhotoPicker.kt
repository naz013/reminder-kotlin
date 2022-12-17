package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.UriUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraPhotoPicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (Uri) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
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
    val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (Module.isNougat) {
      val photoFile = createImageFile()
      imageUri = UriUtil.getUri(getActivity(), photoFile)
      pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
      runCatching {
        launch(pictureIntent)
      }
    } else {
      val values = ContentValues()
      values.put(MediaStore.Images.Media.TITLE, "Picture")
      values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
      imageUri =
        getActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
      runCatching {
        launch(pictureIntent)
      }
    }
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val uri = imageUri ?: return
      resultCallback.invoke(uri)
    }
  }

  private fun createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "IMG_" + timeStamp + "_"
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    if (!storageDir.exists()) storageDir.mkdirs()
    return File(storageDir, "$imageFileName.jpg")
  }

  private fun getExternalFilesDir(directoryPictures: String): File {
    val sd = Environment.getExternalStorageDirectory()
    return File(sd, File(directoryPictures, "Reminder").toString())
  }
}
