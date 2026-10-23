package com.github.naz013.logic.tag

import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository

class DeleteTagWithAssignmentsUseCase(
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {

  suspend operator fun invoke(tagId: String) {
    tagAssignmentRepository.detachAllForTag(tagId)
    tagRepository.delete(tagId)
    Logger.i(TAG, "Deleted tag, id: $tagId")
  }

  companion object {
    private const val TAG = "DeleteTagWithAssignmentsUseCase"
  }
}
