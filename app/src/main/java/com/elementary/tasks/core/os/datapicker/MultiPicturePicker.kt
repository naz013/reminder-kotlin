package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.R

class MultiPicturePicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (List<Uri>) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

  constructor(
    activity: ComponentActivity,
    resultCallback: (List<Uri>) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (List<Uri>) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun pickPictures() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val imageUri = result.data?.data
      val clipData = result.data?.clipData
      if (imageUri != null) {
        resultCallback.invoke(listOf(imageUri))
      } else if (clipData != null) {
        val list = mutableListOf<Uri>()
        val count = clipData.itemCount
        for (i in 0 until count) {
          val item = clipData.getItemAt(i)
          list.add(item.uri)
        }
        resultCallback.invoke(list)
      }
    }
  }

  private fun getIntent(): Intent {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    return Intent.createChooser(intent, getActivity().getString(R.string.gallery))
  }
}
