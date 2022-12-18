package com.elementary.tasks.core.app_widgets.buttons

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.os.datapicker.VoiceRecognitionLauncher
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class VoiceWidgetDialog : FragmentActivity() {

  private val viewModel by viewModel<ConversationViewModel>()
  private val prefs by inject<Prefs>()
  private val voiceRecognitionLauncher = VoiceRecognitionLauncher(this) { processResult(it) }
  private val loginLauncher = LoginLauncher(this) {
    if (it) {
      isLogged = true
      startVoiceRecognitionActivity()
    } else {
      finish()
    }
  }
  private var isLogged = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
    if (Module.hasMicrophone(this)) {
      if (prefs.hasPinCode && !isLogged) {
        loginLauncher.askLogin()
      } else {
        startVoiceRecognitionActivity()
      }
    } else {
      finish()
    }
  }

  private fun startVoiceRecognitionActivity() {
    voiceRecognitionLauncher.recognize(true)
  }

  private fun processResult(matches: List<String>) {
    if (matches.isNotEmpty()) {
      viewModel.parseResults(matches, true, this)
      if (prefs.isSbNotificationEnabled) {
        PermanentReminderReceiver.show(this)
      }
    }
    finish()
  }
}
