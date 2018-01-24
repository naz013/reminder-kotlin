package com.elementary.tasks.core.services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.ReminderDialogActivity;
import com.elementary.tasks.reminder.models.Reminder;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.Calendar;

import timber.log.Timber;

/**
 * Copyright 2018 Nazar Suhovich
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
public class EventJobService extends Job {

    private static final String TAG = "EventJobService";
    private static final String ARG_LOCATION = "arg_location";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Timber.d("onRunJob: %s, tag -> %s", TimeUtil.getGmtFromDateTime(System.currentTimeMillis()), params.getTag());
        if (!params.getExtras().getBoolean(ARG_LOCATION, false)) {
            start(getContext(), params.getTag());
        } else {
            SuperUtil.startGpsTracking(getContext());
        }
        return Result.SUCCESS;
    }

    private void start(Context context, String id) {
        if (Prefs.getInstance(context).getReminderType() == 0) {
            context.startActivity(ReminderDialogActivity.getLaunchIntent(context, id));
        } else {
            ReminderUtils.showSimpleReminder(context, id);
        }
    }

    public static void enableDelay(int time, String uuId) {
        long min = TimeCount.MINUTE;
        long due = System.currentTimeMillis() + (min * time);
        new JobRequest.Builder(uuId)
                .setExact(due - System.currentTimeMillis())
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(false)
                .build()
                .schedule();
    }

    public static boolean enablePositionDelay(String id) {
        Reminder item = RealmDb.getInstance().getReminder(id);
        if (item == null) {
            return false;
        }
        long startTime = TimeUtil.getDateTimeFromGmt(item.getEventTime());
        if (startTime == 0 || startTime < System.currentTimeMillis()) {
            return false;
        }
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putBoolean(ARG_LOCATION, true);
        new JobRequest.Builder(item.getUuId())
                .setExact(startTime - System.currentTimeMillis())
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(false)
                .setExtras(bundle)
                .build()
                .schedule();
        return true;
    }

    public static void enableReminder(String uuId) {
        Reminder item = RealmDb.getInstance().getReminder(uuId);
        long due = 0;
        if (item != null) {
            due = TimeUtil.getDateTimeFromGmt(item.getEventTime());
        }
        LogUtil.d(TAG, "enableReminder: " + TimeUtil.getFullDateTime(due, true, true));
        if (due == 0) {
            return;
        }
        if (item.getRemindBefore() != 0) {
            due -= item.getRemindBefore();
        }
        if (!Reminder.isBase(item.getType(), Reminder.BY_TIME)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(due);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        new JobRequest.Builder(item.getUuId())
                .setExact(due - System.currentTimeMillis())
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(false)
                .build()
                .schedule();
    }

    public static boolean isEnabledReminder(String tag) {
        return !JobManager.instance().getAllJobsForTag(tag).isEmpty();
    }

    public static void cancelReminder(String tag) {
        Timber.i("cancelReminder: %s", tag);
        for (Job job : JobManager.instance().getAllJobsForTag(tag)) {
            job.cancel();
        }
    }
}
