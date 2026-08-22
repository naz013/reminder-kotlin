package com.github.naz013.feature.note.preview

import androidx.compose.ui.graphics.Color
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.UiNoteNotification
import com.github.naz013.feature.note.UiNoteNotificationAdapter
import com.github.naz013.feature.note.UiNotePreview
import com.github.naz013.feature.note.UiNotePreviewAdapter
import com.github.naz013.feature.note.preview.reminders.ReminderToUiNoteAttachedReminder
import com.github.naz013.feature.note.preview.reminders.UiNoteAttachedReminder
import com.github.naz013.feature.note.usecase.ChangeNoteArchiveStateUseCase
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.TogglePinnedNoteUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.note.NoteColorEngine
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.io.File

class PreviewNoteViewModelTest : BaseTest() {
  private val key = "42"

  private val noteRepository = mockk<NoteRepository>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val uiNotePreviewAdapter = mockk<UiNotePreviewAdapter>()
  private val textProvider = mockk<com.github.naz013.common.TextProvider>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val uiNoteNotificationAdapter = mockk<UiNoteNotificationAdapter>()
  private val noteNotifier = mockk<NoteNotifier>(relaxed = true)
  private val reminderToUiNoteAttachedReminder = mockk<ReminderToUiNoteAttachedReminder>()
  private val deleteNoteUseCase = mockk<DeleteNoteUseCase>(relaxed = true)
  private val changeNoteArchiveStateUseCase = mockk<ChangeNoteArchiveStateUseCase>(relaxed = true)
  private val togglePinnedNoteUseCase = mockk<TogglePinnedNoteUseCase>(relaxed = true)
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val createSharedNoteFileUseCase = mockk<CreateSharedNoteFileUseCase>()
  private val imagesSingleton = mockk<ImagesSingleton>(relaxed = true)
  private val noteColorEngine = mockk<NoteColorEngine>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()

  @Before
  override fun setUp() {
    super.setUp()
    // PreviewNoteViewModel's init block eagerly observes the note and its tags via Flow, and
    // separately loads attached reminders once - default stubs avoid unstubbed-call failures for
    // tests that don't care about note/reminder/tag details but still collect state at least once.
    coEvery { noteRepository.getById(key) } returns null
    every { noteRepository.observeById(key) } returns flowOf(null)
    coEvery { reminderV2Repository.getByNoteId(key) } returns emptyList()
    coEvery { tagAssignmentRepository.getTagsForItem(key, TaggedItemType.NOTE) } returns emptyList()
    every { tagAssignmentRepository.observeTagsForItem(key, TaggedItemType.NOTE) } returns flowOf(emptyList())
  }

  private fun note(
    id: String = key,
    summary: String = "Summary",
    archived: Boolean = false,
    color: Int = 0,
  ): NoteWithImages =
    NoteWithImages(
      note = Note(key = id, summary = summary, archived = archived, color = color, syncState = SyncState.Synced),
    )

  private fun uiPreview(
    id: String = key,
    title: String = "Title",
    text: String = "Summary",
    images: List<UiNoteImage> = emptyList(),
    isArchived: Boolean = false,
    isPinned: Boolean = false,
  ) = UiNotePreview(
    id = id,
    text = text,
    title = title,
    typeface = null,
    images = images,
    uniqueId = 1,
    textSize = 18f,
    titleTypeface = null,
    titleTextSize = 20f,
    isArchived = isArchived,
    isPinned = isPinned,
  )

