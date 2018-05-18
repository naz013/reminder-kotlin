package com.elementary.tasks.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;

import com.elementary.tasks.Actions;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.reminder.ReminderDialogActivity;
import com.elementary.tasks.reminder.ReminderUpdateEvent;
import com.elementary.tasks.reminder.models.Reminder;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

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
public class ReminderActionService extends BroadcastReceiver {

    public static final String ACTION_SHOW = Actions.Reminder.ACTION_SHOW_FULL;
    public static final String ACTION_HIDE = Actions.Reminder.ACTION_HIDE_SIMPLE;
    public static final String ACTION_RUN = Actions.Reminder.ACTION_RUN;

    private static final String TAG = "ReminderActionService";

    private void showReminder(Context context, String id) {
        Reminder reminder = RealmDb.getInstance().getReminder(id);
        if (reminder == null) return;
        Intent notificationIntent = ReminderDialogActivity.getLaunchIntent(context, id);
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true);
        context.startActivity(notificationIntent);
        endService(context, reminder.getUniqueId());
    }

    private void hidePermanent(Context context, String id) {
        Reminder reminder = RealmDb.getInstance().getReminder(id);
        if (reminder == null) return;
        EventControl control = EventControlFactory.getController(context, reminder);
        control.next();
        EventBus.getDefault().post(new ReminderUpdateEvent());
        endService(context, reminder.getUniqueId());
    }

    private void endService(Context context, int id) {
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        mNotifyMgr.cancel(id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onStartCommand: " + action);
            if (action != null) {
                if (action.matches(ACTION_HIDE)) {
                    hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID));
                } else if (action.matches(ACTION_RUN)) {
                    String id = intent.getStringExtra(Constants.INTENT_ID);
                    int windowType = Prefs.getInstance(context).getReminderType();
                    boolean ignore = Prefs.getInstance(context).isIgnoreWindowType();
                    Reminder reminder = RealmDb.getInstance().getReminder(id);
                    if (!ignore) {
                        if (reminder != null) {
                            windowType = reminder.getWindowType();
                        }
                    }
                    Timber.d("start: ignore -> %b, event -> %s", ignore, reminder);
                    if (windowType == 0) {
                        context.startActivity(ReminderDialogActivity.getLaunchIntent(context, id));
                    } else {
                        ReminderUtils.showSimpleReminder(context, id);
                    }
                } else {
                    showReminder(context, intent.getStringExtra(Constants.INTENT_ID));
                }
            }
        }
    }
}
