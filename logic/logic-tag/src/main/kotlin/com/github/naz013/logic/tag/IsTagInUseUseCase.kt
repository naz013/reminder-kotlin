package com.github.naz013.logic.tag

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.repository.TagAssignmentRepository

class IsTagInUseUseCase(
  private val tagAssignmentRepository: TagAssignmentRepository,
) {

  suspend operator fun invoke(tagId: String): Boolean {
    return TaggedItemType.entries.any { type ->
      tagAssignmentRepository.getItemIdsForTag(tagId, type).isNotEmpty()
    }
  }

  suspend fun anyInUse(tagIds: Collection<String>): Boolean {
    return tagIds.any { invoke(it) }
  }
}
