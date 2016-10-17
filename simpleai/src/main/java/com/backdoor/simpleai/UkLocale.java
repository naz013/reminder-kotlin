package com.backdoor.simpleai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
class UkLocale extends RecUtils implements LocaleImpl {

    private final static String[] weekDays = {
            "неділ", "понеділ", "вівтор", "середу?а?", "четвер", "п'ятниц", "субот"
    };

    public boolean hasCalendar(String input) {
        return input.matches(".*календар.*");
    }

    public String clearCalendar(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*календар.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public ArrayList<Integer> getWeekDays(String input) {
        int[] array = {0, 0, 0, 0, 0, 0, 0};

        String[] parts = input.split("\\s");
        for (String part : parts) {
            for (int i = 0; i < weekDays.length; i++) {
                String day = weekDays[i];
                if (part.matches(".*" + day + ".*"))
                    array[i] = 1;
            }
        }
        ArrayList<Integer> list = new ArrayList<>();
        for (int anArray : array) list.add(anArray);
        return list;
    }

    public String clearWeekDays(String input) {
        String[] parts = input.split("\\s");
        for (String part : parts) {
            for (String day : weekDays) {
                if (part.matches(".*" + day + ".*"))
                    input = input.replace(part, "");
            }
        }
        parts = input.split("\\s");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.matches("в")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    public long getDaysRepeat(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasDays(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }
                return integer * DAY;
            }
        }
        return 0;
    }

