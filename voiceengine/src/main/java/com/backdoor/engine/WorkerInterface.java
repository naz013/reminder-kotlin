package com.backdoor.engine;

import java.util.List;

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

public interface WorkerInterface {
    boolean hasCalendar(String input);

    String clearCalendar(String input);

    List<Integer> getWeekDays(String input);

    String clearWeekDays(String input);

    long getDaysRepeat(String input);

    String clearDaysRepeat(String input);

    boolean hasRepeat(String input);

    String clearRepeat(String input);

    boolean hasTomorrow(String input);

    String clearTomorrow(String input);

    String getMessage(String input);

    String clearMessage(String input);

    Action getMessageType(String input);

    String clearMessageType(String input);

    Ampm getAmpm(String input);

    String clearAmpm(String input);

    long getTime(String input, Ampm ampm, String[] times);

    String clearTime(String input);

    String getDate(String input, Long res);

    boolean hasCall(String input);

    String clearCall(String input);

    boolean isTimer(String input);

    String cleanTimer(String input);

    boolean hasSender(String input);

    String clearSender(String input);

    boolean hasNote(String input);

    String clearNote(String input);

    boolean hasAction(String input);

    Action getAction(String input);

    boolean hasEvent(String input);

    Action getEvent(String input);

    String getMultiplier(String input, Long res);

    String replaceNumbers(String input);

    boolean hasDisableReminders(String input);

    boolean hasEmptyTrash(String input);

    boolean hasGroup(String input);

    String clearGroup(String input);

    boolean hasToday(String input);

    String clearToday(String input);

    boolean hasAfterTomorrow(String input);

    String clearAfterTomorrow(String input);

    boolean hasAnswer(String input);

    Action getAnswer(String input);

    boolean hasShowAction(String input);

    Action getShowAction(String input);

    boolean hasNextModifier(String input);
}
