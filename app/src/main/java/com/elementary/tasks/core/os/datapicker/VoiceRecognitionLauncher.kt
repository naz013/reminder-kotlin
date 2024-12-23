package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.voice.VoiceWidgetDialog
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.voice.ConversationActivity
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.context.intentForClass
import com.github.naz013.ui.common.locale.Language
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VoiceRecognitionLauncher private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (List<String>) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
),
  KoinComponent {

  private val language by inject<Language>()
  private val prefs by inject<Prefs>()

  constructor(
    activity: ComponentActivity,
    resultCallback: (List<String>) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (List<String>) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun recognize(isLiveSupported: Boolean) {
    try {
      launch(getIntent(isLiveSupported))
    } catch (e: Throwable) {
      getActivity().toast(R.string.no_recognizer_found)
    }
  }

  override fun dispatchResult(result: ActivityResult) {
    val matches = if (result.resultCode == Activity.RESULT_OK) {
      result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        ?: emptyList<String>()
    } else {
      emptyList()
    }
    resultCallback.invoke(matches)
  }

  private fun getIntent(isLiveSupported: Boolean): Intent {
    val intent: Intent
    when {
      isLiveSupported -> {
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
          RecognizerIntent.EXTRA_LANGUAGE,
          language.getLanguage(prefs.voiceLocale)
        )
        intent.putExtra(
          RecognizerIntent.EXTRA_PROMPT,
          getActivity().getString(R.string.say_something)
        )
      }

      prefs.isLiveEnabled -> {
        (getActivity() as? VoiceWidgetDialog)?.finish()
        intent = getActivity().intentForClass(ConversationActivity::class.java)
      }

      else -> {
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.getLanguage(prefs.voiceLocale))
        intent.putExtra(
          RecognizerIntent.EXTRA_PROMPT,
          getActivity().getString(R.string.say_something)
        )
      }
    }
    return intent
  }
}
