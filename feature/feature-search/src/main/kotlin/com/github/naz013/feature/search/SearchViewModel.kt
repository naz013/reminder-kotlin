package com.github.naz013.feature.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.logic.search.GetRecentSearchQueriesUseCase
import com.github.naz013.logic.search.GlobalSearchUseCase
import com.github.naz013.logic.search.SaveRecentSearchQueryUseCase
import com.github.naz013.logic.search.SearchResult
import com.github.naz013.logic.search.SearchResultType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class SearchViewModel(
  private val globalSearchUseCase: GlobalSearchUseCase,
  private val saveRecentSearchQueryUseCase: SaveRecentSearchQueryUseCase,
  private val getRecentSearchQueriesUseCase: GetRecentSearchQueriesUseCase,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _state = MutableStateFlow(SearchScreenState())
  val state: StateFlow<SearchScreenState> = _state.asStateFlow()
  val event: LiveData<Event<SearchEvent>> field = mutableLiveEventOf()

  private val queryFlow = MutableStateFlow("")
  private var allResults: List<SearchResult> = emptyList()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.SEARCH))
    loadRecentQueries()
    viewModelScope.launch {
      queryFlow
        .debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
        .collectLatest { runSearch(it) }
    }
  }

  fun onQueryChanged(query: String) {
    _state.update { it.copy(query = query) }
    queryFlow.value = query
  }

  fun onRecentQueryClicked(query: String) {
    Logger.i(TAG, "On recent query clicked")
    onQueryChanged(query)
  }

  fun onFilterSelected(type: SearchResultType?) {
    _state.update { it.copy(selectedType = type).withResults(allResults) }
  }

  fun onResultClicked(result: SearchResult) {
    Logger.i(TAG, "On result clicked: type=${result.type}")
    saveCurrentQuery()
    analyticsEventSender.send(FeatureUsedEvent(Feature.GLOBAL_SEARCH))
    event.emit(SearchEvent.OpenResult(result))
  }

  fun onBackClicked() {
    event.emit(SearchEvent.MoveBack)
  }

  private fun loadRecentQueries() {
    viewModelScope.launch {
      val recent = getRecentSearchQueriesUseCase()
        .sortedByDescending { it.lastUsedAt }
        .map { it.queryText }
      _state.update { it.copy(recentQueries = recent) }
    }
  }

  private fun saveCurrentQuery() {
    val query = _state.value.query
    viewModelScope.launch {
      saveRecentSearchQueryUseCase(query)
      loadRecentQueries()
    }
  }

  private suspend fun runSearch(query: String) {
    if (query.trim().length < GlobalSearchUseCase.MIN_QUERY_LENGTH) {
      allResults = emptyList()
      _state.update {
        it.copy(isLoading = false, hasSearched = false, selectedType = null).withResults(emptyList())
      }
      return
    }
    _state.update { it.copy(isLoading = true) }
    allResults = globalSearchUseCase(query)
    _state.update {
      val selected = it.selectedType?.takeIf { type -> allResults.any { r -> r.type == type } }
      it.copy(isLoading = false, hasSearched = true, selectedType = selected).withResults(allResults)
    }
  }

  private fun SearchScreenState.withResults(results: List<SearchResult>): SearchScreenState {
    val grouped = results.groupBy { it.type }
    val orderedTypes = SearchResultType.entries.filter { it in grouped }
    return copy(
      chips = orderedTypes.map { SearchFilterChip(it, grouped.getValue(it).size) },
      sections = orderedTypes
        .filter { selectedType == null || it == selectedType }
        .map { SearchSection(it, grouped.getValue(it).size, grouped.getValue(it)) },
    )
  }

  sealed interface SearchEvent {
    data class OpenResult(val result: SearchResult) : SearchEvent
    data object MoveBack : SearchEvent
  }

  companion object {
    private const val TAG = "SearchViewModel"
    internal const val SEARCH_DEBOUNCE_MS = 300L
  }
}
