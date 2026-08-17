package com.github.naz013.feature.settings.troubleshooting

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TroubleshootingViewModelTest : BaseTest() {
  @get:Rule
  val temporaryFolder = TemporaryFolder()

  private val systemServiceProvider = mockk<SystemServiceProvider>()
  private val packageManagerWrapper = mockk<PackageManagerWrapper>()
  private val featureFlags = mockk<FeatureFlags>()
  private val contextProvider = mockk<ContextProvider>()
  private val cacheUtil = mockk<TroubleshootingCacheUtil>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private lateinit var dataDir: File
  private lateinit var viewModel: TroubleshootingViewModel

  @Before
  override fun setUp() {
    super.setUp()
    dataDir = temporaryFolder.newFolder("dataDir")
    every { systemServiceProvider.providePowerManager() } returns null
    every { packageManagerWrapper.getPackageName() } returns "com.cray.software.justreminder"
    every { featureFlags.isEnabled(any()) } returns false
    every { contextProvider.context.dataDir } returns dataDir

    viewModel = newViewModel()
  }

  // TroubleshootingViewModel reads `systemServiceProvider.providePowerManager()` once, eagerly,
  // into a constructor-time `val` - re-stubbing the provider after the shared `viewModel` from
  // setUp() already exists has no effect, so tests that need a specific PowerManager must build
  // a fresh instance after stubbing.
  private fun newViewModel() =
    TroubleshootingViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      systemServiceProvider = systemServiceProvider,
      packageManagerWrapper = packageManagerWrapper,
      featureFlags = featureFlags,
      contextProvider = contextProvider,
      cacheUtil = cacheUtil,
      analyticsEventSender = analyticsEventSender,
    )

  private fun writeLogFile(): File {
    val logDir = File(dataDir, "files/log")
    logDir.mkdirs()
    val logFile = File(logDir, "session.log")
    logFile.writeText("log contents")
    return logFile
  }

  @Test
  fun `hides send logs when the feature flag is disabled`() =
    runTest {
      writeLogFile()
      every { featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS) } returns false

      val state = viewModel.state.first()

      assertFalse(state.showSendLogs)
    }

  @Test
  fun `hides send logs when the feature flag is enabled but no log file exists`() =
    runTest {
      every { featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS) } returns true

      val state = viewModel.state.first()

      assertFalse(state.showSendLogs)
    }

  @Test
  fun `shows send logs when the feature flag is enabled and a log file exists`() =
    runTest {
      writeLogFile()
      every { featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS) } returns true

      val state = viewModel.state.first()

      assertTrue(state.showSendLogs)
    }

  @Test
  fun `shows battery optimization card when optimizations are not disabled`() =
    runTest {
      val powerManager = mockk<android.os.PowerManager>()
      every { powerManager.isIgnoringBatteryOptimizations(any()) } returns false
      every { systemServiceProvider.providePowerManager() } returns powerManager

      val state = newViewModel().state.first()

      assertTrue(state.showBatteryOptimizationCard)
    }

  @Test
  fun `hides battery optimization card when optimizations are already disabled`() =
    runTest {
      val powerManager = mockk<android.os.PowerManager>()
      every { powerManager.isIgnoringBatteryOptimizations(any()) } returns true
      every { systemServiceProvider.providePowerManager() } returns powerManager

      val state = newViewModel().state.first()

      assertFalse(state.showBatteryOptimizationCard)
    }

  @Test
  fun `hides battery optimization card when the power manager is unavailable`() =
    runTest {
      every { systemServiceProvider.providePowerManager() } returns null

      val state = newViewModel().state.first()

      assertFalse(state.showBatteryOptimizationCard)
    }

  @Test
  fun `shows empty view when optimizations disabled and logs feature is off`() =
    runTest {
      val powerManager = mockk<android.os.PowerManager>()
      every { powerManager.isIgnoringBatteryOptimizations(any()) } returns true
      every { systemServiceProvider.providePowerManager() } returns powerManager
      every { featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS) } returns false

      val state = newViewModel().state.first()

      assertTrue(state.showEmptyView)
    }

  @Test
  fun `hides empty view when logs feature is enabled`() =
    runTest {
      val powerManager = mockk<android.os.PowerManager>()
      every { powerManager.isIgnoringBatteryOptimizations(any()) } returns true
      every { systemServiceProvider.providePowerManager() } returns powerManager
      every { featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS) } returns true

      val state = newViewModel().state.first()

      assertFalse(state.showEmptyView)
    }

  @Test
  fun `packageName delegates to the package manager wrapper`() {
    assertEquals("com.cray.software.justreminder", viewModel.packageName())
  }

  @Test
  fun `onOpenOptimizationSettingsClicked sends analytics and posts OpenOptimizationSettings`() {
    viewModel.onOpenOptimizationSettingsClicked()

    verify { analyticsEventSender.send(any()) }
    val event = viewModel.event.value?.peekContent()
    assertEquals(TroubleshootingViewModel.ViewModelEvent.OpenOptimizationSettings, event)
  }

  @Test
  fun `sendLogs does not emit when no log file is present`() {
    viewModel.sendLogs()

    assertEquals(null, viewModel.event.value)
  }

  @Test
  fun `sendLogs caches the log file and emits SendLogs`() {
    val logFile = writeLogFile()
    val cachedFile = mockk<File>()
    every { cacheUtil.cacheFile(any<File>()) } returns cachedFile

    viewModel.sendLogs()

    val event = viewModel.event.value?.peekContent() as TroubleshootingViewModel.ViewModelEvent.SendLogs
    assertEquals(cachedFile, event.file)
    verify { cacheUtil.cacheFile(logFile) }
  }

  @Test
  fun `sendLogs does not emit when caching the log file fails`() {
    writeLogFile()
    every { cacheUtil.cacheFile(any<File>()) } returns null

    viewModel.sendLogs()

    assertEquals(null, viewModel.event.value)
  }
}
