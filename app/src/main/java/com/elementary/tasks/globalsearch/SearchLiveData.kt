package com.elementary.tasks.globalsearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.elementary.tasks.core.data.observeTable
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryTarget
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.Note
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecentQueryRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchLiveData(
  private val recentQueryRepository: RecentQueryRepository,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val noteRepository: NoteRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val placeRepository: PlaceRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dispatcherProvider: DispatcherProvider,
  private val tableChangeListenerFactory: TableChangeListenerFactory
) : MediatorLiveData<List<SearchResult>>() {

  private val queryLiveDate = MutableLiveData<String>()

  private val recentSearchSource = queryLiveDate.switchMap {
    createRecentQueryLiveData(it)
  }
  private val reminderSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      createReminderLiveData(it)
    }
  }
  private val birthdaySearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      createBirthdayLiveData(it)
    }
  }
  private val noteSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      createNoteLiveData(it)
    }
  }
  private val googleTaskSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      createGoogleTaskLiveData(it)
    }
  }
  private val placeSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      createPlaceLiveData(it)
    }
  }
  private val groupSearchSource = queryLiveDate.switchMap {
    if (it.isBlank()) {
      emptyLiveData()
    } else {
      createGroupLiveData(it)
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
    return MutableLiveData()
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

  private fun createReminderLiveData(
    query: String
  ): LiveData<List<Reminder>> {
    return scope.observeTable(
      Table.Reminder,
      tableChangeListenerFactory,
      { reminderRepository.search(query) }
    )
  }

  private fun createNoteLiveData(
    query: String
  ): LiveData<List<Note>> {
    return scope.observeTable(
      Table.Note,
      tableChangeListenerFactory,
      { noteRepository.search(query) }
    )
  }

  private fun createGoogleTaskLiveData(
    query: String
  ): LiveData<List<GoogleTask>> {
    return scope.observeTable(
      Table.GoogleTask,
      tableChangeListenerFactory,
      { googleTaskRepository.search(query) }
    )
  }

  private fun createGroupLiveData(
    query: String
  ): LiveData<List<ReminderGroup>> {
    return scope.observeTable(
      Table.ReminderGroup,
      tableChangeListenerFactory,
      { reminderGroupRepository.search(query) }
    )
  }

  private fun createPlaceLiveData(
    query: String
  ): LiveData<List<Place>> {
    return scope.observeTable(
      Table.Place,
      tableChangeListenerFactory,
      { placeRepository.searchByName(query) }
    )
  }

  private fun createBirthdayLiveData(
    query: String
  ): LiveData<List<Birthday>> {
    return scope.observeTable(
      Table.Birthday,
      tableChangeListenerFactory,
      { birthdayRepository.searchByName(query) }
    )
  }

  private fun createRecentQueryLiveData(
    query: String
  ): LiveData<List<RecentQuery>> {
    return scope.observeTable(
      Table.RecentQuery,
      tableChangeListenerFactory,
      {
        if (query.isBlank()) {
          recentQueryRepository.getAll()
        } else {
          recentQueryRepository.search(query)
        }
      }
    )
  }
}
