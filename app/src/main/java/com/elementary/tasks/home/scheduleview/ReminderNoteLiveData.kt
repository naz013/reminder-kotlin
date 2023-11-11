package com.elementary.tasks.home.scheduleview

import androidx.lifecycle.MediatorLiveData
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReminderNoteLiveData(
  private val dispatcherProvider: DispatcherProvider,
  notesDao: NotesDao,
  private val uiNoteListAdapter: UiNoteListAdapter,
  reminders: List<Reminder>
) : MediatorLiveData<Map<String, UiNoteList>>() {

  private val noteIds = reminders.filter { it.noteId.isNotEmpty() }
    .map { it.noteId }
  private val source = notesDao.loadByIds(noteIds)

  private var transformJob: Job? = null
  private val scope = CoroutineScope(Job())

  override fun onActive() {
    super.onActive()
    addSource(source) { transform(it) }
  }

  override fun onInactive() {
    super.onInactive()
    removeSource(source)
  }

  private fun transform(notes: List<NoteWithImages>) {
    transformJob?.cancel()

    if (notes.isEmpty()) {
      postValue(emptyMap())
      return
    }

    transformJob = scope.launch(dispatcherProvider.default()) {
      mapNotes(notes.map { uiNoteListAdapter.convert(it) })
        .also { postValue(it) }
    }
  }

  private fun mapNotes(list: List<UiNoteList>): Map<String, UiNoteList> {
    val map = mutableMapOf<String, UiNoteList>()
    list.forEach { map[it.id] = it }
    return map
  }
}
