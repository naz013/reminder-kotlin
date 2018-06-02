package com.elementary.tasks.core.app_widgets.voice_control;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import com.elementary.tasks.core.services.PermanentReminderReceiver;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class VoiceWidgetDialog extends FragmentActivity {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    private ConversationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil cs = ThemeUtil.getInstance(this);
        setTheme(cs.getDialogStyle());
        startVoiceRecognitionActivity();

        viewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
    }

    public void startVoiceRecognitionActivity() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            viewModel.parseResults(matches, true);
        }
        Notifier.updateReminderPermanent(this, PermanentReminderReceiver.ACTION_SHOW);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
