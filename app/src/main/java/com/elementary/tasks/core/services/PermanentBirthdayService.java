package com.elementary.tasks.core.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeUtil;

import java.util.Calendar;
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

public class PermanentBirthdayService extends Service {

    public static final String ACTION_SHOW = "com.elementary.tasks.birthday.SHOW";
    public static final String ACTION_HIDE = "com.elementary.tasks.birthday.HIDE";

    private static final String TAG = "PermanentBirthdayS";
    private static final int PERM_ID = 356665;

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (!Prefs.getInstance(getApplicationContext()).isBirthdayPermanentEnabled()) {
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
        Intent dismissIntent = new Intent(getApplicationContext(), PermanentBirthdayService.class);
        dismissIntent.setAction(ACTION_HIDE);
        PendingIntent piDismiss = PendingIntent.getService(getApplicationContext(), 0, dismissIntent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        List<BirthdayItem> list = RealmDb.getInstance().getBirthdays(day, month);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentTitle(getString(R.string.events));
        if (list.size() > 0) {
            BirthdayItem item = list.get(0);
            builder.setContentText(item.getDate() + " | " + item.getName() + " | " + TimeUtil.getAgeFormatted(getApplicationContext(), item.getDate()));
            if (list.size() > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                for (BirthdayItem birthdayItem : list){
                    stringBuilder.append(birthdayItem.getDate()).append(" | ").
                            append(birthdayItem.getName()).append(" | ")
                            .append(TimeUtil.getAgeFormatted(getApplicationContext(), birthdayItem.getDate()));
                    stringBuilder.append("\n");
                }
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(stringBuilder.toString()));
            }
            builder.addAction(R.drawable.ic_clear_white_24dp, getString(R.string.ok), piDismiss);
            startForeground(PERM_ID, builder.build());
        } else hidePermanent();
    }
}
