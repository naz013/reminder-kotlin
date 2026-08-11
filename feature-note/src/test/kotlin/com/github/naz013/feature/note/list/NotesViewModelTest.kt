package com.github.naz013.feature.note.list

import androidx.compose.ui.graphics.Color
import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.UiNoteNotificationAdapter
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.feature.note.UiNoteNotification
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.note.UiNoteListItemAdapter
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.feature.note.preview.ImagesSingleton
import com.github.naz013.feature.note.usecase.ChangeNoteArchiveStateUseCase
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.SaveNoteUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class NotesViewModelTest : BaseTest() {
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val uiNoteListItemAdapter = mockk<UiNoteListItemAdapter>()
  private val notePreferences = mockk<NotePreferences>(relaxed = true)
  private val noteRepository = mockk<NoteRepository>()
  private val uiNoteNotificationAdapter = mockk<UiNoteNotificationAdapter>()
  private val noteNotifier = mockk<NoteNotifier>(relaxed = true)
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val deleteNoteUseCase = mockk<DeleteNoteUseCase>(relaxed = true)
  private val changeNoteArchiveStateUseCase = mockk<ChangeNoteArchiveStateUseCase>(relaxed = true)
  private val saveNoteUseCase = mockk<SaveNoteUseCase>(relaxed = true)
  private val createSharedNoteFileUseCase = mockk<CreateSharedNoteFileUseCase>()
  private val imagesSingleton = mockk<ImagesSingleton>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { notePreferences.isNotesGridEnabled } returns false
    every { notePreferences.noteOrder } returns NoteSortProcessor.DATE_ZA
    // NotesViewModel's init block eagerly queries the repository as soon as the view model is
    // constructed (independent of state collection), and every fresh state collection re-triggers
    // it via onStart{refresh()} - a default stub avoids an unstubbed-call failure everywhere.
    coEvery { noteRepository.getNotes(any(), any(), any()) } returns emptyList()
  }

  private fun note(
    id: String = "1",
    title: String = "Title",
    summary: String = "Summary",
    archived: Boolean = false,
    color: Int = 0,
  ): NoteWithImages =
    NoteWithImages(
      note =
        Note(
          key = id,
          title = title,
          summary = summary,
          archived = archived,
          color = color,
          syncState = SyncState.Synced,
        ),
    )

  private fun uiItem(
    id: String = "1",
    title: String = "Title",
    text: String = "Summary",
    backgroundColor: Color = Color.White,
    images: List<UiNoteImage> = emptyList(),
  ) = UiNoteListItem(
    id = id,
    title = title,
    text = text,
    backgroundColor = backgroundColor,
    textColor = Color.Black,
    fontStyle = 0,
    fontSize = 16f,
    titleFontStyle = 0,
    titleFontSize = 20f,
    images = images,
  )

  private fun createViewModel(isArchived: Boolean = false): NotesViewModel =
    NotesViewModel(
      isArchived = isArchived,
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      uiNoteListItemAdapter = uiNoteListItemAdapter,
      notePreferences = notePreferences,
      noteRepository = noteRepository,
      uiNoteNotificationAdapter = uiNoteNotificationAdapter,
      noteNotifier = noteNotifier,
      appWidgetUpdater = appWidgetUpdater,
      deleteNoteUseCase = deleteNoteUseCase,
      changeNoteArchiveStateUseCase = changeNoteArchiveStateUseCase,
      saveNoteUseCase = saveNoteUseCase,
      createSharedNoteFileUseCase = createSharedNoteFileUseCase,
      imagesSingleton = imagesSingleton,
      analyticsEventSender = analyticsEventSender,
    )

  @Test
  fun `sends screen used analytics event on init`() {
    createViewModel()

    verify(exactly = 1) { analyticsEventSender.send(ScreenUsedEvent(Screen.NOTES_LIST)) }
  }

  @Test
  fun `loads notes into ready state on first collection`() =
    runTest {
      val n = note()
      coEvery { noteRepository.getNotes(false, "", NoteSortProcessor.DATE_ZA) } returns listOf(n)
      every { uiNoteListItemAdapter.convert(n) } returns uiItem()
      val viewModel = createViewModel()

      val state = viewModel.notesScreenState.first()

      val ready = state.listState as ListState.Ready
      assertEquals(1, ready.notes.size)
      assertEquals(false, state.isGrid)
      assertEquals(NoteSortProcessor.DATE_ZA, state.sortOrder)
      assertEquals(false, state.isArchived)
    }

  @Test
  fun `loads empty state when there are no notes`() =
    runTest {
      coEvery { noteRepository.getNotes(false, "", NoteSortProcessor.DATE_ZA) } returns emptyList()
      val viewModel = createViewModel()

      val state = viewModel.notesScreenState.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `queries the archived flag when created for the archive screen`() =
    runTest {
      coEvery { noteRepository.getNotes(true, "", NoteSortProcessor.DATE_ZA) } returns emptyList()
      val viewModel = createViewModel(isArchived = true)

      val state = viewModel.notesScreenState.first()

      assertEquals(true, state.isArchived)
      coVerify(atLeast = 1) { noteRepository.getNotes(true, "", NoteSortProcessor.DATE_ZA) }
    }

  @Test
  fun `reloads notes on each fresh state collection`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.notesScreenState.first()
      viewModel.notesScreenState.first()

      // Once from the init block's eager collection, plus once per state collection above.
      coVerify(atLeast = 3) { noteRepository.getNotes(any(), any(), any()) }
    }

  @Test
  fun `onSearchQueryChange updates the search query in state immediately`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onSearchQueryChange("Alpha")

      assertEquals("Alpha", viewModel.notesScreenState.first().searchQuery)
    }

  @Test
  fun `onSortOrderSelected persists the sort order and updates state`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onSortOrderSelected(NoteSortProcessor.TEXT_AZ)

      verify { notePreferences.noteOrder = NoteSortProcessor.TEXT_AZ }
      assertEquals(NoteSortProcessor.TEXT_AZ, viewModel.notesScreenState.first().sortOrder)
    }

  @Test
  fun `onGridToggleClick flips the grid flag and persists it`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onGridToggleClick()

      verify { notePreferences.isNotesGridEnabled = true }
      assertEquals(true, viewModel.notesScreenState.first().isGrid)
    }

  @Test
  fun `onGridToggleClick twice restores the original flag`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onGridToggleClick()
      viewModel.onGridToggleClick()

      assertEquals(false, viewModel.notesScreenState.first().isGrid)
    }

  @Test
  fun `onAddClick posts OpenCreateNote navigation event`() {
    val viewModel = createViewModel()

    viewModel.onAddClick()

    assertEquals(NotesViewModel.NavigationEvent.OpenCreateNote, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onArchiveClick posts OpenArchive navigation event`() {
    val viewModel = createViewModel()

    viewModel.onArchiveClick()

    assertEquals(NotesViewModel.NavigationEvent.OpenArchive, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onSettingsClick posts OpenSettings navigation event with the settings title`() {
    every { textProvider.getString(R.string.action_settings) } returns "Settings"
    val viewModel = createViewModel()

    viewModel.onSettingsClick()

    assertEquals(
      NotesViewModel.NavigationEvent.OpenSettings("Settings"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onNoteClick posts OpenNotePreview navigation event`() {
    val viewModel = createViewModel()

    viewModel.onNoteClick("7")

    assertEquals(NotesViewModel.NavigationEvent.OpenNotePreview("7"), viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onNoteMenuAction OPEN posts OpenNotePreview navigation event`() {
    val viewModel = createViewModel()

    viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.OPEN)

    assertEquals(NotesViewModel.NavigationEvent.OpenNotePreview("7"), viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onNoteMenuAction EDIT posts OpenEditNote navigation event`() {
    val viewModel = createViewModel()

    viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.EDIT)

    assertEquals(NotesViewModel.NavigationEvent.OpenEditNote("7"), viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onNoteMenuAction SHOW_IN_STATUS_BAR posts RequestNotificationPermission navigation event`() {
    val viewModel = createViewModel()

    viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.SHOW_IN_STATUS_BAR)

    assertEquals(
      NotesViewModel.NavigationEvent.RequestNotificationPermission("7"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onNoteMenuAction DELETE posts ConfirmDelete navigation event`() {
    val viewModel = createViewModel()

    viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.DELETE)

    assertEquals(NotesViewModel.NavigationEvent.ConfirmDelete("7"), viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onNoteMenuAction ARCHIVE archives the note, refreshes the list and updates the widget`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.ARCHIVE)

      coVerify(exactly = 1) { changeNoteArchiveStateUseCase("7", true) }
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `onNoteMenuAction UNARCHIVE unarchives the note without touching the widget`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.UNARCHIVE)

      coVerify(exactly = 1) { changeNoteArchiveStateUseCase("7", false) }
      verify(exactly = 0) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `onImageClick sets the current images and posts OpenImagePreview at the matching position`() {
    val images = listOf(UiNoteImage(id = 1, fileName = "a.jpg"), UiNoteImage(id = 2, fileName = "b.jpg"))
    val item = uiItem(id = "7", images = images, backgroundColor = Color.Red)
    val viewModel = createViewModel()

    viewModel.onImageClick(item, imageId = 2)

    verify(exactly = 1) { imagesSingleton.setCurrent(images = images, backgroundColor = Color.Red) }
    assertEquals(
      NotesViewModel.NavigationEvent.OpenImagePreview("7", 1),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onImageClick defaults to position 0 when the image id is not found`() {
    val images = listOf(UiNoteImage(id = 1, fileName = "a.jpg"))
    val item = uiItem(id = "7", images = images)
    val viewModel = createViewModel()

    viewModel.onImageClick(item, imageId = 99)

    assertEquals(
      NotesViewModel.NavigationEvent.OpenImagePreview("7", 0),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `deleteNote deletes, refreshes and updates the widget when not archived`() =
    runTest {
      val viewModel = createViewModel(isArchived = false)

      viewModel.deleteNote("7")

      coVerify(exactly = 1) { deleteNoteUseCase("7") }
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `deleteNote does not update the widget on the archive screen`() =
    runTest {
      val viewModel = createViewModel(isArchived = true)

      viewModel.deleteNote("7")

      coVerify(exactly = 1) { deleteNoteUseCase("7") }
      verify(exactly = 0) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `shareNote posts ShareNote event when the file is created and readable`() =
    runTest {
      val n = note(id = "7", summary = "Buy milk")
      coEvery { noteRepository.getById("7") } returns n
      val file = File.createTempFile("note", ".note").apply { writeText("data") }
      file.deleteOnExit()
      coEvery { createSharedNoteFileUseCase(n) } returns file
      val viewModel = createViewModel()

      viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.SHARE)

      assertEquals(
        NotesViewModel.NavigationEvent.ShareNote(file, "Buy milk"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `shareNote posts Error when the note is not found`() =
    runTest {
      coEvery { noteRepository.getById("7") } returns null
      every { textProvider.getText(R.string.failed_to_send_note) } returns "Failed to send note"
      val viewModel = createViewModel()

      viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.SHARE)

      assertEquals(
        NotesViewModel.NavigationEvent.Error("Failed to send note"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `shareNote posts Error when the shared file cannot be created`() =
    runTest {
      val n = note(id = "7")
      coEvery { noteRepository.getById("7") } returns n
      coEvery { createSharedNoteFileUseCase(n) } returns null
      every { textProvider.getText(R.string.failed_to_send_note) } returns "Failed to send note"
      val viewModel = createViewModel()

      viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.SHARE)

      assertEquals(
        NotesViewModel.NavigationEvent.Error("Failed to send note"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `shareNote posts Error when the shared file cannot be read`() =
    runTest {
      val n = note(id = "7")
      coEvery { noteRepository.getById("7") } returns n
      val missingFile = File.createTempFile("note", ".note")
      missingFile.delete()
      coEvery { createSharedNoteFileUseCase(n) } returns missingFile
      every { textProvider.getText(R.string.error_sending) } returns "Error sending"
      val viewModel = createViewModel()

      viewModel.onNoteMenuAction(uiItem(id = "7"), NoteMenuAction.SHARE)

      assertEquals(
        NotesViewModel.NavigationEvent.Error("Error sending"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `saveNoteColor updates the color, saves and refreshes the widget`() =
    runTest {
      val n = note(id = "7", color = 1)
      coEvery { noteRepository.getById("7") } returns n
      val viewModel = createViewModel()

      viewModel.saveNoteColor("7", 5)

      assertEquals(5, n.note?.color)
      coVerify(exactly = 1) { saveNoteUseCase(n) }
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `saveNoteColor does nothing when the note is not found`() =
    runTest {
      coEvery { noteRepository.getById("7") } returns null
      val viewModel = createViewModel()

      viewModel.saveNoteColor("7", 5)

      coVerify(exactly = 0) { saveNoteUseCase(any()) }
      verify(exactly = 0) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `showNoteInNotification shows the notification for the note`() =
    runTest {
      val n = note(id = "7")
      coEvery { noteRepository.getById("7") } returns n
      val uiNotification =
        UiNoteNotification(id = "7", text = "Summary", backgroundColor = 0, textColor = 0, uniqueId = 1)
      every { uiNoteNotificationAdapter.convert(n) } returns uiNotification
      val viewModel = createViewModel()

      viewModel.showNoteInNotification("7")

      verify(exactly = 1) {
        noteNotifier.showNoteNotification(text = "Summary", image = null, uniqueId = 1)
      }
    }

  @Test
  fun `showNoteInNotification does nothing when the note is not found`() =
    runTest {
      coEvery { noteRepository.getById("7") } returns null
      val viewModel = createViewModel()

      viewModel.showNoteInNotification("7")

      verify(exactly = 0) { noteNotifier.showNoteNotification(any(), any(), any()) }
    }
}
