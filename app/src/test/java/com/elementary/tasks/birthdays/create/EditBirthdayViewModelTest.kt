package com.elementary.tasks.birthdays.create

import android.os.Bundle
import com.elementary.tasks.BaseTest
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.os.data.ContactData
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
  private val uiBirthdayEditAdapter = UiBirthdayEditAdapter()

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getCurrentDate() } returns LocalDate.now()
    every { dateTimeManager.formatBirthdayFullDateForUi(any()) } returns "formatted"
    every { dateTimeManager.formatBirthdayDate(any()) } returns "1999-10-01"
    every { dateTimeManager.getNowGmtDateTime() } returns "2024-01-01T00:00:00"
    every { contactsReader.getIdFromNumber(any()) } returns 0L
  }

  private fun createViewModel(id: String = ""): EditBirthdayViewModel =
    EditBirthdayViewModel(
      id = id,
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
    )

  /** [Bundle] isn't backed by real Android framework code under plain JUnit, so its methods must
   *  be mocked rather than exercised via a real instance. */
  private fun bundleWithIntentItem(): Bundle =
    mockk<Bundle> {
      every { getBoolean(IntentKeys.INTENT_ITEM, any()) } returns true
      every { getBoolean(IntentKeys.INTENT_DEEP_LINK, any()) } returns false
    }

  @Test
  fun `leaves state empty when no birthday exists for id`() =
    runTest {
      coEvery { birthdayRepository.getById("42") } returns null

      val viewModel = createViewModel(id = "42")

      assertEquals("", viewModel.state.value.name)
      assertEquals(false, viewModel.state.value.canDelete)
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

      val viewModel = createViewModel(id = "42")

      assertEquals("Alice", viewModel.state.value.name)
      assertEquals("555", viewModel.state.value.number)
      assertEquals("Alice Contact", viewModel.state.value.contactName)
      assertEquals(true, viewModel.state.value.canDelete)
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
      coEvery { birthdayRepository.getById("") } returns null
      coEvery { birthdayRepository.getById("from-file") } returns null

      val viewModel = createViewModel()
      viewModel.checkArguments(bundleWithIntentItem())

      assertEquals("Bob", viewModel.state.value.name)
      assertEquals(false, viewModel.state.value.canDelete)
    }

  @Test
  fun `onSaveClick shows copy conflict dialog when the imported birthday already exists in db`() =
    runTest {
      val birthday = Birthday(uuId = "dup", name = "Bob", date = "1999-10-01", syncState = SyncState.Synced)
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, Birthday::class.java) } returns birthday
      every { dateTimeManager.parseBirthdayDate("1999-10-01") } returns LocalDate.of(1999, 10, 1)
      coEvery { birthdayRepository.getById("") } returns null
      coEvery { birthdayRepository.getById("dup") } returns birthday

      val viewModel = createViewModel()
      viewModel.checkArguments(bundleWithIntentItem())

      viewModel.onSaveClick()

      assertEquals(EditBirthdayDialog.CopyConflict, viewModel.state.value.dialog)
      coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    }

  @Test
  fun `onSaveClick sets name error and does not save when name is blank`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      val viewModel = createViewModel()

      viewModel.onSaveClick()

      assertEquals(true, viewModel.state.value.nameError)
      coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    }

  @Test
  fun `onNameChanged clears a previous name error`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      val viewModel = createViewModel()
      viewModel.onSaveClick()

      viewModel.onNameChanged("Charlie")

      assertEquals(false, viewModel.state.value.nameError)
      assertEquals("Charlie", viewModel.state.value.name)
    }

  @Test
  fun `onSaveClick saves a new birthday and posts SAVED`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      val viewModel = createViewModel()
      viewModel.onNameChanged("Charlie")

      viewModel.onSaveClick()

      coVerify(exactly = 1) { saveBirthdayUseCase(any()) }
      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.SAVED, event?.getContentIfNotHandled())
    }

  @Test
  fun `onContactPicked fills the number and fills a blank name from the contact`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      every { contactsReader.getIdFromNumber("555") } returns 7L
      every { contactsReader.getPhotoBitmap(7L) } returns null
      val viewModel = createViewModel()

      viewModel.onContactPicked(ContactData(name = "Dana", phone = "555"))

      assertEquals("555", viewModel.state.value.number)
      assertEquals("Dana", viewModel.state.value.name)
    }

  @Test
  fun `onContactPicked does not overwrite an already entered name`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      every { contactsReader.getIdFromNumber("555") } returns 7L
      every { contactsReader.getPhotoBitmap(7L) } returns null
      val viewModel = createViewModel()
      viewModel.onNameChanged("Charlie")

      viewModel.onContactPicked(ContactData(name = "Dana", phone = "555"))

      assertEquals("Charlie", viewModel.state.value.name)
    }

  @Test
  fun `onDeleteConfirmed deletes and posts DELETED when delete is allowed`() =
    runTest {
      val birthday = Birthday(uuId = "42", name = "Alice", date = "1999-10-01", syncState = SyncState.Synced)
      coEvery { birthdayRepository.getById("42") } returns birthday
      every { dateTimeManager.parseBirthdayDate("1999-10-01") } returns LocalDate.of(1999, 10, 1)
      val viewModel = createViewModel(id = "42")

      viewModel.onDeleteMenuClick()
      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteBirthdayUseCase("42") }
      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.DELETED, event?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteConfirmed does nothing when delete is not allowed`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      val viewModel = createViewModel()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
      assertNull(viewModel.resultEvent.value)
    }

  @Test
  fun `onYearCheckChanged updates ignoreYear and reformats the date`() =
    runTest {
      coEvery { birthdayRepository.getById("") } returns null
      every { dateTimeManager.formatBirthdayDateForUi(any()) } returns "no-year"
      val viewModel = createViewModel()

      viewModel.onYearCheckChanged(true)

      assertEquals(true, viewModel.state.value.ignoreYear)
      assertEquals("no-year", viewModel.state.value.dateText)
    }
}
