package com.elementary.tasks.reminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.fragments.AdvancedMapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.IntervalUtil;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding;
import com.elementary.tasks.databinding.ListItemTaskBinding;
import com.elementary.tasks.databinding.NoteListItemBinding;
import com.elementary.tasks.google_tasks.TaskActivity;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TasksConstants;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.notes.NotePreviewActivity;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    private String id;
    private Reminder item;
    private List<Long> list = new ArrayList<>();
    private NoteItem mNoteItem;
    private TaskItem mTaskItem;

    private ReadyListener mReadyCallback = object -> {
        if (object == null) return;
        if (object instanceof NoteItem) {
            showNote((NoteItem) object);
        } else if (object instanceof TaskItem) {
            showTask((TaskItem) object);
        }
    };
    private MapCallback mMapReadyCallback = new MapCallback() {
        @Override
        public void onMapReady() {
            mGoogleMap.setSearchEnabled(false);
            showMapData();
        }
    };
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
        id = getIntent().getStringExtra(Constants.INTENT_ID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_preview);
        initActionBar();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        item = RealmDb.getInstance().getReminder(id);
        loadInfo();
    }

    private void showTask(TaskItem taskItem) {
        this.mTaskItem = taskItem;
        ListItemTaskBinding binding = ListItemTaskBinding.inflate(LayoutInflater.from(this));
        binding.setTaskItem(taskItem);
        binding.setClick(v -> startActivity(new Intent(ReminderPreviewActivity.this, TaskActivity.class)
                .putExtra(Constants.INTENT_ID, mTaskItem.getTaskId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)));
        this.binding.dataContainer.addView(binding.getRoot());
    }

    private void showNote(NoteItem noteItem) {
        this.mNoteItem = noteItem;
        NoteListItemBinding binding = NoteListItemBinding.inflate(LayoutInflater.from(this));
        binding.setNoteItem(noteItem);
        binding.setClick(v -> startActivity(new Intent(this, NotePreviewActivity.class)
                .putExtra(Constants.INTENT_ID, mNoteItem.getKey())));
        this.binding.dataContainer.addView(binding.getRoot());
    }

    private void showMapData() {
        Place place = item.getPlaces().get(0);
        double lat = place.getLatitude();
        double lon = place.getLongitude();
        binding.mapContainer.setVisibility(View.VISIBLE);
        binding.location.setVisibility(View.VISIBLE);
        binding.location.setText(String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.getLatitude(), place.getLongitude(), item.getPlaces().size()));
        mGoogleMap.addMarker(new LatLng(lat, lon), item.getSummary(), true, true, place.getRadius());
    }

    private void loadInfo() {
        if (item != null) {
            binding.statusSwitch.setChecked(item.isActive());
            if (!item.isActive()) {
                binding.statusText.setText(R.string.disabled);
            } else {
                binding.statusText.setText(R.string.enabled4);
            }
            binding.taskText.setText(item.getSummary());
            binding.type.setText(ReminderUtils.getTypeString(this, item.getType()));
            binding.itemPhoto.setImageResource(getThemeUtil().getReminderIllustration(item.getType()));
            long due = TimeUtil.getDateTimeFromGmt(item.getEventTime());
            if (due > 0) {
                binding.time.setText(TimeUtil.getFullDateTime(due, getPrefs().is24HourFormatEnabled(), false));
                String repeatStr = IntervalUtil.getInterval(this, item.getRepeatInterval());
                if (Reminder.isBase(item.getType(), Reminder.BY_WEEK)) {
                    repeatStr = ReminderUtils.getRepeatString(this, item.getWeekdays());
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
            if (Reminder.isGpsType(item.getType())) {
                initMap();
            } else {
                binding.location.setVisibility(View.GONE);
                binding.mapContainer.setVisibility(View.GONE);
            }
            String numberStr = item.getTarget();
            if (!TextUtils.isEmpty(numberStr)) {
                binding.number.setText(numberStr);
            } else {
                binding.number.setVisibility(View.GONE);
            }

            String melodyStr = item.getMelodyPath();
            File file;
            if (!TextUtils.isEmpty(melodyStr)) {
                file = new File(melodyStr);
            } else {
                String path = getPrefs().getMelodyFile();
                if (path != null && !Sound.isDefaultMelody(path)) {
                    file = new File(path);
                } else {
                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    file = new File(soundUri.getPath());
                }
            }
            melodyStr = file.getName();
            binding.melody.setText(melodyStr);

            int catColor = 0;
            GroupItem group = RealmDb.getInstance().getGroup(item.getGroupUuId());
            if (group != null) {
                binding.group.setText(group.getTitle());
                catColor = group.getColor();
            }
            int mColor = getThemeUtil().getColor(getThemeUtil().getCategoryColor(catColor));
            binding.appBar.setBackgroundColor(mColor);
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(catColor));
            }
            binding.dataContainer.removeAllViewsInLayout();
            new Thread(new NoteThread(mReadyCallback, item.getNoteId())).start();
            new Thread(new TaskThread(mReadyCallback, item.getUuId())).start();
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
        startActivity(new Intent(this, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, item.getUuId()));
    }

    private void removeReminder() {
        if (RealmDb.getInstance().moveToTrash(item.getUuId())) {
            EventControl control = EventControlFactory.getController(this, item.setRemoved(true));
            control.stop();
        }
        closeWindow();
    }

    private void makeCopy() {
        if (item != null) {
            int type = item.getType();
            if (!Reminder.isGpsType(type) && !Reminder.isSame(type, Reminder.BY_TIME)) {
                showDialog();
            }
        }
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            finishAfterTransition();
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
        list = new ArrayList<>();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_time);
        builder.setItems(time.toArray(new String[time.size()]), (dialog, which) -> {
            dialog.dismiss();
            saveCopy(which);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveCopy(int which) {
        LogUtil.d(TAG, "saveCopy: " + which);
        Reminder newItem = item.copy();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeInMillis(list.get(which));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(TimeUtil.getDateTimeFromGmt(newItem.getEventTime()));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        newItem.setEventTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()));
        newItem.setStartTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()));
        RealmDb.getInstance().saveObject(newItem);
        EventControl control = EventControlFactory.getController(this, newItem);
        control.start();
        Toast.makeText(this, R.string.reminder_created, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        binding.switchWrapper.setOnClickListener(v -> switchClick());
        binding.mapContainer.setVisibility(View.GONE);
    }

    private void switchClick() {
        EventControl control = EventControlFactory.getController(this, item);
        if (!control.onOff()) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
        }
        item = RealmDb.getInstance().getReminder(getIntent().getStringExtra(Constants.INTENT_ID));
        loadInfo();
    }


    private void initMap() {
        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false, getPrefs().getMarkerStyle(), getThemeUtil().isDark());
        mGoogleMap.setCallback(mMapReadyCallback);
        mGoogleMap.setOnMarkerClick(mOnMarkerClick);
        getFragmentManager().beginTransaction()
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
