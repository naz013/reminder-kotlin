package com.github.naz013.ui.common.login

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator

class LoginLauncher private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (Boolean) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

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
    return LoginApi.authIntent(getActivity())
  }
}
