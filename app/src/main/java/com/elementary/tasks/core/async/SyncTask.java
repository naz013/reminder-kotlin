package com.elementary.tasks.core.async;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.ContextHolder;
import com.elementary.tasks.core.utils.IoHelper;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.groups.GroupsUtil;

import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

    private ContextHolder mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private SyncListener mListener;
    private boolean quiet;

    public SyncTask(Context context, SyncListener mListener, boolean quiet) {
        this.mContext = new ContextHolder(context);
        this.mListener = mListener;
        this.quiet = quiet;
        builder = new NotificationCompat.Builder(context, Notifier.CHANNEL_SYSTEM);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!quiet) {
            builder.setContentTitle(Module.isPro() ? mContext.getContext().getString(R.string.app_name_pro) :
                    mContext.getContext().getString(R.string.app_name));
            builder.setContentText(mContext.getContext().getString(R.string.sync));
            if (Module.isLollipop()) {
                builder.setSmallIcon(R.drawable.ic_cached_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_cached_nv_white);
            }
            mNotifyMgr = NotificationManagerCompat.from(mContext.getContext());
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
        IoHelper ioHelper = new IoHelper(mContext.getContext());
        publishProgress(mContext.getContext().getString(R.string.syncing_groups));
        ioHelper.restoreGroup(true);
        List<Group> list = AppDb.getAppDatabase(mContext.getContext()).groupDao().getAll();
        if (list.size() == 0) {
            String defUiID = GroupsUtil.initDefault(mContext.getContext());
            List<Reminder> items = AppDb.getAppDatabase(mContext.getContext()).reminderDao().getAll();
            for (Reminder item : items) {
                item.setGroupUuId(defUiID);
            }
            AppDb.getAppDatabase(mContext.getContext()).reminderDao().insertAll(items);
        }
        ioHelper.backupGroup();

        //export & import reminders
        publishProgress(mContext.getContext().getString(R.string.syncing_reminders));
        ioHelper.restoreReminder(true);
        ioHelper.backupReminder();

        //export & import notes
        publishProgress(mContext.getContext().getString(R.string.syncing_notes));
        ioHelper.restoreNote(true);
        ioHelper.backupNote();

        //export & import birthdays
        publishProgress(mContext.getContext().getString(R.string.syncing_birthdays));
        ioHelper.restoreBirthday(true);
        ioHelper.backupBirthday();

        //export & import places
        publishProgress(mContext.getContext().getString(R.string.syncing_places));
        ioHelper.restorePlaces(true);
        ioHelper.backupPlaces();

        //export & import templates
        publishProgress(mContext.getContext().getString(R.string.syncing_templates));
        ioHelper.restoreTemplates(true);
        ioHelper.backupTemplates();
        ioHelper.backupSettings();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        if (!quiet) {
            builder.setContentTitle(mContext.getContext().getString(R.string.done));
            if (Module.isLollipop()) {
                builder.setSmallIcon(R.drawable.ic_done_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_done_nv_white);
            }
            if (Module.isPro()) {
                builder.setContentText(mContext.getContext().getString(R.string.app_name_pro));
            } else {
                builder.setContentText(mContext.getContext().getString(R.string.app_name));
            }
            builder.setWhen(System.currentTimeMillis());
            mNotifyMgr.notify(2, builder.build());
            if (mListener != null && mContext != null) {
                mListener.endExecution(aVoid);
            }
        }
        if (mContext != null) {
            UpdatesHelper.getInstance(mContext.getContext()).updateWidget();
            UpdatesHelper.getInstance(mContext.getContext()).updateNotesWidget();
        }
    }

    public interface SyncListener {
        void endExecution(boolean b);
    }
}
