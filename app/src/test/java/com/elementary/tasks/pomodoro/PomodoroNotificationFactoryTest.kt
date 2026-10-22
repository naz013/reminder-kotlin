package com.elementary.tasks.pomodoro

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class PomodoroNotificationFactoryTest {

  private val factory = PomodoroNotificationFactory()
  private val builder = mockk<NotificationCompat.Builder>(relaxed = true)
  private val intent = mockk<PendingIntent>()
  private val texts = PomodoroNotificationTexts("Work", "Pause", "Stop")

  private fun plan(paused: Boolean, promoted: Boolean) = PomodoroNotificationPlan(
    mode = PomodoroMode.Work,
    isPaused = paused,
    timeLabel = "10:00",
    useChronometer = !paused,
    endTimeMillis = 5_000L,
    showProgress = false,
    progressMax = 1500,
    progressElapsed = 900,
    promoted = promoted,
  )

  @Test
  fun `uses countdown chronometer while running`() {
    factory.apply(builder, plan(paused = false, promoted = false), texts, intent, intent)

    verify { builder.setWhen(5_000L) }
    verify { builder.setUsesChronometer(true) }
    verify { builder.setChronometerCountDown(true) }
    verify(exactly = 0) { builder.setRequestPromotedOngoing(any()) }
  }

  @Test
  fun `shows static time and critical text when paused on a promoted device`() {
    factory.apply(builder, plan(paused = true, promoted = true), texts, intent, intent)

    verify { builder.setUsesChronometer(false) }
    verify { builder.setContentText("10:00") }
    verify { builder.setRequestPromotedOngoing(true) }
    verify { builder.setShortCriticalText("10:00") }
  }

  @Test
  fun `adds pause and stop actions`() {
    factory.apply(builder, plan(paused = false, promoted = false), texts, intent, intent)

    verify { builder.addAction(0, "Pause", intent) }
    verify { builder.addAction(0, "Stop", intent) }
  }
}
