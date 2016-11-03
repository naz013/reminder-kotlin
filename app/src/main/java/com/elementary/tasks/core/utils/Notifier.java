package com.elementary.tasks.core.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.elementary.tasks.R;

import java.util.ArrayList;
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

public class Notifier {

    private Context mContext;
    private int NOT_ID = 0;
    private Sound sound;

    public Notifier(Context context){
        this.mContext = context;
        sound = new Sound(context);
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
