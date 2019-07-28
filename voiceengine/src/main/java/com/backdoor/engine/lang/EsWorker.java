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

class EsWorker extends Worker {

    @Override
    protected String[] getWeekdays() {
        return new String[]{"domin", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
    }

    @Override
    public boolean hasCalendar(String input) {
        return input.matches(".*calendario.*");
    }

    @Override
    public String clearCalendar(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (string.matches(".*calendario.*")) {
                parts[i] = "";
                if (i > 0 && parts[i - 1].toLowerCase().equalsIgnoreCase("al")) {
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
            if (!part.matches("los"))
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
        return input.matches(".*cada.*") || input.matches(".*todos.*") || hasEveryDay(input);
    }

    @Override
    public boolean hasEveryDay(String input) {
        return input.matches(".*diario.*");
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
        return input.matches(".*mañana.*") || input.matches(".*(día )?siguiente.*");
    }

    @Override
    public String clearTomorrow(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*mañana.*") || part.matches(".*(día )?siguiente.*")) {
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
            if (part.matches("texto"))
                isStart = true;
        }
        return sb.toString().trim();
    }

    @Override
    public String clearMessage(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("texto")) {
                try {
                    if (parts[i - 1].matches("con( el)?")) {
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
        if (input.matches(".*(un )?mensaje.*")) return Action.MESSAGE;
        else if (input.matches(".*carta.*")) return Action.MAIL;
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
                if (nextIndex < parts.length && parts[nextIndex].matches("al")) {
                    parts[nextIndex] = "";
                }
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Ampm getAmpm(String input) {
        if (input.matches(".*mañana.*") || input.matches(".*madrugada.*")
                || input.matches(".*matutino.*")  || input.matches(".*mañanero.*")) return Ampm.MORNING;
        else if (input.matches(".*vespertino.*")) return Ampm.EVENING;
        else if (input.matches(".*(de )?mediodía.*")) return Ampm.NOON;
        else if (input.matches(".*(de )?noche.*")) return Ampm.NIGHT;
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
                boolean hourSuccess = false;
                try {
                    Integer.parseInt(parts[i - index]);
                    hourSuccess = true;
                    parts[i - index] = "";
                } catch (Exception ignored) {
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
            if (!part.matches("(a )?las")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    @Override
    protected int getMonth(String input) {
        int res = -1;
        if (input.contains("enero")) res = 0;
        else if (input.contains("febrero")) res = 1;
        else if (input.contains("marzo") || input.contains("marcha")) res = 2;
        else if (input.contains("abril")) res = 3;
        else if (input.contains("mayo")) res = 4;
        else if (input.contains("junio")) res = 5;
        else if (input.contains("julio")) res = 6;
        else if (input.contains("agosto")) res = 7;
        else if (input.contains("septiembre") || input.contains("setiembre")) res = 8;
        else if (input.contains("octubre")) res = 9;
        else if (input.contains("noviembre")) res = 10;
        else if (input.contains("diciembre")) res = 11;
        return res;
    }

    @Override
    public boolean hasCall(String input) {
        return input.matches(".*llamada.*");
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
        return input.matches(".*después.*") || input.matches(".* en .*");
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
        return clipStrings(parts);
    }

    @Override
    public boolean hasSender(String input) {
        return input.matches(".*enviar.*");
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
        return input.contains("nota");
    }

    @Override
    public String clearNote(String input) {
        input = input.replace("nota", "");
        return input.trim();
    }

    @Override
    public boolean hasAction(String input) {
        return input.startsWith("abierta") || input.startsWith("abierto")
                || input.matches(".*ayuda.*")
                || input.matches(".*ajustar.*") || input.matches(".*modificar.*")
                || input.matches(".*informe.*") ||
                input.matches(".*cambio.*");
    }

    @Override
    public Action getAction(String input) {
        if (input.matches(".*ayuda.*")) {
            return Action.HELP;
        } else if (input.matches(".*lo chillón.*") || input.matches(".*volumen.*")) {
            return Action.VOLUME;
        } else if (input.matches(".*ajustes.*")) {
            return Action.SETTINGS;
        } else if (input.matches(".*informe.*")) {
            return Action.REPORT;
        } else {
            return Action.APP;
        }
    }

    @Override
    public boolean hasEvent(String input) {
        return input.startsWith("nueva") || input.startsWith("nuevo") || input.startsWith("añadir")
                || input.startsWith("crear");
    }

    @Override
    public Action getEvent(String input) {
        if (input.matches(".*(el )?cumpleaños.*")) {
            return Action.BIRTHDAY;
        } else if (input.matches(".*(el )?recordatorio.*")) {
            return Action.REMINDER;
        } else return Action.NO_EVENT;
    }

    @Override
    public boolean hasEmptyTrash(String input) {
        return input.matches(".*papelera vacía.*");
    }

    @Override
    public boolean hasDisableReminders(String input) {
        return input.matches(".*deshabilitar el recordatorio.*");
    }

    @Override
    public boolean hasGroup(String input) {
        return input.matches(".*añadir grupo.*");
    }

    @Override
    public String clearGroup(String input) {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split(WHITESPACES);
        boolean st = false;
        for (String s : parts) {
            if (s.matches(".*(el )?grupo.*")) {
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
        return input.matches(".*(los )?hoy.*");
    }

    @Override
    public boolean hasAfterTomorrow(String input) {
        return input.matches(".*pasado mañana.*") || input.matches(".*después de mañana.*");
    }

    @Override
    public String clearAfterTomorrow(String input) {
        if (input.matches(".*pasado mañana.*")) {
            return input.replace("pasado mañana", "");
        } else if (input.matches(".*después de mañana.*")) {
            return input.replace("después de mañana", "");
        } else {
            return input;
        }
    }

    @Override
    protected String getAfterTomorrow() {
        return "pasado mañana";
    }

    @Override
    protected int hasHours(String input) {
        if (input.matches(".*(la )?hora.*") || input.matches("en punto.*")) return 1;
        return -1;
    }

    @Override
    protected int hasMinutes(String input) {
        if (input.matches(".*(el )?minutos?.*")) return 1;
        return -1;
    }

    @Override
    protected boolean hasSeconds(String input) {
        return input.matches(".*(el |la )?segundos?.*");
    }

    @Override
    protected boolean hasDays(String input) {
        return input.matches(".*(el )?día.*") || input.matches(".*(el )?dia.*");
    }

    @Override
    protected boolean hasWeeks(String input) {
        return input.matches(".*(la )?semanas?.*");
    }

    @Override
    protected boolean hasMonth(String input) {
        return input.matches(".*(el )?mes(es)?.*");
    }

    @Override
    public boolean hasAnswer(String input) {
        input = " " + input + " ";
        return input.matches(".* (sí|si|no) .*");
    }

    @Override
    public Action getAnswer(String input) {
        if (input.matches(".* ?(no) ?.*")) {
            return Action.NO;
        }
        return Action.YES;
    }

    @Override
    protected float findFloat(String input) {
        if (input.matches("(la )?mitad") || input.matches("(el )?medio?a?")) {
            return 0.5f;
        } else {
            return -1;
        }
    }

    @Override
    protected String clearFloats(String input) {
        if (input.contains("y media")) {
            return input.replace("y media", "");
        }
        if (input.contains("y medio")) {
            return input.replace("y medio", "");
        }
        if (input.contains("media")) {
            return input.replace("media", "");
        }
        if (input.contains("medio")) {
            return input.replace("medio", "");
        }
        return input;
    }

    @Override
    protected float findNumber(String input) {
        float number = -1;
        if (input.matches("cero") || input.matches("nulo")) number = 0;
        else if (input.matches("un([ao])?") || input.matches("primer([ao])?")) number = 1;
        else if (input.matches("dos") || input.matches("segund([ao])?")) number = 2;
        else if (input.matches("(los )?tres") || input.matches("tercer([ao])?")) number = 3;
        else if (input.matches("cuatro") || input.matches("cuart([ao])?")) number = 4;
        else if (input.matches("(los )?cinco") || input.matches("quint([ao])?")) number = 5;
        else if (input.matches("(los )?seis") || input.matches("sext([ao])?")) number = 6;
        else if (input.matches("(los )?siete") || input.matches("séptim([ao])?")) number = 7;
        else if (input.matches("(los )?ocho") || input.matches("octav([ao])?")) number = 8;
        else if (input.matches("(el )?nueve") || input.matches("noven([ao])?")) number = 9;
        else if (input.matches("(los )?diez") || input.matches("décim([ao])?")) number = 10;
        else if (input.matches("(el )?once") || input.matches("undécim([ao])?")) number = 11;
        else if (input.matches("(los )?doce") || input.matches("duodécim([ao])?")) number = 12;
        else if (input.matches("(los )?trece") || input.matches("decimotercer([ao])?")) number = 13;
        else if (input.matches("(los )?catorce") || input.matches("decimocuart([ao])?")) number = 14;
        else if (input.matches("(los )?quince") || input.matches("decimoquint([ao])?")) number = 15;
        else if (input.matches("(el )?dieciséis") || input.matches("decimosext([ao])?")) number = 16;
        else if (input.matches("(los )?diecisiete") || input.matches("decimoséptim([ao])?")) number = 17;
        else if (input.matches("(los )?dieciocho") || input.matches("decimoctav([ao])?")) number = 18;
        else if (input.matches("(el )?diecinueve") || input.matches("decimonoven([ao])?")) number = 19;
        else if (input.matches("(los )?veinte") || input.matches("vigésim([ao])?")) number = 20;
        else if (input.matches("(los )?treinta") || input.matches("trigésim([ao])?")) number = 30;
        else if (input.matches("(los )?cuarenta") || input.matches("cuadragésim([ao])?")) number = 40;
        else if (input.matches("(los )?cincuenta") || input.matches("quincuagésim([ao])?")) number = 50;
        else if (input.matches("(las )?sesenta") || input.matches("sexagésim([ao])?")) number = 60;
        else if (input.matches("(los )?setenta") || input.matches("septuagésim([ao])?")) number = 70;
        else if (input.matches("(los )?ochenta") || input.matches("diecioch([ao])?")) number = 80;
        else if (input.matches("(la )?noventa") || input.matches("nonagésim([ao])?")) number = 90;
        return number;
    }

    @Override
    public boolean hasShowAction(String input) {
        return input.matches(".*mostrar.*");
    }

    @Override
    public Action getShowAction(String input) {
        if (input.matches(".*cumpleaños.*")) {
            return Action.BIRTHDAYS;
        } else if (input.matches(".*recordatorios activos.*")) {
            return Action.ACTIVE_REMINDERS;
        } else if (input.matches(".*(el )?recordatorios.*")) {
            return Action.REMINDERS;
        } else if (input.matches(".*eventos.*")) {
            return Action.EVENTS;
        } else if (input.matches(".*(las )?notas.*")) {
            return Action.NOTES;
        } else if (input.matches(".*grupos.*")) {
            return Action.GROUPS;
        } else if (input.matches(".*lista de (la )?compra.*") || input.matches(".*listas de compras.*")) {
            return Action.SHOP_LISTS;
        }
        return null;
    }

    @Override
    public boolean hasNextModifier(String input) {
        return input.matches(".*siguiente.*") || input.matches(".*próximo.*");
    }
}
