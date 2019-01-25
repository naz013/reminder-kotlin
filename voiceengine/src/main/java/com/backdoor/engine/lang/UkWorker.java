package com.backdoor.engine.lang;

import com.backdoor.engine.misc.Action;
import com.backdoor.engine.misc.Ampm;
import com.backdoor.engine.misc.Long;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
class UkWorker extends Worker {

    @Override
    protected String[] getWeekdays() {
        return new String[]{" неділ", "понеділ", "вівтор", "середу?а?и?", "четвер", "п'ятниц", "субот"};
    }

    @Override
    public boolean hasCalendar(String input) {
        return input.matches(".*календар.*");
    }

    @Override
    public String clearCalendar(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*календар.*")) {
                parts[i] = "";
                if (i > 0 && parts[i - 1].toLowerCase().equalsIgnoreCase("до")) {
                    parts[i - 1] = "";
                }
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public List<Integer> getWeekDays(String input) {
        int[] array = new int[7];
        String[] parts = input.split(WHITESPACES);
        String[] weekDays = getWeekdays();
        for (String part : parts) {
            for (int i = 0; i < weekDays.length; i++) {
                String day = ".*" + weekDays[i] + ".*";
                if (part.matches(day)) {
                    array[i] = 1;
                    break;
                }
            }
        }
        List<Integer> list = new ArrayList<>();
        for (int anArray : array) {
            list.add(anArray);
        }
        return list;
    }

    @Override
    public String clearWeekDays(String input) {
        String[] parts = input.split(WHITESPACES);
        String[] weekDays = getWeekdays();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            for (String day : weekDays) {
                if (part.matches(".*" + day + ".*")) {
                    parts[i] = "";
                    break;
                }
            }
        }
        parts = clipStrings(parts).split(WHITESPACES);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.matches("в")) {
                sb.append(" ").append(part);
            }
        }
        return sb.toString().trim();
    }

    @Override
    public long getDaysRepeat(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasDays(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                } catch (ArrayIndexOutOfBoundsException e) {
                    integer = 0;
                }
                return integer * DAY;
            }
        }
        return 0;
    }

    @Override
    public String clearDaysRepeat(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasDays(part)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    parts[i - 1] = "";
                } catch (NumberFormatException e) {
                }
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasRepeat(String input) {
        return input.matches(".*кожн.*") || hasEveryDay(input);
    }

    @Override
    public boolean hasEveryDay(String input) {
        return input.matches(".*щоден.*") || input.matches(".*щодня.*");
    }

    @Override
    public String clearRepeat(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasRepeat(part)) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasTomorrow(String input) {
        return input.matches(".*завтра.*");
    }

    @Override
    public String clearTomorrow(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*завтра.*")) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public String getMessage(String input) {
        String[] parts = input.split(WHITESPACES);
        StringBuilder sb = new StringBuilder();
        boolean isStart = false;
        for (String part : parts) {
            if (isStart) {
                sb.append(" ").append(part);
            }
            if (part.matches("текст(ом)?")) {
                isStart = true;
            }
        }
        return sb.toString().trim();
    }

    @Override
    public String clearMessage(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("текст(ом)?")) {
                try {
                    if (parts[i - 1].matches("з")) {
                        parts[i - 1] = "";
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
                parts[i] = "";
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Action getMessageType(String input) {
        if (input.matches(".*повідомлення.*")) {
            return Action.MESSAGE;
        }
        if (input.matches(".*листа?.*")) {
            return Action.MAIL;
        }
        return null;
    }

    @Override
    public String clearMessageType(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            Action type = getMessageType(parts[i]);
            if (type != null) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Ampm getAmpm(String input) {
        if (input.matches(".*з?ран(ку|о)?.*") || input.matches(".*вранці.*")) {
            return Ampm.MORNING;
        }
        if (input.matches(".*в?веч(о|е)р.*")) {
            return Ampm.EVENING;
        }
        if (input.matches(".*в?день.*")) {
            return Ampm.NOON;
        }
        if (input.matches(".*в?ночі.*")) {
            return Ampm.NIGHT;
        }
        return null;
    }

    @Override
    public String clearAmpm(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (getAmpm(part) != null) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    protected Date getShortTime(String input) {
        Pattern pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format : getHourFormats()) {
                Date date;
                try {
                    date = format.parse(time);
                    if (date != null) {
                        return date;
                    }
                } catch (NullPointerException | ParseException ignored) {
                }
            }
        }
        return null;
    }

    @Override
    public String clearTime(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasHours(part) != -1) {
                int index = hasHours(part);
                parts[i] = "";
                try {
                    Integer.parseInt(parts[i - index]);
                    parts[i - index] = "";
                } catch (NumberFormatException ignored) {
                }
            }
            if (hasMinutes(part) != -1) {
                int index = hasMinutes(part);
                try {
                    Integer.parseInt(parts[i - index]);
                    parts[i - index] = "";
                } catch (NumberFormatException ignored) {
                }
                parts[i] = "";
            }
        }
        Pattern pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]");
        input = clipStrings(parts);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            input = input.replace(time, "");
        }
        parts = input.split(WHITESPACES);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.matches("об?")) {
                sb.append(" ").append(part);
            }
        }
        return sb.toString().trim();
    }

    @Override
    protected int getMonth(String input) {
        int res = -1;
        if (input.contains("січень") || input.contains("січня")) {
            res = 0;
        }
        if (input.contains("лютий") || input.contains("лютого")) {
            res = 1;
        }
        if (input.contains("березень") || input.contains("березня")) {
            res = 2;
        }
        if (input.contains("квітень") || input.contains("квітня")) {
            res = 3;
        }
        if (input.contains("травень") || input.contains("травня")) {
            res = 4;
        }
        if (input.contains("червень") || input.contains("червня")) {
            res = 5;
        }
        if (input.contains("липень") || input.contains("липня")) {
            res = 6;
        }
        if (input.contains("серпень") || input.contains("серпня")) {
            res = 7;
        }
        if (input.contains("вересень") || input.contains("вересня")) {
            res = 8;
        }
        if (input.contains("жовтень") || input.contains("жовтня")) {
            res = 9;
        }
        if (input.contains("листопад") || input.contains("листопада")) {
            res = 10;
        }
        if (input.contains("грудень") || input.contains("грудня")) {
            res = 11;
        }
        return res;
    }

    @Override
    public boolean hasCall(String input) {
        return input.matches(".*дзвонити.*");
    }

    @Override
    public String clearCall(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*дзвонити.*")) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean isTimer(String input) {
        return input.matches(".*через.*");
    }

    @Override
    public String cleanTimer(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (isTimer(part)) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasSender(String input) {
        return input.matches(".*надісл.*");
    }

    @Override
    public String clearSender(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*надісл.*")) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasNote(String input) {
        return input.contains("нотатка");
    }

    @Override
    public String clearNote(String input) {
        input = input.replace("нотатка", "");
        return input.trim();
    }

    @Override
    public boolean hasAction(String input) {
        return input.startsWith("відкрити") || input.matches(".*допом.*")
                || input.matches(".*гучніст.*") || input.matches(".*налаштув.*")
                || input.matches(".*повідомити.*");
    }

    @Override
    public Action getAction(String input) {
        if (input.matches(".*допомог.*")) {
            return Action.HELP;
        } else if (input.matches(".*гучніст.*")) {
            return Action.VOLUME;
        } else if (input.matches(".*налаштування.*")) {
            return Action.SETTINGS;
        } else if (input.matches(".*повідомити.*")) {
            return Action.REPORT;
        } else return Action.APP;
    }

    @Override
    public boolean hasEvent(String input) {
        return input.startsWith("додати") || input.matches("нове?и?й?.*");
    }

    @Override
    public boolean hasEmptyTrash(String input) {
        return input.matches(".*очисти(ти)? кошик.*");
    }

    @Override
    public boolean hasDisableReminders(String input) {
        return input.matches(".*вимкн(и|ути)? (всі)? ?нагадування.*");
    }

    @Override
    public boolean hasGroup(String input) {
        return input.matches(".*дода(ти|й)? групу.*");
    }

    @Override
    public String clearGroup(String input) {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split(WHITESPACES);
        boolean st = false;
        for (String s : parts) {
            if (s.matches(".*групу.*")) {
                st = true;
                continue;
            }
            if (st) {
                sb.append(s);
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public Action getEvent(String input) {
        if (input.matches(".*день народжен.*")) {
            return Action.BIRTHDAY;
        } else if (input.matches(".*нагадуван.*")) {
            return Action.REMINDER;
        } else {
            return Action.NO_EVENT;
        }
    }

    @Override
    public boolean hasToday(String input) {
        return input.matches(".*сьогодн.*");
    }

    @Override
    public boolean hasAfterTomorrow(String input) {
        return input.matches(".*післязавтр.*");
    }

    @Override
    protected String getAfterTomorrow() {
        return "післязавтра";
    }

    @Override
    protected int hasHours(String input) {
        if (input.matches(".*годині?у?.*")) {
            return 1;
        }
        return -1;
    }

    @Override
    protected int hasMinutes(String input) {
        if (input.matches(".*хвилин.*")) {
            return 1;
        }
        return -1;
    }

    @Override
    protected boolean hasSeconds(String input) {
        return input.matches(".*секунд.*");
    }

    @Override
    protected boolean hasDays(String input) {
        return input.matches(".*дні.*") || input.matches(".*день.*") || input.matches(".*дня.*");
    }

    @Override
    protected boolean hasWeeks(String input) {
        return input.matches(".*тиждень.*") || input.matches(".*тижні.*");
    }

    @Override
    protected boolean hasMonth(String input) {
        return input.matches(".*місяц.*");
    }

    @Override
    public boolean hasAnswer(String input) {
        input = " " + input + " ";
        return input.matches(".* (так|ні) .*");
    }

    @Override
    public Action getAnswer(String input) {
        if (input.matches(".* ?так ?.*")) {
            return Action.YES;
        }
        return Action.NO;
    }

    @Override
    public String getDate(String input, Long res) {
        long mills = 0;
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                    parts[i - 1] = "";
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    integer = 1;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, integer);
                mills = calendar.getTimeInMillis();
                parts[i] = "";
                break;
            }
        }
        res.set(mills);
        return clipStrings(parts);
    }

    @Override
    protected float findFloat(String input) {
        if (input.contains("півтор")) {
            return 1.5f;
        }
        if (input.contains("половин") || input.contains("пів")) {
            return 0.5f;
        }
        return -1;
    }

    @Override
    protected String clearFloats(String input) {
        if (input.contains("з половиною")) {
            input = input.replace("з половиною", "");
        }
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String s = parts[i];
            if (s.contains("півтор") || s.matches("половин*.")) {
                parts[i] = "";
            }
        }
        input = clipStrings(parts);
        if (input.contains(" пів")) {
            input = input.replace("пів", "");
        }
        return input;
    }

    @Override
    protected float findNumber(String input) {
        int number = -1;
        if (input.matches("нуль")) {
            number = 0;
        }
        if (input.matches("один") || input.matches("одну") || input.matches("одна")) {
            number = 1;
        }
        if (input.matches("два") || input.matches("дві")) {
            number = 2;
        }
        if (input.matches("три")) {
            number = 3;
        }
        if (input.matches("чотири")) {
            number = 4;
        }
        if (input.matches("п'ять")) {
            number = 5;
        }
        if (input.matches("шість")) {
            number = 6;
        }
        if (input.matches("сім")) {
            number = 7;
        }
        if (input.matches("вісім")) {
            number = 8;
        }
        if (input.matches("дев'ять")) {
            number = 9;
        }
        if (input.matches("десять")) {
            number = 10;
        }
        if (input.matches("одинадцять")) {
            number = 11;
        }
        if (input.matches("дванадцять")) {
            number = 12;
        }
        if (input.matches("тринадцять")) {
            number = 13;
        }
        if (input.matches("чотирнадцять")) {
            number = 14;
        }
        if (input.matches("п'ятнадцять")) {
            number = 15;
        }
        if (input.matches("шістнадцять")) {
            number = 16;
        }
        if (input.matches("сімнадцять")) {
            number = 17;
        }
        if (input.matches("вісімнадцять")) {
            number = 18;
        }
        if (input.matches("дев'ятнадцять")) {
            number = 19;
        }
        if (input.matches("двадцять")) {
            number = 20;
        }
        if (input.matches("тридцять")) {
            number = 30;
        }
        if (input.matches("сорок")) {
            number = 40;
        }
        if (input.matches("п'ятдесят")) {
            number = 50;
        }
        if (input.matches("шістдесят")) {
            number = 60;
        }
        if (input.matches("сімдесят")) {
            number = 70;
        }
        if (input.matches("вісімдесят")) {
            number = 80;
        }
        if (input.matches("дев'яносто")) {
            number = 90;
        }

        if (input.matches("першого") || input.matches("першій")) {
            number = 1;
        }
        if (input.matches("другого") || input.matches("другій")) {
            number = 2;
        }
        if (input.matches("третього") || input.matches("третій")) {
            number = 3;
        }
        if (input.matches("четвертого") || input.matches("четвертій")) {
            number = 4;
        }
        if (input.matches("п'ятого") || input.matches("п'ятій")) {
            number = 5;
        }
        if (input.matches("шостого") || input.matches("шостій")) {
            number = 6;
        }
        if (input.matches("сьомого") || input.matches("сьомій")) {
            number = 7;
        }
        if (input.matches("восьмого") || input.matches("восьмій")) {
            number = 8;
        }
        if (input.matches("дев'ятого") || input.matches("дев'ятій")) {
            number = 9;
        }
        if (input.matches("десятого") || input.matches("десятій")) {
            number = 10;
        }
        if (input.matches("одинадцятого") || input.matches("одинадцятій")) {
            number = 11;
        }
        if (input.matches("дванадцятого") || input.matches("дванадцятій")) {
            number = 12;
        }
        if (input.matches("тринадцятого") || input.matches("тринадцятій")) {
            number = 13;
        }
        if (input.matches("чотирнадцятого") || input.matches("чотирнадцятій")) {
            number = 14;
        }
        if (input.matches("п'ятнадцятого") || input.matches("п'ятнадцятій")) {
            number = 15;
        }
        if (input.matches("шістнадцятого") || input.matches("шістнадцятій")) {
            number = 16;
        }
        if (input.matches("сімнадцятого") || input.matches("сімнадцятій")) {
            number = 17;
        }
        if (input.matches("вісімнадцятого") || input.matches("вісімнадцятій")) {
            number = 18;
        }
        if (input.matches("дев'ятнадцятого") || input.matches("дев'ятнадцятій")) {
            number = 19;
        }
        if (input.matches("двадцятого") || input.matches("двадцятій")) {
            number = 20;
        }
        if (input.matches("тридцятого")) {
            number = 30;
        }
        if (input.matches("сорокового")) {
            number = 40;
        }
        if (input.matches("п'ятдесятого")) {
            number = 50;
        }
        if (input.matches("шістдесятого")) {
            number = 60;
        }
        if (input.matches("сімдесятого")) {
            number = 70;
        }
        if (input.matches("вісімдесятого")) {
            number = 80;
        }
        if (input.matches("дев'яностого")) {
            number = 90;
        }
        return number;
    }

    @Override
    public boolean hasShowAction(String input) {
        return input.matches(".*пока(зати|жи)?.*");
    }

    @Override
    public Action getShowAction(String input) {
        if (input.matches(".*д?е?н?і?ь? народжен.*")) {
            return Action.BIRTHDAYS;
        } else if (input.matches(".*активні нагадуван.*")) {
            return Action.ACTIVE_REMINDERS;
        } else if (input.matches(".*нагадуван.*")) {
            return Action.REMINDERS;
        } else if (input.matches(".*події.*")) {
            return Action.EVENTS;
        } else if (input.matches(".*нотатки.*")) {
            return Action.NOTES;
        } else if (input.matches(".*групи.*")) {
            return Action.GROUPS;
        } else if (input.matches(".*списо?ки? покупок.*")) {
            return Action.SHOP_LISTS;
        }
        return null;
    }

    @Override
    public boolean hasNextModifier(String input) {
        return input.matches(".*наступн.*");
    }
}
