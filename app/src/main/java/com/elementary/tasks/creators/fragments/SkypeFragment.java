package com.elementary.tasks.creators.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.FragmentReminderSkypeBinding;
import com.elementary.tasks.reminder.models.Reminder;

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

public class SkypeFragment extends RepeatableTypeFragment {

    private static final String TAG = "DateFragment";

    private FragmentReminderSkypeBinding binding;

    public SkypeFragment() {
    }

    @Override
    public Reminder prepare() {
        if (getInterface() == null) return null;
        if (!SuperUtil.isSkypeClientInstalled(getContext())) {
            showInstallSkypeDialog();
            return null;
        }
        if (TextUtils.isEmpty(getInterface().getSummary())) {
            getInterface().showSnackbar(getString(R.string.task_summary_is_empty));
            return null;
        }
        String number = binding.skypeContact.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            getInterface().showSnackbar(getString(R.string.you_dont_insert_number));
            return null;
        }
        Reminder reminder = getInterface().getReminder();
        int type = getType(binding.skypeGroup.getCheckedRadioButtonId());
        long startTime = binding.dateView.getDateTime();
        long before = binding.beforeView.getBeforeValue();
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(getContext(), R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setTarget(number);
        reminder.setType(type);
        long repeat = binding.repeatView.getRepeat();
        reminder.setRepeatInterval(repeat);
        reminder.setExportToCalendar(binding.exportToCalendar.isChecked());
        reminder.setExportToTasks(binding.exportToTasks.isChecked());
        reminder.setClear(getInterface());
        reminder.setRemindBefore(before);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        if (!TimeCount.isCurrent(reminder.getEventTime())) {
            Toast.makeText(getContext(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return null;
        }
        return reminder;
    }

    private void showInstallSkypeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setMessage(R.string.skype_is_not_installed);
        builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            SuperUtil.installSkype(getContext());
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
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
        binding = FragmentReminderSkypeBinding.inflate(inflater, container, false);
        binding.repeatView.enablePrediction(true);
        binding.dateView.setEventListener(binding.repeatView.getEventListener());
        binding.skypeChat.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                getInterface().setEventHint(getString(R.string.message));
            } else {
                getInterface().setEventHint(getString(R.string.remind_me));
            }
        });
        if (getInterface().isExportToCalendar()) {
            binding.exportToCalendar.setVisibility(View.VISIBLE);
        } else {
            binding.exportToCalendar.setVisibility(View.GONE);
        }
        if (getInterface().isExportToTasks()) {
            binding.exportToTasks.setVisibility(View.VISIBLE);
        } else {
            binding.exportToTasks.setVisibility(View.GONE);
        }
        editReminder();
        return binding.getRoot();
    }

    private int getType(int checkedId) {
        int type = Reminder.BY_SKYPE_CALL;
        switch (checkedId) {
            case R.id.skypeCall:
                type = Reminder.BY_SKYPE_CALL;
                break;
            case R.id.skypeChat:
                type = Reminder.BY_SKYPE;
                break;
            case R.id.skypeVideo:
                type = Reminder.BY_SKYPE_VIDEO;
                break;
        }
        return type;
    }

    private void editReminder() {
        if (getInterface().getReminder() == null) return;
        Reminder reminder = getInterface().getReminder();
        binding.exportToCalendar.setChecked(reminder.isExportToCalendar());
        binding.exportToTasks.setChecked(reminder.isExportToTasks());
        binding.dateView.setDateTime(reminder.getEventTime());
        binding.repeatView.setDateTime(reminder.getEventTime());
        binding.repeatView.setRepeat(reminder.getRepeatInterval());
        binding.beforeView.setBefore(reminder.getRemindBefore());
        int type = reminder.getType();
        if (type == Reminder.BY_SKYPE_CALL) binding.skypeCall.setChecked(true);
        else if (type == Reminder.BY_SKYPE_VIDEO) binding.skypeVideo.setChecked(true);
        else if (type == Reminder.BY_SKYPE) binding.skypeChat.setChecked(true);
        if (reminder.getTarget() != null) {
            binding.skypeContact.setText(reminder.getTarget());
        }
    }
}
