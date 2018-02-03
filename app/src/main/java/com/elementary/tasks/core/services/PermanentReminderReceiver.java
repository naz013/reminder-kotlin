package com.elementary.tasks.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;

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
public class PermanentReminderReceiver extends BroadcastReceiver {

    public static final int PERM_ID = 356664;
    public static final String ACTION_SHOW = "com.elementary.tasks.SHOW";
    public static final String ACTION_HIDE = "com.elementary.tasks.HIDE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Prefs.getInstance(context).isSbNotificationEnabled()) {
            Notifier.hideNotification(context, PERM_ID);
        }
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.matches(ACTION_SHOW)) {
                Notifier.showReminderPermanent(context);
            } else {
                Notifier.hideNotification(context, PERM_ID);
            }
        } else {
            Notifier.hideNotification(context, PERM_ID);
        }
    }
}
