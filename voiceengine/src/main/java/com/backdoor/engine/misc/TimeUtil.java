package com.backdoor.engine.misc;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US);
    private static final String GMT = "GMT";

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
