package com.elementary.tasks.creators.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.FragmentReminderShopBinding;
import com.elementary.tasks.reminder.ShopListRecyclerAdapter;
import com.elementary.tasks.reminder.models.Reminder;
import com.elementary.tasks.reminder.models.ShopItem;

import java.util.ArrayList;

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

public class ShopFragment extends RepeatableTypeFragment {

    private static final String TAG = "ShopFragment";

    private FragmentReminderShopBinding binding;
    private ShopListRecyclerAdapter mAdapter;
    private boolean isReminder = false;
    private int mSelectedPosition;
    private ShopListRecyclerAdapter.ActionListener mActionListener = new ShopListRecyclerAdapter.ActionListener() {
        @Override
        public void onItemCheck(int position, boolean isChecked) {
            ShopItem item = mAdapter.getItem(position);
            item.setChecked(!item.isChecked());
            mAdapter.updateData();
        }

        @Override
        public void onItemDelete(int position) {
            mAdapter.delete(position);
        }
    };

    public ShopFragment() {
    }

    @Override
    public boolean save() {
        if (mInterface == null) return false;
        Reminder reminder = mInterface.getReminder();
        int type = Reminder.BY_DATE_SHOP;
        if (mAdapter.getItemCount() == 0) {
            mInterface.showSnackbar(getString(R.string.shopping_list_is_empty));
            return false;
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setShoppings(mAdapter.getData());
        reminder.setTarget(null);
        reminder.setType(type);
        reminder.setRepeatInterval(0);
        fillExtraData(reminder);
        Log.d(TAG, "save: " + type);
        if (isReminder) {
            long startTime = TimeCount.getInstance(mContext).generateStartEvent(type, binding.dateViewShopping.getDateTime(), null, 0, 0);
            reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
            Log.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true));
        } else {
            reminder.setEventTime(null);
            reminder.setStartTime(null);
        }
        Log.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
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
        binding = FragmentReminderShopBinding.inflate(inflater, container, false);
        binding.dateViewShopping.setOnLongClickListener(view -> {
            selectDateDialog();
            return true;
        });
        binding.todoList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ShopListRecyclerAdapter(mContext, new ArrayList<>(), mActionListener);
        binding.todoList.setAdapter(mAdapter);
        binding.shopEdit.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
                addNewItem();
            }
            return false;
        });
        ImageButton addButton = binding.addButton;
        if (ThemeUtil.getInstance(mContext).isDark()) addButton.setImageResource(R.drawable.ic_add_white_24dp);
        else addButton.setImageResource(R.drawable.ic_add_black_24dp);
        addButton.setOnClickListener(v -> addNewItem());
        editReminder();
        return binding.getRoot();
    }

    private void addNewItem() {
        String task = binding.shopEdit.getText().toString().trim();
        if (task.matches("")) {
            binding.shopEdit.setError(getString(R.string.must_be_not_empty));
            return;
        }
        mAdapter.addItem(new ShopItem(task.replaceAll("\n", " ")));
        binding.shopEdit.setText("");
    }

    private void editReminder() {
        if (mInterface.getReminder() == null) return;
        Reminder reminder = mInterface.getReminder();
        binding.dateViewShopping.setDateTime(reminder.getEventTime());
        mAdapter.setData(reminder.getShoppings());
        if (reminder.getEventTime() != null) {
            isReminder = true;
            binding.dateViewShopping.setDateTime(reminder.getEventTime());
        } else {
            isReminder = false;
        }
        switchDate();
    }

    private void selectDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String[] types = new String[]{getString(R.string.no_reminder), getString(R.string.select_time)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, types);
        int selection = 0;
        if (isReminder) selection = 1;
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            mSelectedPosition = which;
        });
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            makeAction();
            dialogInterface.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mSelectedPosition = 0);
        dialog.setOnDismissListener(dialogInterface -> mSelectedPosition = 0);
        dialog.show();
    }

    private void makeAction() {
        switch (mSelectedPosition) {
            case 0:
                isReminder = false;
                break;
            case 1:
                isReminder = true;
                break;
        }
        switchDate();
    }

    private void switchDate() {
        if (isReminder) {
            binding.dateViewShopping.setSingleText(null);
        } else {
            binding.dateViewShopping.setSingleText(getString(R.string.no_reminder));
        }
    }
}
