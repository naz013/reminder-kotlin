package com.elementary.tasks.core.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.app_widgets.WidgetUtils;
import com.elementary.tasks.core.services.PermanentBirthdayReceiver;
import com.elementary.tasks.core.services.PermanentReminderReceiver;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.notes.CreateNoteActivity;
import com.elementary.tasks.notes.NoteImage;
import com.elementary.tasks.notes.NoteItem;
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

public class Notifier {

    public static final String CHANNEL_REMINDER = "reminder.channel1";
    public static final String CHANNEL_SYSTEM = "reminder.channel2";

    private static final String TAG = "Notifier";

    private Context mContext;

    public Notifier(Context context) {
        this.mContext = context;
    }

    public static void createChannels(Context context) {
        if (Module.isO()) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(createReminderChannel(context));
                manager.createNotificationChannel(createSystemChannel(context));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createSystemChannel(Context context) {
        String name = context.getString(R.string.info_channel);
        String descr = context.getString(R.string.channel_for_other_info_notifications);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_SYSTEM, name, importance);
        mChannel.setDescription(descr);
        mChannel.setShowBadge(false);
        return mChannel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createReminderChannel(Context context) {
        String name = context.getString(R.string.reminder_channel);
        String descr = context.getString(R.string.default_reminder_notifications);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_REMINDER, name, importance);
        mChannel.setDescription(descr);
        mChannel.setShowBadge(true);
        return mChannel;
    }

    public static void hideNotification(Context context, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(id);
    }

    public static void updateReminderPermanent(Context context, String action) {
        context.sendBroadcast(new Intent(context, PermanentReminderReceiver.class)
                .setAction(action));
    }

