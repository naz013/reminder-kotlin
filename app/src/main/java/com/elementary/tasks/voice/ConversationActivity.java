package com.elementary.tasks.voice;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import com.backdoor.engine.Action;
import com.backdoor.engine.ActionType;
import com.backdoor.engine.Model;
import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.AddBirthdayActivity;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.dialogs.VolumeDialog;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Recognize;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.ActivityConversationBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.AddReminderActivity;
import com.elementary.tasks.reminder.models.Reminder;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

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
    private static final int CHECK_CODE = 1651;

    @Nullable
    private SpeechRecognizer speech = null;
    private ActivityConversationBinding binding;

    @Nullable
    private ConversationAdapter mAdapter;
    @Nullable
    private Recognize recognize;
    @Nullable
    private TextToSpeech tts;
    private boolean isTtsReady;
    @Nullable
    private AskAction mAskAction;

    @NonNull
    private TextToSpeech.OnInitListener mTextToSpeechListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS && tts != null) {
                int result = tts.setLanguage(new Locale(Language.getLanguage(getPrefs().getVoiceLocale())));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    LogUtil.d(TAG, "This Language is not supported");
                } else {
                    isTtsReady = true;
                    addResponse(getLocalized(R.string.hi_how_can_i_help_you));
                    new Handler().postDelayed(() -> micClick(), 1500);
                }
            } else {
                LogUtil.d(TAG, "Initialization Failed!");
            }
        }
    };
    @NonNull
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
            showSilentMessage();
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
    @NonNull
    private ConversationAdapter.InsertCallback mInsertCallback = new ConversationAdapter.InsertCallback() {
        @Override
        public void onItemAdded() {
            binding.conversationList.scrollToPosition(0);
        }
    };

    private void showSilentMessage() {
        stopView();
        playTts(getLocalized(R.string.did_you_say_something));
    }

    @NonNull
    private String getLocalized(int id) {
        return Language.getLocalized(this, id);
    }

    private void parseResults(@Nullable List<String> list) {
        LogUtil.d(TAG, "parseResults: " + list);
        if (recognize == null) return;
        if (list == null || list.isEmpty()) {
            showSilentMessage();
            return;
        }
        Model model = null;
        String suggestion = null;
        for (String s : list) {
            suggestion = s;
            model = recognize.findSuggestion(s);
            if (model != null) {
                break;
            }
        }
        if (model != null) {
            performResult(model, suggestion);
        } else {
            stopView();
            if (mAdapter != null) mAdapter.addReply(new Reply(Reply.REPLY, list.get(0)));
            addResponse(getLocalized(R.string.can_not_recognize_your_command));
        }
    }

    private void performAnswer(@NonNull Model answer) {
        stopView();
        if (mAskAction != null && mAdapter != null) {
            mAdapter.removeAsk();
            if (answer.getAction() == Action.YES) {
                mAskAction.onYes();
            } else if (answer.getAction() == Action.NO) {
                mAskAction.onNo();
            }
        }
    }

    private void stopView() {
        releaseSpeech();
        binding.recordingView.stop();
    }

    private void addObjectResponse(@NonNull Reply reply) {
        stopView();
        if (mAdapter != null) mAdapter.addReply(reply);
    }

    private void performResult(@NonNull Model model, @NonNull String s) {
        if (mAskAction != null) {
            if (mAdapter != null) mAdapter.removeAsk();
        }
        if (mAdapter != null) mAdapter.addReply(new Reply(Reply.REPLY, s.toLowerCase()));
        LogUtil.d(TAG, "performResult: " + model);
        ActionType actionType = model.getType();
        if (actionType == ActionType.REMINDER) {
            reminderAction(model);
        } else if (actionType == ActionType.NOTE) {
            noteAction(model);
        } else if (actionType == ActionType.ACTION) {
            Action action = model.getAction();
            if (action == Action.BIRTHDAY) {
                stopView();
                startActivity(new Intent(this, AddBirthdayActivity.class));
            } else if (action == Action.REMINDER) {
                stopView();
                startActivity(new Intent(this, AddReminderActivity.class));
            } else if (action == Action.VOLUME) {
                stopView();
                startActivity(new Intent(this, VolumeDialog.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
            } else if (action == Action.TRASH) {
                clearTrash();
            } else if (action == Action.DISABLE) {
                disableReminders();
            } else {
                showUnsupportedMessage();
            }
        } else if (actionType == ActionType.GROUP) {
            groupAction(model);
        } else if (actionType == ActionType.ANSWER) {
            performAnswer(model);
        } else if (actionType == ActionType.SHOW) {
            stopView();
            LogUtil.d(TAG, "performResult: " + TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(model.getDateTime()), true, true));
            Action action = model.getAction();
            if (action == Action.REMINDERS) {
                showActiveReminders(TimeUtil.getDateTimeFromGmt(model.getDateTime()));
            } else if (action == Action.NOTES) {
                showNotes();
            } else if (action == Action.GROUPS) {
                showGroups();
            } else if (action == Action.ACTIVE_REMINDERS) {
                showEnabledReminders(TimeUtil.getDateTimeFromGmt(model.getDateTime()));
            } else if (action == Action.BIRTHDAYS) {
                showBirthdays(TimeUtil.getDateTimeFromGmt(model.getDateTime()));
            } else if (action == Action.SHOP_LISTS) {
                showShoppingLists();
            } else {
                showUnsupportedMessage();
            }
        } else {
            showUnsupportedMessage();
        }
    }

    private void showUnsupportedMessage() {
        stopView();
        addResponse(getLocalized(R.string.this_command_not_supported_on_that_screen));
    }

    private void showShoppingLists() {
        Container<Reminder> items = new Container<>(DataProvider.getShoppingReminders());
        if (items.isEmpty()) {
            addResponse(getLocalized(R.string.no_shopping_lists_found));
        } else {
            if (items.getList().size() == 1) {
                addResponse(getLocalized(R.string.found_one_shopping_list));
            } else {
                addResponse(getLocalized(R.string.found) + " " + items.getList().size() + " " + getLocalized(R.string.shopping_lists));
            }
            addReminderObject(items.getList().remove(0));
            if (!items.isEmpty()) addObjectResponse(new Reply(Reply.SHOW_MORE, items));
        }
    }

    private void showBirthdays(long dateTime) {
        long time = TimeUtil.getBirthdayTime(getPrefs().getBirthdayTime());
        Container<BirthdayItem> items = new Container<>(DataProvider.getBirthdays(dateTime, time));
        if (items.isEmpty()) {
            addResponse(getLocalized(R.string.no_birthdays_found));
        } else {
            if (items.getList().size() == 1) {
                addResponse(getLocalized(R.string.found_one_birthday));
            } else {
                addResponse(StringUtils.capitalize(StringUtils.lowerCase(getLocalized(R.string.found) +
                        " " + items.getList().size() + " " + getLocalized(R.string.birthdays))));
            }
            addObjectResponse(new Reply(Reply.BIRTHDAY, items.getList().remove(0)));
            if (!items.isEmpty()) addObjectResponse(new Reply(Reply.SHOW_MORE, items));
        }
    }

    private void showEnabledReminders(long dateTime) {
        Container<Reminder> items = new Container<>(DataProvider.getActiveReminders(dateTime));
        if (items.isEmpty()) {
            addResponse(getLocalized(R.string.no_reminders_found));
        } else {
            if (items.getList().size() == 1) {
                addResponse(getLocalized(R.string.found_one_reminder));
            } else {
                addResponse(getLocalized(R.string.found) + " " + items.getList().size() + " " +
                        getLocalized(R.string.reminders));
            }
            addReminderObject(items.getList().remove(0));
            if (!items.isEmpty()) addObjectResponse(new Reply(Reply.SHOW_MORE, items));
        }
    }

    private void showGroups() {
        Container<GroupItem> items = new Container<>(DataProvider.getGroups());
        if (items.isEmpty()) {
            addResponse(getLocalized(R.string.no_groups_found));
        } else {
            if (items.getList().size() == 1) {
                addResponse(getLocalized(R.string.found_one_group));
            } else {
                addResponse(StringUtils.capitalize(StringUtils.lowerCase(getLocalized(R.string.found) +
                        " " + items.getList().size() + " " + getLocalized(R.string.groups))));
            }
            addObjectResponse(new Reply(Reply.GROUP, items.getList().remove(0)));
            if (!items.isEmpty()) addObjectResponse(new Reply(Reply.SHOW_MORE, items));
        }
    }

    private void showNotes() {
        Container<NoteItem> items = new Container<>(DataProvider.getNotes());
        if (items.isEmpty()) {
            addResponse(getLocalized(R.string.no_notes_found));
        } else {
            if (items.getList().size() == 1) {
                addResponse(getLocalized(R.string.found_one_note));
            } else {
                addResponse(StringUtils.capitalize(StringUtils.lowerCase(getLocalized(R.string.found) +
                        " " + items.getList().size() + " " + getLocalized(R.string.notes))));
            }
            addObjectResponse(new Reply(Reply.NOTE, items.getList().remove(0)));
            if (!items.isEmpty()) addObjectResponse(new Reply(Reply.SHOW_MORE, items));
        }
    }

    private void showActiveReminders(long dateTime) {
        Container<Reminder> items = new Container<>(DataProvider.getReminders(dateTime));
        if (items.isEmpty()) {
            addResponse(getLocalized(R.string.no_reminders_found));
        } else {
            if (items.getList().size() == 1) {
                addResponse(getLocalized(R.string.found_one_reminder));
            } else {
                addResponse(getLocalized(R.string.found) + " " + items.getList().size() + " " +
                        getLocalized(R.string.reminders));
            }
            addReminderObject(items.getList().remove(0));
            if (!items.isEmpty()) addObjectResponse(new Reply(Reply.SHOW_MORE, items));
        }
    }

    private void addReminderObject(@NonNull Reminder reminder) {
        if (reminder.getViewType() == Reminder.REMINDER) {
            addObjectResponse(new Reply(Reply.REMINDER, reminder));
        } else {
            addObjectResponse(new Reply(Reply.SHOPPING, reminder));
        }
    }

    private void groupAction(@NonNull Model model) {
        stopView();
        if (recognize == null) return;
        addResponse(getLocalized(R.string.group_created));
        GroupItem item = recognize.createGroup(model);
        addObjectResponse(new Reply(Reply.GROUP, item));
        new Handler().postDelayed(() -> askGroupAction(item), 1000);
    }

    private void noteAction(@NonNull Model model) {
        stopView();
        if (recognize == null) return;
        addResponse(getLocalized(R.string.note_created));
        NoteItem item = recognize.createNote(model.getSummary());
        addObjectResponse(new Reply(Reply.NOTE, item));
        new Handler().postDelayed(() -> askNoteAction(item), 1000);
    }

    private void reminderAction(@NonNull Model model) {
        stopView();
        if (recognize == null) return;
        Reminder reminder = recognize.createReminder(model);
        addObjectResponse(new Reply(Reply.REMINDER, reminder));
        if (getPrefs().isTellAboutEvent()) {
            addResponse(getLocalized(R.string.reminder_created_on) + " " +
                    TimeUtil.getVoiceDateTime(reminder.getEventTime(), getPrefs().is24HourFormatEnabled(), getPrefs().getVoiceLocale()) +
            ". " + getLocalized(R.string.would_you_like_to_save_it));
            new Handler().postDelayed(() -> askReminderAction(reminder, false), 8000);
        } else {
            addResponse(getLocalized(R.string.reminder_created));
            new Handler().postDelayed(() -> askReminderAction(reminder, true), 1000);
        }
    }

    private void askGroupAction(@NonNull GroupItem groupItem) {
        addResponse(getLocalized(R.string.would_you_like_to_save_it));
        mAskAction = new AskAction() {
            @Override
            public void onYes() {
                if (recognize == null) return;
                recognize.saveGroup(groupItem, false);
                addResponse(getLocalized(R.string.group_saved));
                mAskAction = null;
            }

            @Override
            public void onNo() {
                addResponse(getLocalized(R.string.group_canceled));
                mAskAction = null;
            }
        };
        addAskReply();
        new Handler().postDelayed(this::micClick, 1500);
    }

    private void askReminderAction(@NonNull Reminder reminder, boolean ask) {
        if (ask) addResponse(getLocalized(R.string.would_you_like_to_save_it));
        mAskAction = new AskAction() {
            @Override
            public void onYes() {
                EventControl control = EventControlFactory.getController(ConversationActivity.this, reminder);
                control.start();
                addResponse(getLocalized(R.string.reminder_saved));
                mAskAction = null;
            }

            @Override
            public void onNo() {
                addResponse(getLocalized(R.string.reminder_canceled));
                mAskAction = null;
            }
        };
        addAskReply();
        new Handler().postDelayed(this::micClick, 1500);
    }

    private void askNoteAction(@NonNull NoteItem noteItem) {
        addResponse(getLocalized(R.string.would_you_like_to_save_it));
        mAskAction = new AskAction() {
            @Override
            public void onYes() {
                if (recognize == null) return;
                recognize.saveNote(noteItem, false, false);
                addResponse(getLocalized(R.string.note_saved));
                if (getPrefs().isNoteReminderEnabled()) {
                    new Handler().postDelayed(() -> askQuickReminder(noteItem), 1500);
                } else {
                    mAskAction = null;
                }
            }

            @Override
            public void onNo() {
                addResponse(getLocalized(R.string.note_canceled));
                mAskAction = null;
            }
        };
        addAskReply();
        new Handler().postDelayed(this::micClick, 1500);
    }

    private void askQuickReminder(@NonNull NoteItem noteItem) {
        addResponse(getLocalized(R.string.would_you_like_to_add_reminder));
        mAskAction = new AskAction() {
            @Override
            public void onYes() {
                if (recognize == null) return;
                Model model = recognize.findSuggestion(noteItem.getSummary());
                addResponse(getLocalized(R.string.reminder_saved));
                if (model != null && model.getType() == ActionType.REMINDER) {
                    Reminder reminder = recognize.createReminder(model);
                    EventControl control = EventControlFactory.getController(ConversationActivity.this, reminder);
                    control.start();
                    addObjectResponse(new Reply(Reply.REMINDER, reminder));
                } else {
                    Reminder reminder = recognize.saveQuickReminder(noteItem.getKey(), noteItem.getSummary());
                    addObjectResponse(new Reply(Reply.REMINDER, reminder));
                }
                mAskAction = null;
            }

            @Override
            public void onNo() {
                addResponse(getLocalized(R.string.note_saved_without_reminder));
                mAskAction = null;
            }
        };
        addAskReply();
        new Handler().postDelayed(this::micClick, 1500);
    }

    private void addAskReply() {
        if (mAdapter != null && mAskAction != null) mAdapter.addReply(new Reply(Reply.ASK, createAsk(mAskAction)));
    }

    private void addResponse(@NonNull String message) {
        if (mAdapter != null) mAdapter.addReply(new Reply(Reply.RESPONSE, message));
        playTts(message);
    }

    private void disableReminders() {
        if (recognize == null) return;
        recognize.disableAllReminders(false);
        stopView();
        addResponse(getLocalized(R.string.all_reminders_were_disabled));
    }

    private void clearTrash() {
        if (recognize == null) return;
        recognize.emptyTrash(false, () -> {
            stopView();
            addResponse(getLocalized(R.string.trash_was_cleared));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation);
        recognize = new Recognize(this);
        initList();
        binding.recordingView.setOnClickListener(view -> micClick());
        binding.settingsButton.setOnClickListener(v -> showSettingsPopup());
        binding.backButton.setOnClickListener(view -> onBackPressed());
        checkTts();
    }

    private void showSettingsPopup() {
        PopupMenu popupMenu = new PopupMenu(this, binding.settingsButton);
        popupMenu.inflate(R.menu.activity_conversation);
        popupMenu.getMenu().getItem(1).setChecked(getPrefs().isTellAboutEvent());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_locale:
                    showLanguageDialog();
                    return true;
                case R.id.action_tell:
                    getPrefs().setTellAboutEvent(!getPrefs().isTellAboutEvent());
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.language));
        List<String> locales = Language.getLanguages(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, locales);
        int language = getPrefs().getVoiceLocale();
        builder.setSingleChoiceItems(adapter, language, (dialog, which) -> {
            if (which != -1) {
                getPrefs().setVoiceLocale(which);
            }
        });
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            recreate();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playTts(@NonNull String text) {
        if (!isTtsReady || tts == null) return;
        if (Module.isLollipop()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void checkTts() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        try {
            startActivityForResult(checkTTSIntent, CHECK_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
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
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Language.getLanguage(getPrefs().getVoiceLocale()));
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(mRecognitionListener);
        speech.startListening(recognizerIntent);
    }

    private void micClick() {
        if (binding.recordingView.isWorking()) {
            if (speech != null) speech.stopListening();
            stopView();
            return;
        }
        if (!Permissions.checkPermission(this, Permissions.RECORD_AUDIO)) {
            Permissions.requestPermission(this, AUDIO_CODE, Permissions.RECORD_AUDIO);
            return;
        }
        binding.recordingView.start();
        initRecognizer();
    }

    private void releaseTts() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseSpeech();
        releaseTts();
    }

    private void releaseSpeech() {
        try {
            if (speech != null) {
                speech.stopListening();
                speech.cancel();
                speech.destroy();
                speech = null;
            }
        } catch (IllegalArgumentException ignored) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, mTextToSpeechListener);
            } else {
                showInstallTtsDialog();
            }
        }
    }

    private void showInstallTtsDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setMessage(R.string.would_you_like_to_install_tts);
        builder.setPositiveButton(R.string.install, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            installTts();
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void installTts() {
        Intent installTTSIntent = new Intent();
        installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        try {
            startActivity(installTTSIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case AUDIO_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    micClick();
                }
                break;
        }
    }

    @NonNull
    private AskAction createAsk(@NonNull AskAction askAction) {
        return new AskAction() {
            @Override
            public void onYes() {
                stopView();
                askAction.onYes();
            }

            @Override
            public void onNo() {
                stopView();
                askAction.onNo();
            }
        };
    }
}
