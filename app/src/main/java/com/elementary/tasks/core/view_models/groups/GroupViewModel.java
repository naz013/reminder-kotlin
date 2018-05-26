package com.elementary.tasks.core.view_models.groups;

import android.app.Application;

import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel;

import androidx.annotation.NonNull;
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
public class GroupViewModel extends BaseGroupsViewModel {

    public LiveData<Group> group;

    private GroupViewModel(Application application, String id) {
        super(application);
        group = getAppDb().groupDao().loadById(id);
    }

    public void saveGroup(@NonNull Group group) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().groupDao().insert(group);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private Application application;
        private String id;

        public Factory(Application application, String id) {
            this.application = application;
            this.id = id;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new GroupViewModel(application, id);
        }
    }
}
