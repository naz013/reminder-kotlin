package com.backdoor.simpleai;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RecUtils {

    /**
     * Millisecond constants.
     */
    public final static long SECOND = 1000;
    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = MINUTE * 60;
    public final static long HALF_DAY = HOUR * 12;
    public final static long DAY = HALF_DAY * 2;

    /**
     * Action types.
     */
    public static final int SETTINGS = 172;
    public static final int APP = 725;
    public static final int VOLUME = 171;
    public static final int HELP = 15;
    public static final int REMINDER = 944;
    public static final int BIRTHDAY = 844;
    public static final int REPORT = 629;

    /**
     * Parts of a day.
     */
    public static final int MORNING = 454;
    public static final int NOON = 935;
    public static final int EVENING = 565;
    public static final int NIGHT = 136;

    /**
     * Message types.
     */
    public static final int MESSAGE = 874;
    public static final int MAIL = 681;

    public static final SimpleDateFormat[] dateTaskFormats = {
            new SimpleDateFormat("HH mm", Locale.getDefault()),
            new SimpleDateFormat("HH:mm", Locale.getDefault())
    };

    public static SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat simpleDate = new SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault());

    public static int getWeekday(ArrayList<Integer> days) {
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i) == 1) return i;
        }
        return -1;
    }

    public static boolean isCorrectTime(int hourOfDay, int minuteOfHour) {
        return hourOfDay < 24 && minuteOfHour < 60;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0) && year % 100 != 0 ||
                (year % 4 == 0) && (year % 100 == 0) && (year % 400 == 0);
    }
}