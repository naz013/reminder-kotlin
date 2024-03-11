package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class MultipleUriPicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>
) :
  IntentPicker<Intent, ActivityResult>(
    ActivityResultContracts.StartActivityForResult(),
    launcherCreator
  ) {

  private var resultCallback: ((List<Uri>) -> Unit)? = null

  constructor(activity: ComponentActivity) : this(ActivityLauncherCreator(activity))

  constructor(fragment: Fragment) : this(FragmentLauncherCreator(fragment))

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val fileUri = result.data?.data
      val clipData = result.data?.clipData
      if (fileUri != null) {
        takePermission(fileUri)
        resultCallback?.invoke(listOf(fileUri))
      } else if (clipData != null) {
        val list = mutableListOf<Uri>()
        val count = clipData.itemCount
        for (i in 0 until count) {
          val item = clipData.getItemAt(i)
          takePermission(item.uri)
          list.add(item.uri)
        }
        resultCallback?.invoke(list)
      }
    }
  }

  fun pickFiles(resultCallback: (List<Uri>) -> Unit) {
    this.resultCallback = resultCallback
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "*/*"
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    launch(intent)
  }

  private fun takePermission(uri: Uri) {
    getActivity().contentResolver.takePersistableUriPermission(
      uri,
      (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    )
  }
}
