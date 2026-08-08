package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.files.model.TagAssignmentRowJson
import com.github.naz013.files.model.TagAssignmentsSnapshotJson
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DataPostProcessorImplTest {
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val prefs = mockk<Prefs>(relaxed = true)
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)
  private lateinit var processor: DataPostProcessorImpl

  @Before
  fun setUp() {
    processor = DataPostProcessorImpl(
      groupV2Repository,
      prefs,
      activateReminderUseCase,
      tagAssignmentRepository
    )
  }

  @Test
  fun `process with a tag assignments snapshot replaces the local assignment table wholesale`() = runTest {
    val snapshot = TagAssignmentsSnapshotJson(
      assignments = listOf(TagAssignmentRowJson(tagId = "tag-1", itemId = "note-1", itemType = "NOTE"))
    )

    processor.process(DataType.TagAssignments, snapshot)

    coVerify(exactly = 1) {
      tagAssignmentRepository.replaceAll(
        listOf(TagAssignment(tagId = "tag-1", itemId = "note-1", itemType = TaggedItemType.NOTE))
      )
    }
  }

  @Test
  fun `process with an empty tag assignments snapshot still replaces with an empty list`() = runTest {
    val snapshot = TagAssignmentsSnapshotJson(assignments = emptyList())

    processor.process(DataType.TagAssignments, snapshot)

    coVerify(exactly = 1) { tagAssignmentRepository.replaceAll(emptyList()) }
  }

  @Test
  fun `process swallows a failure from the repository instead of throwing`() = runTest {
    val snapshot = TagAssignmentsSnapshotJson(assignments = emptyList())
    coEvery { tagAssignmentRepository.replaceAll(any()) } throws RuntimeException("db error")

    processor.process(DataType.TagAssignments, snapshot)

    // No assertion needed beyond "doesn't throw" - postProcessTagAssignments logs and swallows,
    // matching postProcessSettings' existing error-handling convention.
  }
}
