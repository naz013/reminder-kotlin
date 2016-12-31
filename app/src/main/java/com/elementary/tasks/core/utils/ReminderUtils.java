package com.elementary.tasks.core.utils;

import android.content.Context;

import com.elementary.tasks.R;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
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

public class ReminderUtils {

    public static final String DAY_CHECK = "1";
    public static final int DAY_CHECKED = 1;

    public static ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(DAY_CHECK)) res.add(1);
        else res.add(0);
        return res;
    }

    public static long getTime(int day, int month, int year, int hour, int minute, long after){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar.getTimeInMillis() + after;
    }

    public static String getRepeatString(Context context, List<Integer> repCode){
        StringBuilder sb = new StringBuilder();
        int first = Prefs.getInstance(context).getStartDay();
        if (first == 0) {
            if (repCode.get(0) == DAY_CHECKED) {
                sb.append(" ");
                sb.append(context.getString(R.string.sun));
            }
        }
        if (repCode.get(1) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.mon));
        }
        if (repCode.get(2) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.tue));
        }
        if (repCode.get(3) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.wed));
        }
        if (repCode.get(4) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.thu));
        }
        if (repCode.get(5) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.fri));
        }
        if (repCode.get(6) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.sat));
        }
        if (first == 1) {
            if (repCode.get(0) == DAY_CHECKED) {
                sb.append(" ");
                sb.append(context.getString(R.string.sun));
            }
        }

        if (isAllChecked(repCode)){
            return context.getString(R.string.everyday);
        } else return sb.toString();
    }

    public static boolean isAllChecked(List<Integer> repCode) {
        boolean is = true;
        for (int i : repCode) {
            if (i == 0) {
                is = false;
                break;
            }
        }
        return is;
    }

    public static String getTypeString(Context context, int type){
        String res;
        if (Reminder.isKind(type, Reminder.Kind.CALL)){
            String init = context.getString(R.string.make_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isKind(type, Reminder.Kind.SMS)){
            String init = context.getString(R.string.message);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE_CALL)){
            String init = context.getString(R.string.skype_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE)){
            String init = context.getString(R.string.skype_chat);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE_VIDEO)){
            String init = context.getString(R.string.video_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)){
            String init = context.getString(R.string.application);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)){
            String init = context.getString(R.string.open_link);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)){
            res = context.getString(R.string.shopping_list);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)){
            res = context.getString(R.string.e_mail);
        } else {
            String init = context.getString(R.string.reminder);
            res = init + " (" + getType(context, type) + ")";
        }
        return res;
    }

    public static String getType(Context context, int type){
        String res;
        if (Reminder.isBase(type, Reminder.BY_MONTH)){
            res = context.getString(R.string.day_of_month);
        } else if (Reminder.isBase(type, Reminder.BY_WEEK)){
            res = context.getString(R.string.alarm);
        } else if (Reminder.isBase(type, Reminder.BY_LOCATION)){
            res = context.getString(R.string.location);
        } else if (Reminder.isBase(type, Reminder.BY_OUT)){
            res = context.getString(R.string.place_out);
        } else if (Reminder.isSame(type, Reminder.BY_TIME)){
            res = context.getString(R.string.timer);
        } else if (Reminder.isBase(type, Reminder.BY_PLACES)){
            res = context.getString(R.string.places);
        } else {
            res = context.getString(R.string.by_date);
        }
        return res;
    }
}
