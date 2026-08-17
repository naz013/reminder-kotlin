package com.github.naz013.feature.settings.other.whatsnew

import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.settings.R
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.StringUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WhatsNewViewModelTest : BaseTest() {
  private val packageManagerWrapper = mockk<PackageManagerWrapper>()
  private val textProvider = mockk<TextProvider>()
  private val buildInfo = mockk<BuildInfo>()

  private lateinit var viewModel: WhatsNewViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { packageManagerWrapper.getVersionName() } returns "1.2.3"
    every { textProvider.getString(R.string.whats_new_text) } returns "Whats new text"
    every { buildInfo.buildDate } returns "june 1, 2026"

    viewModel =
      WhatsNewViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        packageManagerWrapper = packageManagerWrapper,
        textProvider = textProvider,
        buildInfo = buildInfo,
      )
  }

  @Test
  fun `loads version, build date and whats new text on first collection`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("1.2.3", state.versionName)
      assertEquals(StringUtils.capitalize("june 1, 2026"), state.lastUpdated)
      assertEquals("Whats new text", state.whatsNewText)
    }

  @Test
  fun `reloads the version name on every collection`() =
    runTest {
      assertEquals("1.2.3", viewModel.state.first().versionName)

      every { packageManagerWrapper.getVersionName() } returns "4.5.6"

      assertEquals("4.5.6", viewModel.state.first().versionName)
    }
}
