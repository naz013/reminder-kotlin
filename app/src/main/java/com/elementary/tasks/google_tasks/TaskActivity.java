package com.elementary.tasks.google_tasks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskViewModel;
import com.elementary.tasks.databinding.ActivityCreateGoogleTaskBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

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
public class TaskActivity extends ThemedActivity {

    private static final String TAG = "TaskActivity";

    private ActivityCreateGoogleTaskBinding binding;
    private GoogleTaskViewModel viewModel;

    private int mHour = 0;
    private int mMinute = 0;
    private int mYear = 0;
    private int mMonth = 0;
    private int mDay = 1;
    @Nullable
    private String listId = null;
    private String action;
    private boolean isReminder = false;
    private boolean isDate = false;

    @Nullable
    private GoogleTask mItem;
    @Nullable
    private ProgressDialog mDialog;

    private static final int MENU_ITEM_DELETE = 12;
    private static final int MENU_ITEM_MOVE = 14;
    @NonNull
    private TasksCallback mSimpleCallback = new TasksCallback() {
        @Override
        public void onFailed() {
            hideDialog();
        }

        @Override
        public void onComplete() {
            hideDialog();
            finish();
        }
    };

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            try {
                mDialog.dismiss();
            } catch (IllegalArgumentException e) {
                LogUtil.d(TAG, "hideDialog: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_google_task);
        initToolbar();
        initFields();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        Intent intent = getIntent();
        String tmp = intent.getStringExtra(Constants.INTENT_ID);
        action = intent.getStringExtra(TasksConstants.INTENT_ACTION);
        if (action == null) action = TasksConstants.CREATE;

        if (action.matches(TasksConstants.CREATE)) {
            initViewModel(null, tmp);
        } else {
            initViewModel(tmp, null);
        }
        switchDate();
    }

    private void initViewModel(String taskId, String listId) {
        this.listId = listId;
        viewModel = ViewModelProviders.of(this, new GoogleTaskViewModel.Factory(getApplication(), taskId)).get(GoogleTaskViewModel.class);
        viewModel.isInProgress.observe(this, aBoolean -> {
            if (aBoolean != null) {
                if (aBoolean) showProgressDialog();
                else hideDialog();
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case SAVED:
                    case DELETED:
                        finish();
                        break;
                }
            }
        });
        viewModel.googleTask.observe(this, googleTask -> {
            if (googleTask != null) {
                editTask(googleTask);
            }
        });
        viewModel.googleTaskLists.observe(this, googleTaskLists -> {
            if (googleTaskLists != null && listId != null) {
                selectCurrent(googleTaskLists);
            }
        });
        viewModel.defaultTaskList.observe(this, googleTaskList -> {
            if (googleTaskList != null && listId == null) {
                showTaskList(googleTaskList);
            }
        });
        viewModel.reminder.observe(this, reminder -> {
            if (reminder != null) {
                showReminder(reminder);
            }
        });
    }

