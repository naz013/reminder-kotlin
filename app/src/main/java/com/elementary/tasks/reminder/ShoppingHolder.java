package com.elementary.tasks.reminder;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ShoppingListItemBinding;
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

public class ShoppingHolder extends RecyclerView.ViewHolder {

    private RoboTextView listHeader;
    private ShoppingListItemBinding binding;
    private RecyclerListener mEventListener;

    public ShoppingHolder(View v, RecyclerListener listener, ThemeUtil themeUtil) {
        super(v);
        this.mEventListener = listener;
        binding = DataBindingUtil.bind(v);
        listHeader = binding.listHeader;
        binding.subBackground.setBackgroundColor(themeUtil.getCardStyle());
        binding.itemCard.setOnLongClickListener(view -> {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), v);
            }
            return true;
        });
        binding.setClick(v1 -> {
            switch (v1.getId()) {
                case R.id.itemCard:
                    if (mEventListener != null) {
                        mEventListener.onItemClicked(getAdapterPosition(), binding.subBackground);
                    }
                    break;
            }
        });
    }

    public RoboTextView getListHeader() {
        return listHeader;
    }

    public void setData(Reminder reminder) {
        binding.setItem(reminder);
    }
}