    public String clearDaysRepeat(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasDays(part)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    public boolean hasRepeat(String input) {
        return input.matches(".*кожн.*");
    }

    public String clearRepeat(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*кожн.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public boolean hasTomorrow(String input) {
        return input.matches(".*завтра.*");
    }

    public String clearTomorrow(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*завтра.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public String getMessage(String input) {
        String[] parts = input.split("\\s");
        StringBuilder sb = new StringBuilder();
        boolean isStart = false;
        for (String part : parts) {
            if (isStart) sb.append(" ").append(part);
            if (part.matches("текст(ом)?"))
                isStart = true;
        }
        return sb.toString().trim();
    }

    public String clearMessage(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("текст(ом)?")) {
                int index = input.indexOf(part);
                input = input.replace(parts[i - 1], "");
                input = input.substring(0, index - 1);
            }
        }
        return input.trim();
    }

    public int getType(String input) {
        if (input.matches(".*повідомлення.*")) return MESSAGE;
        if (input.matches(".*листа?.*")) return MAIL;
        return -1;
    }

    public String clearType(String input) {
        String[] parts = input.split("\\s");
        for (String part : parts) {
            int type = getType(part);
            if (type != -1) {
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    public int getAmpm(String input) {
        if (input.matches(".*з?ран(ку|о)?.*") || input.matches(".*вранці.*")) return MORNING;
        if (input.matches(".*в?веч(о|е)р.*")) return EVENING;
        if (input.matches(".*в?день.*")) return NOON;
        if (input.matches(".*в?ночі.*")) return NIGHT;
        return -1;
    }

    public String clearAmpm(String input) {
        String[] parts = input.split("\\s");
        for (String part : parts) {
            int ampm = getAmpm(part);
            if (ampm != -1) {
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    public long getTime(String input, int ampm, String[] times) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasHours(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }
                if (ampm == EVENING) integer += 12;

                calendar.set(Calendar.HOUR_OF_DAY, integer);
            }
            if (hasMinutes(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }

                calendar.set(Calendar.MINUTE, integer);
            }
        }
        Date date = getShortTime(input);
        if (date != null) {
            calendar.setTime(date);
            if (ampm == EVENING) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(Calendar.HOUR_OF_DAY, hour < 12 ? hour + 12 : hour);
            }
            return calendar.getTimeInMillis();
        }
        if (calendar.getTimeInMillis() == 0 && ampm != -1) {
            try {
                if (ampm == MORNING)
                    calendar.setTime(mFormat.parse(times[0]));
                if (ampm == NOON)
                    calendar.setTime(mFormat.parse(times[1]));
                if (ampm == EVENING)
                    calendar.setTime(mFormat.parse(times[2]));
                if (ampm == NIGHT)
                    calendar.setTime(mFormat.parse(times[3]));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar.getTimeInMillis();
    }

    private Date getShortTime(String input) {
        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3])( |:)?(([0-5]?\\d?)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format : RecUtils.dateTaskFormats){
                Date date;
                try {
                    date = format.parse(time);
                    if (date != null) return date;
                } catch (NullPointerException | ParseException e){
                }
            }
        }
        return null;
    }

    public String clearTime(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasHours(part)) {
                input = input.replace(part, "");
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
            }
            if (hasMinutes(part)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
            }
        }
        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3])( |:)(([0-5]?\\d?)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            input = input.replace(time, "");
        }
        parts = input.split("\\s");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.matches("об?")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    public long getDate(String input) {
        long mills = 0;
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
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

    public String clearDate(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    static int getMonth(String input){
        int res = -1;
        if (input.contains("січень") || input.contains("січня")) res = 0;
        if (input.contains("лютий") || input.contains("лютого")) res = 1;
        if (input.contains("березень") || input.contains("березня")) res = 2;
        if (input.contains("квітень") || input.contains("квітня")) res = 3;
        if (input.contains("травень") || input.contains("травня")) res = 4;
        if (input.contains("червень") || input.contains("червня")) res = 5;
        if (input.contains("липень") || input.contains("липня")) res = 6;
        if (input.contains("серпень") || input.contains("серпня")) res = 7;
        if (input.contains("вересень") || input.contains("вересня")) res = 8;
        if (input.contains("жовтень") || input.contains("жовтня")) res = 9;
        if (input.contains("листопад") || input.contains("листопада")) res = 10;
        if (input.contains("грудень") || input.contains("грудня")) res = 11;
        return res;
    }

    public boolean hasCall(String input) {
        return input.matches(".*дзвонити.*");
    }

    public String clearCall(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*дзвонити.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public boolean isTimer(String input) {
        return input.matches(".*через.*");
    }

    public String cleanTimer(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (isTimer(string)) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public boolean hasSender(String input) {
        return input.matches(".*надіслати.*");
    }

    public String clearSender(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*надіслати.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public boolean hasNote(String input) {
        return input.startsWith("нотатка");
    }

    public String clearNote(String input) {
        input = input.replace("нотатка", "");
        return input.trim();
    }

    public boolean hasAction(String input) {
        return input.startsWith("відкрити") || input.matches(".*допом.*")
                || input.matches(".*гучніст.*") || input.matches(".*налаштув.*")
                || input.matches(".*повідомити.*");
    }

    public int getAction(String input) {
        if (input.matches(".*допомог.*"))
            return HELP;
        else if (input.matches(".*гучніст.*"))
            return VOLUME;
        else if (input.matches(".*налаштування.*"))
            return SETTINGS;
        else if (input.matches(".*повідомити.*"))
            return REPORT;
        else return APP;
    }

    public boolean hasEvent(String input) {
        return input.startsWith("додати") || input.matches("нове?и?й?.*");
    }

    public int getEvent(String input) {
        if (input.matches(".*день народження.*"))
            return BIRTHDAY;
        else return REMINDER;
    }

    public long getMultiplier(String input) {
        long result = 0;
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (hasSeconds(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * SECOND;
            } else if (hasMinutes(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * MINUTE;
            } else if (hasHours(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * HOUR;
            } else if (hasDays(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * DAY;
            } else if (hasWeeks(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * DAY * 7;
            }
        }
        return result;
    }

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
            } else if (hasMinutes(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasHours(string)) {
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
            }
        }
        return input.trim();
    }

    private boolean hasHours(String input) {
        return input.matches(".*годині?у?.*");
    }

    private boolean hasMinutes(String input) {
        return input.matches(".*хвилин.*");
    }

    private boolean hasSeconds(String input) {
        return input.matches(".*секунд.*");
    }

    private boolean hasDays(String input) {
        return input.matches(".*дні.*") || input.matches(".*день.*") || input.matches(".*дня.*");
    }

    private boolean hasWeeks(String input) {
        return input.matches(".*тиждень.*") || input.matches(".*тижні.*");
    }

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

    private int getNumber(String[] parts, int index) {
        int number = findNumber(parts[index]);
        if (number == -1) return -1;
        if (number >= 20) {
            int res = getNumber(parts, index + 1);
            if (res != -1) return res + number;
            else return number;
        } else return number;
    }

    private int findNumber(String input){
        int number = -1;
        if (input.matches("нуль")) number = 0;
        if (input.matches("один") || input.matches("одну") || input.matches("одна")) number = 1;
        if (input.matches("два") || input.matches("дві")) number = 2;
        if (input.matches("три")) number = 3;
        if (input.matches("чотири")) number = 4;
        if (input.matches("п'ять")) number = 5;
        if (input.matches("шість")) number = 6;
        if (input.matches("сім")) number = 7;
        if (input.matches("вісім")) number = 8;
        if (input.matches("дев'ять")) number = 9;
        if (input.matches("десять")) number = 10;
        if (input.matches("одинадцять")) number = 11;
        if (input.matches("дванадцять")) number = 12;
        if (input.matches("тринадцять")) number = 13;
        if (input.matches("чотирнадцять")) number = 14;
        if (input.matches("п'ятнадцять")) number = 15;
        if (input.matches("шістнадцять")) number = 16;
        if (input.matches("сімнадцять")) number = 17;
        if (input.matches("вісімнадцять")) number = 18;
        if (input.matches("дев'ятнадцять")) number = 19;
        if (input.matches("двадцять")) number = 20;
        if (input.matches("тридцять")) number = 30;
        if (input.matches("сорок")) number = 40;
        if (input.matches("п'ятдесят")) number = 50;
        if (input.matches("шістдесят")) number = 60;
        if (input.matches("сімдесят")) number = 70;
        if (input.matches("вісімдесят")) number = 80;
        if (input.matches("дев'яносто")) number = 90;

        if (input.matches("першого") || input.matches("першій")) number = 1;
        if (input.matches("другого") || input.matches("другій")) number = 2;
        if (input.matches("третього") || input.matches("третій")) number = 3;
        if (input.matches("четвертого") || input.matches("четвертій")) number = 4;
        if (input.matches("п'ятого") || input.matches("п'ятій")) number = 5;
        if (input.matches("шостого") || input.matches("шостій")) number = 6;
        if (input.matches("сьомого") || input.matches("сьомій")) number = 7;
        if (input.matches("восьмого") || input.matches("восьмій")) number = 8;
        if (input.matches("дев'ятого") || input.matches("дев'ятій")) number = 9;
        if (input.matches("десятого") || input.matches("десятій")) number = 10;
        if (input.matches("одинадцятого") || input.matches("одинадцятій")) number = 11;
        if (input.matches("дванадцятого") || input.matches("дванадцятій")) number = 12;
        if (input.matches("тринадцятого") || input.matches("тринадцятій")) number = 13;
        if (input.matches("чотирнадцятого") || input.matches("чотирнадцятій")) number = 14;
        if (input.matches("п'ятнадцятого") || input.matches("п'ятнадцятій")) number = 15;
        if (input.matches("шістнадцятого") || input.matches("шістнадцятій")) number = 16;
        if (input.matches("сімнадцятого") || input.matches("сімнадцятій")) number = 17;
        if (input.matches("вісімнадцятого") || input.matches("вісімнадцятій")) number = 18;
        if (input.matches("дев'ятнадцятого") || input.matches("дев'ятнадцятій")) number = 19;
        if (input.matches("двадцятого") || input.matches("двадцятій")) number = 20;
        if (input.matches("тридцятого")) number = 30;
        if (input.matches("сорокового")) number = 40;
        if (input.matches("п'ятдесятого")) number = 50;
        if (input.matches("шістдесятого")) number = 60;
        if (input.matches("сімдесятого")) number = 70;
        if (input.matches("вісімдесятого")) number = 80;
        if (input.matches("дев'яностого")) number = 90;
        return number;
    }
}
