package com.elementary.tasks.core.cloud;

import android.content.Context;

import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TaskListItem;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
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

public class GoogleTasks {

    public static final String TASKS_NEED_ACTION = "needsAction";
    public static final String TASKS_COMPLETE = "completed";

    private Context mContext;

    private final HttpTransport m_transport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory m_jsonFactory = GsonFactory.getDefaultInstance();
    private Tasks service;
    private static final String APPLICATION_NAME = "Reminder/5.0";

    public GoogleTasks(Context context) {
        this.mContext = context;
    }

    public void authorize() {
        GoogleAccountCredential m_credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(TasksScopes.TASKS));
        m_credential.setSelectedAccountName(SuperUtil.decrypt(Prefs.getInstance(mContext).getDriveUser()));
        service = new Tasks.Builder(m_transport, m_jsonFactory, m_credential).setApplicationName(APPLICATION_NAME).build();
    }

    public boolean isLinked() {
        return SuperUtil.decrypt(Prefs.getInstance(mContext).getDriveUser()).matches(".*@.*");
    }

    public boolean insertTask(String taskTitle, String listId, long time, String note) throws IOException {
        if (isLinked()) {
            authorize();
            Task task = new Task();
            task.setTitle(taskTitle);
            if (note != null) task.setNotes(note);
            if (time != 0) task.setDue(new DateTime(time));
            Task result;
            if (listId != null && !listId.matches("")) {
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
                TaskItem taskItem = new TaskItem(result, listId);
                RealmDb.getInstance().saveTask(taskItem);
                return true;
            }
        }
        return false;
    }

    public void updateTaskStatus(String status, String listId, String taskId) throws IOException {
        if (isLinked() && taskId != null && listId != null) {
            authorize();
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(status);
            if (status.matches(TASKS_NEED_ACTION)) {
                task.setCompleted(Data.NULL_DATE_TIME);
            }
            task.setUpdated(new DateTime(System.currentTimeMillis()));
            service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    public void deleteTask(String listId, String taskId) throws IOException {
        if (isLinked() && taskId != null && listId != null) {
            authorize();
            service.tasks().delete(listId, taskId).execute();
        }
    }

    public void updateTask(String text, String listId, String taskId, String note, long time) throws IOException {
        if (isLinked() && taskId != null && listId != null) {
            authorize();
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(TASKS_NEED_ACTION);
            task.setTitle(text);
            task.setCompleted(Data.NULL_DATE_TIME);
            if (time != 0) task.setDue(new DateTime(time));
            if (note != null) task.setNotes(note);
            task.setUpdated(new DateTime(System.currentTimeMillis()));
            service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    public List<Task> getTasks(String listId) {
        List<Task> taskLists = new ArrayList<>();
        if (isLinked() && listId != null) {
            authorize();
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
        if (isLinked()) {
            authorize();
            taskLists = service.tasklists().list().execute();
        }
        return taskLists;
    }

    public void insertTasksList(String listTitle, int color) {
        if (isLinked()) {
            authorize();
            TaskList taskList = new TaskList();
            taskList.setTitle(listTitle);
            try {
                TaskList result = service.tasklists().insert(taskList).execute();
                TaskListItem item = new TaskListItem(result, color);
                RealmDb.getInstance().saveTaskList(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTasksList(final String listTitle, final String listId) throws IOException {
        if (isLinked()) {
            authorize();
            TaskList taskList = service.tasklists().get(listId).execute();
            taskList.setTitle(listTitle);
            service.tasklists().update(listId, taskList).execute();
            TaskListItem item = RealmDb.getInstance().getTaskList(listId);
            item.update(taskList);
            RealmDb.getInstance().saveTaskList(item);
        }
    }

    public void deleteTaskList(final String listId) {
        if (isLinked()) {
            authorize();
            try {
                service.tasklists().delete(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearTaskList(final String listId) {
        if (isLinked()) {
            authorize();
            try {
                service.tasks().clear(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean moveTask(String listId, String taskId, String oldList) {
        if (isLinked()) {
            authorize();
            try {
                Task task = service.tasks().get(oldList, taskId).execute();
                if (task != null) {
                    deleteTask(oldList, taskId);
                    DateTime dateTime = task.getDue();
                    long time = dateTime != null ? dateTime.getValue() : 0;
                    return insertTask(task.getTitle(), listId, time, task.getNotes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
