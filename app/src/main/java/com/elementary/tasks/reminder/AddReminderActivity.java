package com.elementary.tasks.reminder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.ActionView;
import com.elementary.tasks.databinding.ActivityAddReminderBinding;
import com.elementary.tasks.reminder.models.Reminder;

/**
 * Copyright 2017 Nazar Suhovich
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

public class AddReminderActivity extends ThemedActivity {

    private static final String TAG = "AddReminderActivity";
    private static final int CONTACTS = 112;
    public static final int CONTACTS_ACTION = 113;

    private ActivityAddReminderBinding binding;

    private ActionView.OnActionListener mActionListener = new ActionView.OnActionListener() {
        @Override
        public void onActionChange(boolean hasAction) {
            if (!hasAction) {
                binding.taskText.setText(getString(R.string.remind_me));
            }
        }

        @Override
        public void onTypeChange(boolean isMessageType) {
            if (isMessageType) {
                binding.taskText.setText(getString(R.string.message));
            } else {
                binding.taskText.setText(getString(R.string.remind_me));
            }
        }
    };

    @Override
    protected String getStats() {
        return "Quick reminder create";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                save();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_reminder);
        initActionBar();
        long date = getIntent().getLongExtra(Constants.INTENT_DATE, 0);
        binding.repeatView.enablePrediction(true);
        binding.dateView.setEventListener(binding.repeatView.getEventListener());
        binding.actionView.setListener(mActionListener);
        binding.actionView.setActivity(this);
        binding.actionView.setContactClickListener(view -> selectContact());
        if (isExportToCalendar()) {
            binding.exportToCalendar.setVisibility(View.VISIBLE);
        } else {
            binding.exportToCalendar.setVisibility(View.GONE);
        }
        if (Google.getInstance(this) != null) {
            binding.exportToTasks.setVisibility(View.VISIBLE);
        } else {
            binding.exportToTasks.setVisibility(View.GONE);
        }

        if (date != 0) {
            binding.dateView.setDateTime(date);
        }
        if (SuperUtil.checkNotificationPermission(this)) {
            SuperUtil.askNotificationPermission(this);
        }
    }

    private void initActionBar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    private boolean isExportToCalendar() {
        return getPrefs().isCalendarEnabled() || getPrefs().isStockCalendarEnabled();
    }

    private void save() {
        String summary = binding.taskText.getText().toString();
        int type = Reminder.BY_DATE;
        boolean isAction = binding.actionView.hasAction();
        if (TextUtils.isEmpty(summary) && !isAction) {
            Snackbar.make(binding.getRoot(), getString(R.string.task_summary_is_empty), Snackbar.LENGTH_SHORT).show();
            return;
        }
        String number = null;
        if (isAction) {
            number = binding.actionView.getNumber();
            if (TextUtils.isEmpty(number)) {
                Snackbar.make(binding.getRoot(), getString(R.string.you_dont_insert_number), Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (binding.actionView.getType() == ActionView.TYPE_CALL) {
                type = Reminder.BY_DATE_CALL;
            } else {
                type = Reminder.BY_DATE_SMS;
            }
        }
        Reminder reminder = new Reminder();
        reminder.setTarget(number);
        reminder.setType(type);
        reminder.setRepeatInterval(binding.repeatView.getRepeat());
        reminder.setExportToCalendar(binding.exportToCalendar.isChecked());
        reminder.setExportToTasks(binding.exportToTasks.isChecked());
        reminder.setSummary(summary);
        reminder.setGroupUuId(RealmDb.getInstance().getDefaultGroup().getUuId());
        LogUtil.d(TAG, "save: " + type);
        long startTime = binding.dateView.getDateTime();
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        if (!TimeCount.isCurrent(reminder.getEventTime())) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return;
        }
        RealmDb.getInstance().saveObject(reminder);
        EventControl control = EventControlFactory.getController(this, reminder);
        if (!control.start()) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return;
        }
        setResult(RESULT_OK);
        finish();
    }

    private void selectContact() {
        if (Permissions.checkPermission(this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(this, Constants.REQUEST_CODE_CONTACTS);
        } else {
            Permissions.requestPermission(this, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
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
