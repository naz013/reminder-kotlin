package com.github.naz013.feature.note.create

import android.net.Uri
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.UiNoteImageState
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Covers [NoteEditViewModel]'s `init {}` / `load()` behavior: the color/opacity/font/slider
 * defaults computed on every construction, and the four mutually-exclusive load paths (new note,
 * shared text, shared image uris, from-intent-data, edit-existing-by-id) plus linked-reminder
 * loading. `loadFromFile(uri)` has its own test file since it needs `MemoryUtil` companion mocking.
 */
class NoteEditViewModelLoadTest : NoteEditViewModelTestSupport() {

  @Test
  fun `initializes color, opacity and slider defaults for a brand-new note`() {
    val viewModel = buildViewModel()

    val state = viewModel.state.value

    // getColorCode(palette=0, code=2) -> stubbed to 0*100+2 = 2
    assertEquals(2, state.colorIndex)
    assertEquals(80, state.opacity)
    assertEquals(listOf(androidx.compose.ui.graphics.Color.Red, androidx.compose.ui.graphics.Color.Green, androidx.compose.ui.graphics.Color.Blue), state.sliderColors)
  }

  @Test
  fun `initializes default font size and style when remembering is disabled`() {
    every { notePreferences.isNoteFontSizeRememberingEnabled } returns false
    every { notePreferences.isNoteFontStyleRememberingEnabled } returns false

    val state = buildViewModel().state.value

    assertEquals(FontParams.DEFAULT_FONT_SIZE, state.fontSize)
    assertEquals(FontParams.DEFAULT_FONT_STYLE, state.fontStyle)
    assertEquals(FontParams.DEFAULT_TITLE_FONT_SIZE, state.titleFontSize)
    assertEquals(FontParams.DEFAULT_FONT_STYLE, state.titleFontStyle)
  }

  @Test
  fun `initializes remembered font size and style from prefs when remembering is enabled`() {
    every { notePreferences.isNoteFontSizeRememberingEnabled } returns true
    every { notePreferences.isNoteFontStyleRememberingEnabled } returns true
    every { notePreferences.lastNoteFontSize } returns 22
    every { notePreferences.lastNoteFontStyle } returns 3
    every { notePreferences.lastNoteTitleFontSize } returns 30
    every { notePreferences.lastNoteTitleFontStyle } returns 4

    val state = buildViewModel().state.value

    assertEquals(22, state.fontSize)
    assertEquals(3, state.fontStyle)
    assertEquals(30, state.titleFontSize)
    assertEquals(4, state.titleFontStyle)
  }

  @Test
  fun `reflects hasCamera from system info`() {
    every { systemInfo.hasCamera } returns true

    val state = buildViewModel().state.value

    assertTrue(state.hasCamera)
  }

  @Test
  fun `a brand-new note has an empty text and title and no reminder attached`() {
    val state = buildViewModel().state.value

    assertEquals("", state.textFieldValue.text)
    assertEquals("", state.titleFieldValue.text)
    assertEquals(false, state.isReminderAttached)
    assertEquals(emptyList<UiNoteImage>(), state.images)
    assertNotNull(state.noteId)
  }

  // canDelete is declared on NoteEditState and read by NoteEditScreen to show/hide the delete
  // icon, but NoteEditViewModel never assigns it anywhere (grep confirms no `canDelete =` site) -
  // it stays false even when editing an existing note below. This pins down that (suspected bug)
  // behavior; see the final report.
  @Test
  fun `canDelete becomes true after loading an existing note`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")

    val state = buildViewModel(id = "42").state.value

