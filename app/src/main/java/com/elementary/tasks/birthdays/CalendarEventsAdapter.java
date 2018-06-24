package com.elementary.tasks.birthdays;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.elementary.tasks.core.data.models.Birthday;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ListItemEventsBinding;
import com.elementary.tasks.databinding.ListItemReminderBinding;
import com.elementary.tasks.databinding.ListItemShoppingBinding;
import com.elementary.tasks.reminder.lists.RecyclerListener;
import com.elementary.tasks.reminder.lists.ReminderHolder;
import com.elementary.tasks.reminder.lists.ShoppingHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
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
public class CalendarEventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EventsItem> data = new ArrayList<>();
    @Nullable
    private RecyclerListener mEventListener;

    public CalendarEventsAdapter() {
    }

    public void setData(List<EventsItem> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public final void setEventListener(@Nullable final RecyclerListener listener) {
        this.mEventListener = listener;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            return new ReminderHolder(ListItemReminderBinding.inflate(inflater, parent, false).getRoot(), mEventListener, false);
        } else if (viewType == 1) {
            return new ShoppingHolder(ListItemShoppingBinding.inflate(inflater, parent, false).getRoot(), mEventListener);
        } else {
            return new BirthdayHolder(ListItemEventsBinding.inflate(inflater, parent, false).getRoot(), mEventListener);
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof BirthdayHolder) {
            Birthday item = (Birthday) data.get(position).getObject();
            BirthdayHolder birthdayHolder = (BirthdayHolder) holder;
            birthdayHolder.setData(item);
            birthdayHolder.setColor(data.get(position).getColor());
        } else if (holder instanceof ReminderHolder) {
            Reminder item = (Reminder) data.get(position).getObject();
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.setData(item);
        } else if (holder instanceof ShoppingHolder) {
            Reminder item = (Reminder) data.get(position).getObject();
            ShoppingHolder shoppingHolder = (ShoppingHolder) holder;
            shoppingHolder.setData(item);
        }
    }

    @Override
    public final long getItemId(final int position) {
        return 0;
    }

    @Override
    public final int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getViewType();
    }

    @BindingAdapter({"loadBirthday"})
    public static void loadBirthday(RoboTextView textView, String fullDate) {
        boolean is24 = Prefs.getInstance(textView.getContext()).is24HourFormatEnabled();
        TimeUtil.DateItem dateItem = TimeUtil.getFutureBirthdayDate(textView.getContext(), fullDate);
        if (dateItem != null) {
            textView.setText(SuperUtil.appendString(TimeUtil.getFullDateTime(dateItem.getCalendar().getTimeInMillis(), is24, false),
                    "\n", TimeUtil.getAgeFormatted(textView.getContext(), dateItem.getYear())));
        }
    }

    public AdapterItem getItem(int position) {
        return data.get(position);
    }
}
