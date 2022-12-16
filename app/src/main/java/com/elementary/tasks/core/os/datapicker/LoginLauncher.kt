package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.core.component.KoinComponent

class LoginLauncher private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (Boolean) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
), KoinComponent {

  constructor(
    activity: ComponentActivity,
    resultCallback: (Boolean) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (Boolean) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun askLogin() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    resultCallback.invoke(result.resultCode == Activity.RESULT_OK)
  }

  private fun getIntent(): Intent {
    return Intent(getActivity(), PinLoginActivity::class.java)
      .putExtra(PinLoginActivity.ARG_BACK, true)
  }
}
