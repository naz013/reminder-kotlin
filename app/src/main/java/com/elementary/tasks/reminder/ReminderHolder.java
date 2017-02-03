package com.elementary.tasks.reminder;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ReminderListItemBinding;
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

public class ReminderHolder extends RecyclerView.ViewHolder {

    private RoboTextView listHeader;
    private ReminderListItemBinding binding;
    private RecyclerListener mEventListener;

    public ReminderHolder(View v, RecyclerListener listener, ThemeUtil themeUtil, boolean editable) {
            super(v);
        this.mEventListener = listener;
        binding = DataBindingUtil.bind(v);
        listHeader = binding.listHeader;
        binding.reminderContainer.setBackgroundColor(themeUtil.getCardStyle());
        if (editable) {
            binding.itemCheck.setVisibility(View.VISIBLE);
        } else {
            binding.itemCheck.setVisibility(View.GONE);
        }
        if (mEventListener != null) {
            binding.itemCard.setOnLongClickListener(view -> {
                mEventListener.onItemLongClicked(getAdapterPosition(), v);
                return true;
            });
            binding.setClick(v1 -> {
                switch (v1.getId()) {
                    case R.id.itemCard:
                        mEventListener.onItemClicked(getAdapterPosition(), binding.itemCheck);
                        break;
                    case R.id.itemCheck:
                        mEventListener.onItemSwitched(getAdapterPosition(), v1);
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
    }
}
