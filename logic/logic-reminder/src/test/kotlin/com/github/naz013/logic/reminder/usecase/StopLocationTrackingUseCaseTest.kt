package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.location.LocationTrackingApi
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class StopLocationTrackingUseCaseTest {
  private lateinit var locationTrackingApi: LocationTrackingApi
  private lateinit var reminderV2Repository: ReminderV2Repository

  private lateinit var useCase: StopLocationTrackingUseCase

  @Before
  fun setUp() {
    locationTrackingApi = mockk(relaxed = true)
    reminderV2Repository = mockk()

    useCase = StopLocationTrackingUseCase(locationTrackingApi, reminderV2Repository)
  }

  private fun place() = Place(latitude = 1.0, longitude = 1.0, syncState = SyncState.Synced)

  private fun gpsReminder(
    uuId: String,
    isNotificationShown: Boolean,
  ) = ReminderV2(
    uuId = uuId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    location = LocationSettings(isNotificationShown = isNotificationShown),
    places = listOf(place()),
  )

  @Test
  fun `stops tracking when there are no active gps reminders left`() =
    runTest {
      coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns emptyList()

      useCase(gpsReminder("r1", isNotificationShown = true), isPaused = false)

      coVerify(exactly = 1) { locationTrackingApi.stopTracking() }
    }

  @Test
  fun `keeps tracking when another gps reminder is still pending`() =
    runTest {
      val firedReminder = gpsReminder("r1", isNotificationShown = true)
      val pendingReminder = gpsReminder("r2", isNotificationShown = false)
      coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns
        listOf(firedReminder, pendingReminder)

      useCase(firedReminder, isPaused = false)

      coVerify(exactly = 0) { locationTrackingApi.stopTracking() }
    }

  @Test
  fun `stops tracking when every remaining gps reminder has already fired`() =
    runTest {
      val first = gpsReminder("r1", isNotificationShown = true)
      val second = gpsReminder("r2", isNotificationShown = true)
      coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(first, second)

      useCase(first, isPaused = false)

      coVerify(exactly = 1) { locationTrackingApi.stopTracking() }
    }

  @Test
  fun `pausing a reminder that has not fired yet excludes it from its own pending check`() =
    runTest {
      val pausedReminder = gpsReminder("r1", isNotificationShown = false)
      coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(pausedReminder)

      useCase(pausedReminder, isPaused = true)

      coVerify(exactly = 1) { locationTrackingApi.stopTracking() }
    }

  @Test
  fun `pausing one reminder keeps tracking alive for a different still-pending reminder`() =
    runTest {
      val pausedReminder = gpsReminder("r1", isNotificationShown = false)
      val otherPendingReminder = gpsReminder("r2", isNotificationShown = false)
      coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns
        listOf(pausedReminder, otherPendingReminder)

      useCase(pausedReminder, isPaused = true)

      coVerify(exactly = 0) { locationTrackingApi.stopTracking() }
    }
}
