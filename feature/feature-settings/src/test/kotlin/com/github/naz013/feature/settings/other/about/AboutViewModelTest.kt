package com.github.naz013.feature.settings.other.about

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AboutViewModelTest : BaseTest() {
  private val packageManagerWrapper = mockk<PackageManagerWrapper>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val buildInfo = mockk<BuildInfo>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private lateinit var viewModel: AboutViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { buildInfo.isPro } returns false
    every { textProvider.getString(R.string.app_name) } returns "Reminder"
    every { packageManagerWrapper.getVersionName() } returns "1.2.3"
    every { packageManagerWrapper.getVersionCode() } returns 42L
    every { textProvider.getStringArray(R.array.app_translators) } returns arrayOf("Alice", "Bob")
    every { textProvider.getString(R.string.about_version_info, "1.2.3", 42L) } returns "Version 1.2.3 (42)"

    viewModel =
      AboutViewModel(
        packageManagerWrapper = packageManagerWrapper,
        textProvider = textProvider,
        buildInfo = buildInfo,
        analyticsEventSender = analyticsEventSender,
      )
  }

  @Test
  fun `sends screen used analytics event on creation`() {
    verify { analyticsEventSender.send(any()) }
  }

  @Test
  fun `uses the pro app name when the build is pro`() =
    runTest {
      every { buildInfo.isPro } returns true
      every { textProvider.getString(R.string.app_name_pro) } returns "Reminder PRO"

      val state = viewModel.state.first()

      assertEquals("Reminder PRO", state.appName)
    }

  @Test
  fun `builds version info from package manager values`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("Version 1.2.3 (42)", state.versionInfo)
    }

  @Test
  fun `joins translators into a single string`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("Alice\nBob", state.translators)
    }
}
