package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NoteViewModel private constructor(key: String) : BaseNotesViewModel() {

    val note = appDb.notesDao().loadById(key)
    val reminder = appDb.reminderDao().loadByNoteKey(if (key == "") "1" else key)

    class Factory(private val key: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(key) as T
        }
    }
}
