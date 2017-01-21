package com.elementary.tasks.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.elementary.tasks.core.utils.Prefs;

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

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TasksService.class));
        if (Prefs.getInstance(context).isBirthdayReminderEnabled()){
            new BirthdayAlarm().setAlarm(context);
        }
        if (Prefs.getInstance(context).isSbNotificationEnabled()){
            context.startService(new Intent(context, PermanentReminderService.class).setAction(PermanentReminderService.ACTION_SHOW));
        }
        if (Prefs.getInstance(context).isContactAutoCheckEnabled()){
            new BirthdayCheckAlarm().setAlarm(context);
        }
        if (Prefs.getInstance(context).isAutoEventsCheckEnabled()){
            new EventsCheckAlarm().setAlarm(context);
        }
        if (Prefs.getInstance(context).isAutoBackupEnabled()){
            new AutoSyncAlarm().setAlarm(context);
        }
        if (Prefs.getInstance(context).isBirthdayPermanentEnabled()){
            new BirthdayPermanentAlarm().setAlarm(context);
            context.startService(new Intent(context, PermanentBirthdayService.class).setAction(PermanentBirthdayService.ACTION_SHOW));
        }
//        if (Prefs.getInstance(context).isWearEnabled()) {
//            context.startService(new Intent(context, WearService.class));
//        }
    }
}
