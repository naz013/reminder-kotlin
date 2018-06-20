package com.elementary.tasks.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.data.dao.ReminderDao;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.ContextHolder;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.groups.GroupsUtil;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Copyright 2018 Nazar Suhovich
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
public class RestoreLocalTask extends AsyncTask<Void, String, Integer> {

    private ContextHolder mContext;
    @NonNull
    private RestoreLocalTask.SyncListener mListener;
    private ProgressDialog mDialog;

    RestoreLocalTask(@NonNull Context context, @NonNull RestoreLocalTask.SyncListener listener) {
        this.mContext = new ContextHolder(context);
        this.mListener = listener;
        this.mDialog = new ProgressDialog(context);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        try {
            ProgressDialog dialog = this.mDialog;
            if (dialog != null) {
                dialog.setTitle(mContext.getContext().getString(R.string.sync));
                dialog.setMessage(mContext.getContext().getString(R.string.please_wait));
                dialog.show();
                this.mDialog = dialog;
            }
        } catch (Exception var2) {
            this.mDialog = null;
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        mDialog.setMessage(values[0]);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @NonNull
    protected Integer doInBackground(@NonNull Void... p0) {
        publishProgress(mContext.getContext().getString(R.string.syncing_groups));
        try {
            BackupTool.getInstance().importGroups();
        } catch (IOException ignored) { }

        List<Group> list = AppDb.getAppDatabase(mContext.getContext()).groupDao().getAll();
        if (list.size() == 0) {
            String defUiID = GroupsUtil.initDefault(mContext.getContext());
            List<Reminder> items = AppDb.getAppDatabase(mContext.getContext()).reminderDao().getAll();
            ReminderDao dao = AppDb.getAppDatabase(mContext.getContext()).reminderDao();
            for (Reminder item : items) {
                item.setGroupUuId(defUiID);
                dao.insert(item);
            }
        }

        this.publishProgress(mContext.getContext().getString(R.string.syncing_reminders));
        try {
            BackupTool.getInstance().importReminders(this.mContext.getContext());
        } catch (IOException ignored) { }
        this.publishProgress(mContext.getContext().getString(R.string.syncing_notes));
        try {
            BackupTool.getInstance().importNotes();
        } catch (IOException ignored) { }
        this.publishProgress(mContext.getContext().getString(R.string.syncing_birthdays));
        try {
            BackupTool.getInstance().importBirthdays();
        } catch (IOException ignored) { }
        publishProgress(mContext.getContext().getString(R.string.syncing_places));
        try {
            BackupTool.getInstance().importPlaces();
        } catch (IOException ignored) { }
        publishProgress(mContext.getContext().getString(R.string.syncing_templates));
        try {
            BackupTool.getInstance().importTemplates();
        } catch (IOException ignored) { }
        Prefs.getInstance(this.mContext.getContext()).loadPrefsFromFile();
        return 1;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            LogUtil.d("RestoreLocalTask", "onPostExecute: " + e.getLocalizedMessage());
        }
        UpdatesHelper.getInstance(mContext.getContext()).updateWidget();
        UpdatesHelper.getInstance(mContext.getContext()).updateNotesWidget();
        mListener.onFinish();
    }

    public interface SyncListener {
        void onFinish();
    }
}
