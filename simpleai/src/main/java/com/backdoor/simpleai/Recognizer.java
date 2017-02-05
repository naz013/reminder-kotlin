package com.backdoor.simpleai;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Recognizer {

    private static final String TAG = "Recognizer";

    private Context mContext;
    private String[] times;
    private WorkerInterface wrapper;
    private ContactsInterface contactsInterface;

    private Recognizer(Context context, String[] times, String locale, ContactsInterface contactsInterface) {
        this.mContext = context;
        this.times = times;
        this.contactsInterface = contactsInterface;
        wrapper = WorkerFactory.getWorker(locale);
    }

    public Model parse(String string) {
        String keyStr = string.toLowerCase().trim();
        keyStr = wrapper.replaceNumbers(keyStr);
        Log.d(TAG, "parse: " + keyStr);
        if (wrapper.hasNote(keyStr)) {
            return getNote(keyStr);
        }
        if (wrapper.hasGroup(keyStr)) {
            keyStr = wrapper.clearGroup(keyStr);
            return getGroup(keyStr);
        }
        if (wrapper.hasEvent(keyStr)) {
            Model model = getEvent(keyStr);
            if (model != null) return model;
        }
        if (wrapper.hasAction(keyStr)) {
            return getAction(keyStr);
        }
        if (wrapper.hasEmptyTrash(keyStr)) {
            return getEmptyTrash();
        }
        if (wrapper.hasDisableReminders(keyStr)) {
            return getDisableAction();
        }
        if (wrapper.hasAnswer(keyStr)) {
            return getAnswer(keyStr);
        }

        Action type = Action.DATE;
        String number = null;
        boolean hasAction = false;
        if (wrapper.hasCall(keyStr)) {
            keyStr = wrapper.clearCall(keyStr);
            hasAction = true;
            type = Action.CALL;
        }
        if (wrapper.hasSender(keyStr)) {
            keyStr = wrapper.clearSender(keyStr);
            Action actionType = wrapper.getMessageType(keyStr);
            if (actionType != null) {
                hasAction = true;
                keyStr = wrapper.clearMessageType(keyStr);
                type = actionType;
            }
        }

        boolean repeating = false;
        long repeat = 0;
        if (wrapper.hasRepeat(keyStr)) {
            keyStr = wrapper.clearRepeat(keyStr);
            repeating = true;
            repeat = wrapper.getDaysRepeat(keyStr);
            if (repeat != 0) keyStr = wrapper.clearDaysRepeat(keyStr);
        }

        boolean isCalendar = false;
        if (wrapper.hasCalendar(keyStr)) {
            keyStr = wrapper.clearCalendar(keyStr);
            isCalendar = true;
        }

        boolean today = false;
        if (wrapper.hasToday(keyStr)) {
            keyStr = wrapper.clearToday(keyStr);
            today = true;
        }
        boolean afterTomorrow = false;
        if (wrapper.hasAfterTomorrow(keyStr)) {
            keyStr = wrapper.clearAfterTomorrow(keyStr);
            afterTomorrow = true;
        }
        boolean tomorrow = false;
        if (wrapper.hasTomorrow(keyStr)) {
            keyStr = wrapper.clearTomorrow(keyStr);
            tomorrow = true;
        }

        Ampm ampm = wrapper.getAmpm(keyStr);
        if (ampm != null) keyStr = wrapper.clearAmpm(keyStr);
        List<Integer> weekdays = wrapper.getWeekDays(keyStr);
        boolean hasWeekday = false;
        for (int day : weekdays) {
            if (day == 1) {
                hasWeekday = true;
                break;
            }
        }
        keyStr = wrapper.clearWeekDays(keyStr);
        if (hasWeekday) {
            if (type == Action.CALL) type = Action.WEEK_CALL;
            else if (type == Action.MESSAGE) type = Action.WEEK_SMS;
            else type = Action.WEEK;
        }

        boolean hasTimer = false;
        long afterTime = 0;
        if (wrapper.isTimer(keyStr)) {
            keyStr = wrapper.cleanTimer(keyStr);
            afterTime = wrapper.getMultiplier(keyStr);
            keyStr = wrapper.clearMultiplier(keyStr);
            hasTimer = true;
        }

        long date = wrapper.getDate(keyStr);
        if (date != 0) keyStr = wrapper.clearDate(keyStr);

        long time = wrapper.getTime(keyStr, ampm, times);
        if (time != 0) keyStr = wrapper.clearTime(keyStr);
        Log.d(TAG, "parse: " + keyStr);
        if (today) {
            time = getTodayTime(time);
        } else if (afterTomorrow) {
            time = getAfterTomorrowTime(time);
        } else if (tomorrow) {
            time = getTomorrowTime(time);
        } else if (hasWeekday && !repeating) {
            time = getDayTime(time, weekdays);
        } else if (repeating) {
            time = getRepeatingTime(time, hasWeekday);
        } else if (hasTimer) {
            time = System.currentTimeMillis() + afterTime;
        } else if (date != 0 || time != 0) {
            time = getDateTime(date, time);
        } else return null;

        String message = null;
        if (hasAction && (type == Action.MESSAGE || type == Action.MAIL)) {
            message = wrapper.getMessage(keyStr);
            keyStr = wrapper.clearMessage(keyStr);
            if (message != null) {
                keyStr = keyStr.replace(message, "");
            }
        }
        if (hasAction && contactsInterface != null) {
            ContactOutput output = contactsInterface.findNumber(keyStr, mContext);
            keyStr = output.getOutput();
            number = output.getNumber();
            if (type == Action.MAIL) {
                output = contactsInterface.findEmail(keyStr, mContext);
                number = output.getNumber();
                keyStr = output.getOutput();
            }
            if (number == null)
                return null;
        }

        String task = StringUtils.capitalize(keyStr);
        if (hasAction) {
            task = StringUtils.capitalize(message);
            if ((type == Action.MESSAGE || type == Action.MAIL) && task == null)
                return null;
        }
        Model model = new Model();
        model.setType(ActionType.REMINDER);
        model.setSummary(task);
        model.setDateTime(time);
        model.setWeekdays(weekdays);
        model.setRepeatInterval(repeat);
        model.setTarget(number);
        model.setHasCalendar(isCalendar);
        model.setAction(type);
        return model;
    }

    private Model getAnswer(String keyStr) {
        Model model = new Model();
        model.setType(ActionType.ANSWER);
        model.setAction(wrapper.getAnswer(keyStr));
        return model;
    }

    private long getDateTime(long date, long time) {
        if (date == 0) date = System.currentTimeMillis();
        if (time == 0) time = date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + Worker.DAY);
        }
        return calendar.getTimeInMillis();
    }

    private long getRepeatingTime(long time, boolean hasWeekday) {
        Calendar calendar = Calendar.getInstance();
        if (time == 0) time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (!hasWeekday) {
            if (calendar.getTimeInMillis() < System.currentTimeMillis())
                calendar.setTimeInMillis(calendar.getTimeInMillis() + Worker.DAY);
        }
        return calendar.getTimeInMillis();
    }

    private long getDayTime(long time, List<Integer> weekdays) {
        Calendar calendar = Calendar.getInstance();
        if (time == 0) time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int count = Worker.getNumberOfSelectedWeekdays(weekdays);
        if (count == 1) {
            while (true) {
                int mDay = calendar.get(Calendar.DAY_OF_WEEK);
                if (weekdays.get(mDay - 1) == 1 && calendar.getTimeInMillis() > System.currentTimeMillis()) {
                    break;
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return calendar.getTimeInMillis();
    }

    private long getTomorrowTime(long time) {
        Calendar calendar = Calendar.getInstance();
        if (time == 0) time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis() + Worker.DAY);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getAfterTomorrowTime(long time) {
        Calendar calendar = Calendar.getInstance();
        if (time == 0) time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis() + (Worker.DAY * 2));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getTodayTime(long time) {
        Calendar calendar = Calendar.getInstance();
        if (time == 0) time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private Model getDisableAction() {
        Model model = new Model();
        model.setType(ActionType.ACTION);
        model.setAction(Action.DISABLE);
        return model;
    }

    private Model getEmptyTrash() {
        Model model = new Model();
        model.setType(ActionType.ACTION);
        model.setAction(Action.TRASH);
        return model;
    }

    private Model getGroup(String keyStr) {
        keyStr = StringUtils.capitalize(wrapper.clearNote(keyStr));
        Model model = new Model();
        model.setSummary(keyStr);
        model.setType(ActionType.GROUP);
        return model;
    }

    private Model getEvent(String keyStr) {
        Action event = wrapper.getEvent(keyStr);
        if (event == Action.NO_EVENT) return null;
        Model model = new Model();
        model.setType(ActionType.ACTION);
        model.setAction(event);
        return model;
    }

    private Model getAction(String keyStr) {
        Model model = new Model();
        model.setType(ActionType.ACTION);
        model.setAction(wrapper.getAction(keyStr));
        return model;
    }

    private Model getNote(String keyStr) {
        keyStr = StringUtils.capitalize(wrapper.clearNote(keyStr));
        Model model = new Model();
        model.setSummary(keyStr);
        model.setType(ActionType.NOTE);
        return model;
    }

    public static class Builder {

        public Builder() {
        }

        public LocaleBuilder with(Context context) {
            return new LocaleBuilder(context);
        }

        public class LocaleBuilder {

            private Context context;

            LocaleBuilder(Context context) {
                this.context = context;
            }

            public TimeBuilder setLocale(String locale) {
                return new TimeBuilder(context, locale);
            }
        }

        public class TimeBuilder {

            private Context context;
            private String locale;

            TimeBuilder(Context context, String locale) {
                this.context = context;
                this.locale = locale;
            }

            public ExtraBuilder setTimes(String[] times) {
                return new ExtraBuilder(context, locale, times);
            }
        }

        public class ExtraBuilder {

            private Context context;
            private ContactsInterface contactsInterface;
            private String[] times;
            private String locale;

            ExtraBuilder(Context context, String locale, String[] times) {
                this.context = context;
                this.locale = locale;
                this.times = times;
            }

            public ExtraBuilder setContactsInterface(ContactsInterface contactsInterface) {
                this.contactsInterface = contactsInterface;
                return this;
            }

            public Recognizer build() {
                return new Recognizer(context, times, locale, contactsInterface);
            }
        }
    }
}
