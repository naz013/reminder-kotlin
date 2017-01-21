package com.elementary.tasks.core.controller;

import android.content.Context;
import android.content.Intent;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.GoogleTasks;
import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.services.DelayReceiver;
import com.elementary.tasks.core.services.PermanentReminderService;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
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

abstract class RepeatableEventManager implements EventControl {

    protected Reminder mReminder;
    protected Context mContext;

    RepeatableEventManager(Reminder reminder, Context context) {
        this.mReminder = reminder;
        this.mContext = context;
    }

    protected void save() {
        RealmDb.getInstance().saveObject(mReminder);
        mContext.startService(new Intent(mContext, PermanentReminderService.class).setAction(PermanentReminderService.ACTION_SHOW));
    }

    protected void export() {
        if (mReminder.isExportToTasks()) {
            long due = TimeUtil.getDateTimeFromGmt(mReminder.getEventTime());
            TaskItem mItem = new TaskItem();
            mItem.setListId(null);
            mItem.setStatus(GoogleTasks.TASKS_NEED_ACTION);
            mItem.setTitle(mReminder.getSummary());
            mItem.setDueDate(due);
            mItem.setNotes(mContext.getString(R.string.from_reminder));
            mItem.setUuId(mReminder.getUuId());
            new TaskAsync(mContext, TasksConstants.INSERT_TASK, null, mItem, null).execute();
        }
        if (mReminder.isExportToCalendar()) {
            if (Prefs.getInstance(mContext).isStockCalendarEnabled()) {
                CalendarUtils.getInstance(mContext).addEventToStock(mReminder.getSummary(), TimeUtil.getDateTimeFromGmt(mReminder.getEventTime()));
            }
            if (Prefs.getInstance(mContext).isCalendarEnabled()) {
                CalendarUtils.getInstance(mContext).addEvent(mReminder);
            }

        }
    }

    @Override
    public boolean stop() {
        Notifier.hideNotification(mContext, mReminder.getUniqueId());
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        new DelayReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        new RepeatNotificationReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        mReminder.setActive(false);
        save();
        return true;
    }
}
