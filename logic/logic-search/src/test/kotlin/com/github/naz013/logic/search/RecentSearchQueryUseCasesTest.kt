package com.github.naz013.logic.search

import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.repository.RecentQueryRepository
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchQueryUseCasesTest {

  private val repository = mockk<RecentQueryRepository>(relaxed = true)

  @Test
  fun `saves trimmed query as text recent query`() = runTest {
    coEvery { repository.getByQuery("milk") } returns null
    val saved = slot<RecentQuery>()
    coEvery { repository.save(capture(saved)) } returns Unit

    SaveRecentSearchQueryUseCase(repository, mockDispatcherProvider())("  milk ")

    assertEquals("milk", saved.captured.queryText)
    assertEquals(RecentQueryType.TEXT, saved.captured.queryType)
  }

  @Test
  fun `reuses id of an existing identical query`() = runTest {
    coEvery { repository.getByQuery("milk") } returns RecentQuery(RecentQueryType.TEXT, "milk", id = 7)
    val saved = slot<RecentQuery>()
    coEvery { repository.save(capture(saved)) } returns Unit

    SaveRecentSearchQueryUseCase(repository, mockDispatcherProvider())("milk")

    assertEquals(7L, saved.captured.id)
  }

  @Test
  fun `does not save query shorter than minimum length`() = runTest {
    SaveRecentSearchQueryUseCase(repository, mockDispatcherProvider())(" a ")

    coVerify(exactly = 0) { repository.save(any()) }
  }

  @Test
  fun `returns only text queries`() = runTest {
    coEvery { repository.getAll() } returns listOf(
      RecentQuery(RecentQueryType.TEXT, "milk"),
      RecentQuery(RecentQueryType.OBJECT, "Note 1")
    )

    val result = GetRecentSearchQueriesUseCase(repository, mockDispatcherProvider())()

    assertEquals(listOf("milk"), result.map { it.queryText })
  }
}
