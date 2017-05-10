package com.elementary.tasks.core.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.reminder.ReminderDialogActivity;
import com.elementary.tasks.reminder.ReminderUpdateEvent;
import com.elementary.tasks.reminder.models.Reminder;

import org.greenrobot.eventbus.EventBus;

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

public class ReminderActionService extends Service {

    public static final String ACTION_SHOW = "com.elementary.tasks.reminder.SHOW";
    public static final String ACTION_HIDE = "com.elementary.tasks.reminder.HIDE";

    private static final String TAG = "ReminderActionService";

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onStartCommand: " + action);
            if (action != null && action.matches(ACTION_HIDE)) {
                hidePermanent(intent.getStringExtra(Constants.INTENT_ID));
            } else {
                showReminder(intent);
            }
        }
        return START_STICKY;
    }

    private void showReminder(Intent intent) {
        Reminder reminder = RealmDb.getInstance().getReminder(intent.getStringExtra(Constants.INTENT_ID));
        Intent notificationIntent = ReminderDialogActivity.getLaunchIntent(getApplicationContext(),
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
        Reminder reminder = RealmDb.getInstance().getReminder(id);
        EventControl control = EventControlFactory.getController(getApplicationContext(), reminder);
        control.next();
        EventBus.getDefault().post(new ReminderUpdateEvent());
        endService(reminder.getUniqueId());
    }

    private void endService(int id) {
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.cancel(id);
        stopSelf();
    }
}