    public void showNoteNotification(@NonNull NoteItem item) {
        Prefs sPrefs = Prefs.getInstance(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, Notifier.CHANNEL_REMINDER);
        builder.setContentText(mContext.getString(R.string.note));
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        String content = item.getSummary();
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_note_white);
        } else {
            builder.setSmallIcon(R.drawable.ic_note_nv_white);
        }
        builder.setContentTitle(content);
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear && Module.isJellyMR2()) {
            builder.setOnlyAlertOnce(true);
            builder.setGroup("GROUP");
            builder.setGroupSummary(true);
        }
        if (!item.getImages().isEmpty() && Module.isMarshmallow()) {
            NoteImage image = item.getImages().get(0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image.getImage(), 0, image.getImage().length);
            builder.setLargeIcon(bitmap);
            NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();
            s.bigLargeIcon(bitmap);
            s.bigPicture(bitmap);
            builder.setStyle(s);
        }
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(item.getUniqueId(), builder.build());
        if (isWear && Module.isJellyMR2()) {
            NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext, Notifier.CHANNEL_REMINDER);
            wearableNotificationBuilder.setSmallIcon(R.drawable.ic_note_nv_white);
            wearableNotificationBuilder.setContentTitle(content);
            wearableNotificationBuilder.setContentText(mContext.getString(R.string.note));
            wearableNotificationBuilder.setOngoing(false);
            if (Module.isLollipop()) {
                wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
            }
            wearableNotificationBuilder.setOnlyAlertOnce(true);
            wearableNotificationBuilder.setGroup("GROUP");
            wearableNotificationBuilder.setGroupSummary(false);
            if (manager != null)
                manager.notify(item.getUniqueId(), wearableNotificationBuilder.build());
        }
    }

    public static void showBirthdayPermanent(Context context) {
        Intent dismissIntent = new Intent(context, PermanentBirthdayReceiver.class);
        dismissIntent.setAction(PermanentBirthdayReceiver.ACTION_HIDE);
        PendingIntent piDismiss = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        List<BirthdayItem> list = RealmDb.getInstance().getBirthdays(day, month);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_cake_nv_white);
        }
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentTitle(context.getString(R.string.events));
        if (list.size() > 0) {
            BirthdayItem item = list.get(0);
            builder.setContentText(item.getDate() + " | " + item.getName() + " | " + TimeUtil.getAgeFormatted(context, item.getDate()));
            if (list.size() > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                for (BirthdayItem birthdayItem : list) {
                    stringBuilder.append(birthdayItem.getDate()).append(" | ").
                            append(birthdayItem.getName()).append(" | ")
                            .append(TimeUtil.getAgeFormatted(context, birthdayItem.getDate()));
                    stringBuilder.append("\n");
                }
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(stringBuilder.toString()));
            }
            if (Module.isLollipop()) {
                builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.ok), piDismiss);
            } else {
                builder.addAction(R.drawable.ic_clear_nv_white, context.getString(R.string.ok), piDismiss);
            }
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null)
                manager.notify(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID, builder.build());
        }
    }

    public static void showReminderPermanent(Context context) {
        LogUtil.d(TAG, "showPermanent: ");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER);
        builder.setAutoCancel(false);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white);
        }
        builder.setContent(remoteViews);
        builder.setOngoing(true);
        if (Prefs.getInstance(context).isSbIconEnabled()) {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }
        Intent resultIntent = new Intent(context, CreateReminderActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CreateReminderActivity.class);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, 0);
        remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent);
        Intent noteIntent = new Intent(context, CreateNoteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder noteBuilder = TaskStackBuilder.create(context);
        noteBuilder.addParentStack(CreateNoteActivity.class);
        noteBuilder.addNextIntent(noteIntent);
        PendingIntent notePendingIntent = noteBuilder.getPendingIntent(0, 0);
        remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent);
        Intent resInt = new Intent(context, SplashScreen.class);
        TaskStackBuilder stackInt = TaskStackBuilder.create(context);
        stackInt.addParentStack(SplashScreen.class);
        stackInt.addNextIntent(resInt);
        PendingIntent resultPendingInt = stackInt.getPendingIntent(0, 0);
        remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt);
        remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt);
        List<Reminder> reminders = RealmDb.getInstance().getEnabledReminders();
        int count = reminders.size();
        for (int i = reminders.size() - 1; i >= 0; i--) {
            Reminder item = reminders.get(i);
            long eventTime = item.getDateTime();
            if (eventTime <= 0) {
                reminders.remove(i);
            }
        }
        String event = "";
        long prevTime = 0;
        for (int i = 0; i < reminders.size(); i++) {
            Reminder item = reminders.get(i);
            if (item.getDateTime() > System.currentTimeMillis()) {
                if (prevTime == 0) {
                    prevTime = item.getDateTime();
                    event = item.getSummary();
                } else if (item.getDateTime() < prevTime) {
                    prevTime = item.getDateTime();
                    event = item.getSummary();
                }
            }
        }
        if (count != 0) {
            if (!TextUtils.isEmpty(event)) {
                remoteViews.setTextViewText(R.id.text, event);
                remoteViews.setViewVisibility(R.id.featured, View.VISIBLE);
            } else {
                remoteViews.setTextViewText(R.id.text, context.getString(R.string.active_reminders) + " " + count);
                remoteViews.setViewVisibility(R.id.featured, View.GONE);
            }
        } else {
            remoteViews.setTextViewText(R.id.text, context.getString(R.string.no_events));
            remoteViews.setViewVisibility(R.id.featured, View.GONE);
        }
        ThemeUtil cs = ThemeUtil.getInstance(context);
        WidgetUtils.setIcon(context, remoteViews, R.drawable.ic_alarm_white, R.id.notificationAdd);
        WidgetUtils.setIcon(context, remoteViews, R.drawable.ic_note_white, R.id.noteAdd);
        WidgetUtils.setIcon(context, remoteViews, R.drawable.ic_notifications_white_24dp, R.id.bellIcon);
        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", cs.getColor(cs.colorPrimary()));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(PermanentReminderReceiver.PERM_ID, builder.build());
    }
}
