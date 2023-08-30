package com.elementary.tasks.globalsearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.switchMap
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.RecentQueryDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.RecentQuery
import com.elementary.tasks.core.data.models.RecentQueryTarget
import com.elementary.tasks.core.data.models.RecentQueryType
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchLiveData(
  private val recentQueryDao: RecentQueryDao,
  private val reminderDao: ReminderDao,
  private val birthdaysDao: BirthdaysDao,
  private val notesDao: NotesDao,
  private val googleTasksDao: GoogleTasksDao,
  private val placesDao: PlacesDao,
  private val groupDao: ReminderGroupDao,
  private val dispatcherProvider: DispatcherProvider
) : MediatorLiveData<List<SearchResult>>() {

  private val queryLiveDate = mutableLiveDataOf<String>()

  private val recentSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      recentQueryDao.search()
    } else {
      recentQueryDao.search(it)
    }
  }
  private val reminderSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      reminderDao.search(it)
    }
  }
  private val birthdaySearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      birthdaysDao.search(it)
    }
  }
  private val noteSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      notesDao.search(it)
    }
  }
  private val googleTaskSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      googleTasksDao.search(it)
    }
  }
  private val placeSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      placesDao.search(it)
    }
  }
  private val groupSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      groupDao.search(it)
    }
  }

  private var transformJob: Job? = null
  private val scope = CoroutineScope(Job())

  override fun onActive() {
    super.onActive()
    addSource(recentSearchSource) { transform(recentQueries = it) }
    addSource(reminderSearchSource) { transform(reminders = it) }
    addSource(birthdaySearchSource) { transform(birthdays = it) }
    addSource(noteSearchSource) { transform(notes = it) }
    addSource(googleTaskSearchSource) { transform(googleTasks = it) }
    addSource(placeSearchSource) { transform(places = it) }
    addSource(groupSearchSource) { transform(groups = it) }
  }

  override fun onInactive() {
    super.onInactive()
    removeSource(recentSearchSource)
    removeSource(reminderSearchSource)
    removeSource(birthdaySearchSource)
    removeSource(noteSearchSource)
    removeSource(googleTaskSearchSource)
    removeSource(placeSearchSource)
    removeSource(groupSearchSource)
  }

  fun onNewQuery(query: String) {
    queryLiveDate.postValue(query)
  }

  private fun <T> getNonNullList(liveData: LiveData<List<T>>): List<T> {
    return liveData.value ?: emptyList()
  }

  private fun <T> emptyLiveData(): LiveData<T> {
    return mutableLiveDataOf()
  }

  private fun transform(
    recentQueries: List<RecentQuery> = getNonNullList(recentSearchSource),
    reminders: List<Reminder> = getNonNullList(reminderSearchSource),
    birthdays: List<Birthday> = getNonNullList(birthdaySearchSource),
    notes: List<Note> = getNonNullList(noteSearchSource),
    googleTasks: List<GoogleTask> = getNonNullList(googleTaskSearchSource),
    places: List<Place> = getNonNullList(placeSearchSource),
    groups: List<ReminderGroup> = getNonNullList(groupSearchSource)
  ) {
    val query = queryLiveDate.value ?: return
    transformJob?.cancel()
    transformJob = scope.launch(dispatcherProvider.default()) {
      val results = mutableListOf<SearchResult>()

      recentQueries.mapNotNull { it.toSearchResult(query) }.also { results.addAll(it) }

      if (query.isNotBlank()) {
        mutableListOf<ObjectSearchResult>().apply {
          addAll(reminders.map { it.toSearchResult(query) })
          addAll(birthdays.map { it.toSearchResult(query) })
          addAll(notes.map { it.toSearchResult(query) })
          addAll(googleTasks.map { it.toSearchResult(query) })
          addAll(places.map { it.toSearchResult(query) })
          addAll(groups.map { it.toSearchResult(query) })
        }
          .sortedBy { it.text.indexOf(query) }
          .also { results.addAll(it) }
      }

      postValue(results)
    }
  }

  private fun ReminderGroup.toSearchResult(query: String): ObjectSearchResult {
    return ObjectSearchResult(
      text = groupTitle,
      objectType = ObjectType.GROUP,
      objectId = groupUuId,
      query = query
    )
  }

  private fun Place.toSearchResult(query: String): ObjectSearchResult {
    return ObjectSearchResult(
      text = name,
      objectType = ObjectType.PLACE,
      objectId = id,
      query = query
    )
  }

  private fun GoogleTask.toSearchResult(query: String): ObjectSearchResult {
    return ObjectSearchResult(
      text = title,
      objectType = ObjectType.GOOGLE_TASK,
      objectId = taskId,
      query = query
    )
  }

  private fun Note.toSearchResult(query: String): ObjectSearchResult {
    return ObjectSearchResult(
      text = summary,
      objectType = ObjectType.NOTE,
      objectId = key,
      query = query
    )
  }

  private fun Birthday.toSearchResult(query: String): ObjectSearchResult {
    return ObjectSearchResult(
      text = name,
      objectType = ObjectType.BIRTHDAY,
      objectId = uuId,
      query = query
    )
  }

  private fun Reminder.toSearchResult(query: String): ObjectSearchResult {
    return ObjectSearchResult(
      text = summary,
      objectType = ObjectType.REMINDER,
      objectId = uuId,
      query = query
    )
  }

  private fun RecentQuery.toSearchResult(query: String): SearchResult? {
    return when (queryType) {
      RecentQueryType.TEXT -> {
        RecentSearchResult(queryText, id, query)
      }

      RecentQueryType.OBJECT -> {
        val objectType = targetType?.toObjectType() ?: return null
        val objectId = targetId ?: return null
        RecentObjectSearchResult(queryText, id, objectType, objectId, query)
      }
    }
  }

  private fun RecentQueryTarget.toObjectType(): ObjectType? {
    return when (this) {
      RecentQueryTarget.NONE -> null
      RecentQueryTarget.BIRTHDAY -> ObjectType.BIRTHDAY
      RecentQueryTarget.GOOGLE_TASK -> ObjectType.GOOGLE_TASK
      RecentQueryTarget.GROUP -> ObjectType.GROUP
      RecentQueryTarget.NOTE -> ObjectType.NOTE
      RecentQueryTarget.PLACE -> ObjectType.PLACE
      RecentQueryTarget.REMINDER -> ObjectType.REMINDER
      RecentQueryTarget.SCREEN -> null
    }
  }
}
