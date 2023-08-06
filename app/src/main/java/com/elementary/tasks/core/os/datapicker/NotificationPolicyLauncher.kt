package com.elementary.tasks.core.os.datapicker

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class NotificationPolicyLauncher private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

  constructor(
    activity: ComponentActivity
  ) : this(ActivityLauncherCreator(activity))

  constructor(
    fragment: Fragment
  ) : this(FragmentLauncherCreator(fragment))

  fun openSettings() {
    runCatching {
      launch(getIntent())
    }
  }

  override fun dispatchResult(result: ActivityResult) {
  }

  private fun getIntent(): Intent {
    return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
  }
}
