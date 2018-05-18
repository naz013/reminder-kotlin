package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.Nullable;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.RealmDb;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

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

public class GetTaskListAsync extends AsyncTask<Void, Void, Boolean> {

    @Nullable
    private TasksCallback mListener;
    @Nullable
    private Google mGoogle;
    @Nullable
    private WeakReference<UpdatesHelper> mHelper;

    public GetTaskListAsync(Context context, @Nullable TasksCallback listener) {
        this.mGoogle = Google.getInstance(context);
        this.mHelper = new WeakReference<>(UpdatesHelper.getInstance(context));
        this.mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (mGoogle != null && mGoogle.getTasks() != null) {
            TaskLists lists = null;
            try {
                lists = mGoogle.getTasks().getTaskLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (lists != null && lists.size() > 0 && lists.getItems() != null) {
                for (TaskList item : lists.getItems()) {
                    String listId = item.getId();
                    TaskListItem taskList = RealmDb.getInstance().getTaskList(listId);
                    if (taskList != null) {
                        taskList.update(item);
                    } else {
                        Random r = new Random();
                        int color = r.nextInt(15);
                        taskList = new TaskListItem(item, color);
                    }
                    RealmDb.getInstance().saveObject(taskList);
                    TaskListItem listItem = RealmDb.getInstance().getTaskLists().get(0);
                    RealmDb.getInstance().setDefault(listItem.getListId());
                    RealmDb.getInstance().setSystemDefault(listItem.getListId());
                    List<Task> tasks = mGoogle.getTasks().getTasks(listId);
                    if (tasks.isEmpty()) return false;
                    for (Task task : tasks) {
                        TaskItem taskItem = RealmDb.getInstance().getTask(task.getId());
                        if (taskItem != null) {
                            taskItem.update(task);
                            taskItem.setListId(task.getId());
                        } else {
                            taskItem = new TaskItem(task, listId);
                        }
                        RealmDb.getInstance().saveObject(taskItem);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        if (mHelper != null) {
            UpdatesHelper helper = mHelper.get();
            if (helper != null) helper.updateTasksWidget();
        }
        if (mListener != null) {
            if (aVoid) {
                mListener.onComplete();
            } else {
                mListener.onFailed();
            }
        }
    }
}
