package com.github.naz013.appwidgets.pomodoro

import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.platform.BuildInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PomodoroWidgetActionHandlerTest {

  private val buildInfo = mockk<BuildInfo>()
  private val engine = mockk<PomodoroTimerEngine>(relaxed = true)
  private val handler = PomodoroWidgetActionHandler(buildInfo, engine)

  @Before
  fun setUp() {
    every { buildInfo.isPro } returns true
  }

  @Test
  fun `starts a focus session on start action`() {
    val handled = handler.handle(PomodoroWidgetAction.START)

    assertTrue(handled)
    verify { engine.start(null) }
  }

  @Test
  fun `pauses resumes and stops through the engine`() {
    handler.handle(PomodoroWidgetAction.PAUSE)
    handler.handle(PomodoroWidgetAction.RESUME)
    handler.handle(PomodoroWidgetAction.STOP)

    verify { engine.pause() }
    verify { engine.resume() }
    verify { engine.stop(false) }
  }

  @Test
  fun `ignores actions when the build is not pro`() {
    every { buildInfo.isPro } returns false

    val handled = handler.handle(PomodoroWidgetAction.START)

    assertFalse(handled)
    verify(exactly = 0) { engine.start(any()) }
  }
}