  private fun reminder(
    id: String = "r1",
    noteId: String = key,
    summary: String = "Buy milk",
  ) = ReminderV2(
    uuId = id,
    noteId = noteId,
    summary = summary,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  private fun createViewModel(): PreviewNoteViewModel =
    PreviewNoteViewModel(
      key = key,
      dispatcherProvider = mockDispatcherProvider(),
      noteRepository = noteRepository,
      reminderV2Repository = reminderV2Repository,
      uiNotePreviewAdapter = uiNotePreviewAdapter,
      textProvider = textProvider,
      analyticsEventSender = analyticsEventSender,
      uiNoteNotificationAdapter = uiNoteNotificationAdapter,
      noteNotifier = noteNotifier,
      reminderToUiNoteAttachedReminder = reminderToUiNoteAttachedReminder,
      deleteNoteUseCase = deleteNoteUseCase,
      changeNoteArchiveStateUseCase = changeNoteArchiveStateUseCase,
      togglePinnedNoteUseCase = togglePinnedNoteUseCase,
      saveReminderUseCase = saveReminderUseCase,
      createSharedNoteFileUseCase = createSharedNoteFileUseCase,
      imagesSingleton = imagesSingleton,
      noteColorEngine = noteColorEngine,
      tagAssignmentRepository = tagAssignmentRepository,
      tagChipStateAdapter = tagChipStateAdapter,
    )

  @Test
  fun `sends screen used analytics event on init`() {
    createViewModel()

    verify(exactly = 1) { analyticsEventSender.send(ScreenUsedEvent(Screen.NOTE_PREVIEW)) }
  }

  @Test
  fun `loads note details into state on first collection`() =
    runTest {
      val n = note(summary = "Buy milk", archived = false)
      every { noteRepository.observeById(key) } returns flowOf(n)
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview(text = "Buy milk")
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Red, content = Color.White)
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals(key, state.id)
      assertEquals("Buy milk", state.text)
      assertEquals(Color.Red, state.background)
      assertEquals(Color.White, state.content)
      assertEquals(false, state.isArchived)
    }

  @Test
  fun `state updates automatically when the note flow emits a new value`() =
    runTest {
      val noteFlow = MutableStateFlow<NoteWithImages?>(null)
      every { noteRepository.observeById(key) } returns noteFlow
      val n = note(summary = "Buy milk")
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview(text = "Buy milk")
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Unspecified, content = Color.Unspecified)
      val viewModel = createViewModel()
      var latest = PreviewNoteState(id = key)
      backgroundScope.launch(Dispatchers.Unconfined) {
        viewModel.state.collect { latest = it }
      }
      assertEquals("", latest.text)

      // Simulates an edit made elsewhere (e.g. the edit screen) writing to the DB - no explicit
      // reload call from the view model is needed for this to show up.
      noteFlow.value = n

