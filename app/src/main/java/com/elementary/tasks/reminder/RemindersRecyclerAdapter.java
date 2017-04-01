package com.elementary.tasks.reminder;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.BindingAdapter;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.adapter.FilterableAdapter;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.IntervalUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboSwitchCompat;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ListItemTaskItemWidgetBinding;
import com.elementary.tasks.databinding.ReminderListItemBinding;
import com.elementary.tasks.databinding.ShoppingListItemBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
import com.elementary.tasks.reminder.models.ShopItem;

import java.util.List;
import java.util.Locale;

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

public class RemindersRecyclerAdapter extends FilterableAdapter<Reminder, String, RecyclerView.ViewHolder> {

    private static final String TAG = "RRA";

    private Context mContext;
    private RecyclerListener mEventListener;
    private ThemeUtil themeUtil;
    private boolean isEditable = true;

    public RemindersRecyclerAdapter(Context context, List<Reminder> list, Filter<Reminder, String> filter) {
        super(list, filter);
        this.mContext = context;
        themeUtil = ThemeUtil.getInstance(context);
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    private void initLabel(RoboTextView listHeader, int position) {
        Reminder item = getUsedData().get(position);
        long due = TimeUtil.getDateTimeFromGmt(item.getEventTime());
        String simpleDate = TimeUtil.getSimpleDate(due);
        Reminder prevItem = null;
        try {
            prevItem = getUsedData().get(position - 1);
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        if (!item.isActive() && position > 0 && (prevItem != null && prevItem.isActive())) {
            simpleDate = mContext.getString(R.string.disabled);
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        } else if (!item.isActive() && position > 0 && (prevItem != null && !prevItem.isActive())) {
            listHeader.setVisibility(View.GONE);
        } else if (!item.isActive() && position == 0) {
            simpleDate = mContext.getString(R.string.disabled);
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        } else if (item.isActive() && position > 0 && (prevItem != null && simpleDate.equals(TimeUtil.getSimpleDate(prevItem.getEventTime())))) {
            listHeader.setVisibility(View.GONE);
        } else {
            if (due <= 0 || due < (System.currentTimeMillis() - AlarmManager.INTERVAL_DAY)) {
                simpleDate = mContext.getString(R.string.permanent);
            } else {
                if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                    simpleDate = mContext.getString(R.string.today);
                } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                    simpleDate = mContext.getString(R.string.tomorrow);
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
        Reminder item = getUsedData().get(position);
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
        return getUsedData().get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return getUsedData().get(position).getUniqueId();
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }

    @BindingAdapter({"loadLeft"})
    public static void loadLeft(RoboTextView textView, Reminder item) {
        if (item.isActive() && !item.isRemoved()) {
            textView.setText(TimeCount.getInstance(textView.getContext()).getRemaining(item.getEventTime(), item.getDelay()));
        } else {
            textView.setText("");
        }
    }

    @BindingAdapter({"loadRepeat"})
    public static void loadRepeat(RoboTextView textView, Reminder model) {
        if (Reminder.isBase(model.getType(), Reminder.BY_MONTH)) {
            textView.setText(String.format(textView.getContext().getString(R.string.xM), String.valueOf(1)));
        } else if (Reminder.isBase(model.getType(), Reminder.BY_WEEK)) {
            textView.setText(ReminderUtils.getRepeatString(textView.getContext(), model.getWeekdays()));
        } else {
            textView.setText(IntervalUtil.getInterval(textView.getContext(), model.getRepeatInterval()));
        }
    }

    @BindingAdapter({"loadContainer"})
    public static void loadContainer(LinearLayout layout, int type) {
        if (Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_OUT) || Reminder.isBase(type, Reminder.BY_PLACES)) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter({"loadType"})
    public static void loadType(RoboTextView textView, int type) {
        textView.setText(ReminderUtils.getTypeString(textView.getContext(), type));
    }

    @BindingAdapter({"loadItems"})
    public static void loadItems(LinearLayout container, List<ShopItem> shoppings) {
        boolean isDark = ThemeUtil.getInstance(container.getContext()).isDark();
        container.setFocusableInTouchMode(false);
        container.setFocusable(false);
        container.removeAllViewsInLayout();
        int count = 0;
        for (ShopItem list : shoppings){
            ListItemTaskItemWidgetBinding binding = ListItemTaskItemWidgetBinding.inflate(LayoutInflater.from(container.getContext()), null, false);
            ImageView checkView = binding.checkView;
            RoboTextView textView = binding.shopText;
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
                container.addView(binding.getRoot());
                break;
            } else {
                checkView.setVisibility(View.VISIBLE);
                textView.setText(list.getSummary());
                container.addView(binding.getRoot());
            }
        }
    }

    @BindingAdapter({"loadCard"})
    public static void loadCard(CardView cardView, String groupId) {
        ThemeUtil cs = ThemeUtil.getInstance(cardView.getContext());
        GroupItem item = RealmDb.getInstance().getGroup(groupId);
        if (item != null) {
            cardView.setCardBackgroundColor(cs.getColor(cs.getCategoryColor(item.getColor())));
        } else {
            cardView.setCardBackgroundColor(cs.getColor(cs.getCategoryColor(0)));
        }
    }

    @BindingAdapter({"loadDate"})
    public static void loadDate(RoboTextView textView, Reminder model) {
        boolean is24 = Prefs.getInstance(textView.getContext()).is24HourFormatEnabled();
        if (Reminder.isGpsType(model.getType())) {
            Place place = model.getPlaces().get(0);
            textView.setText(String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.getLatitude(), place.getLongitude(), model.getPlaces().size()));
            return;
        }
        textView.setText(TimeUtil.getRealDateTime(model.getEventTime(), model.getDelay(), is24));
    }

    @BindingAdapter({"loadShoppingDate"})
    public static void loadShoppingDate(RoboTextView textView, String eventTime) {
        boolean is24 = Prefs.getInstance(textView.getContext()).is24HourFormatEnabled();
        long due = TimeUtil.getDateTimeFromGmt(eventTime);
        if (due > 0){
            textView.setText(TimeUtil.getFullDateTime(due, is24, false));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"loadCheck"})
    public static void loadCheck(RoboSwitchCompat switchCompat, Reminder item) {
        if (item.isRemoved()) {
            switchCompat.setVisibility(View.GONE);
            return;
        }
        if (!item.isActive()) {
            switchCompat.setChecked(false);
        } else {
            switchCompat.setChecked(true);
        }
    }

    @BindingAdapter({"loadContact"})
    public static void loadContact(RoboTextView textView, Reminder model) {
        int type = model.getType();
        String number = model.getTarget();
        if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(number);
        } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
            textView.setVisibility(View.VISIBLE);
            String name = Contacts.getNameFromNumber(number, textView.getContext());
            if (name == null) {
                textView.setText(number);
            } else {
                textView.setText(name + "(" + number + ")");
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            PackageManager packageManager = textView.getContext().getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0);
            } catch (final PackageManager.NameNotFoundException ignored) {
            }
            final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            textView.setVisibility(View.VISIBLE);
            textView.setText(name + "/" + number);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            String name = Contacts.getNameFromMail(number, textView.getContext());
            textView.setVisibility(View.VISIBLE);
            if (name == null) {
                textView.setText(number);
            } else {
                textView.setText(name + "(" + number + ")");
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(number);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
