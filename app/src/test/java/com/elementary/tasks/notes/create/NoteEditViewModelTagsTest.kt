package com.elementary.tasks.notes.create

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.tag.TagChipState
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Test

/**
 * Covers [NoteEditViewModel.onTagToggle]: attach/detach delegation to the tag assignment
 * repository and the resulting cloud-sync upload trigger for the whole tag-assignments snapshot.
 */
class NoteEditViewModelTagsTest : NoteEditViewModelTestSupport() {

  @Test
  fun `onTagToggle attaches an unselected tag and schedules an upload of the tag assignments snapshot`() {
    val viewModel = buildViewModel(id = "note-1")
    coEvery { toggleTagAssignmentUseCase.invoke(any(), any(), any(), any()) } returns Unit

    viewModel.onTagToggle(TagChipState(id = "tag-1", name = "Work", color = Color.Unspecified))

    coVerify { toggleTagAssignmentUseCase.invoke(any(), any(), "tag-1", false) }
  }
}
