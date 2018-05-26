package com.elementary.tasks.reminder.lists;

import android.app.AlarmManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ReminderListItemBinding;
import com.elementary.tasks.databinding.ShoppingListItemBinding;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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
public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Nullable
    private RecyclerListener mEventListener;
    @Inject
    private ThemeUtil themeUtil;
    private boolean isEditable = true;
    private List<Reminder> mData = new ArrayList<>();

    public RemindersRecyclerAdapter() {
        ReminderApp.getAppComponent().inject(this);
    }

    public void setData(List<Reminder> list) {
        this.mData.clear();
        this.mData.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    @Nullable
    public Reminder getItem(int position) {
        if (position >= 0 && position < mData.size()) return mData.get(position);
        return null;
    }

    public void removeItem(int position) {
        if (position < mData.size()) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, mData.size());
        }
    }

    private void initLabel(RoboTextView listHeader, int position) {
        Reminder item = getItem(position);
        if (item == null) return;
        long due = TimeUtil.getDateTimeFromGmt(item.getEventTime());
        String simpleDate = TimeUtil.getSimpleDate(due);
        Reminder prevItem = null;
        try {
            prevItem = getItem(position - 1);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        Context context = listHeader.getContext();
        if (!item.isActive() && position > 0 && (prevItem != null && prevItem.isActive())) {
            simpleDate = context.getString(R.string.disabled);
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        } else if (!item.isActive() && position > 0 && (prevItem != null && !prevItem.isActive())) {
            listHeader.setVisibility(View.GONE);
        } else if (!item.isActive() && position == 0) {
            simpleDate = context.getString(R.string.disabled);
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        } else if (item.isActive() && position > 0 && (prevItem != null && simpleDate.equals(TimeUtil.getSimpleDate(prevItem.getEventTime())))) {
            listHeader.setVisibility(View.GONE);
        } else {
            if (due <= 0 || due < (System.currentTimeMillis() - AlarmManager.INTERVAL_DAY)) {
                simpleDate = context.getString(R.string.permanent);
            } else {
                if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                    simpleDate = context.getString(R.string.today);
                } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                    simpleDate = context.getString(R.string.tomorrow);
                }
            }
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == Reminder.REMINDER) {
            return new ReminderHolder(ReminderListItemBinding.inflate(inflater, parent, false).getRoot(), mEventListener, themeUtil, isEditable);
        } else {
            return new ShoppingHolder(ShoppingListItemBinding.inflate(inflater, parent, false).getRoot(), mEventListener, themeUtil);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Reminder item = getItem(position);
        if (holder instanceof ReminderHolder) {
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.setData(item);
            if (isEditable) {
                initLabel(reminderHolder.getListHeader(), position);
            }
        } else if (holder instanceof ShoppingHolder) {
            ShoppingHolder shoppingHolder = (ShoppingHolder) holder;
            shoppingHolder.setData(item);
            if (isEditable) {
                initLabel(shoppingHolder.getListHeader(), position);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Reminder item = getItem(position);
        if (item == null) return 0;
        return item.getViewType();
    }

    @Override
    public long getItemId(int position) {
        Reminder item = getItem(position);
        if (item == null) return 0;
        return item.getUniqueId();
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }

    public List<Reminder> getData() {
        return mData;
    }
}
