package com.elementary.tasks.reminder.preview;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.fragments.AdvancedMapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.IntervalUtil;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding;
import com.elementary.tasks.databinding.ListItemTaskBinding;
import com.elementary.tasks.databinding.NoteListItemBinding;
import com.elementary.tasks.google_tasks.TaskActivity;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TasksConstants;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.notes.NotePreviewActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

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
public class ReminderPreviewActivity extends ThemedActivity {

    private static final String TAG = "ReminderPreviewActivity";

    private ActivityReminderPreviewBinding binding;
    private AdvancedMapFragment mGoogleMap;
    private ReminderViewModel viewModel;

    @NonNull
    private List<Long> list = new ArrayList<>();
    @Nullable
    private NoteItem mNoteItem;
    @Nullable
    private TaskItem mTaskItem;
    @NonNull
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private Reminder reminder;

    @NonNull
    private ReadyListener mReadyCallback = object -> {
        if (object == null) return;
        if (object instanceof NoteItem) {
            this.mNoteItem = (NoteItem) object;
            showNote();
        } else if (object instanceof TaskItem) {
            this.mTaskItem = (TaskItem) object;
            showTask();
        }
    };
    @NonNull
    private MapCallback mMapReadyCallback = new MapCallback() {
        @Override
        public void onMapReady() {
            mGoogleMap.setSearchEnabled(false);
            if (reminder != null) showMapData(reminder);
        }
    };
    @NonNull
    private GoogleMap.OnMarkerClickListener mOnMarkerClick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            mGoogleMap.moveCamera(marker.getPosition(), 0, 0, 0, 0);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = getIntent().getIntExtra(Constants.INTENT_ID, 0);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_preview);
        initActionBar();
        initViews();

        initViewModel(id);
    }

    private void initViewModel(int id) {
        ReminderViewModel.Factory factory = new ReminderViewModel.Factory(getApplication(), id);
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel.class);
        viewModel.reminder.observe(this, reminder -> {
            if (reminder != null) {
                showInfo(reminder);
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        closeWindow();
                        break;
                }
            }
        });
    }

    private void showTask() {
        if (mTaskItem != null) {
            ListItemTaskBinding binding = ListItemTaskBinding.inflate(LayoutInflater.from(this));
            binding.setTaskItem(mTaskItem);
            binding.setClick(v -> startActivity(new Intent(ReminderPreviewActivity.this, TaskActivity.class)
                    .putExtra(Constants.INTENT_ID, mTaskItem.getTaskId())
                    .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)));
            this.binding.dataContainer.addView(binding.getRoot());
        }
    }

    private void showNote() {
        if (mNoteItem != null) {
            NoteListItemBinding binding = NoteListItemBinding.inflate(LayoutInflater.from(this));
            binding.setNoteItem(mNoteItem);
            binding.noteClick.setOnClickListener(v ->
                    startActivity(new Intent(ReminderPreviewActivity.this, NotePreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, mNoteItem.getKey())));
            this.binding.dataContainer.addView(binding.getRoot());
        }
    }

    private void showMapData(Reminder reminder) {
        Place place = reminder.getPlaces().get(0);
        double lat = place.getLatitude();
        double lon = place.getLongitude();
        binding.mapContainer.setVisibility(View.VISIBLE);
        binding.location.setVisibility(View.VISIBLE);
        binding.location.setText(String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.getLatitude(), place.getLongitude(), reminder.getPlaces().size()));
        mGoogleMap.addMarker(new LatLng(lat, lon), reminder.getSummary(), true, true, place.getRadius());
    }

    private void showInfo(Reminder reminder) {
        this.reminder = reminder;
        if (reminder != null) {
            binding.statusSwitch.setChecked(reminder.isActive());
            if (!reminder.isActive()) {
                binding.statusText.setText(R.string.disabled);
            } else {
                binding.statusText.setText(R.string.enabled4);
            }
            binding.windowTypeView.setText(getWindowType(reminder.getWindowType()));
            binding.taskText.setText(reminder.getSummary());
            binding.type.setText(ReminderUtils.getTypeString(this, reminder.getType()));
            binding.itemPhoto.setImageResource(getThemeUtil().getReminderIllustration(reminder.getType()));
            long due = TimeUtil.getDateTimeFromGmt(reminder.getEventTime());
            if (due > 0) {
                binding.time.setText(TimeUtil.getFullDateTime(due, getPrefs().is24HourFormatEnabled(), false));
                String repeatStr = IntervalUtil.getInterval(this, reminder.getRepeatInterval());
                if (Reminder.isBase(reminder.getType(), Reminder.BY_WEEK)) {
                    repeatStr = ReminderUtils.getRepeatString(this, reminder.getWeekdays());
                }
                if (repeatStr != null) {
                    binding.repeat.setText(repeatStr);
                } else {
                    binding.repeat.setVisibility(View.GONE);
                }
            } else {
                binding.time.setVisibility(View.GONE);
                binding.repeat.setVisibility(View.GONE);
            }
            if (Reminder.isGpsType(reminder.getType())) {
                initMap();
            } else {
                binding.location.setVisibility(View.GONE);
                binding.mapContainer.setVisibility(View.GONE);
            }
            String numberStr = reminder.getTarget();
            if (!TextUtils.isEmpty(numberStr)) {
                binding.number.setText(numberStr);
            } else {
                binding.number.setVisibility(View.GONE);
            }

            File file = null;
            if (!TextUtils.isEmpty(reminder.getMelodyPath())) {
                file = new File(reminder.getMelodyPath());
            } else {
                String path = getPrefs().getMelodyFile();
                if (path != null && !Sound.isDefaultMelody(path)) {
                    file = new File(path);
                } else {
                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    if (soundUri != null && soundUri.getPath() != null) {
                        file = new File(soundUri.getPath());
                    }
                }
            }
            if (file != null) binding.melody.setText(file.getName());

            int catColor = 0;
            if (reminder.getGroup() != null) {
                binding.group.setText(reminder.getGroup().getTitle());
                catColor = reminder.getGroup().getColor();
            }
            int mColor = getThemeUtil().getColor(getThemeUtil().getCategoryColor(catColor));
            binding.appBar.setBackgroundColor(mColor);
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(catColor));
            }
            binding.dataContainer.removeAllViewsInLayout();
            showAttachment(reminder);
            new Thread(new NoteThread(mReadyCallback, reminder.getNoteId())).start();
            new Thread(new TaskThread(mReadyCallback, reminder.getUuId())).start();
        }
    }

    private String getWindowType(int reminderWType) {
        int windowType = Prefs.getInstance(this).getReminderType();
        boolean ignore = Prefs.getInstance(this).isIgnoreWindowType();
        if (!ignore) {
            windowType = reminderWType;
        }
        return windowType == 0 ? getString(R.string.full_screen) : getString(R.string.simple);
    }

    private void showAttachment(Reminder reminder) {
        if (reminder != null) {
            if (reminder.getAttachmentFile() != null) {
                File file = new File(reminder.getAttachmentFile());
                binding.attachmentView.setText(file.getName());
                binding.attachmentView.setVisibility(View.VISIBLE);
            } else {
                binding.attachmentView.setVisibility(View.GONE);
            }
        } else {
            binding.attachmentView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();
        switch (ids) {
            case R.id.action_delete:
                removeReminder();
                return true;
            case android.R.id.home:
                closeWindow();
                return true;
            case R.id.action_make_copy:
                makeCopy();
                return true;
            case R.id.action_edit:
                editReminder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editReminder() {
        if (reminder != null) {
            startActivity(new Intent(this, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, reminder.getUniqueId()));
        }
    }

    private void removeReminder() {
        if (reminder != null) {
            viewModel.moveToTrash(reminder);
        }
    }

    private void makeCopy() {
        if (reminder != null) {
            int type = reminder.getType();
            if (!Reminder.isGpsType(type) && !Reminder.isSame(type, Reminder.BY_TIME)) {
                showDialog();
            }
        }
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            mUiHandler.post(this::finishAfterTransition);
        } else {
            finish();
        }
    }

    public void showDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int hour = 0;
        int minute = 0;
        list.clear();
        List<String> time = new ArrayList<>();
        boolean is24 = getPrefs().is24HourFormatEnabled();
        do {
            if (hour == 23 && minute == 30) {
                hour = -1;
            } else {
                long tmp = calendar.getTimeInMillis();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                list.add(tmp);
                time.add(TimeUtil.getTime(calendar.getTime(), is24));
                calendar.setTimeInMillis(tmp + AlarmManager.INTERVAL_HALF_HOUR);
            }
        } while (hour != -1);
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.choose_time);
        builder.setItems(time.toArray(new String[0]), (dialog, which) -> {
            dialog.dismiss();
            saveCopy(which);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveCopy(int which) {
        LogUtil.d(TAG, "saveCopy: " + which);
        if (reminder != null) {
            viewModel.copyReminder(reminder, list.get(which), reminder.getSummary() + " - cope");
        }
    }

    private void initViews() {
        binding.switchWrapper.setOnClickListener(v -> switchClick());
        binding.mapContainer.setVisibility(View.GONE);
    }

    private void switchClick() {
        if (reminder != null) {
            viewModel.toggleReminder(reminder);
        }
    }

    private void initMap() {
        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false, getPrefs().getMarkerStyle(), getThemeUtil().isDark());
        mGoogleMap.setCallback(mMapReadyCallback);
        mGoogleMap.setOnMarkerClick(mOnMarkerClick);
        getSupportFragmentManager().beginTransaction()
                .replace(binding.mapContainer.getId(), mGoogleMap)
                .addToBackStack(null)
                .commit();
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class NoteThread implements Runnable {

        private ReadyListener listener;
        private String uuId;

        NoteThread(ReadyListener listener, String uuId) {
            this.listener = listener;
            this.uuId = uuId;
        }

        @Override
        public void run() {
            NoteItem item = RealmDb.getInstance().getNote(uuId);
            runOnUiThread(() -> {
                if (listener != null && item != null) listener.onReady(item);
            });
        }
    }

    private class TaskThread implements Runnable {

        private ReadyListener listener;
        private String uuId;

        TaskThread(ReadyListener listener, String uuId) {
            this.listener = listener;
            this.uuId = uuId;
        }

        @Override
        public void run() {
            TaskItem item = RealmDb.getInstance().getTaskByReminder(uuId);
            runOnUiThread(() -> {
                if (listener != null && item != null) listener.onReady(item);
            });
        }
    }

    interface ReadyListener {
        void onReady(Object object);
    }
}
