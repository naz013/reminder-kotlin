package com.elementary.tasks.core.controller;

import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.LogUtil;

import androidx.annotation.NonNull;

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

public final class EventControlFactory {

    private static final String TAG = "EventControlFactory";

    private EventControlFactory() {
    }

    @NonNull
    public static EventControl getController(@NonNull Reminder reminder) {
        EventControl control;
        if (Reminder.isSame(reminder.getType(), Reminder.BY_DATE_SHOP)) {
            control = new ShoppingEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_DATE)) {
            control = new DateEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_LOCATION)) {
            control = new LocationEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_MONTH)) {
            control = new MonthlyEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_WEEK)) {
            control = new WeeklyEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_OUT)) {
            control = new LocationEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_PLACES)) {
            control = new LocationEvent(reminder);
        } else if (Reminder.isSame(reminder.getType(), Reminder.BY_TIME)) {
            control = new TimerEvent(reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_DAY_OF_YEAR)) {
            control = new YearlyEvent(reminder);
        } else {
            control = new DateEvent(reminder);
        }
        LogUtil.d(TAG, "getController: " + control);
        return control;
    }
}
