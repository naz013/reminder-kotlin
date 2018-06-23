package com.elementary.tasks.core.view_models.google_tasks;

import android.app.Application;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;

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
abstract class BaseTaskListsViewModel extends BaseDbViewModel {

    BaseTaskListsViewModel(Application application) {
        super(application);
    }

    public void deleteGoogleTaskList(@NonNull GoogleTaskList googleTaskList) {
        Google google = Google.getInstance(getApplication());
        if (google == null || google.getTasks() == null) {
            return;
        }
        boolean isConnected = SuperUtil.isConnected(getApplication());
        if (!isConnected) {
            result.postValue(Commands.FAILED);
            return;
        }
        isInProgress.postValue(true);
        run(() -> {
            int def = googleTaskList.getDef();
            google.getTasks().deleteTaskList(googleTaskList.getListId());
            getAppDb().googleTaskListsDao().delete(googleTaskList);
            getAppDb().googleTasksDao().deleteAll(googleTaskList.getListId());
            if (def == 1) {
                List<GoogleTaskList> lists = getAppDb().googleTaskListsDao().getAll();
                if (!lists.isEmpty()) {
                    GoogleTaskList taskList = lists.get(0);
                    taskList.setDef(1);
                    getAppDb().googleTaskListsDao().insert(taskList);
                }
            }
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
            });
        });
    }

    public void toggleTask(@NonNull GoogleTask googleTask) {
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
                try {
                    if (googleTask.getStatus().equals(Google.TASKS_NEED_ACTION)) {
                        mGoogle.getTasks().updateTaskStatus(Google.TASKS_COMPLETE, googleTask.getListId(), googleTask.getTaskId());
                    } else {
                        mGoogle.getTasks().updateTaskStatus(Google.TASKS_NEED_ACTION, googleTask.getListId(), googleTask.getTaskId());
                    }
                    end(() -> {
                        isInProgress.postValue(false);
                        result.postValue(Commands.UPDATED);
                        UpdatesHelper.getInstance(getApplication()).updateTasksWidget();
                    });
                } catch (IOException e) {
                    end(() -> {
                        isInProgress.postValue(false);
                        result.postValue(Commands.FAILED);
                    });
                }
            });
        }
    }
}
