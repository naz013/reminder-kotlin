package com.elementary.tasks.navigation.settings.calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.calendar.CalendarEvent;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.services.EventsCheckAlarm;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboButton;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.databinding.FragmentEventsImportBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.elementary.tasks.reminder.models.Reminder;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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

public class FragmentEventsImport extends BaseSettingsFragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    public static final String EVENT_KEY = "Events";
    private static final int CALENDAR_PERM = 500;

    private FragmentEventsImportBinding binding;

    private RoboCheckBox eventsCheck;
    private Spinner eventCalendar;
    private RoboButton syncInterval;

    private int mItemSelect;
    private ArrayList<CalendarUtils.CalendarItem> list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsImportBinding.inflate(inflater, container, false);
        binding.button.setOnClickListener(this);

        syncInterval = binding.syncInterval;
        syncInterval.setOnClickListener(v -> showIntervalDialog());

        eventsCheck = binding.eventsCheck;
        RoboCheckBox autoCheck = binding.autoCheck;
        eventsCheck.setOnCheckedChangeListener(this);
        autoCheck.setOnCheckedChangeListener(this);
        autoCheck.setChecked(Prefs.getInstance(mContext).isAutoEventsCheckEnabled());

        if (autoCheck.isChecked()) syncInterval.setEnabled(true);
        else syncInterval.setEnabled(false);

        eventCalendar = binding.eventCalendar;
        return binding.getRoot();
    }

    private void showIntervalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.interval));
        final CharSequence[] items = {mContext.getString(R.string.one_hour),
                mContext.getString(R.string.six_hours),
                mContext.getString(R.string.twelve_hours),
                mContext.getString(R.string.one_day),
                mContext.getString(R.string.two_days)};

        builder.setSingleChoiceItems(items, getIntervalPosition(), (dialog, item) -> {
            mItemSelect = item;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            saveIntervalPrefs();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void saveIntervalPrefs() {
        if (mItemSelect == 0) {
            Prefs.getInstance(mContext).setAutoCheckInterval(1);
        } else if (mItemSelect == 1) {
            Prefs.getInstance(mContext).setAutoCheckInterval(6);
        } else if (mItemSelect == 2) {
            Prefs.getInstance(mContext).setAutoCheckInterval(12);
        } else if (mItemSelect == 3) {
            Prefs.getInstance(mContext).setAutoCheckInterval(24);
        } else if (mItemSelect == 4) {
            Prefs.getInstance(mContext).setAutoCheckInterval(48);
        }
        new EventsCheckAlarm().setAlarm(mContext);
    }

    private int getIntervalPosition() {
        int position;
        int interval = Prefs.getInstance(mContext).getAutoCheckInterval();
        switch (interval){
            case 1:
                position = 0;
                break;
            case 6:
                position = 1;
                break;
            case 12:
                position = 2;
                break;
            case 24:
                position = 3;
                break;
            case 48:
                position = 4;
                break;
            default:
                position = 0;
                break;
        }
        mItemSelect = position;
        return position;
    }

    private void loadCalendars() {
        list = CalendarUtils.getInstance(mContext).getCalendarsList();
        if (list == null || list.size() == 0) {
            Toast.makeText(mContext, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show();
        }
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getString(R.string.choose_calendar));
        if (list != null && list.size() > 0) {
            for (CalendarUtils.CalendarItem item : list) {
                spinnerArray.add(item.getName());
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        eventCalendar.setAdapter(spinnerArrayAdapter);
        eventCalendar.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkCalendarPerm()) {
            loadCalendars();
        }
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.import_events));
            mCallback.onFragmentSelect(this);
        }
    }

    private boolean checkCalendarPerm() {
        if (Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR)) {
            return true;
        } else {
            Permissions.requestPermission(getActivity(), CALENDAR_PERM, Permissions.READ_CALENDAR);
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
                    importEvents();
                } else {
                    Permissions.requestPermission(getActivity(), 102, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR);
                }
                break;
        }
    }

    private void importEvents() {
        if (!eventsCheck.isChecked()) {
            Toast.makeText(mContext, getString(R.string.no_action_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventCalendar.getSelectedItemPosition() == 0) {
            Toast.makeText(mContext, getString(R.string.you_dont_select_any_calendar), Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, Integer> map = new HashMap<>();
        if (eventsCheck.isChecked()) {
            int selectedPosition = eventCalendar.getSelectedItemPosition() - 1;
            map.put(EVENT_KEY, list.get(selectedPosition).getId());
            boolean isEnabled = Prefs.getInstance(mContext).isCalendarEnabled();
            if (!isEnabled) {
                Prefs.getInstance(mContext).setCalendarEnabled(true);
                Prefs.getInstance(mContext).setCalendarId(list.get(selectedPosition).getId());
            }
            Prefs.getInstance(mContext).setEventsCalendar(list.get(selectedPosition).getId());
        }

        new Import(mContext).execute(map);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.eventsCheck:
                if (isChecked) eventCalendar.setEnabled(true);
                else eventCalendar.setEnabled(false);
                break;
            case R.id.autoCheck:
                if (isChecked) {
                    if (Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
                        autoCheck(true);
                    } else {
                        Permissions.requestPermission(getActivity(), 101, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR);
                    }
                } else autoCheck(false);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    autoCheck(true);
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importEvents();
                }
                break;
            case CALENDAR_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadCalendars();
                }
                break;
        }
    }

    private void autoCheck(boolean isChecked) {
        Prefs.getInstance(mContext).setAutoEventsCheckEnabled(isChecked);
        syncInterval.setEnabled(isChecked);
        EventsCheckAlarm alarm = new EventsCheckAlarm();
        if (isChecked) alarm.setAlarm(mContext);
        else alarm.cancelAlarm(mContext);
    }

    public class Import extends AsyncTask<HashMap<String, Integer>, Void, Integer> {

        private Context mContext;
        private ProgressDialog dialog;

        public Import(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(mContext, null, getString(R.string.please_wait), true, false);
        }

        @SafeVarargs
        @Override
        protected final Integer doInBackground(HashMap<String, Integer>... params) {
            if (params == null) {
                return 0;
            }
            CalendarUtils cm = CalendarUtils.getInstance(mContext);
            long currTime = System.currentTimeMillis();
            int eventsCount = 0;
            HashMap<String, Integer> map = params[0];
            if (map.containsKey(EVENT_KEY)) {
                ArrayList<CalendarUtils.EventItem> eventItems = cm.getEvents(map.get(EVENT_KEY));
                if (eventItems != null && eventItems.size() > 0) {
                    List<Long> list = RealmDb.getInstance().getCalendarEventsIds();
                    for (CalendarUtils.EventItem item : eventItems) {
                        long itemId = item.getId();
                        if (!list.contains(itemId)) {
                            String rrule = item.getRrule();
                            int repeat = 0;
                            if (rrule != null && !rrule.matches("")) {
                                try {
                                    RecurrenceRule rule = new RecurrenceRule(rrule);
                                    int interval = rule.getInterval();
                                    Freq freq = rule.getFreq();
                                    if (freq == Freq.HOURLY || freq == Freq.MINUTELY || freq == Freq.SECONDLY) {
                                    } else {
                                        if (freq == Freq.WEEKLY) repeat = interval * 7;
                                        else if (freq == Freq.MONTHLY) repeat = interval * 30;
                                        else if (freq == Freq.YEARLY) repeat = interval * 365;
                                        else repeat = interval;
                                    }
                                } catch (InvalidRecurrenceRuleException e) {
                                    e.printStackTrace();
                                }
                            }
                            String summary = item.getTitle();
                            String categoryId = RealmDb.getInstance().getDefaultGroup().getUuId();
                            Calendar calendar = Calendar.getInstance();
                            long dtStart = item.getDtStart();
                            calendar.setTimeInMillis(dtStart);
                            if (dtStart >= currTime) {
                                eventsCount += 1;
                                saveReminder(itemId, summary, dtStart, repeat, categoryId);
                            } else {
                                if (repeat > 0) {
                                    do {
                                        calendar.setTimeInMillis(dtStart + (repeat * AlarmManager.INTERVAL_DAY));
                                        dtStart = calendar.getTimeInMillis();
                                    } while (dtStart < currTime);
                                    eventsCount += 1;
                                    saveReminder(itemId, summary, dtStart, repeat, categoryId);
                                }
                            }
                        }
                    }
                }
            }
            return eventsCount;
        }

        private void saveReminder(long itemId, String summary, long dtStart, int repeat, String categoryId) {
            Reminder reminder = new Reminder();
            reminder.setType(Reminder.BY_DATE);
            reminder.setRepeatInterval(repeat);
            reminder.setGroupUuId(categoryId);
            reminder.setSummary(summary);
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(dtStart));
            reminder.setStartTime(TimeUtil.getGmtFromDateTime(dtStart));
            RealmDb.getInstance().saveObject(reminder);
            EventControl control = EventControlImpl.getController(mContext, reminder);
            control.start();
            CalendarEvent event = new CalendarEvent(reminder.getUuId(), summary, itemId);
            RealmDb.getInstance().saveObject(event);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            if (result == 0) Toast.makeText(mContext, getString(R.string.no_events_found), Toast.LENGTH_SHORT).show();
            if (result > 0) {
                Toast.makeText(mContext, result + " " + getString(R.string.events_found), Toast.LENGTH_SHORT).show();
                // TODO: 11.01.2017 Update app widgets.
                new Notifier(mContext).recreatePermanent();
            }
        }
    }


}
