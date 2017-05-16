package com.elementary.tasks.core.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.birthdays.ShowBirthdayActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TelephonyUtil;

import java.util.Calendar;

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

public class BirthdayActionService extends Service {

    public static final String ACTION_SHOW = "com.elementary.tasks.reminder.SHOW";
    public static final String ACTION_HIDE = "com.elementary.tasks.reminder.HIDE";
    public static final String ACTION_CALL = "com.elementary.tasks.reminder.CALL";
    public static final String ACTION_SMS = "com.elementary.tasks.reminder.SMS";

    private static final String TAG = "BirthdayActionService";

    public static Intent hide(Context context, String id) {
        return intent(context, id, ACTION_HIDE);
    }

    public static Intent call(Context context, String id) {
        return intent(context, id, ACTION_CALL);
    }

    public static Intent show(Context context, String id) {
        return intent(context, id, ACTION_SHOW);
    }

    public static Intent sms(Context context, String id) {
        return intent(context, id, ACTION_SMS);
    }

    private static Intent intent(Context context, String id, String action) {
        Intent intent = new Intent(context, BirthdayActionService.class);
        intent.setAction(action);
        intent.putExtra(Constants.INTENT_ID, id);
        return intent;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onStartCommand: " + action);
            if (action != null) {
                if (action.matches(ACTION_HIDE)) {
                    hidePermanent(intent.getStringExtra(Constants.INTENT_ID));
                } else if (action.matches(ACTION_CALL)) {
                    makeCall(intent);
                } else if (action.matches(ACTION_SMS)) {
                    sendSms(intent);
                } else {
                    showReminder(intent);
                }
            } else {
                stopSelf();
            }
        }
        return START_STICKY;
    }

    private void updateBirthday(BirthdayItem item) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        item.setShowedYear(year);
        RealmDb.getInstance().saveObject(item);
    }

    private void sendSms(Intent intent) {
        BirthdayItem item = RealmDb.getInstance().getBirthday(intent.getStringExtra(Constants.INTENT_ID));
        if (Permissions.checkPermission(getApplicationContext(), Permissions.SEND_SMS)) {
            TelephonyUtil.sendSms(item.getNumber(), getApplicationContext());
            updateBirthday(item);
        } else {
            hidePermanent(intent.getStringExtra(Constants.INTENT_ID));
        }
    }

    private void makeCall(Intent intent) {
        BirthdayItem item = RealmDb.getInstance().getBirthday(intent.getStringExtra(Constants.INTENT_ID));
        if (Permissions.checkPermission(getApplicationContext(), Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(item.getNumber(), this);
            updateBirthday(item);
        } else {
            hidePermanent(intent.getStringExtra(Constants.INTENT_ID));
        }
    }

    private void showReminder(Intent intent) {
        BirthdayItem reminder = RealmDb.getInstance().getBirthday(intent.getStringExtra(Constants.INTENT_ID));
        Intent notificationIntent = ShowBirthdayActivity.getLaunchIntent(getApplicationContext(),
                intent.getStringExtra(Constants.INTENT_ID));
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true);
        startActivity(notificationIntent);
        endService(reminder.getUniqueId());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void hidePermanent(String id) {
        BirthdayItem item = RealmDb.getInstance().getBirthday(id);
        updateBirthday(item);
        endService(item.getUniqueId());
    }

    private void endService(int id) {
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.cancel(id);
        stopSelf();
    }
}
