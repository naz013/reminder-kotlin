package com.github.naz013.logic.search

import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.RecentQueryRepository
import kotlinx.coroutines.withContext

class GetRecentSearchQueriesUseCase internal constructor(
  private val recentQueryRepository: RecentQueryRepository,
  private val dispatcherProvider: DispatcherProvider
) {

  suspend operator fun invoke(): List<RecentQuery> = withContext(dispatcherProvider.io()) {
    recentQueryRepository.getAll().filter { it.queryType == RecentQueryType.TEXT }
  }
}
