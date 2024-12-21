package com.elementary.tasks.core.os.datapicker

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.github.naz013.feature.common.android.toast

class TtsLauncher private constructor(
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

  fun checkTts() {
    try {
      launch(getIntent())
    } catch (e: Throwable) {
      getActivity().toast(R.string.no_recognizer_found)
    }
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
      resultCallback(true)
    } else {
      resultCallback(false)
    }
  }

  private fun getIntent(): Intent {
    val checkTTSIntent = Intent()
    checkTTSIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
    return checkTTSIntent
  }
}
