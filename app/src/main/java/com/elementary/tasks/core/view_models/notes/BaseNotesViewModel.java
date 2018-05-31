package com.elementary.tasks.core.view_models.notes;

import android.app.Application;

import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.notes.work.DeleteNoteFilesAsync;
import com.elementary.tasks.reminder.work.DeleteFilesAsync;

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
abstract class BaseNotesViewModel extends BaseDbViewModel {

    BaseNotesViewModel(Application application) {
        super(application);
    }

    public void deleteNote(@NonNull Note note) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().notesDao().delete(note);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
            });
            new DeleteNoteFilesAsync(getApplication()).execute(note.getKey());
        });
    }

    public void saveNote(@NonNull Note note) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().notesDao().insert(note);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }

    public void deleteReminder(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            EventControlFactory.getController(reminder).stop();
            getAppDb().reminderDao().delete(reminder);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.UPDATED);
            });
            CalendarUtils.deleteEvents(getApplication(), reminder.getUniqueId());
            new DeleteFilesAsync(getApplication()).execute(reminder.getUuId());
        });
    }
}
