package com.elementary.tasks.reminder.lists;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.elementary.tasks.R;
import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.data.models.ShopItem;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ListItemTaskItemWidgetBinding;
import com.elementary.tasks.databinding.ShoppingListItemBinding;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

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

public class ShoppingHolder extends RecyclerView.ViewHolder {

    private RoboTextView listHeader;
    private ShoppingListItemBinding binding;
    private RecyclerListener mEventListener;
    @Inject
    public ThemeUtil themeUtil;

    public ShoppingHolder(View v, RecyclerListener listener) {
        super(v);
        ReminderApp.getAppComponent().inject(this);
        this.mEventListener = listener;
        binding = DataBindingUtil.bind(v);
        listHeader = binding.listHeader;
        if (mEventListener != null) {
            binding.setClick(v1 -> {
                switch (v1.getId()) {
                    case R.id.itemCheck:
                        mEventListener.onItemSwitched(getAdapterPosition(), v1);
                        break;
                    default:
                        mEventListener.onItemClicked(getAdapterPosition(), v1);
                        break;
                }
            });
        }
    }

    public RoboTextView getListHeader() {
        return listHeader;
    }

    public void setData(Reminder reminder) {
        binding.setItem(reminder);
        loadShoppingDate(reminder.getEventTime());
        loadCard(reminder.getGroup());
        loadItems(reminder.getShoppings());
    }

    private void loadItems(List<ShopItem> shoppings) {
        boolean isDark = themeUtil.isDark();
        binding.todoList.setFocusableInTouchMode(false);
        binding.todoList.setFocusable(false);
        binding.todoList.removeAllViewsInLayout();
        int count = 0;
        for (ShopItem list : shoppings) {
            ListItemTaskItemWidgetBinding bind = ListItemTaskItemWidgetBinding.inflate(LayoutInflater.from(binding.todoList.getContext()), null, false);
            ImageView checkView = bind.checkView;
            RoboTextView textView = bind.shopText;
            if (list.isChecked()) {
                if (isDark) {
                    checkView.setImageResource(R.drawable.ic_check_box_white_24dp);
                } else {
                    checkView.setImageResource(R.drawable.ic_check_box_black_24dp);
                }
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                if (isDark) {
                    checkView.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp);
                } else {
                    checkView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
                }
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            count++;
            if (count == 9) {
                checkView.setVisibility(View.INVISIBLE);
                textView.setText("...");
                binding.todoList.addView(bind.getRoot());
                break;
            } else {
                checkView.setVisibility(View.VISIBLE);
                textView.setText(list.getSummary());
                binding.todoList.addView(bind.getRoot());
            }
        }
    }

    private void loadShoppingDate(String eventTime) {
        boolean is24 = Prefs.getInstance(binding.shoppingTime.getContext()).is24HourFormatEnabled();
        long due = TimeUtil.getDateTimeFromGmt(eventTime);
        if (due > 0) {
            binding.shoppingTime.setText(TimeUtil.getFullDateTime(due, is24, false));
            binding.shoppingTime.setVisibility(View.VISIBLE);
        } else {
            binding.shoppingTime.setVisibility(View.GONE);
        }
    }

    private void loadCard(@Nullable Group group) {
        if (group != null) {
            binding.itemCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(group.getColor())));
        } else {
            binding.itemCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(0)));
        }
    }
}
