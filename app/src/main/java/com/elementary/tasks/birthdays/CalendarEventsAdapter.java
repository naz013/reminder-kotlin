package com.elementary.tasks.birthdays;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Module;
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
import com.elementary.tasks.reminder.models.Reminder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        if (list != null) mDataList = new ArrayList<>(list);
        cs = ThemeUtil.getInstance(context);
    }

    public final void setEventListener(final RecyclerListener listener) {
        this.mEventListener = listener;
    }

    public class BirthdayHolder extends RecyclerView.ViewHolder {

        ListItemEventsBinding binding;

        public BirthdayHolder(final View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.itemCard.setCardBackgroundColor(cs.getCardStyle());
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
    }

    public class ShoppingHolder extends RecyclerView.ViewHolder {

        public RoboTextView listHeader;
        public ShoppingListItemBinding binding;

        public ShoppingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            listHeader = binding.listHeader;
            binding.subBackground.setBackgroundColor(cs.getCardStyle());
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
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            return new ReminderHolder(ReminderListItemBinding.inflate(inflater, parent, false).getRoot(), mEventListener, cs, false);
        } else if (viewType == 1) {
            return new CalendarEventsAdapter.ShoppingHolder(ShoppingListItemBinding.inflate(inflater, parent, false).getRoot());
        } else {
            View view = DataBindingUtil.inflate(inflater, R.layout.list_item_events, parent, false).getRoot();
            return new BirthdayHolder(view);
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof BirthdayHolder) {
            BirthdayItem item = (BirthdayItem) mDataList.get(position).getObject();
            BirthdayHolder birthdayHolder = (BirthdayHolder) holder;
            birthdayHolder.binding.setItem(item);
            birthdayHolder.binding.setColor(mDataList.get(position).getColor());
        } else if (holder instanceof ReminderHolder) {
            Reminder item = (Reminder) mDataList.get(position).getObject();
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.setData(item);
        } else if (holder instanceof ShoppingHolder) {
            Reminder item = (Reminder) mDataList.get(position).getObject();
            ShoppingHolder shoppingHolder = (ShoppingHolder) holder;
            shoppingHolder.binding.setItem(item);
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
        Date date = null;
        String dateTime = Prefs.getInstance(textView.getContext()).getBirthdayTime();
        boolean is24 = Prefs.getInstance(textView.getContext()).is24HourFormatEnabled();
        try {
            date = CheckBirthdaysAsync.dateFormat.parse(fullDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = System.currentTimeMillis();
        int year = 0;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int bDay = calendar.get(Calendar.DAY_OF_MONTH);
            int bMonth = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
            calendar.setTimeInMillis(TimeUtil.getBirthdayTime(dateTime));
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.MONTH, bMonth);
            calendar.set(Calendar.DAY_OF_MONTH, bDay);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            time = calendar.getTimeInMillis();
        }
        textView.setText(SuperUtil.appendString(TimeUtil.getFullDateTime(time, is24, false),
                "\n", TimeUtil.getAgeFormatted(textView.getContext(), year)));
    }
}
