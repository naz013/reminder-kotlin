package com.elementary.tasks.core.appWidgets.voiceControl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent

import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.viewModels.conversation.ConversationViewModel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class VoiceWidgetDialog : FragmentActivity() {

    private var viewModel: ConversationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = ThemeUtil.getInstance(this)
        setTheme(cs.dialogStyle)
        startVoiceRecognitionActivity()

        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
    }

    private fun startVoiceRecognitionActivity() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            viewModel!!.parseResults(matches, true)
        }
        Notifier.updateReminderPermanent(this, PermanentReminderReceiver.ACTION_SHOW)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {

        const val VOICE_RECOGNITION_REQUEST_CODE = 109
    }
}
