package com.elementary.tasks.home.scheduleview

import androidx.lifecycle.MediatorLiveData
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.getNonNullList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReminderGoogleTaskLiveData(
  private val dispatcherProvider: DispatcherProvider,
  googleTaskListsDao: GoogleTaskListsDao,
  googleTasksDao: GoogleTasksDao,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter
) : MediatorLiveData<Map<String, UiGoogleTaskList>>() {

  private val googleTaskListsSource = googleTaskListsDao.loadAll()
  private val googleTasksSource = googleTasksDao.loadAttachedToReminder()

  private var transformJob: Job? = null
  private val scope = CoroutineScope(Job())

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
