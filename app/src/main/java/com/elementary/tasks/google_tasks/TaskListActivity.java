package com.elementary.tasks.google_tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel;
import com.elementary.tasks.core.views.ColorPickerView;
import com.elementary.tasks.databinding.ActivityCreateTaskListBinding;

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

public class TaskListActivity extends ThemedActivity implements ColorPickerView.OnColorListener {

    private ActivityCreateTaskListBinding binding;
    private GoogleTaskListViewModel viewModel;
    @Nullable
    private GoogleTaskList mItem;
    private int color;

    private ProgressDialog mDialog;

    private static final int MENU_ITEM_DELETE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_task_list);

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.pickerView.setListener(this);

        initViewModel(getIntent().getStringExtra(Constants.INTENT_ID));
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void initViewModel(String id) {
        viewModel = ViewModelProviders.of(this, new GoogleTaskListViewModel.Factory(getApplication(), id)).get(GoogleTaskListViewModel.class);
        viewModel.googleTaskList.observe(this, googleTaskList -> {
            if (googleTaskList != null) {
                editTaskList(googleTaskList);
            }
        });
        viewModel.isInProgress.observe(this, aBoolean -> {
            if (aBoolean != null) {
                if (aBoolean) showProgressDialog();
                else hideDialog();
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                    case SAVED:
                        finish();
                        break;
                }
            }
        });
    }

    private void editTaskList(@NonNull GoogleTaskList googleTaskList) {
        mItem = googleTaskList;
        binding.editField.setText(mItem.getTitle());
        if (mItem.getDef() == 1) {
            binding.defaultCheck.setChecked(true);
            binding.defaultCheck.setEnabled(false);
        }
        color = mItem.getColor();
        binding.pickerView.setSelectedColor(color);
        setColor(color);
    }

    private void saveTaskList() {
        String listName = binding.editField.getText().toString();
        if (listName.matches("")) {
            binding.editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        boolean isNew = false;
        if (mItem == null) {
            mItem = new GoogleTaskList();
            isNew = true;
        }
        mItem.setTitle(listName);
        mItem.setColor(color);
        mItem.setUpdated(System.currentTimeMillis());
        if (binding.defaultCheck.isChecked()) {
            mItem.setDef(1);
            GoogleTaskList defList = viewModel.defaultTaskList.getValue();
            if (defList != null) {
                defList.setDef(0);
                viewModel.saveLocalGoogleTaskList(defList);
            }
        }

        if (isNew) {
            viewModel.newGoogleTaskList(mItem);
        } else {
            viewModel.updateGoogleTaskList(mItem);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mItem != null && getPrefs().isAutoSaveEnabled()) {
            saveTaskList();
        }
    }

    private void showProgressDialog() {
        mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);
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
        AlertDialog.Builder builder = Dialogues.getDialog(this);
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
            viewModel.deleteGoogleTaskList(mItem);
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
        binding.appBar.setBackgroundColor(getThemeUtil().getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(i));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdatesHelper.getInstance(this).updateTasksWidget();
    }


    @Override
    public void onColorSelect(int code) {
        setColor(code);
    }
}
