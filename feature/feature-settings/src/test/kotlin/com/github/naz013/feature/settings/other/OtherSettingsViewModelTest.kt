package com.github.naz013.feature.settings.other

import android.content.Intent
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.Permissions
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.platform.SystemInfo
import com.github.naz013.reviews.AppSource
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OtherSettingsViewModelTest : BaseTest() {
  private val packageManagerWrapper = mockk<PackageManagerWrapper>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>()
  private val systemInfo = mockk<SystemInfo>()
  private val featureFlags = mockk<FeatureFlags>()
  private val buildInfo = mockk<BuildInfo>(relaxed = true)

  private lateinit var viewModel: OtherSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(Permissions)
    every { contextProvider.context } returns mockk(relaxed = true)
    every { systemInfo.is15 } returns false
    every { systemInfo.is13 } returns false
    every { systemInfo.is16 } returns true
    every { systemInfo.currentPackageName } returns "com.cray.software.justreminder"
    every { featureFlags.isEnabled(any()) } returns false
    every { packageManagerWrapper.getVersionName() } returns "1.0.0"
    // All permissions granted by default, so the missing-permissions list starts empty.
    every { Permissions.checkPermission(any(), any<String>()) } returns true

    viewModel =
      OtherSettingsViewModel(
        packageManagerWrapper = packageManagerWrapper,
        textProvider = textProvider,
        analyticsEventSender = analyticsEventSender,
        contextProvider = contextProvider,
        systemInfo = systemInfo,
        featureFlags = featureFlags,
        buildInfo = buildInfo,
      )
  }

  @After
  override fun tearDown() {
    super.tearDown()
    unmockkObject(Permissions)
  }

  @Test
  fun `sends screen used analytics event on creation`() {
    verify { analyticsEventSender.send(any()) }
  }

  @Test
  fun `builds an empty permission list when everything is granted`() =
    runTest {
      val state = viewModel.state.first()

      assertTrue(state.permissionItems.isEmpty())
    }

  @Test
  fun `lists missing permissions reported by the Permissions util`() =
    runTest {
      every { Permissions.checkPermission(any(), any<String>()) } returns false

      val state = viewModel.state.first()

      assertTrue(state.permissionItems.isNotEmpty())
    }

  @Test
  fun `checks 15 and 13 specific permissions only when on that sdk level`() =
    runTest {
      every { systemInfo.is15 } returns true
      every { systemInfo.is13 } returns true
      every { Permissions.checkPermission(any(), any<String>()) } returns true
      every {
        Permissions.checkPermission(any(), Permissions.FOREGROUND_SERVICE_LOCATION)
      } returns false
      every {
        Permissions.checkPermission(any(), Permissions.POST_NOTIFICATION)
      } returns false

      val state = viewModel.state.first()

      assertEquals(2, state.permissionItems.size)
    }

  @Test
  fun `isGeminiFunctionsVisible follows the sdk check regardless of pro status`() =
    runTest {
      every { buildInfo.isPro } returns false
      every { systemInfo.is16 } returns true

      val state = viewModel.state.first()

      assertTrue(state.isGeminiFunctionsVisible)
    }

  @Test
  fun `isGeminiFunctionsLocked is true when the build is not pro`() =
    runTest {
      every { buildInfo.isPro } returns false

      val state = viewModel.state.first()

      assertTrue(state.isGeminiFunctionsLocked)
    }

  @Test
  fun `isGeminiFunctionsLocked is false when the build is pro`() =
    runTest {
      every { buildInfo.isPro } returns true

      val state = viewModel.state.first()

      assertEquals(false, state.isGeminiFunctionsLocked)
    }

  @Test
  fun `onGeminiFunctionsLockedClick sends a feature gate tapped analytics event`() {
    viewModel.onGeminiFunctionsLockedClick()

    verify { analyticsEventSender.send(FeatureGateTappedEvent(Feature.GEMINI_FUNCTIONS)) }
  }

  @Test
  fun `isBuyMeACoffeeVisible follows the feature flag`() =
    runTest {
      every { featureFlags.isEnabled(FeatureFlag.BUY_ME_A_COFFEE) } returns true

      val state = viewModel.state.first()

      assertTrue(state.isBuyMeACoffeeVisible)
    }

  @Test
  fun `onBuyMeACoffeeClicked sends a feature used analytics event and opens the support url`() {
    viewModel.onBuyMeACoffeeClicked()

    verify { analyticsEventSender.send(FeatureUsedEvent(Feature.BUY_ME_A_COFFEE)) }
    val event = viewModel.event.value?.peekContent() as OtherSettingsViewModel.ViewModelEvent.OpenUrl
    assertTrue(event.url.isNotBlank())
  }

  @Test
  fun `onShareClicked emits ShareApp with a share intent and title`() {
    every { textProvider.getString(R.string.share_intent_title) } returns "Share via"

    viewModel.onShareClicked()

    val event = viewModel.event.value?.peekContent() as OtherSettingsViewModel.ViewModelEvent.ShareApp
    assertEquals("Share via", event.title)
    assertTrue(event.intent is Intent)
  }

  @Test
  fun `onFeedbackClicked emits ShowFeedbackDialog with pro app source`() {
    every { featureFlags.isEnabled(FeatureFlag.LOGS_IN_REVIEWS) } returns true
    every { buildInfo.isPro } returns true

    viewModel.onFeedbackClicked()

    val event =
      viewModel.event.value?.peekContent() as OtherSettingsViewModel.ViewModelEvent.ShowFeedbackDialog
    assertEquals(AppSource.PRO, event.appSource)
    assertTrue(event.allowLogsAttachment)
  }

  @Test
  fun `onShowPermissionDialogClicked shows a toast when nothing is missing`() =
    runTest {
      viewModel.state.first()

      viewModel.onShowPermissionDialogClicked()

      val event = viewModel.event.value?.peekContent()
      assertTrue(event is OtherSettingsViewModel.ViewModelEvent.ShowToast)
    }

  @Test
  fun `onShowPermissionDialogClicked shows the permission dialog when items are missing`() =
    runTest {
      every { Permissions.checkPermission(any(), any<String>()) } returns false
      viewModel.state.first()

      viewModel.onShowPermissionDialogClicked()

      val event = viewModel.event.value?.peekContent()
      assertTrue(event is OtherSettingsViewModel.ViewModelEvent.ShowPermissionDialog)
    }

  @Test
  fun `onAboutClick builds the about dialog from package info`() =
    runTest {
      every { packageManagerWrapper.getVersionName() } returns "1.2.3"
      every { textProvider.getStringArray(any()) } returns arrayOf("Alice", "Bob")

      viewModel.onAboutClick()

      val dialog = viewModel.state.first().aboutDialog
      assertEquals("1.2.3", dialog?.version)
      assertEquals("Alice\nBob", dialog?.translators)
    }

  @Test
  fun `onAboutDialogDismiss clears the about dialog`() =
    runTest {
      viewModel.onAboutClick()

      viewModel.onAboutDialogDismiss()

      assertEquals(null, viewModel.state.first().aboutDialog)
    }
}
