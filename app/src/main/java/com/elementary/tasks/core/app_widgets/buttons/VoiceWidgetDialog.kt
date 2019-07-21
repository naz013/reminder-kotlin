package com.elementary.tasks.core.app_widgets.buttons

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import org.koin.android.ext.android.inject

class VoiceWidgetDialog : FragmentActivity() {

    private val viewModel: ConversationViewModel by lazy {
        ViewModelProviders.of(this).get(ConversationViewModel::class.java)
    }
    private val notifier: Notifier by inject()
    private val prefs: Prefs by inject()
    private val language: Language by inject()
    private var mIsLogged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
        if (Module.hasMicrophone(this)) {
            if (prefs.hasPinCode && !mIsLogged) {
                PinLoginActivity.verify(this)
            } else {
                startVoiceRecognitionActivity()
            }
        } else {
            finish()
        }
    }

    private fun startVoiceRecognitionActivity() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true, prefs, language)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startVoiceRecognitionActivity()
            } else {
                finish()
            }
        } else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?: ArrayList()
            viewModel.parseResults(matches, true, this)
            if (prefs.isSbNotificationEnabled) {
                notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
            }
            finish()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        const val VOICE_RECOGNITION_REQUEST_CODE = 109
        private const val ARG_LOGGED = "arg_logged"

        fun openLogged(context: Context, intent: Intent? = null) {
            if (intent == null) {
                context.startActivity(Intent(context, VoiceWidgetDialog::class.java)
                        .putExtra(ARG_LOGGED, true))
            } else {
                intent.putExtra(ARG_LOGGED, true)
                context.startActivity(intent)
            }
        }
    }
}
