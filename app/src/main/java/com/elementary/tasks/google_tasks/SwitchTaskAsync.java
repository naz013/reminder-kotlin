package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.SuperUtil;

import java.io.IOException;

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
class SwitchTaskAsync extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private String taskId, listId;
    private TasksCallback mListener;
    private boolean status;

    SwitchTaskAsync(Context context, String listId, String taskId, boolean status, TasksCallback listener) {
        this.mContext = context;
        this.listId = listId;
        this.taskId = taskId;
        this.status = status;
        this.mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Google helper = Google.getInstance(mContext);
        boolean isConnected = SuperUtil.isConnected(mContext);
        if (isConnected && helper != null) {
            try {
                if (status) {
                    helper.getTasks().updateTaskStatus(Google.TASKS_COMPLETE, listId, taskId);
                } else {
                    helper.getTasks().updateTaskStatus(Google.TASKS_NEED_ACTION, listId, taskId);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        UpdatesHelper.getInstance(mContext).updateTasksWidget();
        if (aVoid) {
            if (mListener != null) {
                mListener.onComplete();
            }
        } else {
            if (mListener != null) {
                mListener.onFailed();
            }
        }
    }
}
