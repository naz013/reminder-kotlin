package com.github.naz013.logic.search

import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryTarget
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RecentQueryRepository
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime

class SaveRecentSearchQueryUseCase internal constructor(
  private val recentQueryRepository: RecentQueryRepository,
  private val dispatcherProvider: DispatcherProvider
) {

  suspend operator fun invoke(query: String) = withContext(dispatcherProvider.io()) {
    val text = query.trim()
    if (text.length < GlobalSearchUseCase.MIN_QUERY_LENGTH) return@withContext
    val existing = recentQueryRepository.getByQuery(text)
    Logger.d(TAG, "Save recent search query, reuse existing: ${existing != null}")
    recentQueryRepository.save(
      RecentQuery(
        queryType = RecentQueryType.TEXT,
        queryText = text,
        lastUsedAt = LocalDateTime.now(),
        id = existing?.id ?: 0,
        targetType = RecentQueryTarget.NONE
      )
    )
  }

  companion object {
    private const val TAG = "SaveRecentSearchQuery"
  }
}
