package com.elementary.tasks.birthdays;

import android.databinding.DataBindingUtil;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemEventsBinding;
import com.elementary.tasks.reminder.RecyclerListener;

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

public class BirthdayHolder extends RecyclerView.ViewHolder {

    private ListItemEventsBinding binding;
    private RecyclerListener mEventListener;

    public BirthdayHolder(final View v, RecyclerListener listener, ThemeUtil themeUtil) {
        super(v);
        this.mEventListener = listener;
        binding = DataBindingUtil.bind(v);
        binding.itemCard.setCardBackgroundColor(themeUtil.getCardStyle());
        if (Module.isLollipop()) {
            binding.itemCard.setCardElevation(Configs.CARD_ELEVATION);
        }
        binding.itemCard.setOnLongClickListener(view -> {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), binding.itemCard);
            }
            return true;
        });
        binding.setClick(v1 -> {
            switch (v1.getId()) {
                case R.id.itemCard:
                    if (mEventListener != null) {
                        mEventListener.onItemClicked(getAdapterPosition(), binding.itemCard);
                    }
                    break;
            }
        });
    }

    public void setColor(int color) {
        binding.setColor(color);
    }

    public void setData(BirthdayItem item) {
        binding.setItem(item);
    }
}
