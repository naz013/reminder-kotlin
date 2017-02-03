package com.elementary.tasks.voice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;

import com.backdoor.simpleai.Action;
import com.backdoor.simpleai.ActionType;
import com.backdoor.simpleai.Model;
import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.AddBirthdayActivity;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.dialogs.VolumeDialog;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.Recognize;
import com.elementary.tasks.databinding.ActivityConversationBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.AddReminderActivity;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.List;

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

public class ConversationActivity extends ThemedActivity {

    private static final String TAG = "ConversationActivity";
    private static final int AUDIO_CODE = 255000;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private ActivityConversationBinding binding;

    private ConversationAdapter mAdapter;
    private Recognize recognize;

    private RecognitionListener mRecognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            LogUtil.d(TAG, "onReadyForSpeech: ");
        }

        @Override
        public void onBeginningOfSpeech() {
            LogUtil.d(TAG, "onBeginningOfSpeech: ");
        }

        @Override
        public void onRmsChanged(float v) {
            LogUtil.d(TAG, "onRmsChanged: " + v);
            v = v * 2000;
            double db = 0;
            if (v > 1) {
                db = 20 * Math.log10(v);
            }
            binding.recordingView.setVolume((float) db);
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            LogUtil.d(TAG, "onBufferReceived: ");
        }

        @Override
        public void onEndOfSpeech() {
            LogUtil.d(TAG, "onEndOfSpeech: ");
        }

        @Override
        public void onError(int i) {
            LogUtil.d(TAG, "onError: " + i);
            showErrorMessage(i);
        }

        @Override
        public void onResults(Bundle bundle) {
            binding.recordingView.loading();
            if (bundle == null) {
                showSilentMessage();
                return;
            }
            parseResults(bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            LogUtil.d(TAG, "onPartialResults: ");
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            LogUtil.d(TAG, "onEvent: ");
        }
    };
    private ConversationAdapter.InsertCallback mInsertCallback = new ConversationAdapter.InsertCallback() {
        @Override
        public void onItemAdded() {
            binding.conversationList.scrollToPosition(0);
        }
    };

    private void showErrorMessage(int i) {
        stopView();
        String text = getErrorText(i);
        mAdapter.addReply(new Reply(Reply.RESPONSE, text));
    }

    private void showSilentMessage() {
        stopView();
        mAdapter.addReply(new Reply(Reply.RESPONSE, "Did you say something?"));
    }

    private void parseResults(List<String> list) {
        LogUtil.d(TAG, "parseResults: " + list);
        if (list == null || list.isEmpty()) {
            showSilentMessage();
            return;
        }
        Model model = null;
        String suggestion = null;
        for (String s : list) {
            suggestion = s;
            model = recognize.findSuggestion(s);
            if (model != null) break;
        }
        stopView();
        if (model != null) {
            performResult(model, suggestion);
        } else {
            mAdapter.addReply(new Reply(Reply.REPLY, list.get(0)));
            mAdapter.addReply(new Reply(Reply.RESPONSE, "Can not recognize your command."));
        }
    }

    private void stopView() {
        binding.recordingView.stop(false);
    }

    private void performResult(Model model, String s) {
        mAdapter.addReply(new Reply(Reply.REPLY, s.toLowerCase()));
        LogUtil.d(TAG, "performResult: " + model);
        ActionType actionType = model.getType();
        if (actionType == ActionType.REMINDER) {
            mAdapter.addReply(new Reply(Reply.RESPONSE, "Reminder created"));
            Reminder reminder = recognize.createReminder(model);
            EventControl control = EventControlImpl.getController(this, reminder);
            control.start();
            mAdapter.addReply(new Reply(Reply.REMINDER, reminder));
        } else if (actionType == ActionType.NOTE) {
            mAdapter.addReply(new Reply(Reply.RESPONSE, "Note saved"));
            NoteItem item = recognize.saveNote(model.getSummary(), false);
            mAdapter.addReply(new Reply(Reply.NOTE, item));
        } else if (actionType == ActionType.ACTION) {
            Action action = model.getAction();
            if (action == Action.BIRTHDAY) {
                startActivity(new Intent(this, AddBirthdayActivity.class));
            } else if (action == Action.REMINDER) {
                startActivity(new Intent(this, AddReminderActivity.class));
            } else if (action == Action.VOLUME) {
                startActivity(new Intent(this, VolumeDialog.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
            } else if (action == Action.TRASH) {
                clearTrash();
            } else if (action == Action.DISABLE) {
                disableReminders();
            }
        } else if (actionType == ActionType.GROUP) {
            mAdapter.addReply(new Reply(Reply.RESPONSE, "Group saved"));
            GroupItem item = recognize.saveGroup(model, false);
            mAdapter.addReply(new Reply(Reply.GROUP, item));
        }
    }

    private void disableReminders() {
        recognize.disableAllReminders();
        mAdapter.addReply(new Reply(Reply.RESPONSE, "All reminders were disabled"));
    }

    private void clearTrash() {
        recognize.emptyTrash();
        mAdapter.addReply(new Reply(Reply.RESPONSE, "Trash was cleared"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation);
        recognize = new Recognize(this);
        initSpeech();
        initRecognizer();
        initList();
        binding.recordingView.setOnClickListener(view -> micClick());
    }

    private void initList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        binding.conversationList.setLayoutManager(layoutManager);
        mAdapter = new ConversationAdapter(this);
        mAdapter.setInsertListener(mInsertCallback);
        binding.conversationList.setAdapter(mAdapter);
    }

    private void initRecognizer() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Language.getLanguage(Prefs.getInstance(this).getVoiceLocale()));
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    private void initSpeech() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(mRecognitionListener);
    }

    private void micClick() {
        if (binding.recordingView.isWorking()) {
            speech.stopListening();
            initSpeech();
        }
        if (!Permissions.checkPermission(this, Permissions.RECORD_AUDIO)) {
            Permissions.requestPermission(this, AUDIO_CODE, Permissions.RECORD_AUDIO);
            return;
        }
        binding.recordingView.start();
        speech.startListening(recognizerIntent);
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
            case SpeechRecognizer.ERROR_CLIENT:
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Something went wrong";
                break;
            case SpeechRecognizer.ERROR_SERVER:
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speech != null) {
            speech.cancel();
            speech.destroy();
            speech = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AUDIO_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    micClick();
                }
                break;
        }
    }
}
