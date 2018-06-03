package com.elementary.tasks.core.app_widgets.events;

import android.app.IntentService;
import android.content.Intent;

import com.elementary.tasks.birthdays.create_edit.AddBirthdayActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;

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

public class EventEditService extends IntentService {

    private static final String TAG = "EventEditService";
    public static final String TYPE = "type";

    public EventEditService() {
        super("EventEditService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String id = intent.getStringExtra(Constants.INTENT_ID);
        boolean isReminder = intent.getBooleanExtra(TYPE, true);
        LogUtil.d(TAG, "onHandleIntent: " + id + " isReminder " + isReminder);
        if (id != null) {
            if (isReminder) {
                startActivity(new Intent(getApplicationContext(), CreateReminderActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Constants.INTENT_ID, id));
            } else {
                startActivity(new Intent(getApplicationContext(), AddBirthdayActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Constants.INTENT_ID, id));
            }
        }
        stopSelf();
    }
}
