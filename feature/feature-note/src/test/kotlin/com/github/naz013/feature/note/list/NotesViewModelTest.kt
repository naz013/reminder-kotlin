package com.github.naz013.feature.note.list

import androidx.compose.ui.graphics.Color
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.UiNoteNotification
import com.github.naz013.feature.note.UiNoteNotificationAdapter
import com.github.naz013.feature.note.preview.ImagesSingleton
import com.github.naz013.feature.note.usecase.ChangeNoteArchiveStateUseCase
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.MergeNotesUseCase
import com.github.naz013.feature.note.usecase.SaveNoteUseCase
import com.github.naz013.feature.note.usecase.TogglePinnedNoteUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.note.ListLayoutMode
import com.github.naz013.ui.note.NoteColorEngine
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.note.UiNoteListItemAdapter
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
import kotlinx.coroutines.test.TestScope
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
  private val mergeNotesUseCase = mockk<MergeNotesUseCase>(relaxed = true)
  private val changeNoteArchiveStateUseCase = mockk<ChangeNoteArchiveStateUseCase>(relaxed = true)
  private val togglePinnedNoteUseCase = mockk<TogglePinnedNoteUseCase>(relaxed = true)
  private val saveNoteUseCase = mockk<SaveNoteUseCase>(relaxed = true)
  private val createSharedNoteFileUseCase = mockk<CreateSharedNoteFileUseCase>()
  private val imagesSingleton = mockk<ImagesSingleton>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()
  private val noteColorEngine = NoteColorEngine(mockk<ThemeProvider>(relaxed = true), mockk(relaxed = true))

  @Before
  override fun setUp() {
    super.setUp()
    every { notePreferences.notesLayoutMode } returns ListLayoutMode.LIST
    every { notePreferences.noteOrder } returns NoteSortProcessor.DATE_ZA
    // NotesViewModel's init block eagerly observes the repository's notes flow as soon as the
    // view model is constructed, independent of state collection - a default stub avoids an
    // unstubbed-call failure everywhere.
    every { noteRepository.observeNotes(any(), any(), any()) } returns flowOf(emptyList())
    // init{} also eagerly collects the available tags, independent of state collection.
    every { tagRepository.observeAll() } returns flowOf(emptyList())
  }

  private fun note(
    id: String = "1",
    summary: String = "Summary",
    archived: Boolean = false,
    color: Int = 0,
  ): NoteWithImages =
    NoteWithImages(
      note =
      Note(
        key = id,
        content = NoteDocument(text = summary),
        archived = archived,
        color = color,
        syncState = SyncState.Synced,
      ),
    )

  private fun uiItem(
    id: String = "1",
    text: String = "Summary",
    backgroundColor: Color = Color.White,
    images: List<UiNoteImage> = emptyList(),
  ) = UiNoteListItem(
    id = id,
    content = NoteDocument(text = text),
    backgroundColor = backgroundColor,
    textColor = Color.Black,
    fontStyle = 0,
    fontSize = 16f,
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
      mergeNotesUseCase = mergeNotesUseCase,
      changeNoteArchiveStateUseCase = changeNoteArchiveStateUseCase,
      togglePinnedNoteUseCase = togglePinnedNoteUseCase,
      saveNoteUseCase = saveNoteUseCase,
      createSharedNoteFileUseCase = createSharedNoteFileUseCase,
      imagesSingleton = imagesSingleton,
      analyticsEventSender = analyticsEventSender,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      tagChipStateAdapter = tagChipStateAdapter,
      noteColorEngine = noteColorEngine,
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
      every { noteRepository.observeNotes(false, "", NoteSortProcessor.DATE_ZA) } returns flowOf(listOf(n))
      every { uiNoteListItemAdapter.convert(n) } returns uiItem()
      val viewModel = createViewModel()

      val state = viewModel.notesScreenState.first()

      val ready = state.listState as ListState.Ready
      assertEquals(1, ready.notes.size)
      assertEquals(ListLayoutMode.LIST, state.layoutMode)
      assertEquals(NoteSortProcessor.DATE_ZA, state.sortOrder)
      assertEquals(false, state.isArchived)
    }

  @Test
  fun `loads empty state when there are no notes`() =
    runTest {
      every { noteRepository.observeNotes(false, "", NoteSortProcessor.DATE_ZA) } returns flowOf(emptyList())
      val viewModel = createViewModel()

      val state = viewModel.notesScreenState.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `queries the archived flag when created for the archive screen`() =
    runTest {
      every { noteRepository.observeNotes(true, "", NoteSortProcessor.DATE_ZA) } returns flowOf(emptyList())
      val viewModel = createViewModel(isArchived = true)

      val state = viewModel.notesScreenState.first()

      assertEquals(true, state.isArchived)
      verify(atLeast = 1) { noteRepository.observeNotes(true, "", NoteSortProcessor.DATE_ZA) }
    }

  @Test
  fun `list updates automatically when the repository flow emits a new value`() =
    runTest {
      val n = note()
      every { uiNoteListItemAdapter.convert(n) } returns uiItem()
      val notesFlow = MutableStateFlow<List<NoteWithImages>>(emptyList())
      every { noteRepository.observeNotes(false, "", NoteSortProcessor.DATE_ZA) } returns notesFlow
      val viewModel = createViewModel()
      var latest = NotesScreenState()
      backgroundScope.launch(Dispatchers.Unconfined) {
        viewModel.notesScreenState.collect { latest = it }
      }
      assertEquals(ListState.Empty, latest.listState)

      // Simulates an edit made elsewhere (e.g. the edit screen) writing to the DB - no explicit
      // refresh call from the view model is needed for this to show up.
      notesFlow.value = listOf(n)

      val ready = latest.listState as ListState.Ready
      assertEquals(1, ready.notes.size)
    }

  @Test
  fun `onTagSelected filters notes down to items carrying that tag`() =
    runTest {
      val n1 = note(id = "1")
      val n2 = note(id = "2")
      every { noteRepository.observeNotes(false, "", NoteSortProcessor.DATE_ZA) } returns flowOf(listOf(n1, n2))
      every { uiNoteListItemAdapter.convert(n1) } returns uiItem(id = "1")
      every { uiNoteListItemAdapter.convert(n2) } returns uiItem(id = "2")
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag1", TaggedItemType.NOTE) } returns listOf("1")
      val viewModel = createViewModel()

      viewModel.onTagSelected("tag1")

      val ready = viewModel.notesScreenState.first().listState as ListState.Ready
      assertEquals(listOf("1"), ready.notes.map { it.id })
    }

  @Test
  fun `onTagSelected twice with the same tag clears the filter`() =
    runTest {
      val n1 = note(id = "1")
      val n2 = note(id = "2")
      every { noteRepository.observeNotes(false, "", NoteSortProcessor.DATE_ZA) } returns flowOf(listOf(n1, n2))
      every { uiNoteListItemAdapter.convert(n1) } returns uiItem(id = "1")
      every { uiNoteListItemAdapter.convert(n2) } returns uiItem(id = "2")
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag1", TaggedItemType.NOTE) } returns listOf("1")
      val viewModel = createViewModel()
      viewModel.onTagSelected("tag1")

      viewModel.onTagSelected("tag1")

      val state = viewModel.notesScreenState.first()
      assertEquals(null, state.selectedTagId)
      val ready = state.listState as ListState.Ready
      assertEquals(2, ready.notes.size)
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
  fun `onSortOrderSelected refreshes the notes widget`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onSortOrderSelected(NoteSortProcessor.DATE_ZA)

      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `onLayoutModeSelected persists the layout mode and updates state`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onLayoutModeSelected(ListLayoutMode.STAGGERED_GRID)

      verify { notePreferences.notesLayoutMode = ListLayoutMode.STAGGERED_GRID }
      assertEquals(ListLayoutMode.STAGGERED_GRID, viewModel.notesScreenState.first().layoutMode)
    }

  @Test
  fun `onLayoutModeSelected back to list restores the original mode`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onLayoutModeSelected(ListLayoutMode.GRID)
      viewModel.onLayoutModeSelected(ListLayoutMode.LIST)

      assertEquals(ListLayoutMode.LIST, viewModel.notesScreenState.first().layoutMode)
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

  /**
   * `notesScreenState` is driven by the repository's notes flow, so it updates in place rather
   * than re-querying on each `.first()` call. Subscribing once here - eagerly, since the whole
   * suite runs on [Dispatchers.Unconfined] - keeps a live view of state.
   */
  private fun TestScope.readyViewModel(
    ids: List<String>,
    isArchived: Boolean = false,
  ): Pair<NotesViewModel, () -> NotesScreenState> {
    val notes = ids.map { note(id = it) }
    every { noteRepository.observeNotes(isArchived, "", NoteSortProcessor.DATE_ZA) } returns flowOf(notes)
    notes.forEachIndexed { index, n -> every { uiNoteListItemAdapter.convert(n) } returns uiItem(id = ids[index]) }
    val viewModel = createViewModel(isArchived = isArchived)
    var latest = NotesScreenState()
    backgroundScope.launch(Dispatchers.Unconfined) {
      viewModel.notesScreenState.collect { latest = it }
    }
    return viewModel to { latest }
  }

  @Test
  fun `onNoteLongClick selects the note and enters selection mode`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"))

      viewModel.onNoteLongClick("1")

      val ready = state().listState as ListState.Ready
      assertEquals(1, state().selectedCount)
      assertEquals(true, ready.notes.first { it.id == "1" }.isSelected)
      assertEquals(false, ready.notes.first { it.id == "2" }.isSelected)
    }

  @Test
  fun `onNoteClick toggles selection while in selection mode instead of opening the note`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"))
      viewModel.onNoteLongClick("1")

      viewModel.onNoteClick("2")

      val ready = state().listState as ListState.Ready
      assertEquals(2, state().selectedCount)
      assertEquals(true, ready.notes.first { it.id == "2" }.isSelected)
      assertEquals(null, viewModel.navigationEvent.value)
    }

  @Test
  fun `onNoteClick exits selection mode automatically after the last note is deselected`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"))
      viewModel.onNoteLongClick("1")

      viewModel.onNoteClick("1")

      assertEquals(0, state().selectedCount)

      viewModel.onNoteClick("2")

      assertEquals(
        NotesViewModel.NavigationEvent.OpenNotePreview("2"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onSelectionCancel clears all selected notes`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"))
      viewModel.onNoteLongClick("1")
      viewModel.onNoteClick("2")

      viewModel.onSelectionCancel()

      assertEquals(0, state().selectedCount)
      val ready = state().listState as ListState.Ready
      assertEquals(false, ready.notes.any { it.isSelected })
    }

  @Test
  fun `onDeleteSelectedClick posts ConfirmDeleteSelected with the selected ids and a formatted title`() =
    runTest {
      every { textProvider.getText(R.string.notes_delete_selected_permanently, 2) } returns "Delete 2 notes permanently?"
      val (viewModel, _) = readyViewModel(listOf("1", "2"))
      viewModel.onNoteLongClick("1")
      viewModel.onNoteClick("2")

      viewModel.onDeleteSelectedClick()

      assertEquals(
        NotesViewModel.NavigationEvent.ConfirmDeleteSelected(setOf("1", "2"), "Delete 2 notes permanently?"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onDeleteSelectedClick does nothing when nothing is selected`() =
    runTest {
      val (viewModel, _) = readyViewModel(listOf("1"))

      viewModel.onDeleteSelectedClick()

      assertEquals(null, viewModel.navigationEvent.value)
    }

  @Test
  fun `deleteSelectedNotes deletes each note, clears selection, refreshes and updates the widget`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"), isArchived = false)
      viewModel.onNoteLongClick("1")
      viewModel.onNoteClick("2")

      viewModel.deleteSelectedNotes(setOf("1", "2"))

      coVerify(exactly = 1) { deleteNoteUseCase("1") }
      coVerify(exactly = 1) { deleteNoteUseCase("2") }
      assertEquals(0, state().selectedCount)
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `deleteSelectedNotes does not update the widget on the archive screen`() =
    runTest {
      val (viewModel, _) = readyViewModel(listOf("1"), isArchived = true)
      viewModel.onNoteLongClick("1")

      viewModel.deleteSelectedNotes(setOf("1"))

      coVerify(exactly = 1) { deleteNoteUseCase("1") }
      verify(exactly = 0) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `onMergeSelectedClick posts ConfirmMergeSelected with ids in tap order`() =
    runTest {
      every { textProvider.getText(R.string.notes_merge_selected_confirm, 2) } returns "Merge 2 notes?"
      val (viewModel, _) = readyViewModel(listOf("1", "2"))
      // Long-press the 2nd note first, then tap the 1st - tap order is the reverse of list order.
      viewModel.onNoteLongClick("2")
      viewModel.onNoteClick("1")

      viewModel.onMergeSelectedClick()

      assertEquals(
        NotesViewModel.NavigationEvent.ConfirmMergeSelected(listOf("2", "1"), "Merge 2 notes?"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onMergeSelectedClick does nothing when fewer than two notes are selected`() =
    runTest {
      val (viewModel, _) = readyViewModel(listOf("1", "2"))
      viewModel.onNoteLongClick("1")

      viewModel.onMergeSelectedClick()

      assertEquals(null, viewModel.navigationEvent.value)
    }

  @Test
  fun `onMergeSelectedClick drops a deselected note from tap order`() =
    runTest {
      every { textProvider.getText(R.string.notes_merge_selected_confirm, 2) } returns "Merge 2 notes?"
      val (viewModel, _) = readyViewModel(listOf("1", "2", "3"))
      viewModel.onNoteLongClick("2")
      viewModel.onNoteClick("1")
      viewModel.onNoteClick("3")
      viewModel.onNoteClick("3") // deselect - should drop out of tap order entirely

      viewModel.onMergeSelectedClick()

      assertEquals(
        NotesViewModel.NavigationEvent.ConfirmMergeSelected(listOf("2", "1"), "Merge 2 notes?"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `mergeSelectedNotes merges, clears selection, refreshes and updates the widget`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"), isArchived = false)
      viewModel.onNoteLongClick("1")
      viewModel.onNoteClick("2")

      viewModel.mergeSelectedNotes(listOf("1", "2"))

      coVerify(exactly = 1) { mergeNotesUseCase(listOf("1", "2")) }
      assertEquals(0, state().selectedCount)
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `onArchiveSelectedClick archives selected notes and clears selection`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"), isArchived = false)
      viewModel.onNoteLongClick("1")
      viewModel.onNoteClick("2")

      viewModel.onArchiveSelectedClick()

      coVerify(exactly = 1) { changeNoteArchiveStateUseCase("1", true) }
      coVerify(exactly = 1) { changeNoteArchiveStateUseCase("2", true) }
      assertEquals(0, state().selectedCount)
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `onArchiveSelectedClick unarchives selected notes on the archive screen`() =
    runTest {
      val (viewModel, _) = readyViewModel(listOf("1"), isArchived = true)
      viewModel.onNoteLongClick("1")

      viewModel.onArchiveSelectedClick()

      coVerify(exactly = 1) { changeNoteArchiveStateUseCase("1", false) }
    }

  @Test
  fun `onArchiveSelectedClick does nothing when nothing is selected`() =
    runTest {
      val (viewModel, _) = readyViewModel(listOf("1"))

      viewModel.onArchiveSelectedClick()

      coVerify(exactly = 0) { changeNoteArchiveStateUseCase(any(), any()) }
    }

  @Test
  fun `applySelectedColor sets the picked color index for each selected note`() =
    runTest {
      val (viewModel, state) = readyViewModel(listOf("1", "2"))
      viewModel.onNoteLongClick("1")
      viewModel.onNoteClick("2")
      val n1 = note(id = "1")
      val n2 = note(id = "2")
      coEvery { noteRepository.getById("1") } returns n1
      coEvery { noteRepository.getById("2") } returns n2

      viewModel.applySelectedColor(25)

      assertEquals(25, n1.note?.color)
      assertEquals(25, n2.note?.color)
      coVerify(exactly = 1) { saveNoteUseCase(n1) }
      coVerify(exactly = 1) { saveNoteUseCase(n2) }
      assertEquals(0, state().selectedCount)
      verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    }

  @Test
  fun `applySelectedColor does nothing when nothing is selected`() =
    runTest {
      val (viewModel, _) = readyViewModel(listOf("1"))

      viewModel.applySelectedColor(3)

      coVerify(exactly = 0) { saveNoteUseCase(any()) }
    }
}
