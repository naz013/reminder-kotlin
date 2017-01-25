package com.elementary.tasks.creators.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
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

public class ShopFragment extends TypeFragment {

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
        reminder.setClear(mInterface);
        if (isReminder) {
            long startTime = binding.dateViewShopping.getDateTime();
            reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
            LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        } else {
            reminder.setEventTime(null);
            reminder.setStartTime(null);
        }
        RealmDb.getInstance().saveObject(reminder);
        EventControl control = EventControlImpl.getController(mContext, reminder);
        if (control.start()) {
            return true;
        } else {
            Toast.makeText(mContext, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        binding.shopEdit.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                addNewItem();
                return true;
            }
            return false;
        });
        ImageButton addButton = binding.addButton;
        if (ThemeUtil.getInstance(mContext).isDark()) addButton.setImageResource(R.drawable.ic_add_white_24dp);
        else addButton.setImageResource(R.drawable.ic_add_black_24dp);
        addButton.setOnClickListener(v -> addNewItem());
        switchDate();
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
            switchDate();
            binding.dateViewShopping.setDateTime(reminder.getEventTime());
        } else {
            isReminder = false;
        }

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
