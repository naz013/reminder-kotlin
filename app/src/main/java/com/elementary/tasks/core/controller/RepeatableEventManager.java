package com.elementary.tasks.core.controller;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.services.EventJobService;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.google_tasks.TaskAsync;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TasksConstants;

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

abstract class RepeatableEventManager extends EventManager {

    RepeatableEventManager(Reminder reminder) {
        super(reminder);
    }

    protected void enableReminder() {
        EventJobService.enableReminder(getReminder());
    }

    protected void export() {
        if (getReminder().isExportToTasks()) {
            long due = TimeUtil.getDateTimeFromGmt(getReminder().getEventTime());
            TaskItem mItem = new TaskItem();
            mItem.setListId(null);
            mItem.setStatus(Google.TASKS_NEED_ACTION);
            mItem.setTitle(getReminder().getSummary());
            mItem.setDueDate(due);
            mItem.setNotes(getContext().getString(R.string.from_reminder));
            mItem.setUuId(getReminder().getUuId());
            new TaskAsync(getContext(), TasksConstants.INSERT_TASK, null, mItem, null).execute();
        }
        if (getReminder().isExportToCalendar()) {
            if (Prefs.getInstance(getContext()).isStockCalendarEnabled()) {
                CalendarUtils.addEventToStock(getContext(), getReminder().getSummary(), TimeUtil.getDateTimeFromGmt(getReminder().getEventTime()));
            }
            if (Prefs.getInstance(getContext()).isCalendarEnabled()) {
                CalendarUtils.addEvent(getContext(), getReminder());
            }
        }
    }

    @Override
    public boolean resume() {
        if (getReminder().isActive()) {
            enableReminder();
        }
        return true;
    }

    @Override
    public boolean pause() {
        Notifier.hideNotification(getContext(), getReminder().getUniqueId());
        EventJobService.cancelReminder(getReminder().getUuId());
        new RepeatNotificationReceiver().cancelAlarm(getContext(), getReminder().getUniqueId());
        return true;
    }

    @Override
    public boolean stop() {
        getReminder().setActive(false);
        save();
        return pause();
    }

    @Override
    public void setDelay(int delay) {
        EventJobService.enableDelay(delay, getReminder().getUuId());
    }
}
