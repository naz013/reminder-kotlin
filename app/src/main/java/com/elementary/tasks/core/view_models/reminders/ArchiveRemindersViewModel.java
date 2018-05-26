package com.elementary.tasks.core.view_models.reminders;

import android.app.Application;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.reminder.work.DeleteFilesAsync;

import java.util.List;

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
public class ArchiveRemindersViewModel extends BaseRemindersViewModel {

    public LiveData<List<Reminder>> events;
    public LiveData<List<Group>> groups;

    public ArchiveRemindersViewModel(Application application) {
        super(application);
        events = getAppDb().reminderDao().loadType(false, false);
        groups = getAppDb().groupDao().loadAll();
    }

    public void deleteAll(List<Reminder> data) {
        isInProgress.postValue(true);
        run(() -> {
            for (Reminder reminder : data) EventControlFactory.getController(reminder).stop();
            getAppDb().reminderDao().deleteAll(data.toArray(new Reminder[0]));
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
                Toast.makeText(getApplication(), R.string.trash_cleared, Toast.LENGTH_SHORT).show();
            });
            new DeleteFilesAsync(getApplication()).execute(data.stream().map(Reminder::getUuId).toArray(String[]::new));
        });
    }
}
