package com.backdoor.simpleai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.backdoor.simpleai.Ampm.EVENING;
import static com.backdoor.simpleai.Ampm.MORNING;
import static com.backdoor.simpleai.Ampm.NIGHT;
import static com.backdoor.simpleai.Ampm.NOON;

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

class EnLocale extends Worker {

    @Override
    protected String[] getWeekdays() {
        return new String[]{"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
    }

    @Override
    public boolean hasCalendar(String input) {
        return input.matches(".*calendar.*");
    }

    @Override
    public String clearCalendar(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*calendar.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public List<Integer> getWeekDays(String input) {
        int[] array = {0, 0, 0, 0, 0, 0, 0};
        String[] parts = input.split("\\s");
        String[] weekDays = getWeekdays();
        for (String part : parts) {
            for (int i = 0; i < weekDays.length; i++) {
                String day = weekDays[i];
                if (part.matches(".*" + day + ".*")) {
                    array[i] = 1;
                    break;
                }
            }
        }
        List<Integer> list = new ArrayList<>();
        for (int anArray : array) list.add(anArray);
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
            if (!part.matches("on") && !part.matches("in") && !part.matches("at"))
                sb.append(" ").append(part);
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
        return input.matches(".*every.*");
    }

    @Override
    public String clearRepeat(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*every.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public boolean hasTomorrow(String input) {
        return input.matches(".*tomorrow.*");
    }

    @Override
    public String clearTomorrow(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*tomorrow.*")) {
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
            if (isStart) sb.append(" ").append(part);
            if (part.matches("text"))
                isStart = true;
        }
        return sb.toString().trim();
    }

    @Override
    public String clearMessage(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("text")) {
                try {
                    if (parts[i -1].matches("with")) input = input.replace(parts[i - 1], "");
                } catch (IndexOutOfBoundsException e) {}
                input = input.replace(part, "");
            }
        }
        return input.trim();
    }

    @Override
    public Action getMessageType(String input) {
        if (input.matches(".*message.*")) return Action.MESSAGE;
        else if (input.matches(".*letter.*")) return Action.MAIL;
        return null;
    }

