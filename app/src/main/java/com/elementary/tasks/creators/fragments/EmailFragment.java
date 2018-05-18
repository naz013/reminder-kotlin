package com.elementary.tasks.creators.fragments;

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
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.FragmentReminderEmailBinding;
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

public class EmailFragment extends RepeatableTypeFragment {

    private static final String TAG = "DateFragment";

    private FragmentReminderEmailBinding binding;

    public EmailFragment() {
    }

    @Override
    public Reminder prepare() {
        if (getInterface() == null) return null;
        Reminder reminder = getInterface().getReminder();
        int type = Reminder.BY_DATE_EMAIL;
        String email = binding.mail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !email.matches(".*@.*..*")) {
            getInterface().showSnackbar(getString(R.string.email_is_incorrect));
            return null;
        }
        String subjectString = binding.subject.getText().toString().trim();
        if (TextUtils.isEmpty(subjectString)) {
            getInterface().showSnackbar(getString(R.string.you_dont_insert_any_message));
            return null;
        }
        long startTime = binding.dateView.getDateTime();
        long before = binding.beforeView.getBeforeValue();
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(getContext(), R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setSubject(subjectString);
        reminder.setSummary(getInterface().getSummary());
        reminder.setTarget(email);
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
        binding.repeatView.enablePrediction(true);
        binding.dateView.setEventListener(binding.repeatView.getEventListener());
        getInterface().setEventHint(getString(R.string.message));
        getInterface().setHasAutoExtra(true, getString(R.string.enable_sending_email_automatically));
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

    private void editReminder() {
        if (getInterface().getReminder() == null) return;
        Reminder reminder = getInterface().getReminder();
        binding.exportToCalendar.setChecked(reminder.isExportToCalendar());
        binding.exportToTasks.setChecked(reminder.isExportToTasks());
        binding.dateView.setDateTime(reminder.getEventTime());
        binding.repeatView.setDateTime(reminder.getEventTime());
        binding.repeatView.setRepeat(reminder.getRepeatInterval());
        binding.mail.setText(reminder.getTarget());
        binding.subject.setText(reminder.getSubject());
        binding.beforeView.setBefore(reminder.getRemindBefore());
    }
}
