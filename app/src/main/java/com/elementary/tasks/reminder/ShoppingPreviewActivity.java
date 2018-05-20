package com.elementary.tasks.reminder;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityShoppingPreviewBinding;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.data.models.ShopItem;

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
    @Nullable
    private ShopListRecyclerAdapter shoppingAdapter;
    private String id;
    @Nullable
    private Reminder mReminder;
    @NonNull
    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getStringExtra(Constants.INTENT_ID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shopping_preview);
        initActionBar();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReminder = RealmDb.getInstance().getReminder(id);
        loadInfo();
    }

    private void loadInfo() {
        if (mReminder != null) {
            binding.statusSwitch.setChecked(mReminder.isActive());
            if (!mReminder.isActive()) {
                binding.statusText.setText(R.string.disabled);
            } else {
                binding.statusText.setText(R.string.enabled4);
            }
            if (TextUtils.isEmpty(mReminder.getEventTime())) {
                binding.switchWrapper.setVisibility(View.GONE);
            } else {
                binding.switchWrapper.setVisibility(View.VISIBLE);
            }
            binding.windowTypeView.setText(getWindowType(mReminder.getWindowType()));
            binding.taskText.setText(mReminder.getSummary());
            binding.type.setText(ReminderUtils.getTypeString(this, mReminder.getType()));
            binding.itemPhoto.setImageResource(getThemeUtil().getReminderIllustration(mReminder.getType()));
            int catColor = 0;
            Group group = RealmDb.getInstance().getGroup(mReminder.getGroupUuId());
            if (group != null) {
                catColor = group.getColor();
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
                                RealmDb.getInstance().saveReminder(mReminder.setShoppings(shoppingAdapter.getData()), null);
                            }
                        }

                        @Override
                        public void onItemDelete(int position) {
                            if (shoppingAdapter != null) shoppingAdapter.delete(position);
                            RealmDb.getInstance().saveReminder(mReminder.setShoppings(shoppingAdapter.getData()), null);
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
                    .putExtra(Constants.INTENT_ID, mReminder.getUuId()));
        }
    }

    private void removeReminder() {
        if (mReminder != null) {
            EventControl control = EventControlFactory.getController(this, mReminder.setRemoved(true));
            control.stop();
        }
        closeWindow();
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
            EventControl control = EventControlFactory.getController(this, mReminder);
            if (!control.onOff()) {
                Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            }
            mReminder = RealmDb.getInstance().getReminder(getIntent().getStringExtra(Constants.INTENT_ID));
            loadInfo();
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
