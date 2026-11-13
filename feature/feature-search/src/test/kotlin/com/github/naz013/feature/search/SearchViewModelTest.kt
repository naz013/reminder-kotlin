package com.github.naz013.feature.search

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.logic.search.GetRecentSearchQueriesUseCase
import com.github.naz013.logic.search.GlobalSearchUseCase
import com.github.naz013.logic.search.SaveRecentSearchQueryUseCase
import com.github.naz013.logic.search.SearchResult
import com.github.naz013.logic.search.SearchResultType
import com.github.naz013.testing.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : BaseTest() {
  private val globalSearchUseCase = mockk<GlobalSearchUseCase>()
  private val saveRecentSearchQueryUseCase = mockk<SaveRecentSearchQueryUseCase>(relaxed = true)
  private val getRecentSearchQueriesUseCase = mockk<GetRecentSearchQueriesUseCase>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private val reminder = SearchResult(SearchResultType.REMINDER, "r1", "Buy milk")
  private val reminder2 = SearchResult(SearchResultType.REMINDER, "r2", "Milk delivery")
  private val note = SearchResult(SearchResultType.NOTE, "n1", "Milk recipe")

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getRecentSearchQueriesUseCase() } returns emptyList()
    coEvery { globalSearchUseCase(any(), any()) } returns emptyList()
  }

  private fun createViewModel() = SearchViewModel(
    globalSearchUseCase,
    saveRecentSearchQueryUseCase,
    getRecentSearchQueriesUseCase,
    analyticsEventSender,
  )

  @Test
  fun `sends screen opened analytics event on creation`() {
    createViewModel()

    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.SEARCH)) }
  }

  @Test
  fun `shows recent queries newest first when query is empty`() {
    coEvery { getRecentSearchQueriesUseCase() } returns listOf(
      recent("old", LocalDateTime.of(2026, 1, 1, 10, 0)),
      recent("new", LocalDateTime.of(2026, 2, 1, 10, 0)),
    )

    val viewModel = createViewModel()

    assertEquals(listOf("new", "old"), viewModel.state.value.recentQueries)
    assertTrue(viewModel.state.value.showRecent)
  }

  @Test
  fun `does not search before the debounce delay has passed`() = runTest {
    val viewModel = createViewModel()

    viewModel.onQueryChanged("milk")
    advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MS - 1)

    coVerify(exactly = 0) { globalSearchUseCase(any(), any()) }
    assertFalse(viewModel.state.value.hasSearched)
  }

  @Test
  fun `searches only once with the latest query after debounce`() = runTest {
    coEvery { globalSearchUseCase("milk", any()) } returns listOf(reminder)
    val viewModel = createViewModel()

    viewModel.onQueryChanged("mi")
    advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MS / 2)
    viewModel.onQueryChanged("milk")
    advanceUntilIdle()

    coVerify(exactly = 1) { globalSearchUseCase(any(), any()) }
    assertEquals(listOf(reminder), viewModel.state.value.sections.single().items)
  }

  @Test
  fun `groups results by type with counts`() = runTest {
    coEvery { globalSearchUseCase("milk", any()) } returns listOf(note, reminder, reminder2)
    val viewModel = createViewModel()

    viewModel.onQueryChanged("milk")
    advanceUntilIdle()

    val sections = viewModel.state.value.sections
    assertEquals(listOf(SearchResultType.REMINDER, SearchResultType.NOTE), sections.map { it.type })
    assertEquals(listOf(2, 1), sections.map { it.count })
    assertEquals(
      listOf(SearchFilterChip(SearchResultType.REMINDER, 2), SearchFilterChip(SearchResultType.NOTE, 1)),
      viewModel.state.value.chips,
    )
  }

  @Test
  fun `filter keeps only the selected type section and all clears it`() = runTest {
    coEvery { globalSearchUseCase("milk", any()) } returns listOf(note, reminder)
    val viewModel = createViewModel()
    viewModel.onQueryChanged("milk")
    advanceUntilIdle()

    viewModel.onFilterSelected(SearchResultType.NOTE)
    assertEquals(listOf(SearchResultType.NOTE), viewModel.state.value.sections.map { it.type })
    assertEquals(2, viewModel.state.value.chips.size)

    viewModel.onFilterSelected(null)
    assertEquals(2, viewModel.state.value.sections.size)
  }

  @Test
  fun `shows no results state when search finds nothing`() = runTest {
    val viewModel = createViewModel()

    viewModel.onQueryChanged("zzz")
    advanceUntilIdle()

    assertTrue(viewModel.state.value.showNoResults)
    assertFalse(viewModel.state.value.showRecent)
  }

  @Test
  fun `query shorter than minimum does not run a search`() = runTest {
    val viewModel = createViewModel()

    viewModel.onQueryChanged("m")
    advanceUntilIdle()

    coVerify(exactly = 0) { globalSearchUseCase(any(), any()) }
    assertFalse(viewModel.state.value.showNoResults)
  }

  @Test
  fun `clearing the query returns to recent queries`() = runTest {
    coEvery { globalSearchUseCase("milk", any()) } returns listOf(reminder)
    val viewModel = createViewModel()
    viewModel.onQueryChanged("milk")
    advanceUntilIdle()

    viewModel.onQueryChanged("")
    advanceUntilIdle()

    assertTrue(viewModel.state.value.showRecent)
    assertTrue(viewModel.state.value.sections.isEmpty())
  }

  @Test
  fun `recent query click fills the query and searches`() = runTest {
    coEvery { globalSearchUseCase("milk", any()) } returns listOf(reminder)
    val viewModel = createViewModel()

    viewModel.onRecentQueryClicked("milk")
    advanceUntilIdle()

    assertEquals("milk", viewModel.state.value.query)
    assertEquals(1, viewModel.state.value.sections.size)
  }

  @Test
  fun `result click saves query sends analytics and posts open event`() = runTest {
    coEvery { globalSearchUseCase("milk", any()) } returns listOf(reminder)
    val viewModel = createViewModel()
    viewModel.onQueryChanged("milk")
    advanceUntilIdle()

    viewModel.onResultClicked(reminder)

    coVerify { saveRecentSearchQueryUseCase("milk") }
    verify { analyticsEventSender.send(FeatureUsedEvent(Feature.GLOBAL_SEARCH)) }
    assertEquals(
      SearchViewModel.SearchEvent.OpenResult(reminder),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `back click posts move back event`() {
    val viewModel = createViewModel()

    viewModel.onBackClicked()

    assertEquals(SearchViewModel.SearchEvent.MoveBack, viewModel.event.value?.peekContent())
  }

  @Test
  fun `no event is posted initially`() {
    assertNull(createViewModel().event.value)
  }

  private fun recent(text: String, at: LocalDateTime) = RecentQuery(
    queryType = RecentQueryType.TEXT,
    queryText = text,
    lastUsedAt = at,
  )
}
