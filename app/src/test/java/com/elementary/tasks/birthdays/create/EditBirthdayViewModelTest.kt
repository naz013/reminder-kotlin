package com.elementary.tasks.birthdays.create

import com.elementary.tasks.BaseTest
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.os.data.ContactData
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class EditBirthdayViewModelTest : BaseTest() {
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val contactsReader = mockk<ContactsReader>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val intentDataReader = mockk<IntentDataReader>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>(relaxed = true)
  private val saveBirthdayUseCase = mockk<SaveBirthdayUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val uiBirthdayEditAdapter = UiBirthdayEditAdapter()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val toggleTagAssignmentUseCase = mockk<ToggleTagAssignmentUseCase>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getCurrentDate() } returns LocalDate.now()
    every { dateTimeManager.formatBirthdayFullDateForUi(any()) } returns "formatted"
    every { dateTimeManager.formatBirthdayDate(any()) } returns "1999-10-01"
    every { dateTimeManager.getNowGmtDateTime() } returns "2024-01-01T00:00:00"
    every { contactsReader.getIdFromNumber(any()) } returns 0L
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
  }

  private fun createViewModel(
    id: String? = null,
    fromIntentData: Boolean = false,
    prefillDateEpochDay: Long? = null,
  ): EditBirthdayViewModel =
    EditBirthdayViewModel(
      key = BirthdaysNavKey.Edit(id = id, fromIntentData = fromIntentData, prefillDateEpochDay = prefillDateEpochDay),
      birthdayRepository = birthdayRepository,
      dispatcherProvider = mockDispatcherProvider(),
      contactsReader = contactsReader,
      dateTimeManager = dateTimeManager,
      analyticsEventSender = analyticsEventSender,
      uiBirthdayEditAdapter = uiBirthdayEditAdapter,
      intentDataReader = intentDataReader,
      uiBirthdayDateFormatter = UiBirthdayDateFormatter(dateTimeManager),
      deleteBirthdayUseCase = deleteBirthdayUseCase,
      saveBirthdayUseCase = saveBirthdayUseCase,
      textProvider = textProvider,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      toggleTagAssignmentUseCase = toggleTagAssignmentUseCase,
      tagChipStateAdapter = tagChipStateAdapter,
    )

  @Test
  fun `leaves state empty when no birthday exists for id`() =
    runTest {
      coEvery { birthdayRepository.getById("42") } returns null

      val state = createViewModel(id = "42").state.first()

      assertEquals("", state.name)
      assertEquals(false, state.canDelete)
    }

  @Test
  fun `loads existing birthday from the repository into state`() =
    runTest {
      val birthday =
        Birthday(
          uuId = "42",
          name = "Alice",
          number = "555",
          date = "1999-10-01",
          ignoreYear = false,
          syncState = SyncState.Synced,
        )
      coEvery { birthdayRepository.getById("42") } returns birthday
      every { dateTimeManager.parseBirthdayDate("1999-10-01") } returns LocalDate.of(1999, 10, 1)
      every { contactsReader.getIdFromNumber("555") } returns 7L
      every { contactsReader.getPhotoBitmap(7L) } returns null
      every { contactsReader.getNameFromNumber("555") } returns "Alice Contact"

      val state = createViewModel(id = "42").state.first()

      assertEquals("Alice", state.name)
      assertEquals("555", state.number)
      assertEquals("Alice Contact", state.contactName)
      assertEquals(true, state.canDelete)
    }

  @Test
  fun `checkArguments loads birthday from intent and disables delete for imported items`() =
    runTest {
      val birthday =
        Birthday(
          uuId = "from-file",
          name = "Bob",
          date = "1999-10-01",
          syncState = SyncState.Synced,
        )
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, Birthday::class.java) } returns birthday
      every { dateTimeManager.parseBirthdayDate("1999-10-01") } returns LocalDate.of(1999, 10, 1)
      coEvery { birthdayRepository.getById("from-file") } returns null

      val state = createViewModel(fromIntentData = true).state.first()

      assertEquals("Bob", state.name)
      assertEquals(false, state.canDelete)
    }

  @Test
  fun `onSaveClick shows copy conflict dialog when the imported birthday already exists in db`() =
    runTest {
      val birthday = Birthday(uuId = "dup", name = "Bob", date = "1999-10-01", syncState = SyncState.Synced)
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, Birthday::class.java) } returns birthday
      every { dateTimeManager.parseBirthdayDate("1999-10-01") } returns LocalDate.of(1999, 10, 1)
      coEvery { birthdayRepository.getById("dup") } returns birthday

      val viewModel = createViewModel(fromIntentData = true)
      // First collection triggers checkArguments() -> onIntent(), loading "Bob"/marking hasSameInDb
      // - needed before onSaveClick() so it takes the copy-conflict branch instead of saving.
      viewModel.state.first()

      viewModel.onSaveClick()

      // A second, fresh collection re-runs checkArguments()/onIntent(), but neither touches
      // `dialog`, so it doesn't clobber the CopyConflict value onSaveClick() just set.
      assertEquals(EditBirthdayDialog.CopyConflict, viewModel.state.first().dialog)
      coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    }

  @Test
  fun `onSaveClick sets name error and does not save when name is blank`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onSaveClick()

      assertEquals(true, viewModel.state.first().nameError)
      coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    }

  @Test
  fun `onNameChanged clears a previous name error`() =
    runTest {
      val viewModel = createViewModel()
      viewModel.onSaveClick()

      viewModel.onNameChanged("Charlie")

      val state = viewModel.state.first()
      assertEquals(false, state.nameError)
      assertEquals("Charlie", state.name)
    }

  @Test
  fun `onSaveClick saves a new birthday and posts MoveBack`() =
    runTest {
      // A default id-less key gets a random UUID assigned in EditBirthdayViewModel's init block -
      // performSave() looks that id up (to decide new-vs-existing), so it must be stubbed generically.
      coEvery { birthdayRepository.getById(any()) } returns null
      val viewModel = createViewModel()
      viewModel.onNameChanged("Charlie")

      viewModel.onSaveClick()

      coVerify(exactly = 1) { saveBirthdayUseCase(any()) }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(EditBirthdayViewModel.ViewModelEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `onContactPicked fills the number and fills a blank name from the contact`() =
    runTest {
      every { contactsReader.getIdFromNumber("555") } returns 7L
      every { contactsReader.getPhotoBitmap(7L) } returns null
      val viewModel = createViewModel()

      viewModel.onContactPicked(ContactData(name = "Dana", phone = "555"))

      val state = viewModel.state.first()
      assertEquals("555", state.number)
      assertEquals("Dana", state.name)
    }

  @Test
  fun `onContactPicked does not overwrite an already entered name`() =
    runTest {
      every { contactsReader.getIdFromNumber("555") } returns 7L
      every { contactsReader.getPhotoBitmap(7L) } returns null
      val viewModel = createViewModel()
      viewModel.onNameChanged("Charlie")

      viewModel.onContactPicked(ContactData(name = "Dana", phone = "555"))

      assertEquals("Charlie", viewModel.state.first().name)
    }

  @Test
  fun `onDeleteConfirmed deletes and posts MoveBack when delete is allowed`() =
    runTest {
      val birthday = Birthday(uuId = "42", name = "Alice", date = "1999-10-01", syncState = SyncState.Synced)
      coEvery { birthdayRepository.getById("42") } returns birthday
      every { dateTimeManager.parseBirthdayDate("1999-10-01") } returns LocalDate.of(1999, 10, 1)
      val viewModel = createViewModel(id = "42")
      // Trigger checkArguments() -> load() so canDelete is populated before deleting.
      viewModel.state.first()

      viewModel.onDeleteMenuClick()
      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteBirthdayUseCase("42") }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(EditBirthdayViewModel.ViewModelEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteConfirmed does nothing when delete is not allowed`() =
    runTest {
      val viewModel = createViewModel()
      viewModel.state.first()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
      assertNull(viewModel.event.value)
    }

  @Test
  fun `onYearCheckChanged updates ignoreYear and reformats the date`() =
    runTest {
      every { dateTimeManager.formatBirthdayDateForUi(any()) } returns "no-year"
      val viewModel = createViewModel()

      viewModel.onYearCheckChanged(true)

      val state = viewModel.state.first()
      assertEquals(true, state.ignoreYear)
      assertEquals("no-year", state.dateText)
    }
}
