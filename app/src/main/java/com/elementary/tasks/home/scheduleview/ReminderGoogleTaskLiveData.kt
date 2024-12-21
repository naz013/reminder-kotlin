package com.elementary.tasks.home.scheduleview

import androidx.lifecycle.MediatorLiveData
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.getNonNullList
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReminderGoogleTaskLiveData(
  private val dispatcherProvider: DispatcherProvider,
  googleTaskListRepository: GoogleTaskListRepository,
  googleTaskRepository: GoogleTaskRepository,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter,
  tableChangeListenerFactory: TableChangeListenerFactory
) : MediatorLiveData<Map<String, UiGoogleTaskList>>() {

  private val scope = CoroutineScope(Job())
  private val googleTaskListsSource = scope.observeTable(
    table = Table.GoogleTaskList,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { googleTaskListRepository.getAll() }
  )
  private val googleTasksSource = scope.observeTable(
    table = Table.GoogleTask,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { googleTaskRepository.getAttachedToReminder() }
  )

  private var transformJob: Job? = null

  override fun onActive() {
    super.onActive()
    addSource(googleTaskListsSource) { transform(googleTaskLists = it) }
    addSource(googleTasksSource) { transform(googleTasks = it) }
  }

  override fun onInactive() {
    super.onInactive()
    removeSource(googleTasksSource)
    removeSource(googleTaskListsSource)
  }

  private fun transform(
    googleTaskLists: List<GoogleTaskList> = googleTaskListsSource.getNonNullList(),
    googleTasks: List<GoogleTask> = googleTasksSource.getNonNullList()
  ) {
    transformJob?.cancel()

    if (googleTasks.isEmpty()) {
      postValue(emptyMap())
      return
    }

    transformJob = scope.launch(dispatcherProvider.default()) {
      val map = mapTaskLists(googleTaskLists)

      googleTasks.map { uiGoogleTaskListAdapter.convert(it, map[it.listId]) }
        .filter { it.reminderId != null }
        .associateBy { it.reminderId!! }
        .also { postValue(it) }
    }
  }

  private fun mapTaskLists(list: List<GoogleTaskList>): Map<String, GoogleTaskList> {
    val map = mutableMapOf<String, GoogleTaskList>()
    list.forEach { map[it.listId] = it }
    return map
  }
}
