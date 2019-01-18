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
 * Copyright 2018 Nazar Suhovich
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
class DeWorker extends Worker {

    @Override
    protected String[] getWeekdays() {
        return new String[]{"sonntag", "montag", "dienstag", "mittwoch", "donnerstag", "freitag", "samstag"};
    }

    @Override
    public boolean hasCalendar(String input) {
        return input.matches(".*kalender.*");
    }

    @Override
    public String clearCalendar(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (string.matches(".*kalender.*")) {
                parts[i] = "";
                if (i > 0 && parts[i - 1].toLowerCase().equalsIgnoreCase("zum")) {
                    parts[i - 1] = "";
                }
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public List<Integer> getWeekDays(String input) {
        int[] array = {0, 0, 0, 0, 0, 0, 0};
        String[] parts = input.split(WHITESPACES);
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
            if (!part.matches("zum"))
                sb.append(" ").append(part);
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
                } catch (NumberFormatException ignored) {
                }
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasRepeat(String input) {
        return input.matches(".*jeden.*");
    }

    @Override
    public String clearRepeat(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*jeden.*")) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasTomorrow(String input) {
        return input.matches(".*morgen.*");
    }

    @Override
    public String clearTomorrow(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*morgen.*")) {
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
            if (isStart) sb.append(" ").append(part);
            if (part.matches("text"))
                isStart = true;
        }
        return sb.toString().trim();
    }

    @Override
    public String clearMessage(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("text")) {
                try {
                    if (parts[i - 1].matches("mit")) {
                        parts[i - 1] = "";
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
                input = input.replace(part, "");
                parts[i] = "";
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Action getMessageType(String input) {
        if (input.matches(".*nachricht.*")) return Action.MESSAGE;
        else if (input.matches(".*brief.*") || input.matches(".*buchstabe.*")) return Action.MAIL;
        return null;
    }

    @Override
    public String clearMessageType(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Action type = getMessageType(part);
            if (type != null) {
                parts[i] = "";
                int nextIndex = i + 1;
                if (nextIndex < parts.length && parts[nextIndex].matches("an")) {
                    parts[nextIndex] = "";
                }
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Ampm getAmpm(String input) {
        if (input.matches(".*morgen.*")) return Ampm.MORNING;
        if (input.matches(".*abend.*")) return Ampm.EVENING;
        if (input.matches(".*mittag.*")) return Ampm.NOON;
        if (input.matches(".*nacht.*")) return Ampm.NIGHT;
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
                    if (date != null) return date;
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
                boolean hourSuccess = false;
                try {
                    Integer.parseInt(parts[i - index]);
                    hourSuccess = true;
                    parts[i - index] = "";
                } catch (NumberFormatException ignored) {
                }
                if (hourSuccess) {
                    try {
                        Integer.parseInt(parts[i + 1]);
                        parts[i + 1] = "";
                    } catch (Exception ignored) {
                    }
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
            if (!part.matches("bei")) sb.append(" ").append(part);
        }
        return sb.toString().trim().replace("um", "");
    }

    @Override
    protected int getMonth(String input) {
        int res = -1;
        if (input.contains("januar")) res = 0;
        if (input.contains("februar")) res = 1;
        if (input.contains("märz")) res = 2;
        if (input.contains("april")) res = 3;
        if (input.contains("mai")) res = 4;
        if (input.contains("juni")) res = 5;
        if (input.contains("juli")) res = 6;
        if (input.contains("august")) res = 7;
        if (input.contains("september")) res = 8;
        if (input.contains("oktober")) res = 9;
        if (input.contains("november")) res = 10;
        if (input.contains("dezember")) res = 11;
        return res;
    }

    @Override
    public boolean hasCall(String input) {
        return input.matches(".*anruf.*");
    }

    @Override
    public String clearCall(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasCall(part)) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean isTimer(String input) {
        input = " " + input + " ";
        return input.matches(".*nach.*") || input.matches(".*nach dem.*") || input.matches(".* in .*");
    }

    @Override
    public String cleanTimer(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (isTimer(string)) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts).trim();
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
        return clipStrings(parts).replace("am", "");
    }

    @Override
    public boolean hasSender(String input) {
        return input.matches(".*senden.*");
    }

    @Override
    public String clearSender(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (hasSender(string)) {
                parts[i] = "";
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public boolean hasNote(String input) {
        return input.contains("notiz");
    }

    @Override
    public String clearNote(String input) {
        input = input.replace("notiz", "");
        return input.trim();
    }

    @Override
    public boolean hasAction(String input) {
        return input.startsWith("öffnen") || input.matches(".*hilfe.*")
                || input.matches(".*einstellen.*") || input.matches(".*bericht.*") ||
                input.matches(".*verändern.*");
    }

    @Override
    public Action getAction(String input) {
        if (input.matches(".*hilfe.*")) {
            return Action.HELP;
        } else if (input.matches(".*lautstärke.*") || input.matches(".*volumen.*")) {
            return Action.VOLUME;
        } else if (input.matches(".*einstellungen.*")) {
            return Action.SETTINGS;
        } else if (input.matches(".*bericht.*")) {
            return Action.REPORT;
        } else {
            return Action.APP;
        }
    }

    @Override
    public boolean hasEvent(String input) {
        return input.startsWith("neu") || input.startsWith("hinzufügen") || input.startsWith("addieren");
    }

    @Override
    public Action getEvent(String input) {
        if (input.matches(".*geburtstag.*")) {
            return Action.BIRTHDAY;
        } else if (input.matches(".*erinnerung.*") || input.matches(".*mahnung.*")) {
            return Action.REMINDER;
        } else return Action.NO_EVENT;
    }

    @Override
    public boolean hasEmptyTrash(String input) {
        return input.matches(".*klar trash.*");
    }

    @Override
    public boolean hasDisableReminders(String input) {
        return input.matches(".*erinnerung deaktivieren.*");
    }

    @Override
    public boolean hasGroup(String input) {
        return input.matches(".*gruppe hinzufügen.*");
    }

    @Override
    public String clearGroup(String input) {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split(WHITESPACES);
        boolean st = false;
        for (String s : parts) {
            if (s.matches(".*gruppe.*")) {
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
        return input.matches(".*heute.*");
    }

    @Override
    public boolean hasAfterTomorrow(String input) {
        return input.matches(".*übermorgen.*") || input.matches(".*nach morgen.*");
    }

    @Override
    public String clearAfterTomorrow(String input) {
        if (input.matches(".*übermorgen.*")) {
            return input.replace("übermorgen", "");
        } else if (input.matches(".*nach morgen.*")) {
            return input.replace("nach morgen", "");
        } else {
            return input;
        }
    }

    @Override
    protected String getAfterTomorrow() {
        return "übermorgen";
    }

    @Override
    protected int hasHours(String input) {
        if (input.matches(".*stunde.*") || input.matches("uhr.*")) return 1;
        return -1;
    }

    @Override
    protected int hasMinutes(String input) {
        if (input.matches(".*minute.*")) return 1;
        return -1;
    }

    @Override
    protected boolean hasSeconds(String input) {
        return input.matches(".*zweite.*");
    }

    @Override
    protected boolean hasDays(String input) {
        return input.matches(".*tag.*");
    }

    @Override
    protected boolean hasWeeks(String input) {
        return input.matches(".*woche.*");
    }

    @Override
    protected boolean hasMonth(String input) {
        return input.matches(".*monat.*");
    }

    @Override
    public boolean hasAnswer(String input) {
        input = " " + input + " ";
        return input.matches(".* (ja|nein|kein|nicht) .*");
    }

    @Override
    public Action getAnswer(String input) {
        if (input.matches(".* ?(ja) ?.*")) {
            return Action.YES;
        }
        return Action.NO;
    }

    @Override
    protected float findFloat(String input) {
        if (input.matches("halb")) {
            return 0.5f;
        } else {
            return -1;
        }
    }

    @Override
    public String replaceNumbers(String input) {
        return super.replaceNumbers(splitNumbers(input));
    }

    private String splitNumbers(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasNumber(part) != -1 && (part.contains("und") || part.contains("ein")  || part.contains("in"))) {
                part = part.replace("und", " ");
                part = part.replace("ein", " ");
                part = part.replace("in", " ");
                parts[i] = part;
            }
        }
        System.out.println("splitNumbers: in -> " + input);
        input = clipStrings(parts);
        System.out.println("splitNumbers: out -> " + input);
        return input;
    }

    @Override
    protected String clearFloats(String input) {
        if (input.contains("einhalb")) {
            return input.replace("einhalb", "");
        }
        if (input.contains("und eine halbe")) {
            return input.replace("und eine halbe", "");
        }
        if (input.contains("halbe")) {
            return input.replace("halbe", "");
        }
        if (input.contains("halb")) {
            return input.replace("halb", "");
        }
        return input;
    }

    private float hasNumber(String input) {
        float number = -1;
        if (input.contains("zero") || input.contains("null")) number = 0;
        else if (input.matches("ein(e|es|er|s)?") || input.contains("zuerst") || input.matches("erste.*")) number = 1;
        else if (input.contains("zwei") || input.matches("zweite.*")) number = 2;
        else if (input.contains("drei") || input.matches("dritte.*")) number = 3;
        else if (input.contains("vier") || input.matches("vierte.*")) number = 4;
        else if (input.contains("fünf") || input.matches("fünfte.*")) number = 5;
        else if (input.contains("sechs") || input.matches("sechste.*")) number = 6;
        else if (input.contains("sieben") || input.matches("siebte.*")) number = 7;
        else if (input.contains("acht") || input.matches("achte.*")) number = 8;
        else if (input.contains("neun") || input.matches("neunte.*")) number = 9;
        else if (input.contains("zehn") || input.matches("zehnte.*")) number = 10;
        else if (input.contains("elf") || input.matches("elfte.*")) number = 11;
        else if (input.contains("zwölf") || input.matches("zwölfte.*")) number = 12;
        else if (input.contains("dreizehn") || input.matches("dreizehnte.*")) number = 13;
        else if (input.contains("vierzehn") || input.matches("vierzehnte.*")) number = 14;
        else if (input.contains("fünfzehn") || input.matches("fünfzehnte.*")) number = 15;
        else if (input.contains("sechzehn") || input.matches("sechzehnte.*")) number = 16;
        else if (input.contains("siebzehn") || input.matches("siebzehnte.*")) number = 17;
        else if (input.contains("achtzehn") || input.matches("achtzehnte.*")) number = 18;
        else if (input.contains("neunzehn") || input.matches("neunzehnte.*")) number = 19;
        else if (input.contains("zwanzig") || input.contains("zwanzigste")) number = 20;
        else if (input.contains("dreißig") || input.contains("dreißigste")) number = 30;
        else if (input.contains("vierzig") || input.contains("vierzigste")) number = 40;
        else if (input.contains("fünfzig") || input.contains("fünfzigste")) number = 50;
        else if (input.contains("sechzig") || input.contains("sechzigste")) number = 60;
        else if (input.contains("siebzig") || input.contains("siebzigste")) number = 70;
        else if (input.contains("achtzig") || input.contains("achtzigste")) number = 80;
        else if (input.contains("neunzig") || input.contains("neunzigste")) number = 90;
        return number;
    }

    @Override
    protected float findNumber(String input) {
        float number = -1;
        if (input.matches("zero") || input.matches("null")) number = 0;
        else if (input.matches("ein(e|es|er|s)?") || input.matches("zuerst") || input.matches("erste.*")) number = 1;
        else if (input.matches("zwei") || input.matches("zweite.*")) number = 2;
        else if (input.matches("drei") || input.matches("dritte.*")) number = 3;
        else if (input.matches("vier") || input.matches("vierte.*")) number = 4;
        else if (input.matches("fünf") || input.matches("fünfte.*")) number = 5;
        else if (input.matches("sechs") || input.matches("sechste.*")) number = 6;
        else if (input.matches("sieben") || input.matches("siebte.*")) number = 7;
        else if (input.matches("acht") || input.matches("achte.*")) number = 8;
        else if (input.matches("neun") || input.matches("neunte.*")) number = 9;
        else if (input.matches("zehn") || input.matches("zehnte.*")) number = 10;
        else if (input.matches("elf") || input.matches("elfte.*")) number = 11;
        else if (input.matches("zwölf") || input.matches("zwölfte.*")) number = 12;
        else if (input.matches("dreizehn") || input.matches("dreizehnte.*")) number = 13;
        else if (input.matches("vierzehn") || input.matches("vierzehnte.*")) number = 14;
        else if (input.matches("fünfzehn") || input.matches("fünfzehnte.*")) number = 15;
        else if (input.matches("sechzehn") || input.matches("sechzehnte.*")) number = 16;
        else if (input.matches("siebzehn") || input.matches("siebzehnte.*")) number = 17;
        else if (input.matches("achtzehn") || input.matches("achtzehnte.*")) number = 18;
        else if (input.matches("neunzehn") || input.matches("neunzehnte.*")) number = 19;
        else if (input.matches("zwanzig") || input.matches("zwanzigste")) number = 20;
        else if (input.matches("dreißig") || input.matches("dreißigste")) number = 30;
        else if (input.matches("vierzig") || input.matches("vierzigste")) number = 40;
        else if (input.matches("fünfzig") || input.matches("fünfzigste")) number = 50;
        else if (input.matches("sechzig") || input.matches("sechzigste")) number = 60;
        else if (input.matches("siebzig") || input.matches("siebzigste")) number = 70;
        else if (input.matches("achtzig") || input.matches("achtzigste")) number = 80;
        else if (input.matches("neunzig") || input.matches("neunzigste")) number = 90;
        return number;
    }

    @Override
    public boolean hasShowAction(String input) {
        return input.matches(".*show.*") || input.matches(".*zeigen.*");
    }

    @Override
    public Action getShowAction(String input) {
        if (input.matches(".*geburtstage.*")) {
            return Action.BIRTHDAYS;
        } else if (input.matches(".*aktive erinnerungen.*")) {
            return Action.ACTIVE_REMINDERS;
        } else if (input.matches(".*erinnerungen.*")) {
            return Action.REMINDERS;
        } else if (input.matches(".*veranstaltungen.*")) {
            return Action.EVENTS;
        } else if (input.matches(".*notizen.*")) {
            return Action.NOTES;
        } else if (input.matches(".*gruppen.*")) {
            return Action.GROUPS;
        } else if (input.matches(".*einkaufslisten?.*")) {
            return Action.SHOP_LISTS;
        }
        return null;
    }

    @Override
    public boolean hasNextModifier(String input) {
        return input.matches(".*nächste.*");
    }
}
