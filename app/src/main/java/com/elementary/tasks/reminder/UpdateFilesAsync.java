package com.elementary.tasks.reminder;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.reminder.models.Reminder;

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

public class UpdateFilesAsync extends AsyncTask<Reminder, Void, Void> {

    private boolean isConnected;
    private Dropbox dropbox;
    private Google google;

    public UpdateFilesAsync(Context context) {
        isConnected = SuperUtil.isConnected(context);
        dropbox = new Dropbox(context);
        google = Google.getInstance(context);
    }

    @Override
    protected Void doInBackground(Reminder... params) {
        for (Reminder reminder : params) {
            String path = BackupTool.getInstance().exportReminder(reminder);
            if (isConnected) {
                dropbox.uploadReminderByFileName(path);
                if (google != null && google.getDrive() != null && path != null) {
                    google.getDrive().saveReminderToDrive(path);
                }
            }
        }
        return null;
    }
}
