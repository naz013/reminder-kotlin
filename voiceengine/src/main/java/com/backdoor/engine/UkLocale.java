package com.backdoor.engine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

class UkLocale extends Worker {

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
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*календар.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public List<Integer> getWeekDays(String input) {
        int[] array = new int[7];
        String[] parts = input.split("\\s");
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
        String[] parts = input.split("\\s");
        String[] weekDays = getWeekdays();
        for (String part : parts) {
            for (String day : weekDays) {
                if (part.matches(".*" + day + ".*")) {
                    input = input.replace(part, "");
                    break;
                }
            }
        }
        parts = input.split("\\s");
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

    @Override
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

    @Override
    public boolean hasRepeat(String input) {
        return input.matches(".*кожн.*");
    }

    @Override
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

    @Override
    public boolean hasTomorrow(String input) {
        return input.matches(".*завтра.*");
    }

    @Override
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

    @Override
    public String getMessage(String input) {
        String[] parts = input.split("\\s");
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
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("текст(ом)?")) {
                try {
                    if (parts[i -1].matches("з")) {
                        input = input.replace(parts[i - 1], "");
                    }
                } catch (IndexOutOfBoundsException e) {}
                input = input.replace(part, "");
            }
        }
        return input.trim();
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
        String[] parts = input.split("\\s");
        for (String part : parts) {
            Action type = getMessageType(part);
            if (type != null) {
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
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
        String[] parts = input.split("\\s");
        for (String part : parts) {
            if (getAmpm(part) != null) {
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    protected Date getShortTime(String input) {
        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3])( |:)?(([0-5]?\\d?)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format : dateTaskFormats) {
                Date date;
                try {
                    date = format.parse(time);
                    if (date != null) {
                        return date;
                    }
                } catch (NullPointerException | ParseException e) {
                }
            }
        }
        return null;
    }

    @Override
    public String clearTime(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasHours(part) != -1) {
                int index = hasHours(part);
                input = input.replace(part, "");
                try {
                    Integer.parseInt(parts[i - index]);
                    input = input.replace(parts[i - index], "");
                } catch (NumberFormatException e) {
                }
            }
            if (hasMinutes(part) != -1) {
                int index = hasMinutes(part);
                try {
                    Integer.parseInt(parts[i - index]);
                    input = input.replace(parts[i - index], "");
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
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*дзвонити.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public boolean isTimer(String input) {
        return input.matches(".*через.*");
    }

    @Override
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

    @Override
    public boolean hasSender(String input) {
        return input.matches(".*надісл.*");
    }

    @Override
    public String clearSender(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*надісл.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public boolean hasNote(String input) {
        return input.startsWith("нотатка");
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
        String[] parts = input.split("\\s");
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
    protected float findFloat(String input) {
        if (input.contains("півтор")) {
            return 1.5f;
        }
        if (input.matches("половин*.") || input.matches(" пів*.")) {
            return 0.5f;
        }
        return -1;
    }

    @Override
    protected String clearFloats(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String s = parts[i];
            if (s.contains("півтор") || s.matches("половин*.") || s.matches(" пів*.")) {
                parts[i] = "";
            }
        }
        return clipStrings(parts);
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
