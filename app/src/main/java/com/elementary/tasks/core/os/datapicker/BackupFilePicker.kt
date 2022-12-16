package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import org.koin.core.component.KoinComponent

class BackupFilePicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (Uri) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
), KoinComponent {

  constructor(
    activity: ComponentActivity,
    resultCallback: (Uri) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (Uri) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun pickRbakFile() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      result.data?.data?.also { resultCallback.invoke(it) }
    }
  }

  private fun getIntent(): Intent {
    val intent = Intent()
    intent.type = "*/*"
    intent.action = Intent.ACTION_GET_CONTENT
    return Intent.createChooser(intent, getActivity().getString(R.string.choose_file))
  }
}
