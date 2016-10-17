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

public interface LocaleImpl {
    boolean hasCalendar(String input);
    String clearCalendar(String input);
    ArrayList<Integer> getWeekDays(String input);
    String clearWeekDays(String input);
    long getDaysRepeat(String input);
    String clearDaysRepeat(String input);
    boolean hasRepeat(String input);
    String clearRepeat(String input);
    boolean hasTomorrow(String input);
    String clearTomorrow(String input);
    String getMessage(String input);
    String clearMessage(String input);
    int getType(String input);
    String clearType(String input);
    int getAmpm(String input);
    String clearAmpm(String input);
    long getTime(String input, int ampm, String[] times);
    String clearTime(String input);
    long getDate(String input);
    String clearDate(String input);
    boolean hasCall(String input);
    String clearCall(String input);
    boolean isTimer(String input);
    String cleanTimer(String input);
    boolean hasSender(String input);
    String clearSender(String input);
    boolean hasNote(String input);
    String clearNote(String input);
    boolean hasAction(String input);
    int getAction(String input);
    boolean hasEvent(String input);
    int getEvent(String input);
    long getMultiplier(String input);
    String clearMultiplier(String input);
    String replaceNumbers(String input);
}
