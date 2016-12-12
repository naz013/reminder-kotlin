package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.core.cloud.GoogleTasks;
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

public class TaskListAsync extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private TasksCallback mCallback;
    private String title, listId, taskType;
    private int color;

    public TaskListAsync(Context context, String title, int color, String listId, String taskType, TasksCallback mCallback) {
        this.mContext = context;
        this.title = title;
        this.color = color;
        this.listId = listId;
        this.taskType = taskType;
        this.mCallback = mCallback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        GoogleTasks helper = new GoogleTasks(mContext);
        boolean isConnected = SuperUtil.isConnected(mContext);
        if (taskType.matches(TasksConstants.UPDATE_TASK_LIST)) {
            if (isConnected) {
                try {
                    helper.updateTasksList(title, listId);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        } else if (taskType.matches(TasksConstants.INSERT_TASK_LIST)) {
            if (isConnected) {
                helper.insertTasksList(title, color);
                return true;
            } else {
                return false;
            }
        } else if (taskType.matches(TasksConstants.DELETE_TASK_LIST)) {
            if (isConnected) {
                helper.deleteTaskList(listId);
                return true;
            } else {
                return false;
            }
        } else if (taskType.matches(TasksConstants.CLEAR_TASK_LIST)) {
            if (isConnected) {
                helper.clearTaskList(listId);
                return true;
            } else {
                return false;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        // TODO: 12.12.2016 Add widget update
//        UpdatesHelper.getInstance(mContext).updateTasksWidget();
        if (aVoid) {
            if (mCallback != null) mCallback.onComplete();
        } else {
            if (mCallback != null) mCallback.onFailed();
        }
    }
}