      assertEquals("Buy milk", latest.text)
    }

  @Test
  fun `loads reminders attached to the note`() =
    runTest {
      val n = note()
      every { noteRepository.observeById(key) } returns flowOf(n)
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview()
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Unspecified, content = Color.Unspecified)
      val r = reminder(id = "r1")
      coEvery { reminderV2Repository.getByNoteId(key) } returns listOf(r)
      every { reminderToUiNoteAttachedReminder(r) } returns
        UiNoteAttachedReminder(id = "r1", summary = "Buy milk", dateTime = "1 Jan 2026")
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals(1, state.reminders.size)
      assertEquals("r1", state.reminders.first().id)
    }

  @Test
  fun `loads tags attached to the note`() =
    runTest {
      val n = note()
      every { noteRepository.observeById(key) } returns flowOf(n)
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview()
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Unspecified, content = Color.Unspecified)
      val tag = Tag(id = "t1", name = "Work", color = 0xFF0000)
      every { tagAssignmentRepository.observeTagsForItem(key, TaggedItemType.NOTE) } returns flowOf(listOf(tag))
      every { tagChipStateAdapter(tag) } returns TagChipState(id = "t1", name = "Work", color = Color.Red)
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals(1, state.tags.size)
      assertEquals("t1", state.tags.first().id)
      assertEquals("Work", state.tags.first().name)
    }

  @Test
  fun `still loads reminders when the note itself is not found`() =
    runTest {
      every { noteRepository.observeById(key) } returns flowOf(null)
      val r = reminder(id = "r1")
      coEvery { reminderV2Repository.getByNoteId(key) } returns listOf(r)
      every { reminderToUiNoteAttachedReminder(r) } returns
        UiNoteAttachedReminder(id = "r1", summary = "Buy milk", dateTime = null)
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals(1, state.reminders.size)
      // The note itself wasn't found, so note-derived fields stay at their construction-time
      // defaults (id defaults to the view model's key, title/text default to "").
      assertEquals(key, state.id)
      assertEquals("", state.title)
    }

  @Test
  fun `onStatusClick shows the note notification`() =
    runTest {
      val n = note()
      coEvery { noteRepository.getById(key) } returns n
      val uiNotification =
        UiNoteNotification(id = key, text = "Summary", backgroundColor = 0, textColor = 0, uniqueId = 1)
      every { uiNoteNotificationAdapter.convert(n) } returns uiNotification
      val viewModel = createViewModel()

      viewModel.onStatusClick()

      verify(exactly = 1) {
        noteNotifier.showNoteNotification(text = "Summary", image = null, uniqueId = 1)
      }
    }

  @Test
  fun `onStatusClick does nothing when the note is not found`() =
    runTest {
      coEvery { noteRepository.getById(key) } returns null
      val viewModel = createViewModel()

      viewModel.onStatusClick()

      verify(exactly = 0) { noteNotifier.showNoteNotification(any(), any(), any()) }
    }

  @Test
  fun `onArchiveClick archives an unarchived note and shows the moved-to-archive message`() =
    runTest {
      val n = note(archived = false)
      coEvery { noteRepository.getById(key) } returns n
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview()
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Unspecified, content = Color.Unspecified)
      every { textProvider.getText(R.string.note_moved_to_archive) } returns "Moved to archive"
      val viewModel = createViewModel()

      viewModel.onArchiveClick()

      coVerify(exactly = 1) { changeNoteArchiveStateUseCase(key, true) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewNoteViewModel.ViewModelEvent.Message("Moved to archive"), event)
    }

  @Test
  fun `onArchiveClick unarchives an archived note and shows the reverted message`() =
    runTest {
      val n = note(archived = true)
      coEvery { noteRepository.getById(key) } returns n
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview(isArchived = true)
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Unspecified, content = Color.Unspecified)
      every { textProvider.getText(R.string.note_reverted_from_archive) } returns "Reverted from archive"
      val viewModel = createViewModel()

      viewModel.onArchiveClick()

      coVerify(exactly = 1) { changeNoteArchiveStateUseCase(key, false) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewNoteViewModel.ViewModelEvent.Message("Reverted from archive"), event)
    }

  @Test
  fun `onArchiveClick shows an error and does not archive when the note is not found`() =
    runTest {
      coEvery { noteRepository.getById(key) } returns null
      every { textProvider.getText(R.string.notes_failed_to_update) } returns "Failed to update"
      val viewModel = createViewModel()

      viewModel.onArchiveClick()

      coVerify(exactly = 0) { changeNoteArchiveStateUseCase(any(), any()) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewNoteViewModel.ViewModelEvent.Message("Failed to update"), event)
    }

  @Test
  fun `onDeleteClick posts a Delete confirmation event`() {
    val viewModel = createViewModel()

    viewModel.onDeleteClick()

    assertEquals(PreviewNoteViewModel.ViewModelEvent.Delete, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onDeleteConfirmed deletes the note and posts MoveBack`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteNoteUseCase(key) }
      assertEquals(PreviewNoteViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    }

  @Test
  fun `onShareClick posts ShareNote event when the file is created and readable`() =
    runTest {
      val n = note(summary = "Buy milk")
      coEvery { noteRepository.getById(key) } returns n
      val file = File.createTempFile("note", ".note").apply { writeText("data") }
      file.deleteOnExit()
      coEvery { createSharedNoteFileUseCase(n) } returns file
      val viewModel = createViewModel()

      viewModel.onShareClick()

      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewNoteViewModel.ViewModelEvent.ShareNote("", file), event)
    }

  @Test
  fun `onShareClick does nothing when the note is not found`() =
    runTest {
      coEvery { noteRepository.getById(key) } returns null
      val viewModel = createViewModel()

      viewModel.onShareClick()

      assertNull(viewModel.event.value?.peekContent())
    }

  @Test
  fun `onShareClick shows an error message when the shared file cannot be created`() =
    runTest {
      val n = note()
      coEvery { noteRepository.getById(key) } returns n
      coEvery { createSharedNoteFileUseCase(n) } returns null
      every { textProvider.getText(R.string.failed_to_send_note) } returns "Failed to send note"
      val viewModel = createViewModel()

      viewModel.onShareClick()

      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewNoteViewModel.ViewModelEvent.Message("Failed to send note"), event)
    }

  @Test
  fun `onShareClick shows an error message when the shared file cannot be read`() =
    runTest {
      val n = note()
      coEvery { noteRepository.getById(key) } returns n
      val missingFile = File.createTempFile("note", ".note")
      missingFile.delete()
      coEvery { createSharedNoteFileUseCase(n) } returns missingFile
      every { textProvider.getText(R.string.failed_to_send_note) } returns "Failed to send note"
      val viewModel = createViewModel()

      viewModel.onShareClick()

      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewNoteViewModel.ViewModelEvent.Message("Failed to send note"), event)
    }

  @Test
  fun `onEditClick posts EditNote event with the note key`() {
    val viewModel = createViewModel()

    viewModel.onEditClick()

    assertEquals(PreviewNoteViewModel.ViewModelEvent.EditNote(key), viewModel.event.value?.peekContent())
  }

  @Test
  fun `onReminderEditClick posts EditReminder event with the reminder id`() {
    val viewModel = createViewModel()

    viewModel.onReminderEditClick("r1")

    assertEquals(PreviewNoteViewModel.ViewModelEvent.EditReminder("r1"), viewModel.event.value?.peekContent())
  }

  @Test
  fun `onImageOpen sets the current images in the singleton and posts OpenImagePreview`() =
    runTest {
      val n = note()
      every { noteRepository.observeById(key) } returns flowOf(n)
      val images = listOf(UiNoteImage(id = 1, fileName = "a.jpg"))
      every { uiNotePreviewAdapter.convert(n) } returns uiPreview(images = images)
      every {
        noteColorEngine.colorsForLegacy(any(), any(), any())
      } returns NoteColorEngine.Colors(background = Color.Red, content = Color.White)
      val viewModel = createViewModel()
      viewModel.state.first()

      viewModel.onImageOpen(0)

      verify(exactly = 1) { imagesSingleton.setCurrent(images = images, backgroundColor = Color.Red) }
      assertEquals(PreviewNoteViewModel.ViewModelEvent.OpenImagePreview(0), viewModel.event.value?.peekContent())
    }

  @Test
  fun `onReminderDetachClick detaches the reminder and reloads the reminder list`() =
    runTest {
      val attached = reminder(id = "r1", noteId = key)
      coEvery { reminderV2Repository.getById("r1") } returns attached
      coEvery { saveReminderUseCase(any()) } returns Unit
      coEvery { reminderV2Repository.getByNoteId(key) } returns emptyList()
      val viewModel = createViewModel()

      viewModel.onReminderDetachClick("r1")

      coVerify(exactly = 1) {
        saveReminderUseCase(
          match {
            it.noteId == "" &&
              it.sync.version == attached.sync.version + 1 &&
              it.sync.syncState == SyncState.WaitingForUpload
          },
        )
      }
      coVerify(atLeast = 1) { reminderV2Repository.getByNoteId(key) }
    }

  @Test
  fun `onReminderDetachClick does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("missing") } returns null
      val viewModel = createViewModel()

      viewModel.onReminderDetachClick("missing")

      coVerify(exactly = 0) { saveReminderUseCase(any()) }
    }
}
