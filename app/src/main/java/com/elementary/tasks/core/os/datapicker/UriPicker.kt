package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator

class UriPicker(launcherCreator: LauncherCreator<Intent, ActivityResult>) :
  IntentPicker<Intent, ActivityResult>(
    ActivityResultContracts.StartActivityForResult(),
    launcherCreator
  ) {

  private var resultCallback: ((Uri?) -> Unit)? = null

  constructor(activity: ComponentActivity) : this(ActivityLauncherCreator(activity))

  constructor(fragment: Fragment) : this(FragmentLauncherCreator(fragment))

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      result.data?.data?.also { resultCallback?.invoke(it) }
    }
  }

  fun launchIntent(intent: Intent, callback: (Uri?) -> Unit) {
    resultCallback = callback
    launch(intent)
  }
}
