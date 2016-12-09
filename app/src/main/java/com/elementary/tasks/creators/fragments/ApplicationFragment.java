package com.elementary.tasks.creators.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.elementary.tasks.core.apps.ApplicationActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.databinding.FragmentReminderApplicationBinding;
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

public class ApplicationFragment extends RepeatableTypeFragment {

    private static final String TAG = "DateFragment";

    private FragmentReminderApplicationBinding binding;
    private String selectedPackage;
    public View.OnClickListener appClick = v ->
            getActivity().startActivityForResult(new Intent(getActivity(), ApplicationActivity.class), Constants.REQUEST_CODE_APPLICATION);

    public ApplicationFragment() {
    }

    @Override
    public boolean save() {
        if (mInterface == null) return false;
        Reminder reminder = mInterface.getReminder();
        int type = getType();
        String number;
        if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            number = selectedPackage;
            if (TextUtils.isEmpty(number)) {
                mInterface.showSnackbar(getString(R.string.you_dont_select_application));
                return false;
            }
        } else {
            number = binding.phoneNumber.getText().toString().trim();
            if (TextUtils.isEmpty(number) || number.matches(".*https?://")) {
                mInterface.showSnackbar(getString(R.string.you_dont_insert_link));
                return false;
            }
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://" + number;
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
        fillExtraData(reminder);
        Log.d(TAG, "save: " + type);
        long startTime = TimeCount.getInstance(mContext).generateStartEvent(type, binding.dateView.getDateTime(), null, 0);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        Log.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true));
        RealmDb.getInstance().saveObject(reminder);
//        new AlarmReceiver().enableReminder(mContext, reminder.getUuId());
        return true;
    }

    private int getType() {
        if (binding.application.isChecked()) {
            return Reminder.BY_DATE_APP;
        } else {
            return Reminder.BY_DATE_LINK;
        }
    }

    private void fillExtraData(Reminder reminder) {
        reminder.setSummary(mInterface.getSummary());
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
        binding = FragmentReminderApplicationBinding.inflate(inflater, container, false);
        binding.pickApplication.setOnClickListener(appClick);
        if (ThemeUtil.getInstance(mContext).isDark()) binding.pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        else binding.pickApplication.setImageResource(R.drawable.ic_launch_black_24dp);
        binding.repeatView.enablePrediction(true);
        binding.dateView.setEventListener(binding.repeatView.getEventListener());
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
        binding.phoneNumber.setVisibility(View.GONE);
        binding.application.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!b) {
                ViewUtils.collapse(binding.applicationLayout);
                ViewUtils.expand(binding.phoneNumber);
            } else {
                ViewUtils.collapse(binding.phoneNumber);
                ViewUtils.expand(binding.applicationLayout);
            }
        });
        editReminder();
        return binding.getRoot();
    }

    private void editReminder() {
        if (mInterface.getReminder() == null) return;
        Reminder reminder = mInterface.getReminder();
        binding.exportToCalendar.setChecked(reminder.isExportToCalendar());
        binding.exportToTasks.setChecked(reminder.isExportToTasks());
        binding.dateView.setDateTime(reminder.getEventTime());
        binding.repeatView.setDateTime(reminder.getEventTime());
        binding.repeatView.setProgress(reminder.getRepeatInterval());
        if (reminder.getTarget() != null) {
            if (Reminder.isSame(reminder.getType(), Reminder.BY_DATE_APP)) {
                binding.application.setChecked(true);
                selectedPackage = reminder.getTarget();
                binding.applicationName.setText(getAppName());
            } else {
                binding.browser.setChecked(true);
                binding.phoneNumber.setText(reminder.getTarget());
            }
        }
    }

    private String getAppName() {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(selectedPackage, 0);
        } catch (final PackageManager.NameNotFoundException ignored) {}
        return (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_APPLICATION && resultCode == Activity.RESULT_OK) {
            selectedPackage = data.getStringExtra(Constants.SELECTED_APPLICATION);
            binding.applicationName.setText(getAppName());
        }
    }
}
