package com.elementary.tasks.core.view_models.notes;

import android.app.Application;

import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.notes.work.DeleteNoteFilesAsync;

import java.util.ArrayList;
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
public class NotesViewModel extends BaseNotesViewModel {

    public LiveData<List<Note>> notes;

    public NotesViewModel(Application application) {
        super(application);
        notes = getAppDb().notesDao().loadAll();
    }

    public void deleteAll(List<Note> list) {
        List<String> ids = new ArrayList<>();
        for (Note item : list) {
            ids.add(item.getKey());
        }
        getAppDb().notesDao().delete(list);
        new DeleteNoteFilesAsync(getApplication()).execute(ids.toArray(new String[0]));
    }

    public void reload() {

    }
}
