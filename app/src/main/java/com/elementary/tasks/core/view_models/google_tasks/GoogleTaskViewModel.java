package com.elementary.tasks.core.view_models.google_tasks;

import android.app.Application;

import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.view_models.Commands;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
public class GoogleTaskViewModel extends BaseTaskListsViewModel {

    public LiveData<GoogleTask> googleTask;
    public LiveData<GoogleTaskList> defaultTaskList;
    public LiveData<Group> defaultGroup;
    public LiveData<List<GoogleTaskList>> googleTaskLists;
    public MutableLiveData<Reminder> reminder = new MutableLiveData<>();

    public GoogleTaskViewModel(Application application, @Nullable String id) {
        super(application);
        googleTask = getAppDb().googleTasksDao().loadById(id);
        defaultTaskList = getAppDb().googleTaskListsDao().loadDefault();
        googleTaskLists = getAppDb().googleTaskListsDao().loadAll();
        defaultGroup = getAppDb().groupDao().loadDefault();
    }

    public void loadReminder(@Nullable String uuId) {
        isInProgress.postValue(true);
        run(() -> {
            Reminder reminderItem = getAppDb().reminderDao().getByUuId(uuId);
            end(() -> {
                reminder.postValue(reminderItem);
                isInProgress.postValue(false);
            });
        });
    }

    private void saveReminder(@Nullable Reminder reminder) {
        if (reminder != null) {
            getAppDb().reminderDao().insert(reminder);
            EventControlFactory.getController(reminder).start();
        }
    }

    public void deleteGoogleTask(@NonNull GoogleTask googleTask) {
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
            try {
                google.getTasks().deleteTask(googleTask);
                getAppDb().googleTasksDao().delete(googleTask);
                end(() -> {
                    isInProgress.postValue(false);
                    result.postValue(Commands.DELETED);
                });
            } catch (IOException e) {
                end(() -> {
                    isInProgress.postValue(false);
                    result.postValue(Commands.FAILED);
                });
            }
        });
    }

    public void newGoogleTask(@NonNull GoogleTask googleTask, @Nullable Reminder reminder) {
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
            try {
                google.getTasks().insertTask(googleTask);
                saveReminder(reminder);
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

    public void updateGoogleTask(@NonNull GoogleTask googleTask, @Nullable Reminder reminder) {
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
            getAppDb().googleTasksDao().insert(googleTask);
            try {
                google.getTasks().updateTask(googleTask);
                saveReminder(reminder);
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

    public void updateAndMoveGoogleTask(@NonNull GoogleTask googleTask, @NonNull String oldListId, @Nullable Reminder reminder) {
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
            getAppDb().googleTasksDao().insert(googleTask);
            try {
                google.getTasks().updateTask(googleTask);
                google.getTasks().moveTask(googleTask, oldListId);
                saveReminder(reminder);
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

    public void moveGoogleTask(@NonNull GoogleTask googleTask, @NonNull String oldListId) {
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
            getAppDb().googleTasksDao().insert(googleTask);
            google.getTasks().moveTask(googleTask, oldListId);
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
            return (T) new GoogleTaskViewModel(application, id);
        }
    }
}
