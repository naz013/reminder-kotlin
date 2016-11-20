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

    private FragmentDateBinding mBinding;
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
    public void save() {
        if (mInterface == null) return;
        Reminder reminder = new Reminder();
        String summary = mInterface.getSummary();
        boolean isAction = mBinding.actionView.hasAction();
        if (TextUtils.isEmpty(summary) && !isAction) {
            mInterface.showSnackbar(getString(R.string.task_summary_is_empty));
            return;
        }
        int type = Reminder.BY_DATE;
        if (isAction) {
            String number = mBinding.actionView.getNumber();
            if (TextUtils.isEmpty(number)) {
                mInterface.showSnackbar(getString(R.string.you_dont_insert_number));
                return;
            }
            reminder.setTarget(number);
            if (mBinding.actionView.getType() == ActionView.TYPE_CALL) {
                type = Reminder.BY_DATE_CALL;
            } else {
                type = Reminder.BY_DATE_SMS;
            }
        }
        reminder.setSummary(summary);
        reminder.setRepeatInterval(mBinding.repeatView.getRepeat());
        reminder.setRepeatLimit(mInterface.getRepeatLimit());
        reminder.setExportToCalendar(mBinding.exportToCalendar.isChecked());
        reminder.setExportToTasks(mBinding.exportToTasks.isChecked());
        reminder.setColor(mInterface.getLedColor());
        reminder.setMelodyPath(mInterface.getMelodyPath());
        reminder.setVolume(mInterface.getVolume());
        reminder.setAuto(mInterface.getAuto());
        Log.d(TAG, "save: " + type);
        long startTime = TimeCount.getInstance(mContext).generateStartEvent(type, mBinding.dateView.getDateTime(), null, 0);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        Log.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true));
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
        mBinding = FragmentDateBinding.inflate(inflater, container, false);
        mBinding.actionView.setListener(mActionListener);
        mBinding.actionView.setContactClickListener(view -> selectContact());
        return mBinding.getRoot();
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
            mBinding.actionView.setNumber(number);
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
        }
    }
}
