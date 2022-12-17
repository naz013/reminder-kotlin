package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.apps.SelectApplicationActivity
import com.elementary.tasks.core.utils.Constants

class ApplicationPicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (String) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

  constructor(
    activity: ComponentActivity,
    resultCallback: (String) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (String) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun pickApplication() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val appPackage = result.data?.getStringExtra(Constants.SELECTED_APPLICATION) ?: ""
      resultCallback.invoke(appPackage)
    }
  }

  private fun getIntent(): Intent {
    return Intent(getActivity(), SelectApplicationActivity::class.java)
  }
}
