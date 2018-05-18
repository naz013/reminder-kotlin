package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.UriUtil;
import com.elementary.tasks.reminder.ReminderDialogActivity;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.Calendar;

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

public class RepeatNotificationReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(Constants.INTENT_ID);
        Reminder item = RealmDb.getInstance().getReminder(id);
        if (item != null) {
            showNotification(context, item);
        }
    }

    public void setAlarm(Context context, String uuId, int id) {
        int repeat = Prefs.getInstance(context).getNotificationRepeatTime();
        int minutes = repeat * 1000 * 60;
        Intent intent = new Intent(context, RepeatNotificationReceiver.class);
        intent.putExtra(Constants.INTENT_ID, uuId);
        alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + minutes, minutes, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + minutes, minutes, alarmIntent);
        }
    }

    public void cancelAlarm(Context context, int id) {
        Intent intent = new Intent(context, RepeatNotificationReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    private Uri getSoundUri(String melody, Context context) {
        if (!TextUtils.isEmpty(melody)) {
            return UriUtil.getUri(context, melody);
        } else {
            String defMelody = Prefs.getInstance(context).getMelodyFile();
            if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
                return UriUtil.getUri(context, defMelody);
            } else {
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }
    }

    private void showNotification(Context context, Reminder reminder) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER);
        builder.setContentTitle(reminder.getSummary());
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (Prefs.getInstance(context).isFoldingEnabled() && !Reminder.isBase(reminder.getType(), Reminder.BY_WEEK)) {
            PendingIntent intent = PendingIntent.getActivity(context, reminder.getUniqueId(),
                    ReminderDialogActivity.getLaunchIntent(context, reminder.getUuId()), PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(intent);
        }
        if (Module.isPro()) {
            builder.setContentText(context.getString(R.string.app_name_pro));
        } else {
            builder.setContentText(context.getString(R.string.app_name));
        }
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white);
        }
        if (!SuperUtil.isDoNotDisturbEnabled(context) ||
                (SuperUtil.checkNotificationPermission(context) &&
                        Prefs.getInstance(context).isSoundInSilentModeEnabled())) {
            Uri uri = getSoundUri(reminder.getMelodyPath(), context);
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            builder.setSound(uri);
        }
        if (Prefs.getInstance(context).isVibrateEnabled()) {
            long[] pattern;
            if (Prefs.getInstance(context).isInfiniteVibrateEnabled()) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro() && Prefs.getInstance(context).isLedEnabled()) {
            if (reminder.getColor() != 0) {
                builder.setLights(reminder.getColor(), 500, 1000);
            } else {
                builder.setLights(LED.getLED(Prefs.getInstance(context).getLedColor()), 500, 1000);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        mNotifyMgr.notify(reminder.getUniqueId(), builder.build());
    }
}
