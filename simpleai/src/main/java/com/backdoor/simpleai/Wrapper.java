package com.backdoor.simpleai;

import java.util.ArrayList;

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
public class Wrapper extends RecUtils {

    private String locale;

    public Wrapper(String locale) {
        this.locale = locale;
    }

    public boolean hasCalendar(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasCalendar(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasCalendar(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasCalendar(input);
        } else return false;
    }

    public String clearCalendar(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearCalendar(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearCalendar(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearCalendar(input);
        } else return null;
    }

    public ArrayList<Integer> getWeekDays(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getWeekDays(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getWeekDays(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getWeekDays(input);
        } else return null;
    }

    public String clearWeekDays(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearWeekDays(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearWeekDays(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearWeekDays(input);
        } else return null;
    }

    public long getDaysRepeat(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getDaysRepeat(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getDaysRepeat(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getDaysRepeat(input);
        } else return 0;
    }

    public String clearDaysRepeat(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearDaysRepeat(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearDaysRepeat(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearDaysRepeat(input);
        } else return null;
    }

    public boolean hasRepeat(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasRepeat(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasRepeat(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasRepeat(input);
        } else return false;
    }

    public String clearRepeat(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearRepeat(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearRepeat(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearRepeat(input);
        } else return null;
    }

    public boolean hasTomorrow(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasTomorrow(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasTomorrow(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasTomorrow(input);
        } else return false;
    }

    public String clearTomorrow(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearTomorrow(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearTomorrow(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearTomorrow(input);
        } else return null;
    }

    public String getMessage(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getMessage(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getMessage(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getMessage(input);
        } else return null;
    }

    public String clearMessage(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearMessage(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearMessage(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearMessage(input);
        } else return null;
    }

    public int getType(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getType(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getType(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getType(input);
        } else return -1;
    }

    public String clearType(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearType(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearType(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearType(input);
        } else return null;
    }

    public int getAmpm(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getAmpm(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getAmpm(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getAmpm(input);
        } else return -1;
    }

    public String clearAmpm(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearAmpm(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearAmpm(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearAmpm(input);
        } else return null;
    }

    public long getTime(String input, int ampm, String[] times) {
        if (locale.matches(Locale.EN)){
            return EN.getTime(input, ampm, times);
        } else if (locale.matches(Locale.RU)){
            return RU.getTime(input, ampm, times);
        } else if (locale.matches(Locale.UK)){
            return UK.getTime(input, ampm, times);
        } else return 0;
    }

    public String clearTime(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearTime(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearTime(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearTime(input);
        } else return null;
    }

    public long getDate(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getDate(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getDate(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getDate(input);
        } else return 0;
    }

    public String clearDate(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearDate(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearDate(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearDate(input);
        } else return null;
    }

    public boolean hasCall(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasCall(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasCall(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasCall(input);
        } else return false;
    }

    public String clearCall(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearCall(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearCall(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearCall(input);
        } else return null;
    }

    public boolean isTimer(String input) {
        if (locale.matches(Locale.EN)){
            return EN.isTimer(input);
        } else if (locale.matches(Locale.RU)){
            return RU.isTimer(input);
        } else if (locale.matches(Locale.UK)){
            return UK.isTimer(input);
        } else return false;
    }

    public String cleanTimer(String input) {
        if (locale.matches(Locale.EN)){
            return EN.cleanTimer(input);
        } else if (locale.matches(Locale.RU)){
            return RU.cleanTimer(input);
        } else if (locale.matches(Locale.UK)){
            return UK.cleanTimer(input);
        } else return null;
    }

    public boolean hasSender(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasSender(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasSender(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasSender(input);
        } else return false;
    }

    public String clearSender(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearSender(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearSender(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearSender(input);
        } else return null;
    }

    public boolean hasNote(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasNote(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasNote(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasNote(input);
        } else return false;
    }

    public String clearNote(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearNote(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearNote(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearNote(input);
        } else return null;
    }

    public boolean hasAction(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasAction(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasAction(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasAction(input);
        } else return false;
    }

    public int getAction(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getAction(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getAction(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getAction(input);
        } else return APP;
    }

    public boolean hasEvent(String input) {
        if (locale.matches(Locale.EN)){
            return EN.hasEvent(input);
        } else if (locale.matches(Locale.RU)){
            return RU.hasEvent(input);
        } else if (locale.matches(Locale.UK)){
            return UK.hasEvent(input);
        } else return false;
    }

    public int getEvent(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getEvent(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getEvent(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getEvent(input);
        } else return REMINDER;
    }

    public long getMultiplier(String input) {
        if (locale.matches(Locale.EN)){
            return EN.getMultiplier(input);
        } else if (locale.matches(Locale.RU)){
            return RU.getMultiplier(input);
        } else if (locale.matches(Locale.UK)){
            return UK.getMultiplier(input);
        } else return 0;
    }

    public String clearMultiplier(String input) {
        if (locale.matches(Locale.EN)){
            return EN.clearMultiplier(input);
        } else if (locale.matches(Locale.RU)){
            return RU.clearMultiplier(input);
        } else if (locale.matches(Locale.UK)){
            return UK.clearMultiplier(input);
        } else return null;
    }

    public String replaceNumbers(String input) {
        if (locale.matches(Locale.EN)){
            return EN.replaceNumbers(input);
        } else if (locale.matches(Locale.RU)){
            return RU.replaceNumbers(input);
        } else if (locale.matches(Locale.UK)){
            return UK.replaceNumbers(input);
        } else return null;
    }
}
