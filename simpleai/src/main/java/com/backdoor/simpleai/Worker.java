package com.backdoor.simpleai;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

abstract class Worker implements WorkerInterface {

    protected static final String TAG = "Worker";

    /**
     * Millisecond constants.
     */
    protected final static long SECOND = 1000;
    protected final static long MINUTE = 60 * SECOND;
    protected final static long HOUR = MINUTE * 60;
    protected final static long HALF_DAY = HOUR * 12;
    protected final static long DAY = HALF_DAY * 2;


    protected static final SimpleDateFormat[] dateTaskFormats = {
            new SimpleDateFormat("HH mm", Locale.getDefault()),
            new SimpleDateFormat("HH:mm", Locale.getDefault())
    };

    protected static SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    protected static final SimpleDateFormat simpleDate = new SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault());

    static int getNumberOfSelectedWeekdays(List<Integer> days) {
        int count = 0;
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i) == 1) {
                count++;
            }
        }
        return count;
    }

    static int getSelectedWeekday(List<Integer> days) {
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i) == 1) return i;
        }
        return -1;
    }

    boolean isCorrectTime(int hourOfDay, int minuteOfHour) {
        return hourOfDay < 24 && minuteOfHour < 60;
    }

    boolean isLeapYear(int year) {
        return (year % 4 == 0) && year % 100 != 0 ||
                (year % 4 == 0) && (year % 100 == 0) && (year % 400 == 0);
    }

    protected abstract String[] getWeekdays();

    protected abstract int findNumber(String input);

    protected abstract int hasHours(String input);

    protected abstract int hasMinutes(String input);

    protected abstract boolean hasSeconds(String input);

    protected abstract boolean hasDays(String input);

    protected abstract boolean hasWeeks(String input);

    protected abstract boolean hasMonth(String input);

    protected abstract int getMonth(String input);

    @Override
    public long getMultiplier(String input) {
        String[] parts = input.split("\\s");
        int[] times = new int[6];
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (hasSeconds(string)) {
                try {
                    if (times[0] == 0) times[0] = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    if (times[0] == 0) times[0] = 1;
                }
            } else if (hasMinutes(string) != -1) {
                try {
                    if (times[1] == 0) times[1] = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    if (times[1] == 0) times[1] = 1;
                }
            } else if (hasHours(string) != -1) {
                try {
                    if (times[2] == 0) times[2] = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    if (times[2] == 0) times[2] = 1;
                }
            } else if (hasDays(string)) {
                try {
                    if (times[3] == 0) times[3] = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    if (times[3] == 0) times[3] = 1;
                }
            } else if (hasWeeks(string)) {
                try {
                    if (times[4] == 0) times[4] = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    if (times[4] == 0) times[4] = 1;
                }
            } else if (hasMonth(string)) {
                try {
                    if (times[5] == 0) times[5] = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    if (times[5] == 0) times[5] = 1;
                }
            }
        }
        return (times[0] * SECOND) + (times[1] * MINUTE) + (times[2] * HOUR) + (times[3] * DAY) + (times[4] * 7 * DAY) + (times[5] * 30 * DAY);
    }

    @Override
    public String clearMultiplier(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (hasSeconds(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasMinutes(string) != -1) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasHours(string) != -1) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasDays(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasWeeks(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasMonth(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            }
        }
        return input.trim();
    }

    @Override
    public String replaceNumbers(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            int number = getNumber(parts, i);
            if (number != -1) {
                if (number > 20 && (number % 10 > 0)) {
                    input = input.replace(parts[i] + " " + parts[i + 1], String.valueOf(number));
                } else input = input.replace(parts[i], String.valueOf(number));
            }
        }
        return input.trim();
    }

    protected int getNumber(String[] parts, int index) {
        int number = findNumber(parts[index]);
        if (number == -1) return -1;
        if (number >= 20) {
            int res = getNumber(parts, index + 1);
            if (res != -1) return res + number;
            else return number;
        } else return number;
    }

    @Override
    public long getDate(String input) {
        long mills = 0;
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    integer = 1;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, integer);
                mills = calendar.getTimeInMillis();
                break;
            }
        }
        return mills;
    }

    @Override
    public String clearDate(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                try {
                    Integer.parseInt(parts[i + 1]);
                    input = input.replace(parts[i + 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public long getTime(String input, Ampm ampm, String[] times) {
        Log.d(TAG, "getTime: " + ampm);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        String[] parts = input.split("\\s");
        int h = -1;
        int m = -1;
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i];
            if (hasHours(part) != -1) {
                int index = hasHours(part);
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - index]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }
                if (ampm == Ampm.EVENING) integer += 12;
                h = integer;
                parts[i - index] = "";
            }
            if (hasMinutes(part) != -1) {
                int index = hasMinutes(part);
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - index]);
                } catch (NumberFormatException e) {
                    integer = 0;
                }
                m = integer;
            }
        }
        Date date = getShortTime(input);
        if (date != null) {
            calendar.setTime(date);
            if (ampm == Ampm.EVENING) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(Calendar.HOUR_OF_DAY, hour < 12 ? hour + 12 : hour);
            }
            return calendar.getTimeInMillis();
        }
        if (h != -1) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, h);
            if (m != -1) calendar.set(Calendar.MINUTE, m);
            else calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }
        if (calendar.getTimeInMillis() == 0 && ampm != null) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            try {
                if (ampm == Ampm.MORNING)
                    calendar.setTime(mFormat.parse(times[0]));
                if (ampm == Ampm.NOON)
                    calendar.setTime(mFormat.parse(times[1]));
                if (ampm == Ampm.EVENING)
                    calendar.setTime(mFormat.parse(times[2]));
                if (ampm == Ampm.NIGHT)
                    calendar.setTime(mFormat.parse(times[3]));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar.getTimeInMillis();
    }

    protected abstract Date getShortTime(String input);

    @Override
    public String clearToday(String input) {
        String[] parts = input.split("\\s");
        for (String s : parts) {
            if (hasToday(s)) {
                input = input.replace(s, "");
            }
        }
        return input.trim();
    }

    @Override
    public String clearAfterTomorrow(String input) {
        if (hasAfterTomorrow(input)) {
            input = input.replace(getAfterTomorrow(), "");
        }
        return input;
    }

    protected abstract String getAfterTomorrow();
}