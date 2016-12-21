package com.elementary.tasks.creators.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.file_explorer.FileExplorerActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.FragmentReminderEmailBinding;
import com.elementary.tasks.reminder.models.Reminder;

import java.io.File;

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

public class EmailFragment extends RepeatableTypeFragment {

    private static final int FILE_REQUEST = 323;
    private static final String TAG = "DateFragment";

    private FragmentReminderEmailBinding binding;
    private String attachment;

    public View.OnClickListener fileClick = v -> {
        if (Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL)) {
            getActivity().startActivityForResult(new Intent(getActivity(), FileExplorerActivity.class)
                    .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST);
        } else {
            Permissions.requestPermission(getActivity(), 331,
                    Permissions.READ_EXTERNAL);
        }
    };

    public EmailFragment() {
    }

    @Override
    public boolean save() {
        if (mInterface == null) return false;
        Reminder reminder = mInterface.getReminder();
        int type = Reminder.BY_DATE_EMAIL;
        String email = binding.mail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !email.matches(".*@.*..*")) {
            mInterface.showSnackbar(getString(R.string.email_is_incorrect));
            return false;
        }
        String subjectString = binding.subject.getText().toString().trim();
        if (TextUtils.isEmpty(subjectString)) {
            mInterface.showSnackbar(getString(R.string.you_dont_insert_any_message));
            return false;
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setAttachmentFile(attachment);
        reminder.setSubject(subjectString);
        reminder.setSummary(mInterface.getSummary());
        reminder.setTarget(email);
        reminder.setType(type);
        long repeat = binding.repeatView.getRepeat();
        reminder.setRepeatInterval(repeat);
        reminder.setExportToCalendar(binding.exportToCalendar.isChecked());
        reminder.setExportToTasks(binding.exportToTasks.isChecked());
        fillExtraData(reminder);
        Log.d(TAG, "save: " + type);
        long startTime = TimeCount.getInstance(mContext).generateStartEvent(type, binding.dateView.getDateTime(), null, 0, 0);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        Log.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true));
        RealmDb.getInstance().saveObject(reminder);
        EventControl control = EventControlImpl.getController(mContext, reminder);
        control.start();
        return true;
    }

    private void fillExtraData(Reminder reminder) {
        reminder.setGroupUuId(mInterface.getGroup());
        reminder.setRepeatLimit(mInterface.getRepeatLimit());
        reminder.setColor(mInterface.getLedColor());
        reminder.setMelodyPath(mInterface.getMelodyPath());
        reminder.setVolume(mInterface.getVolume());
        reminder.setAuto(mInterface.getAuto());
        reminder.setActive(true);
        reminder.setRemoved(false);
        reminder.setVibrate(mInterface.getVibration());
        reminder.setNotifyByVoice(mInterface.getVoice());
        reminder.setRepeatNotification(mInterface.getNotificationRepeat());
        reminder.setUseGlobal(mInterface.getUseGlobal());
        reminder.setUnlock(mInterface.getUnlock());
        reminder.setAwake(mInterface.getWake());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_date_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_limit:
                changeLimit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReminderEmailBinding.inflate(inflater, container, false);
        binding.chooseFile.setOnClickListener(fileClick);
        if (ThemeUtil.getInstance(mContext).isDark()) binding.chooseFile.setImageResource(R.drawable.ic_attach_file_white_24dp);
        else binding.chooseFile.setImageResource(R.drawable.ic_attach_file_black_24dp);
        binding.repeatView.enablePrediction(true);
        binding.dateView.setEventListener(binding.repeatView.getEventListener());
        mInterface.setEventHint(getString(R.string.subject));
        if (mInterface.isExportToCalendar()) {
            binding.exportToCalendar.setVisibility(View.VISIBLE);
        } else {
            binding.exportToCalendar.setVisibility(View.GONE);
        }
        if (mInterface.isExportToTasks()) {
            binding.exportToTasks.setVisibility(View.VISIBLE);
        } else {
            binding.exportToTasks.setVisibility(View.GONE);
        }
        binding.fileName.setOnClickListener(v -> {
            attachment = null;
            showAttachment();
        });
        editReminder();
        return binding.getRoot();
    }

    private void editReminder() {
        if (mInterface.getReminder() == null) return;
        Reminder reminder = mInterface.getReminder();
        attachment = reminder.getAttachmentFile();
        binding.exportToCalendar.setChecked(reminder.isExportToCalendar());
        binding.exportToTasks.setChecked(reminder.isExportToTasks());
        binding.dateView.setDateTime(reminder.getEventTime());
        binding.repeatView.setDateTime(reminder.getEventTime());
        binding.repeatView.setProgress(reminder.getRepeatInterval());
        binding.mail.setText(reminder.getTarget());
        binding.subject.setText(reminder.getSubject());
        showAttachment();
    }

    private void showAttachment() {
        if (attachment != null) {
            File file = new File(attachment);
            binding.fileName.setText(file.getName());
        } else binding.fileName.setText(getString(R.string.no_files_attached));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_REQUEST) {
            if (resultCode == Activity.RESULT_OK){
                attachment = data.getStringExtra(Constants.FILE_PICKED);
                if (attachment != null) {
                    File file = new File(attachment);
                    binding.fileName.setText(file.getPath());
                    mInterface.showSnackbar(String.format(getString(R.string.file_x_attached), file.getName()),
                            getString(R.string.cancel), v -> {
                                attachment = null;
                                binding.fileName.setText(null);
                            });
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 331:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(mContext, FileExplorerActivity.class)
                            .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST);
                }
                break;
        }
    }
}
