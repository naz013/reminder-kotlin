package com.elementary.tasks.birthdays.usecase

import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.TagAssignmentRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteBirthdayUseCaseTest {
  private val birthdayRepository = mockk<BirthdayRepository>(relaxed = true)
  private val notifier = mockk<Notifier>(relaxed = true)
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)
  private val eventOccurrenceRepository = mockk<EventOccurrenceRepository>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)

  private lateinit var useCase: DeleteBirthdayUseCase

  @Before
  fun setUp() {
    useCase =
      DeleteBirthdayUseCase(
        birthdayRepository = birthdayRepository,
        notifier = notifier,
        appWidgetUpdater = appWidgetUpdater,
        scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
        eventOccurrenceRepository = eventOccurrenceRepository,
        tagAssignmentRepository = tagAssignmentRepository,
      )
  }

  @Test
  fun `invoke deletes the birthday and detaches its tag assignments`() =
    runTest {
      useCase.invoke("42")

      coVerify(exactly = 1) { birthdayRepository.delete("42") }
      coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("42") }
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("42", TaggedItemType.BIRTHDAY) }
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase.invoke(
          workType = WorkType.Delete,
          dataType = DataType.Birthdays,
          id = "42",
        )
      }
    }
}
