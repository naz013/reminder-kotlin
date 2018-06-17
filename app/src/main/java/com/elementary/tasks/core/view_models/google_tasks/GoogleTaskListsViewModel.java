package com.elementary.tasks.core.view_models.google_tasks;

import android.app.Application;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.view_models.Commands;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.lifecycle.LiveData;

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
public class GoogleTaskListsViewModel extends BaseTaskListsViewModel {

    public LiveData<List<GoogleTaskList>> googleTaskLists;

    public GoogleTaskListsViewModel(Application application) {
        super(application);
        googleTaskLists = getAppDb().googleTaskListsDao().loadAll();
    }

    public void sync() {
        Google mGoogle = Google.getInstance(getApplication());
        if (mGoogle == null || mGoogle.getTasks() == null) {
            return;
        }
        boolean isConnected = SuperUtil.isConnected(getApplication());
        if (!isConnected) {
            result.postValue(Commands.FAILED);
            return;
        }
        isInProgress.postValue(true);
        run(() -> {
            TaskLists lists = null;
            try {
                lists = mGoogle.getTasks().getTaskLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (lists != null && lists.size() > 0 && lists.getItems() != null) {
                for (TaskList item : lists.getItems()) {
                    String listId = item.getId();
                    GoogleTaskList taskList = getAppDb().googleTaskListsDao().getById(listId);
                    if (taskList != null) {
                        taskList.update(item);
                    } else {
                        Random r = new Random();
                        int color = r.nextInt(15);
                        taskList = new GoogleTaskList(item, color);
                    }
                    getAppDb().googleTaskListsDao().insert(taskList);
                    List<Task> tasks = mGoogle.getTasks().getTasks(listId);
                    if (tasks.isEmpty()) {
                        end(() -> {
                            isInProgress.postValue(false);
                            result.postValue(Commands.UPDATED);
                            UpdatesHelper.getInstance(getApplication()).updateTasksWidget();
                        });
                    } else {
                        List<GoogleTask> googleTasks = new ArrayList<>();
                        for (Task task : tasks) {
                            GoogleTask googleTask = getAppDb().googleTasksDao().getById(task.getId());
                            if (googleTask != null) {
                                googleTask.setListId(listId);
                                googleTask.update(task);
                            } else {
                                googleTask = new GoogleTask(task, listId);
                            }
                            googleTasks.add(googleTask);
                        }
                        getAppDb().googleTasksDao().insertAll(googleTasks);
                        end(() -> {
                            isInProgress.postValue(false);
                            result.postValue(Commands.UPDATED);
                            UpdatesHelper.getInstance(getApplication()).updateTasksWidget();
                        });
                    }
                }
            }
        });
    }

    public void reload() {

    }

    public void clearList(GoogleTaskList googleTaskList) {
        Google mGoogle = Google.getInstance(getApplication());
        if (mGoogle == null || mGoogle.getTasks() == null) {
            return;
        }
        boolean isConnected = SuperUtil.isConnected(getApplication());
        if (!isConnected) {
            result.postValue(Commands.FAILED);
        } else {
            isInProgress.postValue(true);
            run(() -> {
                List<GoogleTask> googleTasks = getAppDb().googleTasksDao().getAllByList(googleTaskList.getListId(), Google.TASKS_COMPLETE);
                getAppDb().googleTasksDao().deleteAll(googleTasks);
                mGoogle.getTasks().clearTaskList(googleTaskList.getListId());
                end(() -> {
                    isInProgress.postValue(false);
                    result.postValue(Commands.UPDATED);
                    UpdatesHelper.getInstance(getApplication()).updateTasksWidget();
                });
            });
        }
    }
}
