package com.elementary.tasks.core.cloud;

import android.content.Context;
import android.text.TextUtils;

import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TaskListItem;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.Data;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

public class GoogleTasks extends GoogleDrive {

    private static final String TAG = "GoogleTasks";
    public static final String TASKS_NEED_ACTION = "needsAction";
    public static final String TASKS_COMPLETE = "completed";

    private Tasks service;

    public GoogleTasks(Context context) {
        super(context);
    }

    public boolean authorizeTasks() {
        if (service != null) return true;
        String user = SuperUtil.decrypt(Prefs.getInstance(mContext).getDriveUser());
        if (user.matches(".*@.*")) {
            GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(TasksScopes.TASKS));
            mCredential.setSelectedAccountName(user);
            service = new Tasks.Builder(mTransport, mJsonFactory, mCredential).setApplicationName(APPLICATION_NAME).build();
            return true;
        }
        return false;
    }

    public boolean insertTask(TaskItem item) throws IOException {
        if (authorize()) {
            Task task = new Task();
            task.setTitle(item.getTitle());
            if (item.getNotes() != null) task.setNotes(item.getNotes());
            if (item.getDueDate() != 0) {
                task.setDue(new DateTime(item.getDueDate()));
            }
            Task result;
            String listId = item.getListId();
            if (!TextUtils.isEmpty(listId)) {
                result = service.tasks().insert(listId, task).execute();
            } else {
                TaskListItem taskListItem = RealmDb.getInstance().getDefaultTaskList();
                if (taskListItem != null) {
                    result = service.tasks().insert(taskListItem.getListId(), task).execute();
                } else {
                    result = service.tasks().insert("@default", task).execute();
                }
            }
            if (result != null) {
                item.update(result);
                RealmDb.getInstance().saveObject(item);
                return true;
            }
        }
        return false;
    }

    public void updateTaskStatus(String status, String listId, String taskId) throws IOException {
        if (authorize() && taskId != null && listId != null) {
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(status);
            if (status.matches(TASKS_NEED_ACTION)) {
                task.setCompleted(Data.NULL_DATE_TIME);
            }
            task.setUpdated(new DateTime(System.currentTimeMillis()));
            service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    public void deleteTask(TaskItem item) throws IOException {
        if (authorize() && item != null) {
            service.tasks().delete(item.getListId(), item.getTaskId()).execute();
        }
    }

    public void updateTask(TaskItem item) throws IOException {
        if (authorize() && item != null) {
            Task task = service.tasks().get(item.getListId(), item.getTaskId()).execute();
            task.setStatus(TASKS_NEED_ACTION);
            task.setTitle(item.getTitle());
            task.setCompleted(Data.NULL_DATE_TIME);
            if (item.getDueDate() != 0) task.setDue(new DateTime(item.getDueDate()));
            if (item.getNotes() != null) task.setNotes(item.getNotes());
            task.setUpdated(new DateTime(System.currentTimeMillis()));
            service.tasks().update(item.getListId(), task.getId(), task).execute();
        }
    }

    public List<Task> getTasks(String listId) {
        List<Task> taskLists = new ArrayList<>();
        if (authorize() && listId != null) {
            try {
                taskLists = service.tasks().list(listId).execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return taskLists;
    }

    public TaskLists getTaskLists() throws IOException {
        TaskLists taskLists = null;
        if (authorize()) {
            taskLists = service.tasklists().list().execute();
        }
        return taskLists;
    }

    public void insertTasksList(String listTitle, int color) {
        if (authorize()) {
            TaskList taskList = new TaskList();
            taskList.setTitle(listTitle);
            try {
                TaskList result = service.tasklists().insert(taskList).execute();
                TaskListItem item = new TaskListItem(result, color);
                RealmDb.getInstance().saveObject(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTasksList(final String listTitle, final String listId) throws IOException {
        if (authorize()) {
            TaskList taskList = service.tasklists().get(listId).execute();
            taskList.setTitle(listTitle);
            service.tasklists().update(listId, taskList).execute();
            TaskListItem item = RealmDb.getInstance().getTaskList(listId);
            item.update(taskList);
            RealmDb.getInstance().saveObject(item);
        }
    }

    public void deleteTaskList(final String listId) {
        if (authorize()) {
            try {
                service.tasklists().delete(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearTaskList(final String listId) {
        if (authorize()) {
            try {
                service.tasks().clear(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean moveTask(TaskItem item, String oldList) {
        if (authorize()) {
            try {
                Task task = service.tasks().get(oldList, item.getTaskId()).execute();
                if (task != null) {
                    TaskItem clone = new TaskItem(item);
                    clone.setListId(oldList);
                    deleteTask(clone);
                    return insertTask(item);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
