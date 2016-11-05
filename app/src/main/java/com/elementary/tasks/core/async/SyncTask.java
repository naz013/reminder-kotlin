package com.elementary.tasks.core.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Module;

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
//        IOHelper ioHelper = new IOHelper(mContext);
//        ioHelper.restoreGroup(true, true);
//        ioHelper.backupGroup(true);
//        GroupHelper helper = GroupHelper.getInstance(mContext);
//        List<GroupItem> list = helper.getAll();
//        if (list.size() == 0) {
//            long time = System.currentTimeMillis();
//            String defUiID = SyncHelper.generateID();
//            helper.saveGroup(new GroupItem("General", defUiID, 5, 0, time));
//            helper.saveGroup(new GroupItem("Work", SyncHelper.generateID(), 3, 0, time));
//            helper.saveGroup(new GroupItem("Personal", SyncHelper.generateID(), 0, 0, time));
//            List<ReminderItem> items = ReminderHelper.getInstance(mContext).getAll();
//            for (ReminderItem item : items) {
//                item.setGroupId(defUiID);
//            }
//            ReminderHelper.getInstance(mContext).saveReminders(items);
//        }
//        //export & import reminders
//        publishProgress(mContext.getString(R.string.syncing_reminders));
//        ioHelper.restoreReminder(true, true);
//        ioHelper.backupReminder(true);
//
//        //export & import notes
//        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
//        if (prefs.getBoolean(Prefs.SYNC_NOTES)) {
//            publishProgress(mContext.getString(R.string.syncing_notes));
//            ioHelper.restoreNote(true, true);
//            ioHelper.backupNote(true);
//        }
//
//        //export & import birthdays
//        if (prefs.getBoolean(Prefs.SYNC_BIRTHDAYS)) {
//            publishProgress(mContext.getString(R.string.syncing_birthdays));
//            ioHelper.restoreBirthday(true, true);
//            ioHelper.backupBirthday(true);
//        }
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
