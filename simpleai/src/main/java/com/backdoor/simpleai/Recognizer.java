package com.backdoor.simpleai;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;

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

    public static final String WEEK = "weekday";
    public static final String CALL = "call";
    public static final String MESSAGE = "message";
    public static final String REMINDER = "reminder";
    public static final String MAIL = "e_mail";

    private Context mContext;
    private String[] times;

    public Recognizer(Context context, String[] times){
        this.mContext = context;
        this.times = times;
    }

    public Model parseResults(String matches, String locale){
        String keyStr = matches.toLowerCase().trim();

        return parse(keyStr, locale);
    }

    private Model parse(String keyStr, String locale) {
        //if (keyStr.startsWith("#")) return null;
        Log.d("-----BEFORE------", keyStr);
        Wrapper wrapper = new Wrapper(locale);
        keyStr = wrapper.replaceNumbers(keyStr);
        Log.d("-----AFTER------", keyStr);
        Model model = new Model();
        if (wrapper.hasNote(keyStr)) {
            keyStr = StringUtils.capitalize(wrapper.clearNote(keyStr));
            model.setSummary(keyStr);
            model.setTypes(Types.NOTE);
            return model;
        }

        if (wrapper.hasAction(keyStr)) {
            int action = wrapper.getAction(keyStr);
            model.setTypes(Types.ACTION);
            model.setActivity(action);
            return model;
        }

        if (wrapper.hasEvent(keyStr)) {
            int action = wrapper.getEvent(keyStr);
            model.setTypes(Types.ACTION);
            model.setActivity(action);
            return model;
        }

        boolean hasAction = false;
        String type = REMINDER;
        int telephony = -1;
        if (wrapper.hasCall(keyStr)) {
            keyStr = wrapper.clearCall(keyStr);
            hasAction = true;
            telephony = 1;
            type = CALL;
        }

        int actionType = -1;
        String message = null;
        if (wrapper.hasSender(keyStr)) {
            keyStr = wrapper.clearSender(keyStr);
            hasAction = true;
            actionType = wrapper.getType(keyStr);
            if (actionType != -1) {
                keyStr = wrapper.clearType(keyStr);
                message = wrapper.getMessage(keyStr);
                keyStr = wrapper.clearMessage(keyStr);
                telephony = 2;
                if (actionType == RecUtils.MESSAGE) type = MESSAGE;
                else type = MAIL;
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

        boolean tomorrow = false;
        if (wrapper.hasTomorrow(keyStr)) {
            keyStr = wrapper.clearTomorrow(keyStr);
            tomorrow = true;
        }

        int ampm = wrapper.getAmpm(keyStr);
        if (ampm != -1) keyStr = wrapper.clearAmpm(keyStr);
        ArrayList<Integer> weekdays = wrapper.getWeekDays(keyStr);
        boolean hasWeekday = false;
        for (int day : weekdays) {
            if (day == 1) {
                hasWeekday = true;
                type = WEEK;
                break;
            }
        }
        keyStr = wrapper.clearWeekDays(keyStr);

        Calendar calendar = Calendar.getInstance();
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

        if (tomorrow) {
            if (time == 0) time = System.currentTimeMillis();
            calendar.setTimeInMillis(time);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(System.currentTimeMillis() + RecUtils.DAY);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
        } else if (hasWeekday && !repeating) {
            if (time == 0) time = System.currentTimeMillis();
            calendar.setTimeInMillis(time);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            int day = wrapper.getWeekday(weekdays);
            weekdays = null;
            type = REMINDER;
            if (day != -1) {
                int mDay = calendar.get(Calendar.DAY_OF_WEEK);
                while (calendar.getTimeInMillis() < System.currentTimeMillis() || day != mDay) {
                    mDay = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + RecUtils.DAY);
                }
            }
        } else if (repeating) {
            if (time == 0) time = System.currentTimeMillis();
            calendar.setTimeInMillis(time);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            if (!hasWeekday) {
                if (calendar.getTimeInMillis() < System.currentTimeMillis())
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + RecUtils.DAY);
            }
        } else if (hasTimer) {
            calendar.setTimeInMillis(System.currentTimeMillis() + afterTime);
        } else if (date != 0 || time != 0) {
            calendar.setTimeInMillis(time);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(date);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
        } else return null;

        String number = null;
        if (hasAction) {
            number = Contacts.findNumber(keyStr, mContext);
            if (actionType == RecUtils.MAIL)
                number = Contacts.findMail(keyStr, mContext);

            if (number == null)
                return null;
        }

        String task = StringUtils.capitalize(keyStr);
        if (hasAction) {
            task = StringUtils.capitalize(message);
            if (type.matches(MESSAGE) || type.matches(MAIL) && task == null)
                return null;
        }

        model.setTypes(Types.REMINDER);
        model.setSummary(task);
        model.setType(type);
        model.setDateTime(calendar.getTimeInMillis());
        model.setWeekdays(weekdays);
        model.setRepeat(repeat);
        model.setNumber(number);
        model.setCalendar(isCalendar);
        model.setAction(telephony);
        return model;
    }
}
