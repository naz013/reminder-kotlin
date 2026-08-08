package com.elementary.tasks.notes.create

import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.domain.Tag
import com.github.naz013.files.DataType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import org.junit.Test

/**
 * Covers [NoteEditViewModel.onTagToggle]: attach/detach delegation to the tag assignment
 * repository and the resulting cloud-sync upload trigger for the whole tag-assignments snapshot.
 */
class NoteEditViewModelTagsTest : NoteEditViewModelTestSupport() {

  @Test
  fun `onTagToggle attaches an unselected tag and schedules an upload of the tag assignments snapshot`() {
    val viewModel = buildViewModel(id = "note-1")
    coEvery { tagAssignmentRepository.attach(any(), any(), any()) } returns Unit

    viewModel.onTagToggle(Tag(id = "tag-1", name = "Work", color = 0))

    coVerify { tagAssignmentRepository.attach(any(), any(), "tag-1") }
    verify {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Upload,
        dataType = DataType.TagAssignments,
      )
    }
  }
}
