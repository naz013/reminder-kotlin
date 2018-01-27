package com.elementary.tasks.core.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.birthdays.ShowBirthdayActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.missed_calls.MissedCallDialogActivity;
import com.elementary.tasks.reminder.ReminderDialogActivity;
import com.elementary.tasks.reminder.models.Reminder;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.Calendar;
import java.util.List;

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
    private static final String EVENT_BIRTHDAY = "event_birthday";
    private static final String EVENT_BIRTHDAY_CHECK = "event_birthday_check";
    private static final String EVENT_BIRTHDAY_PERMANENT = "event_birthday_permanent";
    private static final String EVENT_CHECK = "event_check";
    private static final String EVENT_SYNC = "event_sync";

    private static final String ARG_LOCATION = "arg_location";
    private static final String ARG_MISSED = "arg_missed";
    private static final String ARG_REPEAT = "arg_repeat";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Timber.d("onRunJob: %s, tag -> %s", TimeUtil.getGmtFromDateTime(System.currentTimeMillis()), params.getTag());
        switch (params.getTag()) {
            case EVENT_BIRTHDAY:
                birthdayAction(getContext());
                break;
            default: {
                PersistableBundleCompat bundle = params.getExtras();
                if (bundle.getBoolean(ARG_MISSED, false)) {
                    openMissedScreen(params.getTag());
                    enableMissedCall(getContext(), params.getTag());
                } else if (bundle.getBoolean(ARG_LOCATION, false)) {
                    SuperUtil.startGpsTracking(getContext());
                } else {
                    start(getContext(), params.getTag());
                }
            }
            break;
        }
        return Result.SUCCESS;
    }

    private void birthdayAction(Context context) {
        cancelBirthdayAlarm();
        enableBirthdayAlarm(context);
        new Thread(() -> {
            List<BirthdayItem> list = RealmDb.getInstance().getTodayBirthdays(Prefs.getInstance(context).getDaysToBirthday());
            if (list.size() > 0) {
                for (BirthdayItem item : list) {
                    showBirthday(context, item);
                }
            }
        }).start();
    }

    private void showBirthday(Context context, BirthdayItem item) {
        if (Prefs.getInstance(context).getReminderType() == 0) {
            context.startActivity(ShowBirthdayActivity.getLaunchIntent(context, item.getUuId()));
        } else {
            ReminderUtils.showSimpleBirthday(context, item.getUuId());
        }
    }

    public static void cancelBirthdayAlarm() {
        cancelReminder(EVENT_BIRTHDAY);
    }

    public static void enableBirthdayAlarm(Context context) {
        String time = Prefs.getInstance(context).getBirthdayTime();
        long mills = TimeUtil.getBirthdayTime(time);
        new JobRequest.Builder(EVENT_BIRTHDAY)
                .setExact(mills - System.currentTimeMillis())
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(false)
                .build()
                .schedule();
    }

    private void openMissedScreen(String tag) {
        Intent resultIntent = new Intent(getContext(), MissedCallDialogActivity.class);
        resultIntent.putExtra(Constants.INTENT_ID, tag);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getContext().startActivity(resultIntent);
    }

    private void start(Context context, String id) {
        if (Prefs.getInstance(context).getReminderType() == 0) {
            context.startActivity(ReminderDialogActivity.getLaunchIntent(context, id));
        } else {
            ReminderUtils.showSimpleReminder(context, id);
        }
    }

    static void enableMissedCall(Context context, @Nullable String number) {
        if (number == null) return;
        int time = Prefs.getInstance(context).getMissedReminderTime();
        long mills = System.currentTimeMillis() + (time * (1000 * 60));
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putBoolean(ARG_MISSED, true);
        new JobRequest.Builder(number)
                .setExact(mills - System.currentTimeMillis())
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(false)
                .setExtras(bundle)
                .build()
                .schedule();
    }

    public static void cancelMissedCall(@Nullable String number) {
        if (number == null) return;
        cancelReminder(number);
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
