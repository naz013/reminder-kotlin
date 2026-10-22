package com.elementary.tasks.routine

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class RoutineRunNotificationFactoryTest {

  private val factory = RoutineRunNotificationFactory()
  private val builder = mockk<NotificationCompat.Builder>(relaxed = true)
  private val toggle = mockk<PendingIntent>()
  private val skip = mockk<PendingIntent>()
  private val stop = mockk<PendingIntent>()
  private val content = mockk<PendingIntent>()
  private val texts = RoutineRunNotificationTexts("Step 2 of 4", "Pause", "Resume", "Skip", "Stop")
  private val intents = RoutineRunNotificationIntents(content, toggle, skip, stop)

  private fun plan(
    paused: Boolean = false,
    promoted: Boolean = false,
    showToggle: Boolean = true,
    timed: Boolean = true,
  ) = RoutineRunNotificationPlan(
    routineTitle = "Morning",
    stepTitle = "Stretch",
    stepNumber = 2,
    stepCount = 4,
    isPaused = paused,
    showToggleAction = showToggle,
    useChronometer = timed && !paused,
    endTimeMillis = 5_000L,
    pausedTimeLabel = if (timed && paused) "01:15" else null,
    showProgress = promoted,
    completedCount = 1,
    promoted = promoted,
  )

  @Test
  fun `uses countdown chronometer while running`() {
    factory.apply(builder, plan(), texts, intents)

    verify { builder.setWhen(5_000L) }
    verify { builder.setUsesChronometer(true) }
    verify { builder.setChronometerCountDown(true) }
    verify { builder.setContentTitle("Morning") }
    verify { builder.setContentText("Stretch") }
    verify { builder.setSubText("Step 2 of 4") }
    verify { builder.setContentIntent(content) }
    verify(exactly = 0) { builder.setRequestPromotedOngoing(any()) }
  }

  @Test
  fun `shows static text and critical text when paused on a promoted device`() {
    factory.apply(builder, plan(paused = true, promoted = true), texts, intents)

    verify { builder.setUsesChronometer(false) }
    verify { builder.setContentText("Stretch - 01:15") }
    verify { builder.setRequestPromotedOngoing(true) }
    verify { builder.setShortCriticalText("01:15") }
    verify { builder.setStyle(any()) }
  }

  @Test
  fun `adds pause skip and stop actions while running`() {
    factory.apply(builder, plan(), texts, intents)

    verify { builder.addAction(0, "Pause", toggle) }
    verify { builder.addAction(0, "Skip", skip) }
    verify { builder.addAction(0, "Stop", stop) }
  }

  @Test
  fun `adds resume action while paused`() {
    factory.apply(builder, plan(paused = true), texts, intents)

    verify { builder.addAction(0, "Resume", toggle) }
    verify(exactly = 0) { builder.addAction(0, "Pause", any()) }
  }

  @Test
  fun `omits the toggle action for an untimed step`() {
    factory.apply(builder, plan(showToggle = false, timed = false), texts, intents)

    verify(exactly = 0) { builder.addAction(0, "Pause", any()) }
    verify(exactly = 0) { builder.addAction(0, "Resume", any()) }
    verify { builder.setUsesChronometer(false) }
    verify { builder.addAction(0, "Skip", skip) }
  }
}
