package com.elementary.tasks.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.models.Reminder;

import java.io.IOException;
import java.util.List;

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

public class RestoreGoogleTask extends AsyncTask<Void, String, Void> {

    private Context mContext;
    private SyncListener mListener;
    private ProgressDialog mDialog;

    public RestoreGoogleTask(Context context, SyncListener mListener) {
        this.mContext = context;
        this.mListener = mListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setTitle(mContext.getString(R.string.sync));
        mDialog.setCancelable(false);
        mDialog.setMessage(mContext.getString(R.string.please_wait));
        mDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        mDialog.setMessage(values[0]);
        mDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Google drive = Google.getInstance(mContext);
        if (drive != null) {
            publishProgress(mContext.getString(R.string.syncing_groups));
            try {
                drive.getDrive().downloadGroups(false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
            List<GroupItem> list = RealmDb.getInstance().getAllGroups();
            if (list.size() == 0) {
                String defUiID = RealmDb.getInstance().setDefaultGroups(mContext);
                List<Reminder> items = RealmDb.getInstance().getAllReminders();
                for (Reminder item : items) {
                    item.setGroupUuId(defUiID);
                    RealmDb.getInstance().saveObject(item);
                }
            }

            publishProgress(mContext.getString(R.string.syncing_reminders));
            try {
                drive.getDrive().downloadReminders(mContext, false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }

            //export & import notes
            publishProgress(mContext.getString(R.string.syncing_notes));
            try {
                drive.getDrive().downloadNotes(false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }

            //export & import birthdays
            publishProgress(mContext.getString(R.string.syncing_birthdays));
            try {
                drive.getDrive().downloadBirthdays(false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }

            //export & import places
            publishProgress(mContext.getString(R.string.syncing_places));
            try {
                drive.getDrive().downloadPlaces(false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }

            //export & import templates
            publishProgress(mContext.getString(R.string.syncing_templates));
            try {
                drive.getDrive().downloadTemplates(false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
            try {
                drive.getDrive().downloadSettings(mContext, false);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mContext != null) {
            UpdatesHelper.getInstance(mContext).updateWidget();
            UpdatesHelper.getInstance(mContext).updateNotesWidget();
        }
        if (mListener != null) {
            mListener.onFinish();
        }
    }

    public interface SyncListener {
        void onFinish();
    }
}
