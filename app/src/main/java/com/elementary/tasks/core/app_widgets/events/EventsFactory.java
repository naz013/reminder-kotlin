package com.elementary.tasks.core.app_widgets.events;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
import com.elementary.tasks.reminder.models.ShopItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Copyright 2015 Nazar Suhovich
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

public class EventsFactory implements RemoteViewsService.RemoteViewsFactory {

    @NonNull
    private List<CalendarItem> data = new ArrayList<>();
    @NonNull
    private Map<String, Reminder> map = new HashMap<>();
    private Context mContext;
    private TimeCount mCount;
    private int widgetID;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    EventsFactory(Context ctx, Intent intent) {
        mContext = ctx;
        mCount = TimeCount.getInstance(ctx);
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        data.clear();
        map.clear();
    }

    @Override
    public void onDataSetChanged() {
        data.clear();
        map.clear();
        boolean is24 = Prefs.getInstance(mContext).is24HourFormatEnabled();
        List<Reminder> reminderItems = RealmDb.getInstance().getEnabledReminders();
        for (Reminder item : reminderItems) {
            if (item.getViewType() == Reminder.SHOPPING) {
                continue;
            }
            int type = item.getType();
            String summary = item.getSummary();
            long eventTime = item.getDateTime();
            String id = item.getUuId();

            String time = "";
            String date = "";
            int viewType = 1;
            if (Reminder.isGpsType(type)) {
                Place place = item.getPlaces().get(0);
                date = String.format(Locale.getDefault(), "%.5f", place.getLatitude());
                time = String.format(Locale.getDefault(), "%.5f", place.getLongitude());
            } else if (Reminder.isBase(type, Reminder.BY_WEEK)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(eventTime);
                date = ReminderUtils.getRepeatString(mContext, item.getWeekdays());
                time = TimeUtil.getTime(calendar.getTime(), is24);
            } else if (Reminder.isBase(type, Reminder.BY_MONTH)) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(eventTime);
                date = TimeUtil.DATE_FORMAT.format(calendar1.getTime());
                time = TimeUtil.getTime(calendar1.getTime(), is24);
            } else if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
                viewType = 2;
                map.put(id, item);
            } else {
                String[] dT = mCount.getNextDateTime(eventTime);
                date = dT[0];
                time = dT[1];
            }
            data.add(new CalendarItem(CalendarItem.Type.REMINDER, summary, item.getTarget(), id, time, date, eventTime, viewType, item));
        }

        Prefs prefs = Prefs.getInstance(mContext);
        if (prefs.isBirthdayInWidgetEnabled()) {
            int mDay;
            int mMonth;
            int n = 0;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(TimeUtil.getBirthdayTime(prefs.getBirthdayTime()));
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(System.currentTimeMillis());
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mMonth = calendar.get(Calendar.MONTH);
                List<BirthdayItem> list = RealmDb.getInstance().getBirthdays(mDay, mMonth);
                for (BirthdayItem item : list) {
                    String birthday = item.getDate();
                    String name = item.getName();
                    long eventTime = 0;
                    try {
                        Date date = format.parse(birthday);
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTimeInMillis(System.currentTimeMillis());
                        int year = calendar1.get(Calendar.YEAR);
                        if (date != null) {
                            calendar1.setTime(date);
                            calendar1.set(Calendar.YEAR, year);
                            calendar1.set(Calendar.HOUR_OF_DAY, hour);
                            calendar1.set(Calendar.MINUTE, minute);
                            eventTime = calendar1.getTimeInMillis();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    data.add(new CalendarItem(CalendarItem.Type.BIRTHDAY, mContext.getString(R.string.birthday), name, item.getUuId(), birthday, "", eventTime, 1, item));
                }
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60 * 24));
                n++;
            } while (n <= 7);
        }
        Collections.sort(data, (eventsItem, o2) -> {
            long time1 = 0, time2 = 0;
            if (eventsItem.getItem() instanceof BirthdayItem) {
                BirthdayItem item = (BirthdayItem) eventsItem.getItem();
                TimeUtil.DateItem dateItem = TimeUtil.getFutureBirthdayDate(mContext, item.getDate());
                if (dateItem != null) {
                    Calendar calendar = dateItem.getCalendar();
                    time1 = calendar.getTimeInMillis();
                }
            } else if (eventsItem.getItem() instanceof Reminder) {
                Reminder reminder = (Reminder) eventsItem.getItem();
                time1 = TimeUtil.getDateTimeFromGmt(reminder.getEventTime());
            }
            if (o2.getItem() instanceof BirthdayItem) {
                BirthdayItem item = (BirthdayItem) o2.getItem();
                TimeUtil.DateItem dateItem = TimeUtil.getFutureBirthdayDate(mContext, item.getDate());
                if (dateItem != null) {
                    Calendar calendar = dateItem.getCalendar();
                    time2 = calendar.getTimeInMillis();
                }
            } else if (o2.getItem() instanceof Reminder) {
                Reminder reminder = (Reminder) o2.getItem();
                time2 = TimeUtil.getDateTimeFromGmt(reminder.getEventTime());
            }
            return (int) (time1 - time2);
        });
    }

    @Override
    public void onDestroy() {
        map.clear();
        data.clear();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                EventsWidgetConfig.EVENTS_WIDGET_PREF, Context.MODE_PRIVATE);
        int theme = sp.getInt(EventsWidgetConfig.EVENTS_WIDGET_THEME + widgetID, 0);
        EventsTheme eventsTheme = EventsTheme.getThemes(mContext).get(theme);
        int itemBackground = eventsTheme.getItemBackground();
        int itemTextColor = eventsTheme.getItemTextColor();
        float itemTextSize = sp.getFloat(EventsWidgetConfig.EVENTS_WIDGET_TEXT_SIZE + widgetID, 0);
        int checkboxColor = eventsTheme.getCheckboxColor();

        RemoteViews rView = null;
        if (i >= getCount()) {
            return null;
        }
        CalendarItem item = data.get(i);
        if (item.getViewType() == 1) {
            rView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_current_widget);
            rView.setInt(R.id.itemBg, "setBackgroundResource", itemBackground);

            String task = item.getName();
            if (task == null || task.matches("")) {
                task = Contacts.getNameFromNumber(item.getNumber(), mContext);
            }
            rView.setTextViewText(R.id.taskText, task);
            rView.setTextColor(R.id.taskText, itemTextColor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rView.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                rView.setTextViewTextSize(R.id.taskNumber, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                rView.setTextViewTextSize(R.id.taskDate, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                rView.setTextViewTextSize(R.id.taskTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                rView.setTextViewTextSize(R.id.leftTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
            } else {
                rView.setFloat(R.id.taskTime, "setTextSize", itemTextSize);
                rView.setFloat(R.id.taskDate, "setTextSize", itemTextSize);
                rView.setFloat(R.id.taskNumber, "setTextSize", itemTextSize);
                rView.setFloat(R.id.taskText, "setTextSize", itemTextSize);
                rView.setFloat(R.id.leftTime, "setTextSize", itemTextSize);
            }

            String number = item.getNumber();
            if (number != null && !number.matches("")) {
                rView.setTextViewText(R.id.taskNumber, number);
                rView.setTextColor(R.id.taskNumber, itemTextColor);
            } else {
                rView.setViewVisibility(R.id.taskNumber, View.GONE);
            }
            rView.setTextViewText(R.id.taskDate, item.getDayDate());
            rView.setTextColor(R.id.taskDate, itemTextColor);

            rView.setTextViewText(R.id.taskTime, item.getTime());
            rView.setTextColor(R.id.taskTime, itemTextColor);

            rView.setTextViewText(R.id.leftTime, mCount.getRemaining(item.getDate()));
            rView.setTextColor(R.id.leftTime, itemTextColor);

            if (item.getId() != null) {
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Constants.INTENT_ID, item.getId());
                if (item.getType() == CalendarItem.Type.REMINDER) {
                    fillInIntent.putExtra(EventEditService.TYPE, true);
                } else {
                    fillInIntent.putExtra(EventEditService.TYPE, false);
                }
                rView.setOnClickFillInIntent(R.id.taskDate, fillInIntent);
                rView.setOnClickFillInIntent(R.id.taskTime, fillInIntent);
                rView.setOnClickFillInIntent(R.id.taskNumber, fillInIntent);
                rView.setOnClickFillInIntent(R.id.taskText, fillInIntent);
                rView.setOnClickFillInIntent(R.id.itemBg, fillInIntent);
            }
        }
        if (item.getViewType() == 2) {
            rView = new RemoteViews(mContext.getPackageName(),
                    R.layout.list_item_current_widget_with_list);
            rView.setInt(R.id.itemBg, "setBackgroundResource", itemBackground);
            String task = item.getName();
            rView.setTextViewText(R.id.taskText, task);
            rView.setTextColor(R.id.taskText, itemTextColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rView.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
            } else {
                rView.setFloat(R.id.taskText, "setTextSize", itemTextSize);
            }

            int count = 0;
            List<ShopItem> lists = map.get(item.getId()).getShoppings();
            rView.removeAllViews(R.id.todoList);
            for (ShopItem list : lists) {
                RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.list_item_task_item_widget);
                boolean isBlack = checkboxColor == 0;
                if (list.isChecked()) {
                    if (isBlack) {
                        view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_black_24dp);
                    } else {
                        view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_white_24dp);
                    }
                } else {
                    if (isBlack) {
                        view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_outline_blank_black_24dp);
                    } else {
                        view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_outline_blank_white_24dp);
                    }
                }

                view.setTextColor(R.id.shopText, itemTextColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setTextViewTextSize(R.id.shopText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                } else {
                    view.setFloat(R.id.shopText, "setTextSize", itemTextSize);
                }

                count++;
                if (count == 9) {
                    view.setViewVisibility(R.id.checkView, View.INVISIBLE);
                    view.setTextViewText(R.id.shopText, "...");
                    rView.addView(R.id.todoList, view);
                    break;
                } else {
                    view.setViewVisibility(R.id.checkView, View.VISIBLE);
                    view.setTextViewText(R.id.shopText, list.getSummary());
                    rView.addView(R.id.todoList, view);
                }
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Constants.INTENT_ID, item.getId());
            fillInIntent.putExtra(EventEditService.TYPE, true);
            rView.setOnClickFillInIntent(R.id.taskText, fillInIntent);
            rView.setOnClickFillInIntent(R.id.itemBg, fillInIntent);
            rView.setOnClickFillInIntent(R.id.todoList, fillInIntent);
        }
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}