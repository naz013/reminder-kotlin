package com.elementary.tasks.core.controller;

import android.content.Context;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.google_tasks.TaskAsync;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TasksConstants;
import com.elementary.tasks.reminder.models.Reminder;

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

    RepeatableEventManager(Reminder reminder, Context context) {
        super(reminder, context);
    }

    protected void export() {
        if (mReminder.isExportToTasks()) {
            long due = TimeUtil.getDateTimeFromGmt(mReminder.getEventTime());
            TaskItem mItem = new TaskItem();
            mItem.setListId(null);
            mItem.setStatus(Google.TASKS_NEED_ACTION);
            mItem.setTitle(mReminder.getSummary());
            mItem.setDueDate(due);
            mItem.setNotes(mContext.getString(R.string.from_reminder));
            mItem.setUuId(mReminder.getUuId());
            new TaskAsync(mContext, TasksConstants.INSERT_TASK, null, mItem, null).execute();
        }
        if (mReminder.isExportToCalendar()) {
            if (Prefs.getInstance(mContext).isStockCalendarEnabled()) {
                CalendarUtils.addEventToStock(mContext, mReminder.getSummary(), TimeUtil.getDateTimeFromGmt(mReminder.getEventTime()));
            }
            if (Prefs.getInstance(mContext).isCalendarEnabled()) {
                CalendarUtils.addEvent(mContext, mReminder);
            }

        }
    }

    @Override
    public boolean resume() {
        if (mReminder.isActive()) {
            new AlarmReceiver().enableReminder(mContext, mReminder.getUuId());
        }
        return true;
    }

    @Override
    public boolean pause() {
        Notifier.hideNotification(mContext, mReminder.getUniqueId());
        new AlarmReceiver().cancelReminder(mContext, mReminder.getUniqueId());
        new AlarmReceiver().cancelDelay(mContext, mReminder.getUniqueId());
        new RepeatNotificationReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        return true;
    }

    @Override
    public boolean stop() {
        mReminder.setActive(false);
        save();
        return pause();
    }
}
