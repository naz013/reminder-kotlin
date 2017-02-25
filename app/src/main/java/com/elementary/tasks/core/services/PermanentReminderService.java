package com.elementary.tasks.core.services;

import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.elementary.tasks.R;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.notes.ActivityCreateNote;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
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

public class PermanentReminderService extends Service {

    public static final String ACTION_SHOW = "com.elementary.tasks.SHOW";
    public static final String ACTION_HIDE = "com.elementary.tasks.HIDE";

    private static final String TAG = "PermanentReminderS";
    private static final int PERM_ID = 356664;

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (!Prefs.getInstance(getApplicationContext()).isSbNotificationEnabled()) {
            hidePermanent();
        }
        if (intent != null) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onStartCommand: " + action);
            if (action != null && action.matches(ACTION_SHOW)) {
                showPermanent();
            } else {
                hidePermanent();
            }
        } else {
            hidePermanent();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy: ");
        hidePermanent();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void hidePermanent() {
        stopForeground(true);
        stopSelf();
    }

    private void showPermanent() {
        RemoteViews remoteViews = new RemoteViews(getApplication().getPackageName(),
                R.layout.notification_layout);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
        notification.setAutoCancel(false);
        notification.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        notification.setContent(remoteViews);
        notification.setOngoing(true);
        if (Prefs.getInstance(getApplicationContext()).isSbIconEnabled()) {
            notification.setPriority(NotificationCompat.PRIORITY_MAX);
        } else {
            notification.setPriority(NotificationCompat.PRIORITY_MIN);
        }
        Intent resultIntent = new Intent(getApplicationContext(), CreateReminderActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(CreateReminderActivity.class);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent);
        Intent noteIntent = new Intent(getApplicationContext(), ActivityCreateNote.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder noteBuilder = TaskStackBuilder.create(getApplicationContext());
        noteBuilder.addParentStack(ActivityCreateNote.class);
        noteBuilder.addNextIntent(noteIntent);
        PendingIntent notePendingIntent = noteBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent);
        Intent resInt = new Intent(getApplicationContext(), SplashScreen.class);
        TaskStackBuilder stackInt = TaskStackBuilder.create(getApplicationContext());
        stackInt.addParentStack(SplashScreen.class);
        stackInt.addNextIntent(resInt);
        PendingIntent resultPendingInt = stackInt.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
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
            if (item.getDateTime() > System.currentTimeMillis()){
                if (prevTime == 0){
                    prevTime = item.getDateTime();
                    event = item.getSummary();
                } else if (item.getDateTime() < prevTime){
                    prevTime = item.getDateTime();
                    event = item.getSummary();
                }
            }
        }
        if (count != 0){
            if (!TextUtils.isEmpty(event)){
                remoteViews.setTextViewText(R.id.text, event);
                remoteViews.setViewVisibility(R.id.featured, View.VISIBLE);
            } else {
                remoteViews.setTextViewText(R.id.text, getString(R.string.active_reminders) + " " + String.valueOf(count));
                remoteViews.setViewVisibility(R.id.featured, View.GONE);
            }
        } else {
            remoteViews.setTextViewText(R.id.text, getString(R.string.no_events));
            remoteViews.setViewVisibility(R.id.featured, View.GONE);
        }
        ThemeUtil cs = ThemeUtil.getInstance(getApplicationContext());
        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", cs.getColor(cs.colorPrimary()));
        startForeground(PERM_ID, notification.build());
    }
}