    assertEquals(true, state.canDelete)
  }

  @Test
  fun `canDelete stays false for a brand-new note`() {
    val state = buildViewModel(id = null).state.value

    assertEquals(false, state.canDelete)
  }

  @Test
  fun `load replaces text field with shared text when opened from a share-text intent`() {
    val viewModel = buildViewModel(sharedText = "Shared from another app")

    val state = viewModel.state.value

    assertEquals("Shared from another app", state.textFieldValue.text)
    assertEquals(state.textFieldValue.text.length, state.textFieldValue.selection.end)
  }

  @Test
  fun `load posts a textUpdate event when opened from a share-text intent`() {
    val viewModel = buildViewModel(sharedText = "Shared text")

    val update = viewModel.textUpdate.value?.peekContent()

    assertEquals("Shared text", update?.text)
  }

  @Test
  fun `load decodes shared image uris when opened from a share-image intent`() {
    // load() does `sharedImageUris.map { Uri.parse(it) }` - a real static call. Under the JVM
    // unit test android stub jar it returns null (isReturnDefaultValues affects method bodies,
    // and Uri.parse's stub body is empty), so it needs mockkStatic here rather than a real Uri.
    mockkStatic(Uri::class)
    try {
      val parsedUri = mockk<Uri>(relaxed = true)
      every { Uri.parse("content://shared/1") } returns parsedUri
      val decoded = UiNoteImage(id = 1, fileName = "shared.jpg", state = UiNoteImageState.READY)
      stubImageDecoder { decoded }

      val state = buildViewModel(sharedImageUris = listOf("content://shared/1")).state.value

      assertEquals(listOf(decoded), state.images)
    } finally {
      unmockkStatic(Uri::class)
    }
  }

  @Test
  fun `load reads the note from the intent data reader when opened from intent data`() {
    val noteWithImages = NoteWithImages(note = Note(key = "from-intent", summary = "Hello", syncState = SyncState.Synced))
    every { intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java) } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "from-intent", text = "Hello")
    coEvery { noteRepository.getById("from-intent") } returns null

    val state = buildViewModel(fromIntentData = true).state.value

    assertEquals("Hello", state.textFieldValue.text)
    assertEquals(true, state.isFromFile)
    assertEquals(false, state.hasSameInDb)
  }

  @Test
  fun `load from intent data marks hasSameInDb true when a note with the same key already exists`() {
    val noteWithImages = NoteWithImages(note = Note(key = "dup", syncState = SyncState.Synced))
    every { intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java) } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "dup")
    coEvery { noteRepository.getById("dup") } returns noteWithImages

    val state = buildViewModel(fromIntentData = true).state.value

    assertEquals(true, state.hasSameInDb)
    assertEquals(true, state.isFromFile)
  }

  @Test
  fun `does nothing when opened from intent data but no item is present`() {
    every { intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java) } returns null

    val state = buildViewModel(fromIntentData = true).state.value

    assertEquals("", state.textFieldValue.text)
    assertEquals(false, state.isFromFile)
  }

  @Test
  fun `loads an existing note by id into state for editing`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", summary = "Body", title = "Title", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    val uiEdit =
      uiNoteEdit(
        id = "42",
        text = "Body",
        title = "Title",
        typeface = 5,
        titleTypeface = 6,
        titleFontSize = 24,
        fontSize = 18,
        colorPosition = 3,
        colorPalette = 1,
        opacity = 60,
      )
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiEdit

    val state = buildViewModel(id = "42").state.value

    assertEquals("Body", state.textFieldValue.text)
    assertEquals("Title", state.titleFieldValue.text)
    assertEquals(5, state.fontStyle)
    assertEquals(18, state.fontSize)
    assertEquals(6, state.titleFontStyle)
    assertEquals(24, state.titleFontSize)
    // colorIndex is the combined code (palette*100 + position per the test stub), matching the
    // brand-new-note init path's convention for this field - not the raw in-palette position.
    assertEquals(103, state.colorIndex)
    assertEquals(60, state.opacity)
    assertEquals("42", state.noteId)
  }

  @Test
  fun `does nothing when the note id is not found in the repository`() {
    coEvery { noteRepository.getById("missing") } returns null

    val state = buildViewModel(id = "missing").state.value

    assertEquals("", state.textFieldValue.text)
  }

  @Test
  fun `loads the linked active reminder time and date when editing a note that has one`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val reminder = ReminderV2(
      uuId = "r1",
      noteId = "42",
      isActive = true,
      isRemoved = false,
      schedule = ReminderSchedule(
        startDateTime = LocalDateTime.of(2026, 8, 1, 9, 0),
        eventDateTime = LocalDateTime.of(2026, 8, 1, 9, 0),
      ),
    )
    coEvery { reminderV2Repository.getByNoteId("42") } returns listOf(reminder)

    val state = buildViewModel(id = "42").state.value

    assertEquals(true, state.isReminderAttached)
    assertEquals("r1", state.reminderId)
    assertEquals(LocalDate.of(2026, 8, 1), state.date)
    assertEquals(LocalTime.of(9, 0), state.time)
  }

  @Test
  fun `ignores an inactive linked reminder when editing a note`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val inactiveReminder = ReminderV2(
      uuId = "r2",
      noteId = "42",
      isActive = false,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )
    coEvery { reminderV2Repository.getByNoteId("42") } returns listOf(inactiveReminder)

    val state = buildViewModel(id = "42").state.value

    assertEquals(false, state.isReminderAttached)
    assertNull(state.reminderId)
  }

  @Test
  fun `onNewDate and onNewTime format the value via DateTimeManager`() {
    val viewModel = buildViewModel()
    every { dateTimeManager.getDate(LocalDate.of(2026, 12, 25)) } returns "25 Dec 2026"
    every { dateTimeManager.getTime(LocalTime.of(18, 30)) } returns "18:30"

    viewModel.onNewDate(LocalDate.of(2026, 12, 25))
    viewModel.onNewTime(LocalTime.of(18, 30))

    val state = viewModel.state.value
    assertEquals(LocalDate.of(2026, 12, 25), state.date)
    assertEquals("25 Dec 2026", state.reminderDateFormatted)
    assertEquals(LocalTime.of(18, 30), state.time)
    assertEquals("18:30", state.reminderTimeFormatted)
  }
}
