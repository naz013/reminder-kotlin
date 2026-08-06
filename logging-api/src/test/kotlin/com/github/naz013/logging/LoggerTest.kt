package com.github.naz013.logging

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoggerTest {
  private val loggerProvider = mockk<LoggerProvider>(relaxed = true)
  private val firebaseLogger = mockk<FirebaseLogger>(relaxed = true)

  @Before
  fun setUp() {
    Logger.loggingEnabled = true
    Logger.reportingEnabled = true
    Logger.initLogging(isDebug = false, loggerProvider = loggerProvider, firebaseLogger = firebaseLogger)
  }

  @Test
  fun `d does not forward to the provider when not in debug mode`() {
    Logger.d("tag", "message")

    verify(exactly = 0) { loggerProvider.debug(any(), any()) }
  }

  @Test
  fun `d forwards to the provider when in debug mode`() {
    Logger.initLogging(isDebug = true, loggerProvider = loggerProvider, firebaseLogger = firebaseLogger)

    Logger.d("tag", "message")

    verify { loggerProvider.debug("tag", "message") }
  }

  @Test
  fun `v is suppressed when logging is disabled`() {
    Logger.loggingEnabled = false

    Logger.v("tag", "message")

    verify(exactly = 0) { loggerProvider.verbose(any(), any()) }
  }

  @Test
  fun `i always reports to firebase and to the provider`() {
    Logger.i("tag", "message")

    verify { loggerProvider.info("tag", "message") }
    verify { firebaseLogger.logEvent("message") }
  }

  @Test
  fun `i skips firebase reporting when reporting is disabled`() {
    Logger.reportingEnabled = false

    Logger.i("tag", "message")

    verify(exactly = 0) { firebaseLogger.logEvent(any()) }
  }

  @Test
  fun `private masks the value outside of debug mode`() {
    every { loggerProvider.info(any(), any()) } returns Unit

    assertEquals("*****", Logger.private("secret"))
  }

  @Test
  fun `private reveals the value in debug mode`() {
    Logger.initLogging(isDebug = true, loggerProvider = loggerProvider, firebaseLogger = firebaseLogger)

    assertEquals("secret", Logger.private("secret"))
  }

  @Test
  fun `private returns Null for a null value regardless of debug mode`() {
    assertEquals("Null", Logger.private(null))
  }

  @Test
  fun `data reports Null, Empty or the raw value`() {
    assertEquals("Null", Logger.data(null))
    assertEquals("Empty", Logger.data(""))
    assertEquals("hello", Logger.data("hello"))
  }
}
