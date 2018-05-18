package com.elementary.tasks.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;

import com.elementary.tasks.Actions;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.birthdays.ShowBirthdayActivity;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Notifier;
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
public class BirthdayActionService extends BroadcastReceiver {

    public static final String ACTION_SHOW = Actions.Birthday.ACTION_SHOW_FULL;
    public static final String ACTION_CALL = Actions.Birthday.ACTION_CALL;
    public static final String ACTION_SMS = Actions.Birthday.ACTION_SMS;

    private static final String TAG = "BirthdayActionService";

    public static Intent hide(Context context, String id) {
        return intent(context, id, PermanentBirthdayReceiver.ACTION_HIDE);
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

    private void updateBirthday(BirthdayItem item) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        item.setShowedYear(year);
        RealmDb.getInstance().saveObject(item);
    }

    private void sendSms(Context context, Intent intent) {
        BirthdayItem item = RealmDb.getInstance().getBirthday(intent.getStringExtra(Constants.INTENT_ID));
        if (item != null && Permissions.checkPermission(context, Permissions.SEND_SMS)) {
            TelephonyUtil.sendSms(item.getNumber(), context);
            updateBirthday(item);
            finish(context, item.getUniqueId());
        } else {
            hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID));
        }
    }

    private void makeCall(Context context, Intent intent) {
        BirthdayItem item = RealmDb.getInstance().getBirthday(intent.getStringExtra(Constants.INTENT_ID));
        if (item != null && Permissions.checkPermission(context, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(item.getNumber(), context);
            updateBirthday(item);
            finish(context, item.getUniqueId());
        } else {
            hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID));
        }
    }

    private void showReminder(Context context, Intent intent) {
        BirthdayItem reminder = RealmDb.getInstance().getBirthday(intent.getStringExtra(Constants.INTENT_ID));
        if (reminder != null) {
            Intent notificationIntent = ShowBirthdayActivity.getLaunchIntent(context,
                    intent.getStringExtra(Constants.INTENT_ID));
            notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true);
            context.startActivity(notificationIntent);
            Notifier.hideNotification(context, PermanentBirthdayReceiver.BIRTHDAY_PERM_ID);
        }
    }

    private void hidePermanent(Context context, @Nullable String id) {
        if (id == null) return;
        BirthdayItem item = RealmDb.getInstance().getBirthday(id);
        if (item != null) {
            updateBirthday(item);
            finish(context, item.getUniqueId());
        }
    }

    private static void finish(Context context, int id) {
        Notifier.hideNotification(context, id);
        UpdatesHelper.getInstance(context).updateWidget();
        UpdatesHelper.getInstance(context).updateCalendarWidget();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onStartCommand: " + action);
            if (action != null) {
                if (action.matches(ACTION_CALL)) {
                    makeCall(context, intent);
                } else if (action.matches(ACTION_SMS)) {
                    sendSms(context, intent);
                } else if (action.matches(PermanentBirthdayReceiver.ACTION_HIDE)) {
                    hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID));
                } else {
                    showReminder(context, intent);
                }
            }
        }
    }
}
