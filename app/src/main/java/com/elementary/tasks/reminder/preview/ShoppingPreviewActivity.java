package com.elementary.tasks.reminder.preview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.data.models.ShopItem;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityShoppingPreviewBinding;
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

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
public class ShoppingPreviewActivity extends ThemedActivity {

    private ActivityShoppingPreviewBinding binding;
    private ReminderViewModel viewModel;

    @Nullable
    private ShopListRecyclerAdapter shoppingAdapter;
    @Nullable
    private Reminder mReminder;
    @NonNull
    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = getIntent().getIntExtra(Constants.INTENT_ID, 0);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shopping_preview);
        initActionBar();
        initViews();

        initViewModel(id);
    }

    private void initViewModel(int id) {
        ReminderViewModel.Factory factory = new ReminderViewModel.Factory(getApplication(), id);
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel.class);
        viewModel.reminder.observe(this, reminder -> {
            if (reminder != null) {
                showInfo(reminder);
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        closeWindow();
                        break;
                }
            }
        });
    }

    private void showInfo(Reminder reminder) {
        this.mReminder = reminder;
        if (reminder != null) {
            binding.statusSwitch.setChecked(reminder.isActive());
            if (!reminder.isActive()) {
                binding.statusText.setText(R.string.disabled);
            } else {
                binding.statusText.setText(R.string.enabled4);
            }
            if (TextUtils.isEmpty(reminder.getEventTime())) {
                binding.switchWrapper.setVisibility(View.GONE);
            } else {
                binding.switchWrapper.setVisibility(View.VISIBLE);
            }
            binding.windowTypeView.setText(getWindowType(reminder.getWindowType()));
            binding.taskText.setText(reminder.getSummary());
            binding.type.setText(ReminderUtils.getTypeString(this, reminder.getType()));
            binding.itemPhoto.setImageResource(getThemeUtil().getReminderIllustration(reminder.getType()));
            int catColor = 0;
            if (reminder.getGroup() != null) {
                catColor = reminder.getGroup().getColor();
            }
            int mColor = getThemeUtil().getColor(getThemeUtil().getCategoryColor(catColor));
            binding.appBar.setBackgroundColor(mColor);
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(catColor));
            }
            loadData();
        }
    }

    private String getWindowType(int reminderWType) {
        int windowType = Prefs.getInstance(this).getReminderType();
        boolean ignore = Prefs.getInstance(this).isIgnoreWindowType();
        if (!ignore) {
            windowType = reminderWType;
        }
        return windowType == 0 ? getString(R.string.full_screen) : getString(R.string.simple);
    }

    private void loadData() {
        if (mReminder != null) {
            shoppingAdapter = new ShopListRecyclerAdapter(this, mReminder.getShoppings(),
                    new ShopListRecyclerAdapter.ActionListener() {
                        @Override
                        public void onItemCheck(int position, boolean isChecked) {
                            if (shoppingAdapter != null) {
                                ShopItem item = shoppingAdapter.getItem(position);
                                item.setChecked(!item.isChecked());
                                shoppingAdapter.updateData();
                                viewModel.saveReminder(mReminder.setShoppings(shoppingAdapter.getData()));
                            }
                        }

                        @Override
                        public void onItemDelete(int position) {
                            if (shoppingAdapter != null) {
                                shoppingAdapter.delete(position);
                                viewModel.saveReminder(mReminder.setShoppings(shoppingAdapter.getData()));
                            }
                        }
                    });
            binding.todoList.setLayoutManager(new LinearLayoutManager(this));
            binding.todoList.setAdapter(shoppingAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_preview, menu);
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();
        switch (ids) {
            case R.id.action_delete:
                removeReminder();
                return true;
            case android.R.id.home:
                closeWindow();
                return true;
            case R.id.action_edit:
                editReminder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editReminder() {
        if (mReminder != null) {
            startActivity(new Intent(this, CreateReminderActivity.class)
                    .putExtra(Constants.INTENT_ID, mReminder.getUniqueId()));
        }
    }

    private void removeReminder() {
        if (mReminder != null) {
            viewModel.moveToTrash(mReminder);
        }
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            mUiHandler.post(this::finishAfterTransition);
        } else {
            finish();
        }
    }

    private void initViews() {
        binding.switchWrapper.setOnClickListener(v -> switchClick());
    }

    private void switchClick() {
        if (mReminder != null) {
            viewModel.toggleReminder(mReminder);
        }
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onBackPressed() {
        closeWindow();
    }
}
