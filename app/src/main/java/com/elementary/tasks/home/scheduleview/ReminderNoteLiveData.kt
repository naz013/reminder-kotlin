package com.elementary.tasks.home.scheduleview

import androidx.lifecycle.MediatorLiveData
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReminderNoteLiveData(
  private val dispatcherProvider: DispatcherProvider,
  noteRepository: NoteRepository,
  private val uiNoteListAdapter: UiNoteListAdapter,
  reminders: List<Reminder>,
  tableChangeListenerFactory: TableChangeListenerFactory
) : MediatorLiveData<Map<String, UiNoteList>>() {

  private val scope = CoroutineScope(Job())
  private val noteIds = reminders.filter { it.noteId.isNotEmpty() }
    .map { it.noteId }
  private val source = scope.observeTable(
    table = Table.Note,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { noteRepository.getByIds(noteIds) }
  )

  private var transformJob: Job? = null

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
