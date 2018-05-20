package com.elementary.tasks.core.view_models;

import android.app.Application;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.reminder.DeleteFilesAsync;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
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
public class ArchiveRemindersViewModel extends AndroidViewModel {

    @Inject
    private AppDb appDb;
    public LiveData<List<Reminder>> events;
    public LiveData<List<Group>> groups;

    public ArchiveRemindersViewModel(Application application) {
        super(application);
        ReminderApp.getAppComponent().inject(this);
        events = appDb.reminderDao().loadType(false, false);
        groups = appDb.groupDao().loadAll();
    }

    public void deleteAll(List<Reminder> data) {
        appDb.reminderDao().deleteAll(data.toArray(new Reminder[0]));
        new DeleteFilesAsync(getApplication()).execute(data.stream().map(Reminder::getUuId).toArray(String[]::new));
        Toast.makeText(getApplication(), R.string.trash_cleared, Toast.LENGTH_SHORT).show();
    }

    public void deleteReminder(Reminder reminder) {
        appDb.reminderDao().delete(reminder);
        CalendarUtils.deleteEvents(getApplication(), reminder.getUuId());
        new DeleteFilesAsync(getApplication()).execute(reminder.getUuId());
        Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show();
    }
}
