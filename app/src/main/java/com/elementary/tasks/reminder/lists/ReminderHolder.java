package com.elementary.tasks.reminder.lists;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.IntervalUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ReminderListItemBinding;

import java.util.Locale;

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

public class ReminderHolder extends RecyclerView.ViewHolder {

    private RoboTextView listHeader;
    private ReminderListItemBinding binding;
    private RecyclerListener mEventListener;
    private ThemeUtil themeUtil;

    public ReminderHolder(View v, RecyclerListener listener, ThemeUtil themeUtil, boolean editable) {
        super(v);
        this.themeUtil = themeUtil;
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
        loadCard(reminder.getGroup());
        loadDate(reminder);
        loadCheck(reminder);
        loadContact(reminder);
        loadLeft(reminder);
        loadRepeat(reminder);
        loadContainer(reminder.getType());
        loadType(reminder.getType());
    }

    private void loadType(int type) {
        binding.reminderType.setText(ReminderUtils.getTypeString(binding.reminderType.getContext(), type));
    }

    private void loadLeft(Reminder item) {
        if (item.isActive() && !item.isRemoved()) {
            binding.remainingTime.setText(TimeCount.getInstance(binding.remainingTime.getContext()).getRemaining(item.getEventTime(), item.getDelay()));
        } else {
            binding.remainingTime.setText("");
        }
    }

    private void loadRepeat(Reminder model) {
        if (Reminder.isBase(model.getType(), Reminder.BY_MONTH)) {
            binding.repeatInterval.setText(String.format(binding.repeatInterval.getContext().getString(R.string.xM), String.valueOf(1)));
        } else if (Reminder.isBase(model.getType(), Reminder.BY_WEEK)) {
            binding.repeatInterval.setText(ReminderUtils.getRepeatString(binding.repeatInterval.getContext(), model.getWeekdays()));
        } else if (Reminder.isBase(model.getType(), Reminder.BY_DAY_OF_YEAR)) {
            binding.repeatInterval.setText(binding.repeatInterval.getContext().getString(R.string.yearly));
        } else {
            binding.repeatInterval.setText(IntervalUtil.getInterval(binding.repeatInterval.getContext(), model.getRepeatInterval()));
        }
    }

    private void loadContainer(int type) {
        if (Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_OUT) || Reminder.isBase(type, Reminder.BY_PLACES)) {
            binding.endContainer.setVisibility(View.GONE);
        } else {
            binding.endContainer.setVisibility(View.VISIBLE);
        }
    }

    private void loadCard(@Nullable Group group) {
        if (group != null) {
            binding.itemCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(group.getColor())));
        } else {
            binding.itemCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(0)));
        }
    }

    private void loadDate(Reminder model) {
        boolean is24 = Prefs.getInstance(binding.taskDate.getContext()).is24HourFormatEnabled();
        if (Reminder.isGpsType(model.getType())) {
            Place place = model.getPlaces().get(0);
            binding.taskDate.setText(String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.getLatitude(), place.getLongitude(), model.getPlaces().size()));
            return;
        }
        binding.taskDate.setText(TimeUtil.getRealDateTime(model.getEventTime(), model.getDelay(), is24));
    }

    private void loadCheck(Reminder item) {
        if (item.isRemoved()) {
            binding.itemCheck.setVisibility(View.GONE);
            return;
        }
        if (!item.isActive()) {
            binding.itemCheck.setChecked(false);
        } else {
            binding.itemCheck.setChecked(true);
        }
    }

    private void loadContact(Reminder model) {
        int type = model.getType();
        String number = model.getTarget();
        if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            binding.reminderPhone.setVisibility(View.VISIBLE);
            binding.reminderPhone.setText(number);
        } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
            binding.reminderPhone.setVisibility(View.VISIBLE);
            String name = Contacts.getNameFromNumber(number, binding.reminderPhone.getContext());
            if (name == null) {
                binding.reminderPhone.setText(number);
            } else {
                binding.reminderPhone.setText(name + "(" + number + ")");
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            PackageManager packageManager = binding.reminderPhone.getContext().getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0);
            } catch (final PackageManager.NameNotFoundException ignored) {
            }
            final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            binding.reminderPhone.setVisibility(View.VISIBLE);
            binding.reminderPhone.setText(name + "/" + number);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            String name = Contacts.getNameFromMail(number, binding.reminderPhone.getContext());
            binding.reminderPhone.setVisibility(View.VISIBLE);
            if (name == null) {
                binding.reminderPhone.setText(number);
            } else {
                binding.reminderPhone.setText(name + "(" + number + ")");
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            binding.reminderPhone.setVisibility(View.VISIBLE);
            binding.reminderPhone.setText(number);
        } else {
            binding.reminderPhone.setVisibility(View.GONE);
        }
    }
}
