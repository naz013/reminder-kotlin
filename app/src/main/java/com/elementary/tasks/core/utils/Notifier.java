package com.elementary.tasks.core.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

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

    private Context mContext;
    private int NOT_ID = 0;

    public Notifier(Context context){
        this.mContext = context;
    }

    public static void hideNotification(Context context, int id) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
    }

    public void showNoteNotification(NoteItem item){
        Prefs sPrefs = Prefs.getInstance(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentText(mContext.getString(R.string.note));
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        String content = item.getSummary();
        builder.setSmallIcon(R.drawable.ic_note_white);
        builder.setContentTitle(content);
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Module.isJellyMR2()) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        if (!item.getImages().isEmpty()) {
            NoteImage image = item.getImages().get(0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image.getImage(), 0, image.getImage().length);
            builder.setLargeIcon(bitmap);
            NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();
            s.bigLargeIcon(bitmap);
            s.bigPicture(bitmap);
            builder.setStyle(s);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify(item.getUniqueId(), builder.build());
        if (isWear){
            if (Module.isJellyMR2()) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
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
                mNotifyMgr.notify(item.getUniqueId(), wearableNotificationBuilder.build());
            }
        }
    }

    public void recreatePermanent(){
        if (Prefs.getInstance(mContext).isSbNotificationEnabled()) showPermanent();
    }

    public void showPermanent(){
//        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
//        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
//        notification.setAutoCancel(false);
//        notification.setSmallIcon(R.drawable.ic_notifications_white_24dp);
//        notification.setContent(remoteViews);
//        notification.setOngoing(true);
//        if (Prefs.getInstance(mContext).isSbIconEnabled()) {
//            notification.setPriority(NotificationCompat.PRIORITY_MAX);
//        } else {
//            notification.setPriority(NotificationCompat.PRIORITY_MIN);
//        }
//        Intent resultIntent = new Intent(mContext, ReminderActivity.class)
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
//        stackBuilder.addParentStack(ReminderActivity.class);
//        stackBuilder.addNextIntentWithParentStack(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
//                PendingIntent.FLAG_ONE_SHOT);
//        remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent);
//        Intent noteIntent = new Intent(mContext, NotesActivity.class)
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        TaskStackBuilder noteBuilder = TaskStackBuilder.create(mContext);
//        noteBuilder.addParentStack(NotesActivity.class);
//        noteBuilder.addNextIntent(noteIntent);
//        PendingIntent notePendingIntent = noteBuilder.getPendingIntent(0,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent);
//        Intent resInt = new Intent(mContext, StartActivity.class);
//        resInt.putExtra("tag", StartActivity.FRAGMENT_ACTIVE);
//        TaskStackBuilder stackInt = TaskStackBuilder.create(mContext);
//        stackInt.addParentStack(StartActivity.class);
//        stackInt.addNextIntent(resInt);
//        PendingIntent resultPendingInt = stackInt.getPendingIntent(0,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt);
//        remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt);
//        ArrayList<Long> dates = new ArrayList<>();
//        ArrayList<String> tasks = new ArrayList<>();
//        List<ReminderItem> list = ReminderHelper.getInstance(mContext).getRemindersEnabled();
//        int count = list.size();
//        for (ReminderItem item : list) {
//            long eventTime = item.getDateTime();
//            String summary = item.getSummary();
//            if (eventTime > 0) {
//                dates.add(eventTime);
//                tasks.add(summary);
//            }
//        }
//        String event = "";
//        long prevTime = 0;
//        for (int i = 0; i < dates.size(); i++) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(System.currentTimeMillis());
//            long currTime = calendar.getTimeInMillis();
//            calendar.setTimeInMillis(dates.get(i));
//            if (calendar.getTimeInMillis() > currTime){
//                if (prevTime == 0){
//                    prevTime = dates.get(i);
//                    event = tasks.get(i);
//                } else {
//                    if (dates.get(i) < prevTime){
//                        prevTime = dates.get(i);
//                        event = tasks.get(i);
//                    }
//                }
//
//            }
//        }
//        if (count != 0){
//            if (!event.matches("")){
//                remoteViews.setTextViewText(R.id.text, event);
//                remoteViews.setViewVisibility(R.id.featured, View.VISIBLE);
//            } else {
//                remoteViews.setTextViewText(R.id.text, mContext.getString(R.string.active_reminders) + " " + String.valueOf(count));
//                remoteViews.setViewVisibility(R.id.featured, View.GONE);
//            }
//        } else {
//            remoteViews.setTextViewText(R.id.text, mContext.getString(R.string.no_events));
//            remoteViews.setViewVisibility(R.id.featured, View.GONE);
//        }
//        ColorSetter cs = ColorSetter.getInstance(mContext);
//        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", cs.getColor(cs.colorPrimary()));
//        NotificationManagerCompat notifier = NotificationManagerCompat.from(mContext);
//        notifier.notify(1, notification.build());
    }

    public void hidePermanent(){
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }
}
