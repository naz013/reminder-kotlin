package com.github.naz013.logic.group

import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class IsGroupInUseUseCaseTest {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private lateinit var useCase: IsGroupInUseUseCase

  @Before
  fun setUp() {
    coEvery { reminderV2Repository.countActiveByGroupId(any()) } returns 0
    useCase = IsGroupInUseUseCase(reminderV2Repository)
  }

  @Test
  fun `is not in use when the group has no active reminders`() =
    runTest {
      assertEquals(false, useCase("1"))
    }

  @Test
  fun `is in use when the group has active reminders`() =
    runTest {
      coEvery { reminderV2Repository.countActiveByGroupId("1") } returns 3

      assertEquals(true, useCase("1"))
    }

  @Test
  fun `anyInUse is true when only one of the groups has reminders`() =
    runTest {
      coEvery { reminderV2Repository.countActiveByGroupId("2") } returns 1

      assertEquals(true, useCase.anyInUse(listOf("1", "2")))
    }

  @Test
  fun `anyInUse is false when none of the groups has reminders`() =
    runTest {
      assertEquals(false, useCase.anyInUse(listOf("1", "2")))
    }
}
