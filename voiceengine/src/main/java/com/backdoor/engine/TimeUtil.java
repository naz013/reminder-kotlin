package com.backdoor.engine;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

public class TimeUtil {

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", java.util.Locale.getDefault());
    public static final String GMT = "GMT";

    public static String getGmtFromDateTime(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
        return GMT_DATE_FORMAT.format(calendar.getTime());
    }

    public static String getDateTimeFromGmt(String dateTime){
        if (StringUtils.isEmpty(dateTime)) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        try {
            GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = GMT_DATE_FORMAT.parse(dateTime);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return GMT_DATE_FORMAT.format(calendar.getTime());
    }
}
