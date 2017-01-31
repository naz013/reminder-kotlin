package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.birthdays.CheckBirthdaysAsync;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.services.PermanentBirthdayService;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentBirthdaysSettingsBinding;

import java.util.Calendar;
import java.util.List;

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

public class BirthdaySettingsFragment extends BaseSettingsFragment implements TimePickerDialog.OnTimeSetListener {

    private static final int CONTACTS_CODE = 302;
    private static final int BIRTHDAYS_CODE = 303;

    private FragmentBirthdaysSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBirthdaysSettingsBinding.inflate(inflater, container, false);
        initBirthdayReminderPrefs();
        initBirthdaysWidgetPrefs();
        initPermanentPrefs();
        initDaysToPrefs();
        initBirthdayTimePrefs();
        initContactsPrefs();
        initContactsAutoPrefs();
        initScanPrefs();
        initNotificationPrefs();
        return binding.getRoot();
    }

    private void initNotificationPrefs() {
        binding.birthdayNotificationPrefs.setOnClickListener(view -> replaceFragment(new BirthdayNotificationFragment(), getString(R.string.birthday_notification)));
        binding.birthdayNotificationPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void initScanPrefs() {
        binding.contactsScanPrefs.setDependentView(binding.useContactsPrefs);
        binding.contactsScanPrefs.setOnClickListener(view -> scanForBirthdays());
        binding.contactsScanPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void scanForBirthdays() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(getActivity(), BIRTHDAYS_CODE, Permissions.READ_CONTACTS);
            return;
        }
        new CheckBirthdaysAsync(getActivity(), true).execute();
    }

    private void initContactsAutoPrefs() {
        binding.autoScanPrefs.setChecked(Prefs.getInstance(mContext).isContactAutoCheckEnabled());
        binding.autoScanPrefs.setOnClickListener(view -> changeAutoPrefs());
        binding.autoScanPrefs.setDependentView(binding.useContactsPrefs);
        binding.autoScanPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void changeAutoPrefs() {
        boolean isChecked = binding.autoScanPrefs.isChecked();
        binding.autoScanPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setContactAutoCheckEnabled(!isChecked);
        if (!isChecked) {
            new AlarmReceiver().enableBirthdayCheckAlarm(mContext);
        } else {
            new AlarmReceiver().cancelBirthdayCheckAlarm(mContext);
        }
    }

    private void initContactsPrefs() {
        binding.useContactsPrefs.setChecked(Prefs.getInstance(mContext).isContactBirthdaysEnabled());
        binding.useContactsPrefs.setOnClickListener(view -> changeContactsPrefs());
        binding.useContactsPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void changeContactsPrefs() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(getActivity(), CONTACTS_CODE, Permissions.READ_CONTACTS);
            return;
        }
        boolean isChecked = binding.useContactsPrefs.isChecked();
        binding.useContactsPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setContactBirthdaysEnabled(!isChecked);
    }

    private void initBirthdayTimePrefs() {
        binding.reminderTimePrefs.setOnClickListener(view -> showTimeDialog());
        binding.reminderTimePrefs.setValueText(Prefs.getInstance(mContext).getBirthdayTime());
        binding.reminderTimePrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void showTimeDialog() {
        Calendar calendar = TimeUtil.getBirthdayCalendar(Prefs.getInstance(mContext).getBirthdayTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(mContext, this, hour, minute, Prefs.getInstance(mContext).is24HourFormatEnabled()).show();
    }

    private void initDaysToPrefs() {
        binding.daysToPrefs.setOnClickListener(view -> showDaysToDialog());
        binding.daysToPrefs.setValue(Prefs.getInstance(mContext).getDaysToBirthday());
        binding.daysToPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void showDaysToDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.days_to_birthday);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(5);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int daysToBirthday = Prefs.getInstance(mContext).getDaysToBirthday();
        b.seekBar.setProgress(daysToBirthday);
        b.titleView.setText(String.valueOf(daysToBirthday));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> saveDays(b.seekBar.getProgress()));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void saveDays(int progress) {
        Prefs.getInstance(mContext).setDaysToBirthday(progress);
        initDaysToPrefs();
    }

    private void initPermanentPrefs() {
        binding.birthdayPermanentPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayPermanentEnabled());
        binding.birthdayPermanentPrefs.setOnClickListener(view -> changeBirthdayPermanentPrefs());
        binding.birthdayPermanentPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void changeBirthdayPermanentPrefs() {
        boolean isChecked = binding.birthdayPermanentPrefs.isChecked();
        binding.birthdayPermanentPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayPermanentEnabled(!isChecked);
        if (!isChecked) {
            mContext.startService(new Intent(mContext, PermanentBirthdayService.class).setAction(PermanentBirthdayService.ACTION_SHOW));
            new AlarmReceiver().enableBirthdayPermanentAlarm(mContext);
        } else {
            mContext.startService(new Intent(mContext, PermanentBirthdayService.class).setAction(PermanentBirthdayService.ACTION_HIDE));
            new AlarmReceiver().cancelBirthdayPermanentAlarm(mContext);
        }
    }

    private void initBirthdaysWidgetPrefs() {
        binding.widgetShowPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayInWidgetEnabled());
        binding.widgetShowPrefs.setOnClickListener(view -> changeWidgetPrefs());
        binding.widgetShowPrefs.setDependentView(binding.birthReminderPrefs);
    }

    private void changeWidgetPrefs() {
        boolean isChecked = binding.widgetShowPrefs.isChecked();
        binding.widgetShowPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayInWidgetEnabled(!isChecked);
        UpdatesHelper.getInstance(mContext).updateCalendarWidget();
        UpdatesHelper.getInstance(mContext).updateWidget();
    }

    private void initBirthdayReminderPrefs() {
        binding.birthReminderPrefs.setOnClickListener(view -> changeBirthdayPrefs());
        binding.birthReminderPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayReminderEnabled());
    }

    private void changeBirthdayPrefs() {
        boolean isChecked = binding.birthReminderPrefs.isChecked();
        binding.birthReminderPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayReminderEnabled(!isChecked);
        if (!isChecked) {
            new AlarmReceiver().enableBirthdayAlarm(mContext);
        } else {
            cleanBirthdays();
            new AlarmReceiver().cancelBirthdayAlarm(mContext);
        }
    }

    private void cleanBirthdays(){
        new Thread(() -> {
            Looper.prepare();
            List<BirthdayItem> list = RealmDb.getInstance().getAllBirthdays();
            for (int i = list.size() - 1; i >= 0; i--) {
                RealmDb.getInstance().deleteBirthday(list.remove(i));
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.birthdays));
            mCallback.onFragmentSelect(this);
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        Prefs.getInstance(mContext).setBirthdayTime(TimeUtil.getBirthdayTime(i, i1));
        initBirthdayTimePrefs();
        if (Prefs.getInstance(mContext).isBirthdayReminderEnabled()) {
            new AlarmReceiver().enableBirthdayAlarm(mContext);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CONTACTS_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeContactsPrefs();
                }
                break;
            case BIRTHDAYS_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanForBirthdays();
                }
                break;
        }
    }
}
