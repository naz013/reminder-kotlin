package com.github.naz013.logic.demodata

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.birthday.SaveBirthdayUseCase
import com.github.naz013.logic.note.InsertDemoNotesUseCase
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.repository.GroupV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class InsertDemoDataUseCaseTest {
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val saveBirthdayUseCase = mockk<SaveBirthdayUseCase>(relaxed = true)
  private val insertDemoNotesUseCase = mockk<InsertDemoNotesUseCase>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val dateTimeManager = mockk<DateTimeManager>()

  private lateinit var useCase: InsertDemoDataUseCase

  @Before
  fun setUp() {
    every { dateTimeManager.getCurrentDateTime() } returns LocalDateTime.now()
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.formatBirthdayDate(any()) } returns "1962-01-01"
    coEvery { groupV2Repository.defaultGroup() } returns mockk<GroupV2> { every { uuId } returns "group-1" }

    useCase =
      InsertDemoDataUseCase(
        activateReminderUseCase = activateReminderUseCase,
        saveBirthdayUseCase = saveBirthdayUseCase,
        insertDemoNotesUseCase = insertDemoNotesUseCase,
        groupV2Repository = groupV2Repository,
        dateTimeManager = dateTimeManager,
      )
  }

  @Test
  fun `invoke activates four reminders that actually get scheduled`() =
    runTest {
      useCase()

      coVerify(exactly = 4) { activateReminderUseCase(any<ReminderV2>(), startAnyway = true) }
    }

  @Test
  fun `invoke saves exactly one birthday`() =
    runTest {
      useCase()

      coVerify(exactly = 1) { saveBirthdayUseCase(any<Birthday>()) }
    }

  @Test
  fun `invoke inserts demo notes`() =
    runTest {
      useCase()

      coVerify(exactly = 1) { insertDemoNotesUseCase() }
    }
}
