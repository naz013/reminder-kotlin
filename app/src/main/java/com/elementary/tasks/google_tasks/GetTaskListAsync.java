package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;

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
    private AppDb appDb;

    public GetTaskListAsync(Context context, @Nullable TasksCallback listener) {
        this.mGoogle = Google.getInstance(context);
        this.appDb = AppDb.getAppDatabase(context);
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
                    GoogleTaskList taskList = appDb.googleTaskListsDao().getById(listId);
                    if (taskList != null) {
                        taskList.update(item);
                    } else {
                        Random r = new Random();
                        int color = r.nextInt(15);
                        taskList = new GoogleTaskList(item, color);
                    }
                    appDb.googleTaskListsDao().insert(taskList);
                    GoogleTaskList listItem = appDb.googleTaskListsDao().getAll().get(0);
                    listItem.setDef(1);
                    listItem.setSystemDefault(1);
                    appDb.googleTaskListsDao().insert(listItem);
                    List<Task> tasks = mGoogle.getTasks().getTasks(listId);
                    if (tasks.isEmpty()) return false;
                    for (Task task : tasks) {
                        GoogleTask googleTask = appDb.googleTasksDao().getById(task.getId());
                        if (googleTask != null) {
                            googleTask.update(task);
                            googleTask.setListId(task.getId());
                        } else {
                            googleTask = new GoogleTask(task, listId);
                        }
                        appDb.googleTasksDao().insert(googleTask);
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
