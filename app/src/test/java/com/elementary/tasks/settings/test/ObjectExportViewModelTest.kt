package com.elementary.tasks.settings.test

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.elementary.tasks.BaseTest
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataConverter
import com.github.naz013.files.FileConfig
import com.github.naz013.files.model.SharedNote
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.io.IOException
import java.io.OutputStream

class ObjectExportViewModelTest : BaseTest() {
  private val contextProvider = mockk<ContextProvider>()
  private val context = mockk<Context>()
  private val contentResolver = mockk<ContentResolver>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val noteRepository = mockk<NoteRepository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val placeRepository = mockk<PlaceRepository>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val dataConverter = mockk<DataConverter>()

  private lateinit var viewModel: ObjectExportViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { context.contentResolver } returns contentResolver
    every { contextProvider.context } returns context
    coEvery { reminderV2Repository.getAll() } returns emptyList()

    viewModel = newViewModel()
  }

  private fun newViewModel() =
    ObjectExportViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      contextProvider = contextProvider,
      reminderV2Repository = reminderV2Repository,
      noteRepository = noteRepository,
      birthdayRepository = birthdayRepository,
      placeRepository = placeRepository,
      groupV2Repository = groupV2Repository,
      dataConverter = dataConverter,
    )

  private fun reminder(summary: String) =
    ReminderV2(summary = summary, schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  @Test
  fun `loads reminder items by default on creation`() {
    val reminder = reminder("Buy milk")
    coEvery { reminderV2Repository.getAll() } returns listOf(reminder)

    val vm = newViewModel()

    val items = vm.state.value.items
    assertEquals(1, items.size)
    assertEquals(reminder.uuId, items[0].id)
    assertEquals("Buy milk\nID: ${reminder.uuId}", items[0].title)
  }

  @Test
  fun `onObjectTypeSelected Note reloads items from the note repository`() {
    val note = NoteWithImages()
    coEvery { noteRepository.getAll() } returns listOf(note)

    viewModel.onObjectTypeSelected(ObjectExportType.Note)

    assertEquals(ObjectExportType.Note, viewModel.state.value.objectType)
    assertEquals(1, viewModel.state.value.items.size)
  }

  @Test
  fun `onObjectTypeSelected Birthday reloads items from the birthday repository`() {
    val birthday = Birthday(uuId = "b1", name = "Alice", syncState = SyncState.Synced)
    coEvery { birthdayRepository.getAll() } returns listOf(birthday)

    viewModel.onObjectTypeSelected(ObjectExportType.Birthday)

    val items = viewModel.state.value.items
    assertEquals("b1", items[0].id)
    assertEquals("Alice\nID: b1", items[0].title)
  }

  @Test
  fun `onObjectTypeSelected Place reloads items from the place repository`() {
    val place = Place(id = "p1", name = "Home", syncState = SyncState.Synced)
    coEvery { placeRepository.getAll() } returns listOf(place)

    viewModel.onObjectTypeSelected(ObjectExportType.Place)

    val items = viewModel.state.value.items
    assertEquals("p1", items[0].id)
    assertEquals("Home\nID: p1", items[0].title)
  }

  @Test
  fun `onObjectTypeSelected Group reloads items from the group repository`() {
    val group = GroupV2(uuId = "g1", title = "Work", createdAt = LocalDateTime.now())
    coEvery { groupV2Repository.getAll() } returns listOf(group)

    viewModel.onObjectTypeSelected(ObjectExportType.Group)

    val items = viewModel.state.value.items
    assertEquals("g1", items[0].id)
    assertEquals("Work\nID: g1", items[0].title)
  }

  @Test
  fun `onItemClick requests a save location with the reminder file extension`() {
    val item = ObjectExportItem(id = "r1", title = "Buy milk")

    viewModel.onItemClick(item)

    val event =
      viewModel.navigationEvent.value?.peekContent() as ObjectExportEvent.RequestSaveLocation
    assertEquals("Buy milk" + FileConfig.FILE_NAME_REMINDER_V2, event.fileName)
    assertEquals("r1", event.itemId)
  }

  @Test
  fun `onItemClick requests a save location with the note file extension`() {
    coEvery { noteRepository.getAll() } returns emptyList()
    viewModel.onObjectTypeSelected(ObjectExportType.Note)
    val item = ObjectExportItem(id = "n1", title = "My note")

    viewModel.onItemClick(item)

    val event =
      viewModel.navigationEvent.value?.peekContent() as ObjectExportEvent.RequestSaveLocation
    assertEquals("My note" + SharedNote.FILE_EXTENSION, event.fileName)
  }

  @Test
  fun `onSaveLocationPicked does not emit when the object is missing`() =
    runTest {
      coEvery { reminderV2Repository.getById("missing") } returns null

      viewModel.onSaveLocationPicked("missing", mockk<Uri>())

      assertNull(viewModel.navigationEvent.value)
    }

  @Test
  fun `onSaveLocationPicked does not emit when the output stream cannot be opened`() =
    runTest {
      val reminder = reminder("Buy milk")
      coEvery { reminderV2Repository.getById(reminder.uuId) } returns reminder
      val uri = mockk<Uri>()
      every { contentResolver.openOutputStream(uri) } returns null

      viewModel.onSaveLocationPicked(reminder.uuId, uri)

      assertNull(viewModel.navigationEvent.value)
    }

  @Test
  fun `onSaveLocationPicked saves a non-note object and emits ObjectSaved`() =
    runTest {
      val reminder = reminder("Buy milk")
      coEvery { reminderV2Repository.getById(reminder.uuId) } returns reminder
      val uri = mockk<Uri>()
      val outputStream = mockk<OutputStream>(relaxed = true)
      every { contentResolver.openOutputStream(uri) } returns outputStream
      coEvery { dataConverter.toOutputStream(reminder, outputStream) } returns Unit

      viewModel.onSaveLocationPicked(reminder.uuId, uri)

      assertEquals(ObjectExportEvent.ObjectSaved, viewModel.navigationEvent.value?.peekContent())
    }

  @Test
  fun `onSaveLocationPicked does not emit when saving the stream fails`() =
    runTest {
      val reminder = reminder("Buy milk")
      coEvery { reminderV2Repository.getById(reminder.uuId) } returns reminder
      val uri = mockk<Uri>()
      val outputStream = mockk<OutputStream>(relaxed = true)
      every { contentResolver.openOutputStream(uri) } returns outputStream
      coEvery { dataConverter.toOutputStream(reminder, outputStream) } throws IOException("disk full")

      viewModel.onSaveLocationPicked(reminder.uuId, uri)

      assertNull(viewModel.navigationEvent.value)
    }

  @Test
  fun `onSaveLocationPicked converts and saves a note, emitting ObjectSaved`() =
    runTest {
      coEvery { noteRepository.getAll() } returns emptyList()
      viewModel.onObjectTypeSelected(ObjectExportType.Note)
      val note = NoteWithImages()
      val sharedNote = SharedNote()
      coEvery { noteRepository.getById("n1") } returns note
      val uri = mockk<Uri>()
      val outputStream = mockk<OutputStream>(relaxed = true)
      every { contentResolver.openOutputStream(uri) } returns outputStream
      coEvery { dataConverter.toOutputStream(sharedNote, outputStream) } returns Unit

      viewModel.onSaveLocationPicked("n1", uri)

      assertEquals(ObjectExportEvent.ObjectSaved, viewModel.navigationEvent.value?.peekContent())
    }
}
