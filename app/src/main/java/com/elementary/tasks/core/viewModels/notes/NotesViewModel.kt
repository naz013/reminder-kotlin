package com.elementary.tasks.core.viewModels.notes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class NotesViewModel(application: Application) : BaseNotesViewModel(application) {

    var notes: LiveData<List<Note>>

    init {
        notes = appDb.notesDao().loadAll()
    }

    fun deleteAll(list: List<Note>) {
        launch(CommonPool) {
            val ids = ArrayList<String>()
            for (item in list) {
                ids.add(item.key)
            }
            appDb.notesDao().delete(list)
            val work = OneTimeWorkRequest.Builder(DeleteNoteBackupWorker::class.java)
                    .setInputData(Data.Builder().putStringArray(Constants.INTENT_IDS, ids.toTypedArray()).build())
                    .addTag("NT_WORK")
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun reload() {

    }
}
