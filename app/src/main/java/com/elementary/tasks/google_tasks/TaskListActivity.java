package com.elementary.tasks.google_tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.views.ColorPickerView;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.databinding.ActivityCreateTaskListBinding;

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

public class TaskListActivity extends ThemedActivity implements ColorPickerView.OnColorListener {

    private ActivityCreateTaskListBinding binding;
    private RoboCheckBox defaultCheck;

    private RoboEditText editField;
    private TaskListItem mItem;
    private int color;

    private ProgressDialog mDialog;

    private static final int MENU_ITEM_DELETE = 12;
    private TasksCallback mSaveCallback = new TasksCallback() {
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
            mDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_task_list);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        editField = binding.editField;
        defaultCheck = binding.defaultCheck;
        ColorPickerView pickerView = binding.pickerView;
        pickerView.setListener(this);
        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.INTENT_ID);
        mItem = RealmDb.getInstance().getTaskList(id);
        if (mItem != null) {
            editField.setText(mItem.getTitle());
            if (mItem.getDef() == 1) {
                defaultCheck.setChecked(true);
                defaultCheck.setEnabled(false);
            }
            color = mItem.getColor();
            pickerView.setSelectedColor(color);
            setColor(color);
        }
    }

    private void saveTaskList() {
        String listName = editField.getText().toString();
        if (listName.matches("")) {
            editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        boolean isNew = false;
        if (mItem == null) {
            mItem = new TaskListItem();
            isNew = true;
        }
        mItem.setTitle(listName);
        mItem.setColor(color);
        mItem.setUpdated(System.currentTimeMillis());
        if (defaultCheck.isChecked()) {
            mItem.setDef(1);
            TaskListItem defList = RealmDb.getInstance().getDefaultTaskList();
            if (defList != null) {
                defList.setDef(0);
                RealmDb.getInstance().saveTaskList(defList);
            }
        }
        showProgressDialog(getString(R.string.saving));
        RealmDb.getInstance().saveTaskList(mItem);
        if (isNew) {
            new TaskListAsync(TaskListActivity.this, listName, mItem.getColor(), null, TasksConstants.INSERT_TASK_LIST, mSaveCallback).execute();
        } else {
            new TaskListAsync(TaskListActivity.this, listName, mItem.getColor(), mItem.getListId(), TasksConstants.UPDATE_TASK_LIST, mSaveCallback).execute();
        }
    }

    private void showProgressDialog(String title) {
        mDialog = ProgressDialog.show(this, null, title, true, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case R.id.action_add:
                saveTaskList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_list));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteList();
            finish();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        if (mItem != null) {
            String listId = mItem.getListId();
            int def = mItem.getDef();
            showProgressDialog(getString(R.string.deleting));
            new TaskListAsync(TaskListActivity.this, null, 0, listId, TasksConstants.DELETE_TASK_LIST, new TasksCallback() {
                @Override
                public void onFailed() {
                    hideDialog();
                }

                @Override
                public void onComplete() {
                    RealmDb.getInstance().deleteTaskList(mItem.getListId());
                    RealmDb.getInstance().deleteTasks(listId);
                    if (def == 1) {
                        TaskListItem listItem = RealmDb.getInstance().getTaskLists().get(0);
                        RealmDb.getInstance().setDefault(listItem.getListId());
                    }
                    hideDialog();
                    finish();
                }
            }).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_task_list, menu);
        if (mItem != null && mItem.getSystemDefault() != 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list);
        }
        return true;
    }

    private void setColor(int i) {
        color = i;
        binding.appBar.setBackgroundColor(themeUtil.getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(themeUtil.getNoteDarkColor(i));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        UpdatesHelper.getInstance(this).updateTasksWidget();
    }


    @Override
    public void onColorSelect(int code) {
        setColor(code);
    }
}