    @Override
    public String clearMessageType(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Action type = getMessageType(part);
            if (type != null) {
                input = input.replace(part, "");
                if (parts[i + 1].matches("to")) input = input.replace(parts[i + 1], "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public Ampm getAmpm(String input) {
        if (input.matches(".*morning.*")) return MORNING;
        if (input.matches(".*evening.*")) return EVENING;
        if (input.matches(".*noon.*")) return NOON;
        if (input.matches(".*night.*")) return NIGHT;
        if (input.matches(".*a m.*")) return MORNING;
        if (input.matches(".*a.m..*")) return MORNING;
        if (input.matches(".*am.*")) return MORNING;
        if (input.matches(".*p m.*")) return EVENING;
        if (input.matches(".*p.m..*")) return EVENING;
        if (input.matches(".*pm.*")) return EVENING;
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
            for (SimpleDateFormat format : Worker.dateTaskFormats) {
                Date date;
                try {
                    date = format.parse(time);
                    if (date != null) return date;
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
            if (!part.matches("at")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    @Override
    protected int getMonth(String input) {
        int res = -1;
        if (input.contains("january")) res = 0;
        if (input.contains("february")) res = 1;
        if (input.contains("march")) res = 2;
        if (input.contains("april")) res = 3;
        if (input.contains("may")) res = 4;
        if (input.contains("june")) res = 5;
        if (input.contains("july")) res = 6;
        if (input.contains("august")) res = 7;
        if (input.contains("september")) res = 8;
        if (input.contains("october")) res = 9;
        if (input.contains("november")) res = 10;
        if (input.contains("december")) res = 11;
        return res;
    }

    @Override
    public boolean hasCall(String input) {
        return input.matches(".*call.*");
    }

    @Override
    public String clearCall(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (hasCall(string)) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public boolean isTimer(String input) {
        return input.matches(".*after.*");
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
        return input.matches(".*send.*");
    }

    @Override
    public String clearSender(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (hasSender(string)) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    @Override
    public boolean hasNote(String input) {
        return input.startsWith("note");
    }

    @Override
    public String clearNote(String input) {
        input = input.replace("note", "");
        return input.trim();
    }

    @Override
    public boolean hasAction(String input) {
        return input.startsWith("open") || input.matches(".*help.*")
                || input.matches(".*adjust.*") || input.matches(".*report.*") ||
                input.matches(".*change.*");
    }

    @Override
    public Action getAction(String input) {
        if (input.matches(".*help.*"))
            return Action.HELP;
        else if (input.matches(".*loudness.*") || input.matches(".*volume.*"))
            return Action.VOLUME;
        else if (input.matches(".*settings.*"))
            return Action.SETTINGS;
        else if (input.matches(".*report.*"))
            return Action.REPORT;
        else return Action.APP;
    }

    @Override
    public boolean hasEvent(String input) {
        return input.startsWith("new") || input.startsWith("add");
    }

    @Override
    public Action getEvent(String input) {
        if (input.matches(".*birthday.*"))
            return Action.BIRTHDAY;
        else return Action.REMINDER;
    }

    @Override
    public boolean hasEmptyTrash(String input) {
        return input.matches(".*empty trash.*");
    }

    @Override
    public boolean hasDisableReminders(String input) {
        return input.matches(".*disable reminder.*");
    }

    @Override
    public boolean hasGroup(String input) {
        return input.matches(".*add group.*");
    }

    @Override
    public String clearGroup(String input) {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split("\\s");
        boolean st = false;
        for (String s : parts) {
            if (s.matches(".*group.*")) {
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
    public boolean hasToday(String input) {
        return input.matches(".*today.*");
    }

    @Override
    public boolean hasAfterTomorrow(String input) {
        return input.matches(".*after tomorrow.*");
    }

    @Override
    protected String getAfterTomorrow() {
        return "after tomorrow";
    }

    @Override
    protected int hasHours(String input) {
        if (input.matches(".*hour.*") || input.matches(".*o'clock.*")
                || input.matches(".*am.*") || input.matches(".*pm.*")) return 1;
        else if (input.matches("\\d+")) return 0;
        return -1;
    }

    @Override
    protected int hasMinutes(String input) {
        if (input.matches(".*minute.*")) return 1;
        else if (input.matches("\\d+")) return 0;
        return -1;
    }

    @Override
    protected boolean hasSeconds(String input) {
        return input.matches(".*second.*");
    }

    @Override
    protected boolean hasDays(String input) {
        return input.matches(".* day.*");
    }

    @Override
    protected boolean hasWeeks(String input) {
        return input.matches(".*week.*");
    }

    @Override
    protected int findNumber(String input) {
        int number = -1;
        if (input.matches("zero") || input.matches("nil")) number = 0;
        if (input.matches("one") || input.matches("first")) number = 1;
        if (input.matches("two") || input.matches("second")) number = 2;
        if (input.matches("three") || input.matches("third")) number = 3;
        if (input.matches("four") || input.matches("fourth")) number = 4;
        if (input.matches("five") || input.matches("fifth")) number = 5;
        if (input.matches("six") || input.matches("sixth")) number = 6;
        if (input.matches("seven") || input.matches("seventh")) number = 7;
        if (input.matches("eight") || input.matches("eighth")) number = 8;
        if (input.matches("nine") || input.matches("ninth")) number = 9;
        if (input.matches("ten") || input.matches("tenth")) number = 10;
        if (input.matches("eleven") || input.matches("eleventh")) number = 11;
        if (input.matches("twelve") || input.matches("twelfth")) number = 12;
        if (input.matches("thirteen") || input.matches("thirteenth")) number = 13;
        if (input.matches("fourteen") || input.matches("fourteenth")) number = 14;
        if (input.matches("fifteen") || input.matches("fifteenth")) number = 15;
        if (input.matches("sixteen") || input.matches("sixteenth")) number = 16;
        if (input.matches("seventeen") || input.matches("seventeenth")) number = 17;
        if (input.matches("eighteen") || input.matches("eighteenth")) number = 18;
        if (input.matches("nineteen") || input.matches("nineteenth")) number = 19;
        if (input.matches("twenty") || input.matches("twentieth")) number = 20;
        if (input.matches("thirty") || input.matches("thirtieth")) number = 30;
        if (input.matches("forty") || input.matches("fortieth")) number = 40;
        if (input.matches("fifty") || input.matches("fiftieth")) number = 50;
        if (input.matches("sixty") || input.matches("sixtieth")) number = 60;
        if (input.matches("seventy") || input.matches("seventieth")) number = 70;
        if (input.matches("eighty") || input.matches("eightieth")) number = 80;
        if (input.matches("ninety") || input.matches("ninetieth")) number = 90;
        return number;
    }
}
