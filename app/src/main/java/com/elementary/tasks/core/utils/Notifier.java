package com.elementary.tasks.core.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.elementary.tasks.R;
import com.elementary.tasks.notes.NoteImage;
import com.elementary.tasks.notes.NoteItem;

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

    private Context mContext;

    public Notifier(Context context) {
        this.mContext = context;
    }

    public static void createChannels(Context context) {
        if (Module.isO()) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(createReminderChannel(context));
            manager.createNotificationChannel(createSystemChannel(context));
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createSystemChannel(Context context) {
        String name = context.getString(R.string.info_channel);
        String descr = context.getString(R.string.channel_for_other_info_notifications);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_SYSTEM, name, importance);
        mChannel.setDescription(descr);
        return mChannel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createReminderChannel(Context context) {
        String name = context.getString(R.string.reminder_channel);
        String descr = context.getString(R.string.default_reminder_notifications);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_REMINDER, name, importance);
        mChannel.setDescription(descr);
        return mChannel;
    }

    public static void hideNotification(Context context, int id) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
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
            builder.setSmallIcon(R.mipmap.ic_launcher);
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
        manager.notify(item.getUniqueId(), builder.build());
        if (isWear && Module.isJellyMR2()) {
            NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext, Notifier.CHANNEL_REMINDER);
            wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            wearableNotificationBuilder.setContentTitle(content);
            wearableNotificationBuilder.setContentText(mContext.getString(R.string.note));
            wearableNotificationBuilder.setOngoing(false);
            if (Module.isLollipop()) {
                wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
            }
            wearableNotificationBuilder.setOnlyAlertOnce(true);
            wearableNotificationBuilder.setGroup("GROUP");
            wearableNotificationBuilder.setGroupSummary(false);
            manager.notify(item.getUniqueId(), wearableNotificationBuilder.build());
        }
    }
}
