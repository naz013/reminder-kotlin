package com.elementary.tasks.birthdays;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ListItemEventsBinding;
import com.elementary.tasks.databinding.ReminderListItemBinding;
import com.elementary.tasks.databinding.ShoppingListItemBinding;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.ReminderHolder;
import com.elementary.tasks.reminder.ShoppingHolder;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.List;

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

    private List<EventsItem> mDataList;
    private ThemeUtil cs;
    private RecyclerListener mEventListener;

    public CalendarEventsAdapter(final Context context, List<EventsItem> list) {
        if (list != null) {
            mDataList = new ArrayList<>(list);
        }
        cs = ThemeUtil.getInstance(context);
    }

    public final void setEventListener(final RecyclerListener listener) {
        this.mEventListener = listener;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            return new ReminderHolder(ReminderListItemBinding.inflate(inflater, parent, false).getRoot(), mEventListener, cs, false);
        } else if (viewType == 1) {
            return new ShoppingHolder(ShoppingListItemBinding.inflate(inflater, parent, false).getRoot(), mEventListener, cs);
        } else {
            return new BirthdayHolder(ListItemEventsBinding.inflate(inflater, parent, false).getRoot(), mEventListener, cs);
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof BirthdayHolder) {
            BirthdayItem item = (BirthdayItem) mDataList.get(position).getObject();
            BirthdayHolder birthdayHolder = (BirthdayHolder) holder;
            birthdayHolder.setData(item);
            birthdayHolder.setColor(mDataList.get(position).getColor());
        } else if (holder instanceof ReminderHolder) {
            Reminder item = (Reminder) mDataList.get(position).getObject();
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.setData(item);
        } else if (holder instanceof ShoppingHolder) {
            Reminder item = (Reminder) mDataList.get(position).getObject();
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
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).getViewType();
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
}
