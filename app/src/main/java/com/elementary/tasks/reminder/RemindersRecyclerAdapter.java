package com.elementary.tasks.reminder;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.file_explorer.FilterCallback;
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

import java.util.ArrayList;
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

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RRA";

    private Context mContext;
    private List<Reminder> mDataList;
    private RecyclerListener mEventListener;
    private FilterCallback mCallback;

    public RemindersRecyclerAdapter(Context context, List<Reminder> list, FilterCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        mDataList = new ArrayList<>(list);
        setHasStableIds(true);
    }

    public Reminder getItem(int position) {
        if (position < mDataList.size()) {
            return mDataList.get(position);
        } return null;
    }

    public void removeItem(int position) {
        if (position < mDataList.size()) {
            mDataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeRemoved(0, mDataList.size());
        }
    }

    public class ReminderHolder extends RecyclerView.ViewHolder {

        public RoboTextView listHeader;
        public ReminderListItemBinding binding;

        public ReminderHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            listHeader = binding.listHeader;
            binding.reminderContainer.setBackgroundColor(ThemeUtil.getInstance(mContext).getCardStyle());
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
                            mEventListener.onItemClicked(getAdapterPosition(), binding.itemCheck);
                        }
                        break;
                    case R.id.itemCheck:
                        if (mEventListener != null) {
                            mEventListener.onItemSwitched(getAdapterPosition(), v1);
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
            binding.subBackground.setBackgroundColor(ThemeUtil.getInstance(mContext).getCardStyle());
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

    private void initLabel(RoboTextView listHeader, int position) {
        Reminder item = mDataList.get(position);
        long due = TimeUtil.getDateTimeFromGmt(item.getEventTime());
        String simpleDate = TimeUtil.getSimpleDate(due);
        Reminder prevItem = null;
        try {
            prevItem = mDataList.get(position - 1);
        } catch (ArrayIndexOutOfBoundsException e) {}
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

    public void filter(String q, List<Reminder> list) {
        List<Reminder> res = filter(list, q);
        animateTo(res);
        if (mCallback != null) mCallback.filter(res.size());
    }

    private List<Reminder> filter(List<Reminder> mData, String q) {
        q = q.toLowerCase();
        if (mData == null) mData = new ArrayList<>();
        List<Reminder> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<Reminder> getFiltered(List<Reminder> models, String query) {
        List<Reminder> list = new ArrayList<>();
        for (Reminder model : models) {
            final String text = model.getSummary().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    private Reminder remove(int position) {
        final Reminder model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, Reminder model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final Reminder model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void animateTo(List<Reminder> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Reminder> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final Reminder model = mDataList.get(i);
            if (!newModels.contains(model)) {
                remove(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Reminder> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Reminder model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Reminder> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Reminder model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == Reminder.REMINDER) {
            return new ReminderHolder(ReminderListItemBinding.inflate(inflater, parent, false).getRoot());
        } else {
            return new ShoppingHolder(ShoppingListItemBinding.inflate(inflater, parent, false).getRoot());
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Reminder item = mDataList.get(position);
        if (holder instanceof ReminderHolder) {
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.binding.setItem(item);
            initLabel(reminderHolder.listHeader, position);
        } else if (holder instanceof ShoppingHolder) {
            ShoppingHolder shoppingHolder = (ShoppingHolder) holder;
            shoppingHolder.binding.setItem(item);
            initLabel(shoppingHolder.listHeader, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return mDataList.get(position).getUniqueId();
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
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
                if (isDark) checkView.setImageResource(R.drawable.ic_check_box_white_24dp);
                else checkView.setImageResource(R.drawable.ic_check_box_black_24dp);
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                if (isDark) checkView.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp);
                else checkView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
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
        textView.setVisibility(View.VISIBLE);
        if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            textView.setText(number);
        } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
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
            textView.setText(name + "/" + number);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            String name = Contacts.getNameFromMail(number, textView.getContext());
            if (name == null) {
                textView.setText(number);
            } else {
                textView.setText(name + "(" + number + ")");
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            textView.setText(number);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
