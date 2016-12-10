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
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.ActionView;
import com.elementary.tasks.databinding.FragmentDateBinding;
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

public class DateFragment extends RepeatableTypeFragment {

    private static final String TAG = "DateFragment";
    private static final int CONTACTS = 112;
    public static final int CONTACTS_ACTION = 113;

    private FragmentDateBinding binding;
    private ActionView.OnActionListener mActionListener = new ActionView.OnActionListener() {
        @Override
        public void onActionChange(boolean hasAction) {
            if (!hasAction) {
                mInterface.setEventHint(getString(R.string.remind_me));
            }
        }

        @Override
        public void onTypeChange(boolean isMessageType) {
            if (isMessageType) {
                mInterface.setEventHint(getString(R.string.message));
            } else {
                mInterface.setEventHint(getString(R.string.remind_me));
            }
        }
    };

    public DateFragment() {
    }

    @Override
    public boolean save() {
        if (mInterface == null) return false;
        Reminder reminder = mInterface.getReminder();
        int type = Reminder.BY_DATE;
        boolean isAction = binding.actionView.hasAction();
        if (TextUtils.isEmpty(mInterface.getSummary()) && !isAction) {
            mInterface.showSnackbar(getString(R.string.task_summary_is_empty));
            return false;
        }
        String number = null;
        if (isAction) {
            number = binding.actionView.getNumber();
            if (TextUtils.isEmpty(number)) {
                mInterface.showSnackbar(getString(R.string.you_dont_insert_number));
                return false;
            }
            if (binding.actionView.getType() == ActionView.TYPE_CALL) {
                type = Reminder.BY_DATE_CALL;
            } else {
                type = Reminder.BY_DATE_SMS;
            }
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
        long startTime = TimeCount.getInstance(mContext).generateStartEvent(type, binding.dateView.getDateTime(), null, 0, 0);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        Log.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true));
        RealmDb.getInstance().saveObject(reminder);
//        new AlarmReceiver().enableReminder(mContext, reminder.getUuId());
        return true;
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
        binding = FragmentDateBinding.inflate(inflater, container, false);
        binding.repeatView.enablePrediction(true);
        binding.dateView.setEventListener(binding.repeatView.getEventListener());
        binding.actionView.setListener(mActionListener);
        binding.actionView.setActivity(getActivity());
        binding.actionView.setContactClickListener(view -> selectContact());
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
            binding.actionView.setAction(true);
            binding.actionView.setNumber(reminder.getTarget());
            if (Reminder.isKind(reminder.getType(), Reminder.Kind.CALL)) {
                binding.actionView.setType(ActionView.TYPE_CALL);
            } else if (Reminder.isKind(reminder.getType(), Reminder.Kind.SMS)) {
                binding.actionView.setType(ActionView.TYPE_MESSAGE);
            }
        }
    }

    private void selectContact() {
        if (Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(getActivity(), Constants.REQUEST_CODE_CONTACTS);
        } else {
            Permissions.requestPermission(getActivity(), CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
            binding.actionView.setNumber(number);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectContact();
                }
                break;
            case CONTACTS_ACTION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.actionView.setAction(true);
                }
                break;
        }
    }
}
