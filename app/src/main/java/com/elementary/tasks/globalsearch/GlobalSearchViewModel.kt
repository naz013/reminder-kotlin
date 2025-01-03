package com.elementary.tasks.globalsearch

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.preview.PreviewBirthdayFragment
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.groups.create.CreateGroupActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.places.create.CreatePlaceActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryTarget
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RecentQueryRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class GlobalSearchViewModel(
  dispatcherProvider: DispatcherProvider,
  private val searchLiveData: SearchLiveData,
  private val recentQueryRepository: RecentQueryRepository,
  private val dateTimeManager: DateTimeManager
) : BaseProgressViewModel(dispatcherProvider) {

  val searchResults = searchLiveData.toLiveData()
  private val _navigateLiveData = mutableLiveDataOf<NavigationAction>()
  val navigateLiveData = _navigateLiveData.toSingleEvent()

  fun onQueryChanged(query: String) {
    searchLiveData.onNewQuery(query.lowercase())
  }

  fun onSearchHistoryUpdate(searchResult: SearchResult) {
    updateRecentQueries(searchResult)
  }

  fun onSearchResultClicked(searchResult: SearchResult) {
    Logger.logEvent("Search result clicked")
    createAction(searchResult)?.also {
      _navigateLiveData.postValue(it)
    }
    updateRecentQueries(searchResult)
  }

  private fun createAction(searchResult: SearchResult): NavigationAction? {
    return when (searchResult) {
      is ObjectSearchResult -> {
        searchResult.navigationAction()
      }

      is RecentObjectSearchResult -> {
        searchResult.navigationAction()
      }

      is RecentSearchResult -> null
    }
  }

  private fun RecentObjectSearchResult.navigationAction(): NavigationAction? {
    val clazz = objectType.toTargetClass()
    return if (clazz.isFragment()) {
      clazz.destinationId()?.let {
        FragmentNavigation(
          id = it,
          objectId = objectId
        )
      }
    } else {
      ActivityNavigation(
        clazz = clazz,
        objectId = objectId
      )
    }
  }

  private fun ObjectSearchResult.navigationAction(): NavigationAction? {
    val clazz = objectType.toTargetClass()
    return if (clazz.isFragment()) {
      clazz.destinationId()?.let {
        FragmentNavigation(
          id = it,
          objectId = objectId
        )
      }
    } else {
      ActivityNavigation(
        clazz = clazz,
        objectId = objectId
      )
    }
  }

  private fun Class<*>.isFragment(): Boolean {
    return this == PreviewBirthdayFragment::class.java
  }

  private fun Class<*>.destinationId(): Int? {
    return when {
      this == PreviewBirthdayFragment::class.java -> {
        R.id.previewBirthdayFragment
      }

      else -> null
    }
  }

  private fun updateRecentQueries(searchResult: SearchResult) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val recentQuery = searchResult.toRecentQuery(dateTimeManager.getCurrentDateTime())
      val similarQuery = recentQueryRepository.getByQuery(recentQuery.queryText)
      if (similarQuery != null) {
        recentQueryRepository.save(recentQuery.copy(id = similarQuery.id))
      } else {
        recentQueryRepository.save(recentQuery)
      }
    }
  }

  private fun SearchResult.toRecentQuery(dateTime: LocalDateTime): RecentQuery {
    return when (this) {
      is RecentSearchResult -> {
        RecentQuery(
          queryType = RecentQueryType.TEXT,
          queryText = text,
          lastUsedAt = dateTime,
          id = id
        )
      }

      is RecentObjectSearchResult -> {
        RecentQuery(
          queryType = RecentQueryType.OBJECT,
          queryText = text,
          lastUsedAt = dateTime,
          id = id,
          targetId = objectId,
          targetType = objectType.toTargetType()
        )
      }

      is ObjectSearchResult -> {
        RecentQuery(
          queryType = RecentQueryType.OBJECT,
          queryText = text,
          lastUsedAt = dateTime,
          targetId = objectId,
          targetType = objectType.toTargetType()
        )
      }
    }
  }

  private fun ObjectType.toTargetType(): RecentQueryTarget {
    return when (this) {
      ObjectType.GROUP -> RecentQueryTarget.GROUP
      ObjectType.PLACE -> RecentQueryTarget.PLACE
      ObjectType.GOOGLE_TASK -> RecentQueryTarget.GOOGLE_TASK
      ObjectType.NOTE -> RecentQueryTarget.NOTE
      ObjectType.BIRTHDAY -> RecentQueryTarget.BIRTHDAY
      ObjectType.REMINDER -> RecentQueryTarget.REMINDER
    }
  }

  private fun ObjectType.toTargetClass(): Class<*> {
    return when (this) {
      ObjectType.GROUP -> CreateGroupActivity::class.java
      ObjectType.PLACE -> CreatePlaceActivity::class.java
      ObjectType.GOOGLE_TASK -> GoogleTaskPreviewActivity::class.java
      ObjectType.NOTE -> NotePreviewActivity::class.java
      ObjectType.BIRTHDAY -> PreviewBirthdayFragment::class.java
      ObjectType.REMINDER -> ReminderPreviewActivity::class.java
    }
  }
}
