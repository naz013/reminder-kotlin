package com.github.naz013.digest

import android.content.Context
import android.content.SharedPreferences
import com.google.common.util.concurrent.Futures
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.Summarizer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DigestCapabilityCheckerImplTest {
  private val context = mockk<Context>(relaxed = true)
  private val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
  private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
  private val summarizer = mockk<Summarizer>(relaxed = true)
  private lateinit var checker: DigestCapabilityCheckerImpl

  @Before
  fun setUp() {
    Locale.setDefault(Locale.US)
    every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
    every { sharedPreferences.edit() } returns editor
    every { editor.putBoolean(any(), any()) } returns editor
    mockkStatic(Summarization::class)
    every { Summarization.getClient(any()) } returns summarizer
    checker = DigestCapabilityCheckerImpl(context)
  }

  @After
  fun tearDown() {
    Locale.setDefault(Locale.US)
    unmockkStatic(Summarization::class)
  }

  @Test
  fun `refreshCapability is false when the device language is unsupported, without calling ML Kit`() = runTest {
    Locale.setDefault(Locale.FRANCE)

    val result = checker.refreshCapability()

    assertFalse(result)
    verify(exactly = 0) { Summarization.getClient(any()) }
  }

  @Test
  fun `refreshCapability is false when ML Kit reports UNAVAILABLE`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.UNAVAILABLE)

    val result = checker.refreshCapability()

    assertFalse(result)
  }

  @Test
  fun `refreshCapability is true when ML Kit reports AVAILABLE`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.AVAILABLE)

    val result = checker.refreshCapability()

    assertTrue(result)
  }

  @Test
  fun `refreshCapability is true when ML Kit reports DOWNLOADABLE - hardware still qualifies`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.DOWNLOADABLE)

    val result = checker.refreshCapability()

    assertTrue(result)
  }

  @Test
  fun `refreshCapability caches the fresh result`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.AVAILABLE)

    checker.refreshCapability()

    verify { editor.putBoolean("device_capable", true) }
  }

  @Test
  fun `isDeviceCapableCached reads the last-known cached value`() {
    every { sharedPreferences.getBoolean("device_capable", false) } returns true

    assertTrue(checker.isDeviceCapableCached())
  }
}
