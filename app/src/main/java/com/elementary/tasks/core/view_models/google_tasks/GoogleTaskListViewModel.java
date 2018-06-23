package com.elementary.tasks.core.view_models.google_tasks;

import android.app.Application;

import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.view_models.Commands;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
public class GoogleTaskListViewModel extends BaseTaskListsViewModel {

    public LiveData<GoogleTaskList> googleTaskList;
    public LiveData<GoogleTaskList> defaultTaskList;
    public LiveData<List<GoogleTask>> googleTasks;

    public GoogleTaskListViewModel(Application application, @Nullable String listId) {
        super(application);
        googleTaskList = getAppDb().googleTaskListsDao().loadById(listId);
        defaultTaskList = getAppDb().googleTaskListsDao().loadDefault();
        if (listId == null) {
            googleTasks = getAppDb().googleTasksDao().loadAll();
        } else {
            googleTasks = getAppDb().googleTasksDao().loadAllByList(listId);
        }
    }

    public void newGoogleTaskList(@NonNull GoogleTaskList googleTaskList) {
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
            google.getTasks().insertTasksList(googleTaskList.getTitle(), googleTaskList.getColor());
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }

    public void updateGoogleTaskList(@NonNull GoogleTaskList googleTaskList) {
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
            getAppDb().googleTaskListsDao().insert(googleTaskList);
            try {
                google.getTasks().updateTasksList(googleTaskList.getTitle(), googleTaskList.getListId());
                end(() -> {
                    isInProgress.postValue(false);
                    result.postValue(Commands.SAVED);
                });
            } catch (IOException e) {
                end(() -> {
                    isInProgress.postValue(false);
                    result.postValue(Commands.FAILED);
                });
            }
        });
    }

    public void saveLocalGoogleTaskList(@NonNull GoogleTaskList googleTaskList) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().googleTaskListsDao().insert(googleTaskList);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private Application application;
        @Nullable
        private String id;

        public Factory(Application application, @Nullable String id) {
            this.application = application;
            this.id = id;
        }

        @NonNull
        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new GoogleTaskListViewModel(application, id);
        }
    }
}
