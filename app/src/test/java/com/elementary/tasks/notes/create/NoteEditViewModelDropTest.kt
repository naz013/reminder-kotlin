package com.elementary.tasks.notes.create

import android.content.ClipData
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.notes.create.drop.DroppedContentParser
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers [NoteEditViewModel.parseDrop], the drag-and-drop entry point. [DroppedContentParser]
 * itself is mocked wholesale here (its own MIME-sniffing/PDF-extraction logic is out of scope for
 * this ViewModel test) - these tests only verify how the ViewModel reacts to its
 * [DroppedContentParser.ParseResult].
 */
class NoteEditViewModelDropTest : NoteEditViewModelTestSupport() {

  private fun clipData(itemCount: Int = 1): ClipData {
    val clipData = mockk<ClipData>(relaxed = true)
    every { clipData.itemCount } returns itemCount
    return clipData
  }

  @Test
  fun `parseDrop appends dropped text after the existing body text`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Existing"))
    every { droppedContentParser.parse(any()) } returns
      DroppedContentParser.ParseResult(textContent = listOf("Dropped line"), imageUris = emptyList(), unsupportedCount = 0)

    viewModel.parseDrop(clipData())

    assertEquals("Existing\nDropped line", viewModel.state.value.textFieldValue.text)
  }

  @Test
  fun `parseDrop sets the body text directly when there was no existing text`() {
    val viewModel = buildViewModel()
    every { droppedContentParser.parse(any()) } returns
      DroppedContentParser.ParseResult(textContent = listOf("Only line"), imageUris = emptyList(), unsupportedCount = 0)

    viewModel.parseDrop(clipData())

    assertEquals("Only line", viewModel.state.value.textFieldValue.text)
  }

  @Test
  fun `parseDrop joins multiple dropped text parts with newlines`() {
    val viewModel = buildViewModel()
    every { droppedContentParser.parse(any()) } returns
      DroppedContentParser.ParseResult(textContent = listOf("Line 1", "Line 2"), imageUris = emptyList(), unsupportedCount = 0)

    viewModel.parseDrop(clipData())

    assertEquals("Line 1\nLine 2", viewModel.state.value.textFieldValue.text)
  }

  @Test
  fun `parseDrop decodes dropped image uris`() {
    val decoded = UiNoteImage(id = 1, fileName = "dropped.png", state = UiNoteImageState.READY)
    stubImageDecoder { decoded }
    val uri = mockk<Uri>(relaxed = true)
    val viewModel = buildViewModel()
    every { droppedContentParser.parse(any()) } returns
      DroppedContentParser.ParseResult(textContent = emptyList(), imageUris = listOf(uri), unsupportedCount = 0)

    viewModel.parseDrop(clipData())

    assertEquals(listOf(decoded), viewModel.state.value.images)
  }

  @Test
  fun `parseDrop shows an error when some dropped items are unsupported`() {
    every { textProvider.getText(R.string.unsupported_file_format) } returns "Unsupported"
    val viewModel = buildViewModel()
    every { droppedContentParser.parse(any()) } returns
      DroppedContentParser.ParseResult(textContent = emptyList(), imageUris = emptyList(), unsupportedCount = 2)

    viewModel.parseDrop(clipData())

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Unsupported"), event)
  }

  @Test
  fun `parseDrop does not show an error when everything was supported`() {
    val viewModel = buildViewModel()
    every { droppedContentParser.parse(any()) } returns
      DroppedContentParser.ParseResult(textContent = listOf("ok"), imageUris = emptyList(), unsupportedCount = 0)

    viewModel.parseDrop(clipData())

    assertEquals(null, viewModel.event.value)
  }
}
