package com.elementary.tasks.notes.create;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.BitmapUtils;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.PhotoSelectionUtil;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.view_models.notes.NoteViewModel;
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel;
import com.elementary.tasks.core.views.ColorPickerView;
import com.elementary.tasks.databinding.ActivityCreateNoteBinding;
import com.elementary.tasks.databinding.DialogColorPickerLayoutBinding;
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration;
import com.elementary.tasks.notes.editor.ImageEditActivity;
import com.elementary.tasks.notes.list.ImagesGridAdapter;
import com.elementary.tasks.notes.list.KeepLayoutManager;
import com.elementary.tasks.notes.preview.ImagePreviewActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import static com.elementary.tasks.notes.preview.NotePreviewActivity.PREVIEW_IMAGES;

/**
 * Copyright 2016 Nazar Suhovich
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
public class CreateNoteActivity extends ThemedActivity implements PhotoSelectionUtil.UriCallback {

    private static final String TAG = "CreateNoteActivity";
    public static final int MENU_ITEM_DELETE = 12;
    private static final int EDIT_CODE = 11223;
    private static final int AUDIO_CODE = 255000;

    private int mHour = 0;
    private int mMinute = 0;
    private int mYear = 0;
    private int mMonth = 0;
    private int mDay = 1;
    private int mColor = 0;
    private int mFontStyle = 9;
    private int mEditPosition = -1;
    private float mLastX = -1;

    private ActivityCreateNoteBinding binding;
    private NoteViewModel viewModel;
    private ReminderViewModel reminderViewModel;
    @Nullable
    private final ImagesGridAdapter mAdapter = new ImagesGridAdapter();
    @Nullable
    private ProgressDialog mProgress;

    @Nullable
    private Note mItem;
    @Nullable
    private Reminder mReminder;

    @Nullable
    private SpeechRecognizer speech = null;
    @Nullable
    private PhotoSelectionUtil photoSelectionUtil;

    private RecognitionListener mRecognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            LogUtil.d(TAG, "onReadyForSpeech: ");
        }

        @Override
        public void onBeginningOfSpeech() {
            LogUtil.d(TAG, "onBeginningOfSpeech: ");
            showRecording();
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
            LogUtil.d(TAG, "onBufferReceived: " + Arrays.toString(bytes));
        }

        @Override
        public void onEndOfSpeech() {
            hideRecording();
            LogUtil.d(TAG, "onEndOfSpeech: ");
        }

        @Override
        public void onError(int i) {
            LogUtil.d(TAG, "onError: " + i);
            releaseSpeech();
            hideRecording();
        }

        @Override
        public void onResults(Bundle bundle) {
            ArrayList res = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (res != null && res.size() > 0) {
                setText(StringUtils.capitalize(res.get(0).toString().toLowerCase()));
            }
            LogUtil.d(TAG, "onResults: " + res);
            releaseSpeech();
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            ArrayList res = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (res != null && res.size() > 0) {
                setText(res.get(0).toString().toLowerCase());
            }
            LogUtil.d(TAG, "onPartialResults: " + res);
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            LogUtil.d(TAG, "onEvent: ");
        }
    };

    private void setText(String text) {
        binding.taskMessage.setText(text);
        binding.taskMessage.setSelection(binding.taskMessage.getText().length());
    }

    private void showRecording() {
        binding.recordingView.start();
        binding.recordingView.setVisibility(View.VISIBLE);
    }

    private void hideRecording() {
        binding.recordingView.stop();
        binding.recordingView.setVisibility(View.GONE);
    }

    private DecodeImagesAsync.DecodeListener mDecodeCallback = result -> {
        if (!result.isEmpty()) {
            mAdapter.addNextImages(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);

        photoSelectionUtil = new PhotoSelectionUtil(this, this);

        initActionBar();
        initMenu();
        initBgContainer();
        ViewUtils.fadeInAnimation(binding.layoutContainer);
        binding.remindDate.setOnClickListener(v -> dateDialog());
        binding.remindTime.setOnClickListener(v -> timeDialog());
        binding.micButton.setOnClickListener(v -> micClick());
        binding.discardReminder.setOnClickListener(v -> ViewUtils.collapse(binding.remindContainer));
        initImagesList();

        loadNote();

        updateBackground();
        updateTextStyle();
        showSaturationAlert();
    }

    private void initRecognizer() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(mRecognitionListener);
        speech.startListening(recognizerIntent);
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

    private void micClick() {
        if (speech != null) {
            hideRecording();
            releaseSpeech();
            return;
        }
        if (!Permissions.checkPermission(this, Permissions.RECORD_AUDIO)) {
            Permissions.requestPermission(this, AUDIO_CODE, Permissions.RECORD_AUDIO);
            return;
        }
        initRecognizer();
    }

    private void showSaturationAlert() {
        if (getPrefs().isNoteHintShowed()) {
            return;
        }
        getPrefs().setNoteHintShowed(true);
        // TODO: 31.05.2018 Add banner about note color opacity
//        mAlerter = Alerter.create(this)
//                .setTitle(R.string.swipe_left_or_right_to_adjust_saturation)
//                .setText(R.string.click_to_hide)
//                .enableInfiniteDuration(true)
//                .setBackgroundColorRes(getThemeUtil().colorPrimaryDark(mColor))
//                .setOnClickListener(v -> {
//                    if (mAlerter != null) mAlerter.hide();
//                })
//                .show();
    }

    private void initBgContainer() {
        binding.touchView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                if (mLastX != -1) {
                    int currentOpacity = getPrefs().getNoteColorOpacity();
                    if (x - mLastX > 0) {
                        if (currentOpacity < 100) {
                            currentOpacity += 1;
                        }
                    } else {
                        if (currentOpacity > 0) {
                            currentOpacity -= 1;
                        }
                    }
                    getPrefs().setNoteColorOpacity(currentOpacity);
                    updateBackground();
                }
                mLastX = x;
                return true;
            }
            return false;
        });
    }

    private void initMenu() {
        binding.bottomBarView.setBackgroundColor(getThemeUtil().getBackgroundStyle());
        binding.colorButton.setOnClickListener(view -> showColorDialog());
        binding.imageButton.setOnClickListener(view -> {
            if (photoSelectionUtil != null) photoSelectionUtil.selectImage();
        });
        binding.reminderButton.setOnClickListener(view -> switchReminder());
        binding.fontButton.setOnClickListener(view -> showStyleDialog());
    }

    private void switchReminder() {
        if (!isReminderAttached()) {
            setDateTime(null);
            ViewUtils.expand(binding.remindContainer);
        } else {
            ViewUtils.collapse(binding.remindContainer);
        }
    }

    private void loadNote() {
        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.INTENT_ID);
        initViewModel(id);
        if (intent.getData() != null) {
            String filePath = intent.getStringExtra(Constants.FILE_PICKED);
            Uri name = intent.getData();
            loadNoteFromFile(filePath, name);
        }
    }

    private void initViewModel(String id) {
        viewModel = ViewModelProviders.of(this, new NoteViewModel.Factory(getApplication(), id)).get(NoteViewModel.class);
        viewModel.note.observe(this, this::showNote);
        viewModel.editedPicture.observe(this, note -> {
            if (note != null && !note.getImages().isEmpty()) {
                mAdapter.setImage(note.getImages().get(0), mEditPosition);
            }
        });
        viewModel.reminder.observe(this, this::showReminder);
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        finish();
                        break;
                }
            }
        });

        reminderViewModel = ViewModelProviders.of(this, new ReminderViewModel.Factory(getApplication(), 0)).get(ReminderViewModel.class);
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        binding.taskMessage.setTextSize(getPrefs().getNoteTextSize() + 12);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0f);
        }
        binding.appBar.setVisibility(View.VISIBLE);
    }

    private void loadNoteFromFile(String filePath, Uri name) {
        try {
            if (name != null) {
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mItem = BackupTool.getInstance().getNote(cr, name);
                } else {
                    mItem = BackupTool.getInstance().getNote(name.getPath(), null);
                }
            } else {
                mItem = BackupTool.getInstance().getNote(filePath, null);
            }
            showNote(mItem);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void showNote(Note note) {
        this.mItem = note;
        if (note != null) {
            mColor = note.getColor();
            mFontStyle = note.getStyle();
            setText(note.getSummary());
            mAdapter.setImages(note.getImages());
        } else {
            mColor = new Random().nextInt(16);
            if (getPrefs().isNoteColorRememberingEnabled()) {
                mColor = getPrefs().getLastNoteColor();
            }
        }
    }

    private void initImagesList() {
        mAdapter.setEditable(true);
        mAdapter.setActionsListener((view, position, noteImage, actions) -> {
            switch (actions) {
                case EDIT:
                    editImage(position);
                    break;
                case OPEN:
                    openImagePreview(position);
                    break;
            }
        });
        binding.imagesList.setLayoutManager(new KeepLayoutManager(this, 6, mAdapter));
        binding.imagesList.addItemDecoration(new GridMarginDecoration(getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        binding.imagesList.setAdapter(mAdapter);
    }

    private void openImagePreview(int position) {
        Note note = new Note();
        note.setKey(PREVIEW_IMAGES);
        note.setImages(mAdapter.getData());
        viewModel.saveNote(note);
        startActivity(new Intent(this, ImagePreviewActivity.class)
                .putExtra(Constants.INTENT_ID, note.getKey())
                .putExtra(Constants.INTENT_POSITION, position));
    }

    private void editImage(int position) {
        Note note = new Note();
        note.setKey(PREVIEW_IMAGES);
        note.setImages(Collections.singletonList(mAdapter.getItem(position)));
        viewModel.saveNote(note);
        this.mEditPosition = position;
        startActivityForResult(new Intent(this, ImageEditActivity.class), EDIT_CODE);
    }

    private void showReminder(Reminder reminder) {
        mReminder = reminder;
        if (reminder != null) {
            setDateTime(reminder.getEventTime());
            ViewUtils.expand(binding.remindContainer);
        }
    }

    private void hideProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

    private void showProgress() {
        mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);
    }

    private void shareNote() {
        createObject();
        showProgress();
        BackupTool.CreateCallback callback = this::sendNote;
        new Thread(() -> BackupTool.getInstance().createNote(mItem, callback)).start();
    }

    private void sendNote(File file) {
        hideProgress();
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mItem != null) {
            TelephonyUtil.sendNote(file, this, mItem.getSummary());
        }
    }

    private void setDateTime(String eventTime) {
        Calendar calendar = Calendar.getInstance();
        if (eventTime == null) calendar.setTimeInMillis(System.currentTimeMillis());
        else calendar.setTimeInMillis(TimeUtil.getDateTimeFromGmt(eventTime));
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH);
        mYear = calendar.get(Calendar.YEAR);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        binding.remindDate.setText(TimeUtil.getDate(calendar.getTimeInMillis()));
        binding.remindTime.setText(TimeUtil.getTime(calendar.getTime(), getPrefs().is24HourFormatEnabled()));
    }

    private boolean isReminderAttached() {
        return binding.remindContainer.getVisibility() == View.VISIBLE;
    }

    private boolean createObject() {
        String note = binding.taskMessage.getText().toString().trim();
        List<NoteImage> images = mAdapter.getData();
        if (TextUtils.isEmpty(note) && images.isEmpty()) {
            binding.taskMessage.setError(getString(R.string.must_be_not_empty));
            return false;
        }
        if (mItem == null) {
            mItem = new Note();
        }
        mItem.setSummary(note);
        mItem.setDate(TimeUtil.getGmtDateTime());
        mItem.setImages(images);
        mItem.setColor(mColor);
        mItem.setStyle(mFontStyle);
        return true;
    }

    private void saveNote() {
        if (!createObject()) {
            return;
        }
        boolean hasReminder = isReminderAttached();
        if (!hasReminder && mItem != null) removeNoteFromReminder();
        viewModel.saveNote(mItem);
        if (hasReminder) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth, mDay, mHour, mMinute);
            createReminder(mItem.getKey(), calendar);
        }
        UpdatesHelper.getInstance(this).updateNotesWidget();
        finish();
    }

    private void createReminder(String key, Calendar calendar) {
        if (mReminder == null) {
            mReminder = new Reminder();
        }
        mReminder.setType(Reminder.BY_DATE);
        mReminder.setDelay(0);
        mReminder.setEventCount(0);
        mReminder.setUseGlobal(true);
        mReminder.setNoteId(key);
        mReminder.setActive(true);
        mReminder.setRemoved(false);
        if (mItem != null) mReminder.setSummary(mItem.getSummary());
        else mReminder.setSummary("");
        Group def = reminderViewModel.defaultGroup.getValue();
        if (def != null) {
            mReminder.setGroupUuId(def.getUuId());
        }
        long startTime = calendar.getTimeInMillis();
        mReminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        mReminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        if (!TimeCount.isCurrent(mReminder.getEventTime())) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return;
        }
        reminderViewModel.saveAndStartReminder(mReminder);
    }

    private void removeNoteFromReminder() {
        if (mReminder != null) {
            reminderViewModel.deleteReminder(mReminder, false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mItem != null && getPrefs().isAutoSaveEnabled()) {
            saveNote();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                shareNote();
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case R.id.action_add:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showColorDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(getString(R.string.change_color));
        DialogColorPickerLayoutBinding binding = DialogColorPickerLayoutBinding.inflate(LayoutInflater.from(this));
        ColorPickerView view = binding.pickerView;
        view.setSelectedColor(mColor);
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        view.setListener(code -> {
            mColor = code;
            if (getPrefs().isNoteColorRememberingEnabled()) {
                getPrefs().setLastNoteColor(mColor);
            }
            updateBackground();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setMessage(getString(R.string.delete_this_note));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteNote();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNote() {
        if (mItem != null) {
            viewModel.deleteNote(mItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_note, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (photoSelectionUtil != null)
            photoSelectionUtil.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EDIT_CODE:
                    if (mEditPosition != -1) updateImage();
                    break;
            }
        }
    }

    private void updateImage() {
        viewModel.loadEditedPicture();
    }

    private void addImageFromUri(Uri uri) {
        if (uri == null) return;
        Bitmap bitmapImage = null;
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(this, uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bitmapImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            mAdapter.addImage(new NoteImage(outputStream.toByteArray()));
        }
    }

    private void updateTextStyle() {
        binding.taskMessage.setTypeface(AssetsUtil.getTypeface(this, mFontStyle));
    }

    private void updateBackground() {
        binding.layoutContainer.setBackgroundColor(getThemeUtil().getNoteLightColor(mColor));
        binding.appBar.setBackgroundColor(getThemeUtil().getNoteLightColor(mColor));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(mColor));
        }
    }

    private void showStyleDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(getString(R.string.font_style));
        ArrayList<String> contacts = new ArrayList<>();
        contacts.clear();
        contacts.add("Black");
        contacts.add("Black Italic");
        contacts.add("Bold");
        contacts.add("Bold Italic");
        contacts.add("Italic");
        contacts.add("Light");
        contacts.add("Light Italic");
        contacts.add("Medium");
        contacts.add("Medium Italic");
        contacts.add("Regular");
        contacts.add("Thin");
        contacts.add("Thin Italic");
        final LayoutInflater inflater = LayoutInflater.from(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, contacts) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null);
                }
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setTypeface(getTypeface(position));
                textView.setText(contacts.get(position));
                return convertView;
            }

            private Typeface getTypeface(int position) {
                return AssetsUtil.getTypeface(CreateNoteActivity.this, position);
            }
        };
        builder.setSingleChoiceItems(adapter, mFontStyle, (dialog, which) -> {
            mFontStyle = which;
            updateTextStyle();
        });
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void dateDialog() {
        TimeUtil.showDatePicker(this, myDateCallBack, mYear, mMonth, mDay);
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = (view, year, monthOfYear, dayOfMonth) -> {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        String dayStr;
        String monthStr;
        if (mDay < 10) {
            dayStr = "0" + mDay;
        } else {
            dayStr = String.valueOf(mDay);
        }
        if (mMonth < 9) {
            monthStr = "0" + (mMonth + 1);
        } else {
            monthStr = String.valueOf(mMonth + 1);
        }
        binding.remindDate.setText(SuperUtil.appendString(dayStr, "/", monthStr, "/", String.valueOf(mYear)));
    };

    protected void timeDialog() {
        TimeUtil.showTimePicker(this, myCallBack, mHour, mMinute);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = (view, hourOfDay, minute) -> {
        mHour = hourOfDay;
        mMinute = minute;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        binding.remindTime.setText(TimeUtil.getTime(c.getTime(), getPrefs().is24HourFormatEnabled()));
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(binding.taskMessage.getWindowToken(), 0);
        releaseSpeech();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        if (photoSelectionUtil != null)
            photoSelectionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AUDIO_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    micClick();
                }
                break;
        }
    }

    @Override
    public void onImageSelected(@Nullable Uri uri, @Nullable ClipData clipData) {
        if (uri != null) {
            addImageFromUri(uri);
        } else if (clipData != null) {
            new DecodeImagesAsync(this, mDecodeCallback, clipData.getItemCount()).execute(clipData);
        }
    }
}