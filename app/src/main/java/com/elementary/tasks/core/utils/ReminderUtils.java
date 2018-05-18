package com.elementary.tasks.core.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.services.BirthdayActionService;
import com.elementary.tasks.core.services.ReminderActionService;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.Calendar;
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
public final class ReminderUtils {

    public static final int DAY_CHECKED = 1;
    private static final String TAG = "ReminderUtils";

    private ReminderUtils() {

    }

    private static Uri getSoundUri(String melody, Context context) {
        if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody)) {
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

    public static void showSimpleBirthday(Context context, String id) {
        BirthdayItem birthdayItem = RealmDb.getInstance().getBirthday(id);
        if (birthdayItem == null) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_cake_nv_white);
        }
        PendingIntent intent = PendingIntent.getBroadcast(context, birthdayItem.getUniqueId(),
                BirthdayActionService.show(context, id), PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentTitle(birthdayItem.getName());
        if (!SuperUtil.isDoNotDisturbEnabled(context) ||
                (SuperUtil.checkNotificationPermission(context) &&
                        Prefs.getInstance(context).isSoundInSilentModeEnabled())) {
            String melodyPath;
            if (Module.isPro() && !isGlobal(context)) {
                melodyPath = Prefs.getInstance(context).getBirthdayMelody();
            } else {
                melodyPath = Prefs.getInstance(context).getMelodyFile();
            }
            Uri uri = getSoundUri(melodyPath, context);
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            builder.setSound(uri);
        }
        boolean vibrate = Prefs.getInstance(context).isVibrateEnabled();
        if (Module.isPro() && !isGlobal(context)) {
            vibrate = Prefs.getInstance(context).isBirthdayVibrationEnabled();
        }
        if (vibrate) {
            vibrate = Prefs.getInstance(context).isInfiniteVibrateEnabled();
            if (Module.isPro() && !isGlobal(context)) {
                vibrate = Prefs.getInstance(context).isBirthdayInfiniteVibrationEnabled();
            }
            long[] pattern;
            if (vibrate) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro() && Prefs.getInstance(context).isLedEnabled()) {
            int ledColor = LED.getLED(Prefs.getInstance(context).getLedColor());
            if (Module.isPro() && !isGlobal(context)) {
                ledColor = LED.getLED(Prefs.getInstance(context).getBirthdayLedColor());
            }
            builder.setLights(ledColor, 500, 1000);
        }
        builder.setContentText(context.getString(R.string.birthday));

        PendingIntent piDismiss = PendingIntent.getBroadcast(context, birthdayItem.getUniqueId(),
                BirthdayActionService.hide(context, id), PendingIntent.FLAG_CANCEL_CURRENT);
        if (Module.isLollipop()) {
            builder.addAction(R.drawable.ic_done_white_24dp, context.getString(R.string.ok), piDismiss);
        } else {
            builder.addAction(R.drawable.ic_done_nv_white, context.getString(R.string.ok), piDismiss);
        }

        if (!TextUtils.isEmpty(birthdayItem.getNumber())) {
            PendingIntent piCall = PendingIntent.getBroadcast(context, birthdayItem.getUniqueId(),
                    BirthdayActionService.call(context, id), PendingIntent.FLAG_CANCEL_CURRENT);
            if (Module.isLollipop()) {
                builder.addAction(R.drawable.ic_call_white_24dp, context.getString(R.string.make_call), piCall);
            } else {
                builder.addAction(R.drawable.ic_call_nv_white, context.getString(R.string.make_call), piCall);
            }

            PendingIntent piSms = PendingIntent.getBroadcast(context, birthdayItem.getUniqueId(),
                    BirthdayActionService.sms(context, id), PendingIntent.FLAG_CANCEL_CURRENT);
            if (Module.isLollipop()) {
                builder.addAction(R.drawable.ic_send_white_24dp, context.getString(R.string.send_sms), piSms);
            } else {
                builder.addAction(R.drawable.ic_send_nv_white, context.getString(R.string.send_sms), piSms);
            }
        }

        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        mNotifyMgr.notify(birthdayItem.getUniqueId(), builder.build());
    }

    private static boolean isGlobal(Context context) {
        return Prefs.getInstance(context).isBirthdayGlobalEnabled();
    }

    public static void showSimpleReminder(Context context, String id) {
        LogUtil.d(TAG, "showSimpleReminder: ");
        Reminder reminder = RealmDb.getInstance().getReminder(id);
        if (reminder == null) return;
        Intent dismissIntent = new Intent(context, ReminderActionService.class);
        dismissIntent.setAction(ReminderActionService.ACTION_HIDE);
        dismissIntent.putExtra(Constants.INTENT_ID, id);
        PendingIntent piDismiss = PendingIntent.getBroadcast(context, reminder.getUniqueId(), dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white);
        }
        Intent notificationIntent = new Intent(context, ReminderActionService.class);
        notificationIntent.setAction(ReminderActionService.ACTION_SHOW);
        notificationIntent.putExtra(Constants.INTENT_ID, id);
        PendingIntent intent = PendingIntent.getBroadcast(context, reminder.getUniqueId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentTitle(reminder.getSummary());
        String appName;
        if (Module.isPro()) {
            appName = context.getString(R.string.app_name_pro);
        } else {
            appName = context.getString(R.string.app_name);
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
            if (reminder.getColor() != -1) {
                builder.setLights(reminder.getColor(), 500, 1000);
            } else {
                builder.setLights(LED.getLED(Prefs.getInstance(context).getLedColor()), 500, 1000);
            }
        }
        builder.setContentText(appName);
        if (Module.isLollipop()) {
            builder.addAction(R.drawable.ic_done_white_24dp, context.getString(R.string.ok), piDismiss);
        } else {
            builder.addAction(R.drawable.ic_done_nv_white, context.getString(R.string.ok), piDismiss);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        mNotifyMgr.notify(reminder.getUniqueId(), builder.build());
    }

    public static long getTime(int day, int month, int year, int hour, int minute, long after) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar.getTimeInMillis() + after;
    }

    public static String getRepeatString(Context context, List<Integer> repCode) {
        StringBuilder sb = new StringBuilder();
        int first = Prefs.getInstance(context).getStartDay();
        if (first == 0 && repCode.get(0) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.sun));
        }
        if (repCode.get(1) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.mon));
        }
        if (repCode.get(2) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.tue));
        }
        if (repCode.get(3) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.wed));
        }
        if (repCode.get(4) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.thu));
        }
        if (repCode.get(5) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.fri));
        }
        if (repCode.get(6) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.sat));
        }
        if (first == 1 && repCode.get(0) == DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.sun));
        }
        if (isAllChecked(repCode)) {
            return context.getString(R.string.everyday);
        } else {
            return sb.toString();
        }
    }

    public static boolean isAllChecked(List<Integer> repCode) {
        boolean is = true;
        for (int i : repCode) {
            if (i == 0) {
                is = false;
                break;
            }
        }
        return is;
    }

    public static String getTypeString(Context context, int type) {
        String res;
        if (Reminder.isKind(type, Reminder.Kind.CALL)) {
            String init = context.getString(R.string.make_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isKind(type, Reminder.Kind.SMS)) {
            String init = context.getString(R.string.message);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE_CALL)) {
            String init = context.getString(R.string.skype_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE)) {
            String init = context.getString(R.string.skype_chat);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE_VIDEO)) {
            String init = context.getString(R.string.video_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            String init = context.getString(R.string.application);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            String init = context.getString(R.string.open_link);
            res = init + " (" + getType(context, type) + ")";
        } else if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            res = context.getString(R.string.shopping_list);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            res = context.getString(R.string.e_mail);
        } else {
            String init = context.getString(R.string.reminder);
            res = init + " (" + getType(context, type) + ")";
        }
        return res;
    }

    public static String getType(Context context, int type) {
        String res;
        if (Reminder.isBase(type, Reminder.BY_MONTH)) {
            res = context.getString(R.string.day_of_month);
        } else if (Reminder.isBase(type, Reminder.BY_WEEK)) {
            res = context.getString(R.string.alarm);
        } else if (Reminder.isBase(type, Reminder.BY_LOCATION)) {
            res = context.getString(R.string.location);
        } else if (Reminder.isBase(type, Reminder.BY_OUT)) {
            res = context.getString(R.string.place_out);
        } else if (Reminder.isSame(type, Reminder.BY_TIME)) {
            res = context.getString(R.string.timer);
        } else if (Reminder.isBase(type, Reminder.BY_PLACES)) {
            res = context.getString(R.string.places);
        } else if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            res = context.getString(R.string.skype);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            res = context.getString(R.string.e_mail);
        } else if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            res = context.getString(R.string.shopping_list);
        } else if (Reminder.isBase(type, Reminder.BY_DAY_OF_YEAR)) {
            res = context.getString(R.string.yearly);
        } else {
            res = context.getString(R.string.by_date);
        }
        return res;
    }
}
