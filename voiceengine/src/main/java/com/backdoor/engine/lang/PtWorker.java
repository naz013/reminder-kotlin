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

class PtWorker extends Worker {

    @Override
    protected String[] getWeekdays() {
        return new String[]{"doming", "segunda-feira", "terça", "quarta-feira", "quinta-feira", "sexta feira", "sábado"};
    }

    @Override
    public boolean hasCalendar(String input) {
        return input.matches(".*calendário.*");
    }

    @Override
    public String clearCalendar(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (string.matches(".*calendário.*")) {
                parts[i] = "";
                if (i > 0 && parts[i - 1].matches("([ao])")) {
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
        for (String part1 : parts) {
            String part = part1.trim();
            if (!part.matches("na") && !part.matches("em"))
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
        return input.matches(".*cada.*") || input.matches(".*tod([ao])s( as)?.*") || hasEveryDay(input);
    }

    @Override
    public boolean hasEveryDay(String input) {
        return input.matches(".*diário.*");
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
        return input.matches(".*amanh([ãa]).*") || input.matches(".*próximo dia.*");
    }

    @Override
    public String clearTomorrow(String input) {
        String[] parts = input.split(WHITESPACES);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches(".*amanh([ãa]).*") || part.matches(".*próximo dia.*")) {
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
                    if (parts[i - 1].matches("com")) {
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
        if (input.matches(".*mensagem.*")) return Action.MESSAGE;
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
                if (nextIndex < parts.length && parts[nextIndex].matches("para")) {
                    parts[nextIndex] = "";
                }
                break;
            }
        }
        return clipStrings(parts);
    }

    @Override
    public Ampm getAmpm(String input) {
        if (input.matches(".*(de )?manhã.*")) return Ampm.MORNING;
        else if (input.matches(".*tarde.*")) return Ampm.EVENING;
        else if (input.matches(".*meio-dia.*")) return Ampm.NOON;
        else if (input.matches(".*noite.*")) return Ampm.NIGHT;
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
        Pattern pattern = Pattern.compile("([01]?[0-9]|2[0-3])([ :])[0-5][0-9]");
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
        Pattern pattern = Pattern.compile("([01]?[0-9]|2[0-3])([ :])[0-5][0-9]");
        input = clipStrings(parts);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            input = input.replace(time, "");
        }
        parts = input.split(WHITESPACES);
        StringBuilder sb = new StringBuilder();
        for (String part1 : parts) {
            String part = part1.trim();
            if (!part.matches("at")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    @Override
    protected int getMonth(String input) {
        int res = -1;
        if (input.contains("janeiro")) res = 0;
        else if (input.contains("fevereiro")) res = 1;
        else if (input.contains("março") || input.contains("marcha")) res = 2;
        else if (input.contains("abril")) res = 3;
        else if (input.contains("maio")) res = 4;
        else if (input.contains("junho")) res = 5;
        else if (input.contains("julho")) res = 6;
        else if (input.contains("agosto")) res = 7;
        else if (input.contains("setembro")) res = 8;
        else if (input.contains("outubro")) res = 9;
        else if (input.contains("novembro")) res = 10;
        else if (input.contains("dezembro")) res = 11;
        return res;
    }

    @Override
    public boolean hasCall(String input) {
        return input.matches(".*ligue.*");
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
        return input.matches(".*após.*") || input.matches(".* em .*") || input.matches(".*depois( de)?.*");
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
        return input.matches(".*mandar.*") || input.matches(".*enviar.*");
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
        return input.startsWith("abrir") || input.startsWith("aberto")
                || input.matches(".*ajuda.*")
                || input.matches(".*ajustar.*")
                || input.matches(".*relatório.*")
                || input.matches(".*mudança.*");
    }

    @Override
    public Action getAction(String input) {
        if (input.matches(".*ajuda.*")) {
            return Action.HELP;
        } else if (input.matches(".*sonoridade.*") || input.matches(".*volume.*")) {
            return Action.VOLUME;
        } else if (input.matches(".*definições.*") || input.matches(".*configurações.*")) {
            return Action.SETTINGS;
        } else if (input.matches(".*relatório.*")) {
            return Action.REPORT;
        } else {
            return Action.APP;
        }
    }

    @Override
    public boolean hasEvent(String input) {
        return input.startsWith("nova") || input.startsWith("novo") || input.startsWith("adicionar")
                || input.startsWith("crio") || input.startsWith("criar");
    }

    @Override
    public Action getEvent(String input) {
        if (input.matches(".*aniversário.*")) {
            return Action.BIRTHDAY;
        } else if (input.matches(".*(o )?lembrete.*")) {
            return Action.REMINDER;
        } else return Action.NO_EVENT;
    }

    @Override
    public boolean hasEmptyTrash(String input) {
        return input.matches(".*lixo vazio.*");
    }

    @Override
    public boolean hasDisableReminders(String input) {
        return input.matches(".*desativar lembrete.*");
    }

    @Override
    public boolean hasGroup(String input) {
        return input.matches(".*adicionar grupo.*");
    }

    @Override
    public String clearGroup(String input) {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split(WHITESPACES);
        boolean st = false;
        for (String s : parts) {
            if (s.matches(".*(o )?grupo.*")) {
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
        return input.matches(".*hoje.*");
    }

    @Override
    public boolean hasAfterTomorrow(String input) {
        return input.matches(".*depois de amanhã.*");
    }

    @Override
    protected String getAfterTomorrow() {
        return "depois de amanhã";
    }

    @Override
    protected int hasHours(String input) {
        if (input.matches(".*hora.*") || input.matches(".*horas.*")) return 1;
        return -1;
    }

    @Override
    protected int hasMinutes(String input) {
        if (input.matches(".*minuto.*")) return 1;
        return -1;
    }

    @Override
    protected boolean hasSeconds(String input) {
        return input.matches(".*segund([ao])?.*");
    }

    @Override
    protected boolean hasDays(String input) {
        return input.matches(".*dia.*");
    }

    @Override
    protected boolean hasWeeks(String input) {
        return input.matches(".*semana.*");
    }

    @Override
    protected boolean hasMonth(String input) {
        return input.matches(".*mês.*") || input.matches(".*meses.*");
    }

    @Override
    public boolean hasAnswer(String input) {
        input = " " + input + " ";
        return input.matches(".* (sim|não) .*");
    }

    @Override
    public Action getAnswer(String input) {
        if (input.matches(".* ?sim ?.*")) {
            return Action.YES;
        }
        return Action.NO;
    }

    @Override
    protected float findFloat(String input) {
        if (input.matches("mei([ao])")) {
            return 0.5f;
        } else {
            return -1;
        }
    }

    @Override
    protected String clearFloats(String input) {
        if (input.contains("e meio")) {
            return input.replace("e meio", "");
        }
        if (input.contains("e meia")) {
            return input.replace("e meia", "");
        }
        if (input.contains("meio")) {
            return input.replace("meio", "");
        }
        if (input.contains("ao meio")) {
            return input.replace("ao meio", "");
        }
        return input;
    }

    @Override
    protected float findNumber(String input) {
        float number = -1;
        if (input.matches("zero") || input.matches("nulo")) number = 0;
        if (input.matches("uma?") || input.matches("primeir([ao])")) number = 1;
        if (input.matches("dois") || input.matches("duas") || input.matches("segund([ao])")) number = 2;
        if (input.matches("(os )?três") || input.matches("terceir([ao])")) number = 3;
        if (input.matches("quatro") || input.matches("quarto")) number = 4;
        if (input.matches("cinco") || input.matches("quint([ao])")) number = 5;
        if (input.matches("(as )?seis") || input.matches("sext([ao])")) number = 6;
        if (input.matches("sete") || input.matches("sétim([ao])")) number = 7;
        if (input.matches("(os )?oito") || input.matches("oitav([ao])")) number = 8;
        if (input.matches("(os )?nove") || input.matches("non([ao])")) number = 9;
        if (input.matches("dez") || input.matches("décim([ao])")) number = 10;
        if (input.matches("(os )?onze") || input.matches("décima primeira")
                || input.matches("décimo primeiro")) number = 11;
        if (input.matches("(os )?doze") || input.matches("décimo segundo")
                || input.matches("décima segunda")) number = 12;
        if (input.matches("(os )?treze") || input.matches("décimo terceiro")) number = 13;
        if (input.matches("quatorze") || input.matches("(o )?catorze")
                || input.matches("décimo quarto")) number = 14;
        if (input.matches("quinze") || input.matches("décimo quinto")) number = 15;
        if (input.matches("(as )?dezesseis") || input.matches("décimo sexto")) number = 16;
        if (input.matches("(os )?dezessete") || input.matches("décimo sétimo")) number = 17;
        if (input.matches("(os )?dezoito") || input.matches("décimo oitavo")) number = 18;
        if (input.matches("(as )?dezenove") || input.matches("décimo nono")) number = 19;
        if (input.matches("vinte") || input.matches("vigésim([ao])")) number = 20;
        if (input.matches("trinta") || input.matches("trigésim([ao])")) number = 30;
        if (input.matches("(os )?quarenta") || input.matches("(o )?quadragésimo")) number = 40;
        if (input.matches("(os )?cinq([üu])enta") || input.matches("quinquagésim([ao])")) number = 50;
        if (input.matches("(os )?sessenta") || input.matches("(o )?sexagésimo")) number = 60;
        if (input.matches("setenta") || input.matches("septuagésim([ao])")) number = 70;
        if (input.matches("(as )?oitenta") || input.matches("octogésim([ao])")) number = 80;
        if (input.matches("(os )?noventa") || input.matches("(o )?nonagésim([ao])")) number = 90;
        return number;
    }

    @Override
    public boolean hasShowAction(String input) {
        return input.matches(".*mostre.*") || input.matches(".*mostrar.*");
    }

    @Override
    public Action getShowAction(String input) {
        if (input.matches(".*aniversários.*")) {
            return Action.BIRTHDAYS;
        } else if (input.matches(".*lembretes ativos.*")) {
            return Action.ACTIVE_REMINDERS;
        } else if (input.matches(".*lembretes.*")) {
            return Action.REMINDERS;
        } else if (input.matches(".*eventos.*")) {
            return Action.EVENTS;
        } else if (input.matches(".*notas.*")) {
            return Action.NOTES;
        } else if (input.matches(".*grupos.*")) {
            return Action.GROUPS;
        } else if (input.matches(".*listas? de compras.*")) {
            return Action.SHOP_LISTS;
        }
        return null;
    }

    @Override
    public boolean hasNextModifier(String input) {
        return input.matches(".*(nos|para)? ?próximos.*");
    }
}
