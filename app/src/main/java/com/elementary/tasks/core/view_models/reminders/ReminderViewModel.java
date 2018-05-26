package com.elementary.tasks.core.view_models.reminders;

import android.app.Application;

import com.elementary.tasks.core.data.models.Reminder;

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
public class ReminderViewModel extends BaseRemindersViewModel {

    public LiveData<Reminder> reminder;

    private ReminderViewModel(Application application, int id) {
        super(application);
        reminder = getAppDb().reminderDao().loadById(id);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private Application application;
        private int id;

        public Factory(Application application, int id) {
            this.application = application;
            this.id = id;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new ReminderViewModel(application, id);
        }
    }
}
