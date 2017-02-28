package com.elementary.tasks.core.utils;

import android.content.Context;

import com.elementary.tasks.R;

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

public final class IntervalUtil {

    public static final int REPEAT_CODE_ONCE = 0;
    public static final int INTERVAL_DAY = 1;
    public static final int INTERVAL_WEEK = INTERVAL_DAY * 7;
    public static final int INTERVAL_TWO_WEEKS = INTERVAL_WEEK * 2;
    public static final int INTERVAL_THREE_WEEKS = INTERVAL_WEEK * 3;
    public static final int INTERVAL_FOUR_WEEKS = INTERVAL_WEEK * 4;

    private IntervalUtil() {}

    public static List<Integer> getWeekRepeat(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri,
                                                   boolean sat, boolean sun){
        List<Integer> sb = new ArrayList<>(7);
        sb.add(0, sun ? 1 : 0);
        sb.add(1, mon ? 1 : 0);
        sb.add(2, tue ? 1 : 0);
        sb.add(3, wed ? 1 : 0);
        sb.add(4, thu ? 1 : 0);
        sb.add(5, fri ? 1 : 0);
        sb.add(6, sat ? 1 : 0);
        return sb;
    }

    public static boolean isWeekday(List<Integer> weekday) {
        for (int day : weekday) {
            if (day == ReminderUtils.DAY_CHECKED) {
                return true;
            }
        }
        return false;
    }

    public static String getInterval(Context mContext, long code){
        long minute = 1000 * 60;
        long day = minute * 60 * 24;
        long tmp = code / minute;
        String interval;
        if (tmp > 1000) {
            code /= day;
            if (code == REPEAT_CODE_ONCE) {
                interval = "0";
            } else if (code == INTERVAL_WEEK) {
                interval = String.format(mContext.getString(R.string.xW), String.valueOf(1));
            } else if (code == INTERVAL_TWO_WEEKS) {
                interval = String.format(mContext.getString(R.string.xW), String.valueOf(2));
            } else if (code == INTERVAL_THREE_WEEKS) {
                interval = String.format(mContext.getString(R.string.xW), String.valueOf(3));
            } else if (code == INTERVAL_FOUR_WEEKS) {
                interval = String.format(mContext.getString(R.string.xW), String.valueOf(4));
            } else {
                interval = String.format(mContext.getString(R.string.xD), String.valueOf(code));
            }
        } else {
            if (tmp == 0) {
                return "0";
            } else {
                return String.format(mContext.getString(R.string.x_min), String.valueOf(tmp));
            }
        }
        return interval;
    }
}
