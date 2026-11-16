package com.github.naz013.feature.search

import com.github.naz013.logic.search.SearchResult
import com.github.naz013.logic.search.SearchResultType

internal data class SearchSection(
  val type: SearchResultType,
  val count: Int,
  val items: List<SearchResult>,
)

internal data class SearchFilterChip(
  val type: SearchResultType,
  val count: Int,
)

internal data class SearchScreenState(
  val query: String = "",
  val recentQueries: List<String> = emptyList(),
  val chips: List<SearchFilterChip> = emptyList(),
  val selectedType: SearchResultType? = null,
  val sections: List<SearchSection> = emptyList(),
  val isLoading: Boolean = false,
  val hasSearched: Boolean = false,
) {
  val showRecent: Boolean
    get() = !hasSearched && !isLoading && query.isBlank()

  val showNoResults: Boolean
    get() = hasSearched && !isLoading && sections.isEmpty()
}
