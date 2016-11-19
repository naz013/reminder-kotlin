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

public class TaskAsync extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private String title, listId, taskId, taskType, note, oldList;
    private long time;
    private TasksCallback mCallback;

    public TaskAsync(Context context, String title, String listId, String taskId, String taskType,
                     long time, String note, String oldList, TasksCallback callback){
        this.mContext = context;
        this.title = title;
        this.time = time;
        this.listId = listId;
        this.taskId = taskId;
        this.taskType = taskType;
        this.note = note;
        this.oldList = oldList;
        this.mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        GoogleTasks helper = new GoogleTasks(mContext);
        boolean isConnected = SuperUtil.isConnected(mContext);
        if (isConnected) {
            if (taskType.matches(TasksConstants.DELETE_TASK)){
                try {
                    helper.deleteTask(listId, taskId);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (taskType.matches(TasksConstants.MOVE_TASK)){
                return helper.moveTask(listId, taskId, oldList);
            } else if (taskType.matches(TasksConstants.UPDATE_TASK)){
                try {
                    helper.updateTask(title, listId, taskId, note, time);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (taskType.matches(TasksConstants.INSERT_TASK)){
                try {
                    return helper.insertTask(title, listId, time, note);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
//        UpdatesHelper.getInstance(mContext).updateTasksWidget();
        if (aVoid) {
            if (mCallback != null) mCallback.onComplete();
        } else {
            if (mCallback != null) mCallback.onFailed();
        }
    }
}
