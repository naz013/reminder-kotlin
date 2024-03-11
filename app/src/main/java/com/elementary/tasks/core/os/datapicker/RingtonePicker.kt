package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.os.readParcelable

class RingtonePicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (Uri) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

  constructor(
    activity: ComponentActivity,
    resultCallback: (Uri) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (Uri) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun pickRingtone() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      result.data?.readParcelable(
        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
        Uri::class.java
      )?.also { resultCallback.invoke(it) }
    }
  }

  private fun getIntent(): Intent {
    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
    intent.putExtra(
      RingtoneManager.EXTRA_RINGTONE_TITLE,
      getActivity().getString(R.string.select_ringtone_for_notifications)
    )
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
    return intent
  }
}
