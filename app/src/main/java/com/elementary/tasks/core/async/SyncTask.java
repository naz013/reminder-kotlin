package com.elementary.tasks.core.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.IoHelper;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.List;

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

public class SyncTask extends AsyncTask<Void, String, Boolean> {

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private SyncListener mListener;
    private boolean quiet = false;

    public SyncTask(Context context, SyncListener mListener, boolean quiet){
        this.mContext = context;
        this.mListener = mListener;
        this.quiet = quiet;
        builder = new NotificationCompat.Builder(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!quiet) {
            builder.setContentTitle(Module.isPro() ? mContext.getString(R.string.app_name_pro) :
                    mContext.getString(R.string.app_name));
            builder.setContentText(mContext.getString(R.string.sync));
            builder.setSmallIcon(R.drawable.ic_cached_white_24dp);
            mNotifyMgr = NotificationManagerCompat.from(mContext);
            mNotifyMgr.notify(2, builder.build());
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (!quiet) {
            builder.setContentTitle(values[0]);
            builder.setWhen(System.currentTimeMillis());
            mNotifyMgr.notify(2, builder.build());
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        IoHelper ioHelper = new IoHelper(mContext);
        ioHelper.restoreGroup(true, true);
        ioHelper.backupGroup(true);
        List<GroupItem> list = RealmDb.getInstance().getAllGroups();
        if (list.size() == 0) {
            String defUiID = RealmDb.getInstance().setDefaultGroups(mContext);
            List<Reminder> items = RealmDb.getInstance().getAllRemindera();
            for (Reminder item : items) {
                item.setGroupUuId(defUiID);
                RealmDb.getInstance().saveObject(item);
            }
        }
        //export & import reminders
        publishProgress(mContext.getString(R.string.syncing_reminders));
        ioHelper.restoreReminder(true, true);
        ioHelper.backupReminder(true);

        //export & import notes
        publishProgress(mContext.getString(R.string.syncing_notes));
        ioHelper.restoreNote(true, true);
        ioHelper.backupNote(true);

        //export & import birthdays
        publishProgress(mContext.getString(R.string.syncing_birthdays));
        ioHelper.restoreBirthday(true, true);
        ioHelper.backupBirthday(true);

        //export & import places
        publishProgress(mContext.getString(R.string.syncing_places));
        ioHelper.restorePlaces(true, true);
        ioHelper.backupPlaces(true);

        //export & import templates
        publishProgress(mContext.getString(R.string.syncing_templates));
        ioHelper.restoreTemplates(true, true);
        ioHelper.backupTemplates(true);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        if (!quiet) {
            builder.setContentTitle(mContext.getString(R.string.done));
            builder.setSmallIcon(R.drawable.ic_done_white_24dp);
            if (Module.isPro()) {
                builder.setContentText(mContext.getString(R.string.app_name_pro));
            } else builder.setContentText(mContext.getString(R.string.app_name));
            builder.setWhen(System.currentTimeMillis());
            mNotifyMgr.notify(2, builder.build());
            if (mListener != null && mContext != null) {
                mListener.endExecution(aVoid);
            }
        }
        if (mContext != null) {
//            UpdatesHelper.getInstance(mContext).updateWidget();
//            UpdatesHelper.getInstance(mContext).updateNotesWidget();
        }
    }

    public interface SyncListener {
        void endExecution(boolean b);
    }
}