    private void showReminder(@NonNull Reminder reminder) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeUtil.getDateTimeFromGmt(reminder.getEventTime()));
        binding.timeField.setText(TimeUtil.getTime(calendar.getTime(), getPrefs().is24HourFormatEnabled()));
        isReminder = true;
    }

    private void showTaskList(GoogleTaskList googleTaskList) {
        this.listId = googleTaskList.getListId();
        binding.toolbar.setTitle(R.string.new_task);
        binding.listText.setText(googleTaskList.getTitle());
        setColor(googleTaskList.getColor());
    }

    private void selectCurrent(List<GoogleTaskList> googleTaskLists) {
        binding.toolbar.setTitle(R.string.new_task);
        for (GoogleTaskList googleTaskList : googleTaskLists) {
            if (googleTaskList.getListId().equals(listId)) {
                showTaskList(googleTaskList);
                break;
            }
        }
    }

    private void editTask(@NonNull GoogleTask googleTask) {
        this.mItem = googleTask;
        this.listId = googleTask.getListId();
        binding.toolbar.setTitle(R.string.edit_task);
        binding.editField.setText(googleTask.getTitle());

        String note = googleTask.getNotes();
        if (note != null) {
            binding.noteField.setText(note);
            binding.noteField.setSelection(binding.noteField.getText().toString().trim().length());
        }
        long time = mItem.getDueDate();
        if (time != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            isDate = true;
            binding.dateField.setText(TimeUtil.getDate(calendar.getTime()));
        }
        if (viewModel.googleTaskLists.getValue() != null) {
            for (GoogleTaskList googleTaskList : viewModel.googleTaskLists.getValue()) {
                if (googleTaskList.getListId().equals(googleTask.getListId())) {
                    showTaskList(googleTaskList);
                    break;
                }
            }
        }
        viewModel.loadReminder(googleTask.getUuId());
    }

    private void initFields() {
        binding.listText.setOnClickListener(v -> selectList(false));
        binding.dateField.setOnClickListener(v -> selectDateAction(1));
        binding.timeField.setOnClickListener(v -> selectDateAction(2));
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void selectDateAction(final int type) {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        String[] types = new String[]{getString(R.string.no_date), getString(R.string.select_date)};
        if (type == 2) {
            types = new String[]{getString(R.string.no_reminder), getString(R.string.select_time)};
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, types);
        int selection = 0;
        if (type == 1) {
            if (isDate) selection = 1;
        }
        if (type == 2) {
            if (isReminder) selection = 1;
        }
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            if (which != -1) {
                dialog.dismiss();
                if (type == 1) {
                    switch (which) {
                        case 0:
                            isDate = false;
                            switchDate();
                            break;
                        case 1:
                            isDate = true;
                            dateDialog();
                            break;
                    }
                }
                if (type == 2) {
                    switch (which) {
                        case 0:
                            isReminder = false;
                            switchDate();
                            break;
                        case 1:
                            isReminder = true;
                            timeDialog();
                            break;
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void switchDate() {
        if (!isDate) binding.dateField.setText(getString(R.string.no_date));
        if (!isReminder) binding.timeField.setText(getString(R.string.no_reminder));
    }

    private void moveTask(String listId) {
        if (mItem != null) {
            String initListId = mItem.getListId();
            if (!listId.matches(initListId)) {
                mItem.setListId(listId);
                viewModel.moveGoogleTask(mItem, initListId);
            } else {
                Toast.makeText(this, getString(R.string.this_is_same_list), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProgressDialog() {
        mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);
    }

    private void selectList(final boolean move) {
        List<GoogleTaskList> list = viewModel.googleTaskLists.getValue();
        if (list == null) list = new ArrayList<>();
        List<String> names = new ArrayList<>();
        int position = 0;
        for (int i = 0; i < list.size(); i++) {
            GoogleTaskList item = list.get(i);
            names.add(item.getTitle());
            if (listId != null && item.getListId() != null && item.getListId().matches(listId)) {
                position = i;
            }
        }
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.choose_list);
        List<GoogleTaskList> finalList = list;
        builder.setSingleChoiceItems(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, names),
                position, (dialog, which) -> {
                    dialog.dismiss();
                    if (move) moveTask(finalList.get(which).getListId());
                    else showTaskList(finalList.get(which));
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mItem != null && getPrefs().isAutoSaveEnabled()) {
            saveTask();
        }
    }

    private void saveTask() {
        String taskName = binding.editField.getText().toString().trim();
        if (taskName.matches("")) {
            binding.editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String note = binding.noteField.getText().toString().trim();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(mYear, mMonth, mDay, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long due = 0;
        if (isDate) due = calendar.getTimeInMillis();
        Reminder reminder = null;
        if (isReminder) reminder = createReminder(taskName);
        if (action.matches(TasksConstants.EDIT) && mItem != null) {
            String initListId = mItem.getListId();
            mItem.setListId(listId);
            mItem.setStatus(Google.TASKS_NEED_ACTION);
            mItem.setTitle(taskName);
            mItem.setNotes(note);
            if (reminder != null) {
                mItem.setUuId(reminder.getUuId());
            }
            mItem.setDueDate(due);
            if (listId != null) {
                viewModel.updateAndMoveGoogleTask(mItem, initListId, reminder);
            } else {
                viewModel.updateGoogleTask(mItem, reminder);
            }
        } else {
            mItem = new GoogleTask();
            mItem.setListId(listId);
            mItem.setStatus(Google.TASKS_NEED_ACTION);
            mItem.setTitle(taskName);
            mItem.setNotes(note);
            mItem.setDueDate(due);
            if (reminder != null) {
                mItem.setUuId(reminder.getUuId());
            }
            viewModel.newGoogleTask(mItem, reminder);
        }
    }

    @Nullable
    private Reminder createReminder(String task) {
        Group group = viewModel.defaultGroup.getValue();
        if (group == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(mYear, mMonth, mDay, mHour, mMinute);
        long due = calendar.getTimeInMillis();
        Reminder reminder = new Reminder();
        reminder.setType(Reminder.BY_DATE);
        reminder.setSummary(task);
        reminder.setGroupUuId(group.getUuId());
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(due));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(due));
        return reminder;
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setMessage(getString(R.string.delete_this_task));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteTask();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteTask() {
        if (mItem != null) {
            viewModel.deleteGoogleTask(mItem);
        }
    }

    private void setColor(int i) {
        binding.appBar.setBackgroundColor(getThemeUtil().getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_task, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_task);
            menu.add(Menu.NONE, MENU_ITEM_MOVE, 100, R.string.move_to_another_list);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case MENU_ITEM_MOVE:
                selectList(true);
                return true;
            case R.id.action_add:
                saveTask();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void dateDialog() {
        TimeUtil.showDatePicker(this, myDateCallBack, mYear, mMonth, mDay);
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(year, monthOfYear, dayOfMonth);
            binding.dateField.setText(TimeUtil.getDate(calendar.getTime()));
        }
    };

    protected void timeDialog() {
        TimeUtil.showTimePicker(this, myCallBack, mHour, mMinute);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            binding.timeField.setText(TimeUtil.getTime(c.getTime(), getPrefs().is24HourFormatEnabled()));
        }
    };

    @Override
    protected void onDestroy() {
        UpdatesHelper.getInstance(this).updateTasksWidget();
        super.onDestroy();
    }
}
