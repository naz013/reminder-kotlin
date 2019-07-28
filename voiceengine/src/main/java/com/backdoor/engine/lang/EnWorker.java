package com.backdoor.engine.lang;

import com.backdoor.engine.misc.Action;
import com.backdoor.engine.misc.Ampm;
import com.backdoor.engine.misc.Long;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EnWorker extends Worker {

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
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (string.matches(".*calendar.*")) {
                parts[i] = "";
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
            if (!part.matches("on") && !part.matches("at"))
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
        return input.matches(".*every.*") || hasEveryDay(input);
    }

    @Override
    public boolean hasEveryDay(String input) {
        return input.matches(".*everyday.*");
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
        return input.matches(".*tomorrow.*") || input.matches(".*next day.*");
    }

    @Override
    public String clearTomorrow(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*tomorrow.*") || part.matches(".*next day.*")) {
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
                    if (parts[i - 1].matches("with")) {
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
        if (input.matches(".*message.*")) return Action.MESSAGE;
        else if (input.matches(".*letter.*")) return Action.MAIL;
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
                if (nextIndex < parts.length && parts[nextIndex].matches("to")) {
                    parts[nextIndex] = "";
                }
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Ampm getAmpm(String input) {
        if (input.matches(".*morning.*")) return Ampm.MORNING;
        else if (input.matches(".*evening.*")) return Ampm.EVENING;
        else if (input.matches(".*noon.*")) return Ampm.NOON;
        else if (input.matches(".*night.*")) return Ampm.NIGHT;
        else if (input.matches(".*a m.*")) return Ampm.MORNING;
        else if (input.matches(".*a.m..*")) return Ampm.MORNING;
        else if (input.matches(".*am.*")) return Ampm.MORNING;
        else if (input.matches(".*p m.*")) return Ampm.EVENING;
        else if (input.matches(".*p.m..*")) return Ampm.EVENING;
        else if (input.matches(".*pm.*")) return Ampm.EVENING;
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
                } catch (Exception ignored) {
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
                } catch (Exception ignored) {
                }
            }
            if (hasMinutes(part) != -1) {
                int index = hasMinutes(part);
                try {
                    Integer.parseInt(parts[i - index]);
                    parts[i - index] = "";
                } catch (Exception ignored) {
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
            if (!part.matches("at")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    @Override
    protected int getMonth(String input) {
        int res = -1;
        if (input.contains("january")) res = 0;
        else if (input.contains("february")) res = 1;
        else if (input.contains("march")) res = 2;
        else if (input.contains("april")) res = 3;
        else if (input.contains("may")) res = 4;
        else if (input.contains("june")) res = 5;
        else if (input.contains("july")) res = 6;
        else if (input.contains("august")) res = 7;
        else if (input.contains("september")) res = 8;
        else if (input.contains("october")) res = 9;
        else if (input.contains("november")) res = 10;
        else if (input.contains("december")) res = 11;
        return res;
    }

    @Override
    public boolean hasCall(String input) {
        return input.matches(".*call.*");
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
        return input.matches(".*after.*") || input.matches(".* in .*");
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
        return clipStrings(parts);
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
                    integer = Integer.parseInt(parts[i + 1]);
                    parts[i + 1] = "";
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
    public boolean hasSender(String input) {
        return input.matches(".*send.*");
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
        return input.contains("note");
    }

    @Override
    public String clearNote(String input) {
        input = input.replace("note", "");
        return input.trim();
    }

    @Override
    public boolean hasAction(String input) {
        return input.startsWith("open")
                || input.matches(".*help.*")
                || input.matches(".*adjust.*")
                || input.matches(".*report.*")
                || input.matches(".*change.*");
    }

    @Override
    public Action getAction(String input) {
        if (input.matches(".*help.*")) {
            return Action.HELP;
        } else if (input.matches(".*loudness.*") || input.matches(".*volume.*")) {
            return Action.VOLUME;
        } else if (input.matches(".*settings.*")) {
            return Action.SETTINGS;
        } else if (input.matches(".*report.*")) {
            return Action.REPORT;
        } else {
            return Action.APP;
        }
    }

    @Override
    public boolean hasEvent(String input) {
        return input.startsWith("new") || input.startsWith("add") || input.startsWith("create");
    }

    @Override
    public Action getEvent(String input) {
        if (input.matches(".*birthday.*")) {
            return Action.BIRTHDAY;
        } else if (input.matches(".*reminder.*")) {
            return Action.REMINDER;
        } else return Action.NO_EVENT;
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
        String[] parts = input.split(WHITESPACES);
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
        return -1;
    }

    @Override
    protected int hasMinutes(String input) {
        if (input.matches(".*minute.*")) return 1;
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
    protected boolean hasMonth(String input) {
        return input.matches(".*month.*");
    }

    @Override
    public boolean hasAnswer(String input) {
        input = " " + input + " ";
        return input.matches(".* (yes|yeah|no) .*");
    }

    @Override
    public Action getAnswer(String input) {
        if (input.matches(".* ?(yes|yeah) ?.*")) {
            return Action.YES;
        }
        return Action.NO;
    }

    @Override
    protected float findFloat(String input) {
        if (input.matches("half")) {
            return 0.5f;
        } else {
            return -1;
        }
    }

    @Override
    protected String clearFloats(String input) {
        if (input.contains("and a half")) {
            return input.replace("and a half", "");
        }
        if (input.contains("and half")) {
            return input.replace("and half", "");
        }
        if (input.contains(" half an ")) {
            return input.replace("half an", "");
        }
        if (input.contains(" in half ")) {
            return input.replace("in half", "");
        }
        return input;
    }

    @Override
    protected float findNumber(String input) {
        float number = -1;
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

    @Override
    public boolean hasShowAction(String input) {
        return input.matches(".*show.*");
    }

    @Override
    public Action getShowAction(String input) {
        if (input.matches(".*birthdays.*")) {
            return Action.BIRTHDAYS;
        } else if (input.matches(".*active reminders.*")) {
            return Action.ACTIVE_REMINDERS;
        } else if (input.matches(".*reminders.*")) {
            return Action.REMINDERS;
        } else if (input.matches(".*events.*")) {
            return Action.EVENTS;
        } else if (input.matches(".*notes.*")) {
            return Action.NOTES;
        } else if (input.matches(".*groups.*")) {
            return Action.GROUPS;
        } else if (input.matches(".*shopping lists?.*")) {
            return Action.SHOP_LISTS;
        }
        return null;
    }

    @Override
    public boolean hasNextModifier(String input) {
        return input.matches(".*next.*");
    }
}
