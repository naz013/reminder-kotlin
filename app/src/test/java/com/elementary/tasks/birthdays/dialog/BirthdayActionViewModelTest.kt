package com.elementary.tasks.birthdays.dialog

import com.elementary.tasks.BaseTest
import com.elementary.tasks.birthdays.actions.BirthdayAction
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BirthdayActionViewModelTest : BaseTest() {
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val createBirthdayActionScreenStateUseCase = mockk<CreateBirthdayActionScreenStateUseCase>()
  private val saveBirthdayUseCase = mockk<SaveBirthdayUseCase>(relaxed = true)

  private fun birthday(
    id: String = "42",
    number: String = "555-1234",
  ) = Birthday(uuId = id, name = "Alice", number = number, syncState = SyncState.Synced)

  private fun screenState(id: String = "42") =
    BirthdayActionScreenState(
      id = id,
      header =
        BirthdayActionScreenHeader(
          text = "Alice",
          phoneNumber = "555-1234",
          contactName = null,
          contactPhoto = null,
          birthdayDate = "1 Jan",
          age = "25",
        ),
      mainAction = BirthdayActionScreenActionItem(action = BirthdayAction.Ok, text = "OK", iconRes = 0),
      secondaryActions = emptyList(),
    )

  private fun buildViewModel(id: String = "42") =
    BirthdayActionViewModel(
      id = id,
      birthdayRepository = birthdayRepository,
      dispatcherProvider = mockDispatcherProvider(),
      dateTimeManager = dateTimeManager,
      createBirthdayActionScreenStateUseCase = createBirthdayActionScreenStateUseCase,
      saveBirthdayUseCase = saveBirthdayUseCase,
    )

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { birthdayRepository.getById("42") } returns birthday()
    every { createBirthdayActionScreenStateUseCase(any()) } returns screenState()
  }

  @Test
  fun `loads the screen state on init when the birthday exists`() {
    val viewModel = buildViewModel()

    assertEquals("42", viewModel.state.getOrAwaitValue()?.id)
  }

  @Test
  fun `state stays null when the birthday is not found`() {
    coEvery { birthdayRepository.getById("missing") } returns null

    val viewModel = buildViewModel(id = "missing")

    assertNull(viewModel.state.value)
  }

  @Test
  fun `onActionClick Ok saves the birthday and finishes`() {
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()

    viewModel.onActionClick(BirthdayAction.Ok)

    coVerify(exactly = 1) { saveBirthdayUseCase(any()) }
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(BirthdayActionViewModel.ViewModelEvent.Finish, event?.getContentIfNotHandled())
  }

  @Test
  fun `onActionClick MakeCall saves birthday and emits MakeCall with the phone number`() {
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()

    viewModel.onActionClick(BirthdayAction.MakeCall)

    coVerify(exactly = 1) { saveBirthdayUseCase(any()) }
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(BirthdayActionViewModel.ViewModelEvent.MakeCall("555-1234"), event?.getContentIfNotHandled())
  }

  @Test
  fun `onActionClick MakeCall finishes without saving when phone number is empty`() {
    coEvery { birthdayRepository.getById("42") } returns birthday(number = "")
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()

    viewModel.onActionClick(BirthdayAction.MakeCall)

    coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(BirthdayActionViewModel.ViewModelEvent.Finish, event?.getContentIfNotHandled())
  }

  @Test
  fun `onActionClick SendSms saves birthday and emits SendSms with the phone number`() {
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()

    viewModel.onActionClick(BirthdayAction.SendSms)

    coVerify(exactly = 1) { saveBirthdayUseCase(any()) }
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(BirthdayActionViewModel.ViewModelEvent.SendSms("555-1234"), event?.getContentIfNotHandled())
  }

  @Test
  fun `onActionClick SendSms finishes without saving when phone number is empty`() {
    coEvery { birthdayRepository.getById("42") } returns birthday(number = "")
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()

    viewModel.onActionClick(BirthdayAction.SendSms)

    coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(BirthdayActionViewModel.ViewModelEvent.Finish, event?.getContentIfNotHandled())
  }

  @Test
  fun `onActionClick with unsupported action logs and does nothing`() {
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()

    viewModel.onActionClick(BirthdayAction.Delete)

    coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
  }

  @Test
  fun `onOkClicked finishes without saving when the birthday was deleted meanwhile`() {
    val viewModel = buildViewModel()
    viewModel.state.getOrAwaitValue()
    coEvery { birthdayRepository.getById("42") } returns null

    viewModel.onActionClick(BirthdayAction.Ok)

    coVerify(exactly = 0) { saveBirthdayUseCase(any()) }
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(BirthdayActionViewModel.ViewModelEvent.Finish, event?.getContentIfNotHandled())
  }
}
