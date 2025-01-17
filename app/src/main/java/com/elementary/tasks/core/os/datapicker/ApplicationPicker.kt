package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.apps.SelectApplicationActivity
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator
import com.github.naz013.ui.common.context.intentForClass

class ApplicationPicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private var resultCallback: (String) -> Unit
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

  private fun pickApplication() {
    launch(getIntent())
  }

  fun pickApplication(resultCallback: (String) -> Unit) {
    this.resultCallback = resultCallback
    pickApplication()
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val appPackage = result.data?.getStringExtra(IntentKeys.SELECTED_APPLICATION) ?: ""
      resultCallback.invoke(appPackage)
    }
  }

  private fun getIntent(): Intent {
    return getActivity().intentForClass(SelectApplicationActivity::class.java)
  }
}
