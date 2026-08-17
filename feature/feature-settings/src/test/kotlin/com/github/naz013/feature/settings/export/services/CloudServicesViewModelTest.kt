package com.github.naz013.feature.settings.export.services

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logic.googletask.usecase.SyncAllGoogleTaskListsUseCase
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CloudServicesViewModelTest : BaseTest() {
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val syncAllGoogleTaskListsUseCase = mockk<SyncAllGoogleTaskListsUseCase>()
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>(relaxed = true)
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxed = true)
  private val featureFlags = mockk<FeatureFlags>()
  private val googleDriveAuthManager = mockk<GoogleDriveAuthManager>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val googleTasksAuthManager = mockk<GoogleTasksAuthManager>(relaxed = true)
  private val systemInfo = mockk<SystemInfo>()

  private lateinit var viewModel: CloudServicesViewModel

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { syncAllGoogleTaskListsUseCase() } just Runs
    every { featureFlags.isEnabled(any()) } returns true
    every { googleDriveAuthManager.isAuthorized() } returns false
    every { googleTasksAuthManager.isAuthorized() } returns false
    every { systemInfo.googlePlayServicesAvailable } returns true

    viewModel =
      CloudServicesViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        appWidgetUpdater = appWidgetUpdater,
        syncAllGoogleTaskListsUseCase = syncAllGoogleTaskListsUseCase,
        googleTaskListRepository = googleTaskListRepository,
        googleTaskRepository = googleTaskRepository,
        featureFlags = featureFlags,
        googleDriveAuthManager = googleDriveAuthManager,
        analyticsEventSender = analyticsEventSender,
        googleTasksAuthManager = googleTasksAuthManager,
        systemInfo = systemInfo,
      )
  }

  @Test
  fun `sends screen used analytics event on creation`() {
    verify { analyticsEventSender.send(any()) }
  }

  @Test
  fun `builds initial state reflecting auth and feature visibility`() =
    runTest {
      val state = viewModel.state.first()

      assertTrue(state.isDropboxVisible)
      assertTrue(state.isGoogleDriveVisible)
      assertTrue(state.isGoogleTasksVisible)
      assertFalse(state.isGoogleDriveLoggedIn)
      assertFalse(state.isGoogleTasksLoggedIn)
    }

  @Test
  fun `google drive and tasks are hidden when play services is unavailable`() =
    runTest {
      every { systemInfo.googlePlayServicesAvailable } returns false

      val state = viewModel.state.first()

      assertFalse(state.isGoogleDriveVisible)
      assertFalse(state.isGoogleTasksVisible)
    }

  @Test
  fun `reloads live login state on every fresh collection`() =
    runTest {
      every { googleDriveAuthManager.isAuthorized() } returns false
      assertFalse(viewModel.state.first().isGoogleDriveLoggedIn)

      every { googleDriveAuthManager.isAuthorized() } returns true
      assertTrue(viewModel.state.first().isGoogleDriveLoggedIn)
    }

  @Test
  fun `onGoogleDriveAuthFailed posts ShowLoginError`() {
    viewModel.onGoogleDriveAuthFailed()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.ShowLoginError,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onGoogleTasksAuthFailed posts ShowLoginError`() {
    viewModel.onGoogleTasksAuthFailed()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.ShowLoginError,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onGoogleDriveClicked requests accounts permission`() {
    viewModel.onGoogleDriveClicked()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.RequestAccountsPermission,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onGoogleTasksClicked requests accounts permission`() {
    viewModel.onGoogleTasksClicked()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.RequestAccountsPermission,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onDropboxClicked posts LogInDropbox`() {
    viewModel.onDropboxClicked()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.LogInDropbox,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onAccountsPermissionGranted for Google Drive logs in when not authorized`() {
    every { googleDriveAuthManager.isAuthorized() } returns false
    viewModel.onGoogleDriveClicked()

    viewModel.onAccountsPermissionGranted()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.LogInGoogleDrive,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onAccountsPermissionGranted for Google Drive logs out when already authorized`() {
    every { googleDriveAuthManager.isAuthorized() } returns true
    viewModel.onGoogleDriveClicked()

    viewModel.onAccountsPermissionGranted()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.LogOutGoogleDrive,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onAccountsPermissionGranted for Google Drive shows toast when play services missing`() {
    every { systemInfo.googlePlayServicesAvailable } returns false
    viewModel.onGoogleDriveClicked()

    viewModel.onAccountsPermissionGranted()

    val event =
      viewModel.event.value?.peekContent() as CloudServicesViewModel.ViewModelEvent.ShowToast
    assertEquals(R.string.google_play_services_not_installed, event.messageRes)
  }

  @Test
  fun `onAccountsPermissionGranted for Google Tasks logs in when not authorized`() {
    every { googleTasksAuthManager.isAuthorized() } returns false
    viewModel.onGoogleTasksClicked()

    viewModel.onAccountsPermissionGranted()

    assertEquals(
      CloudServicesViewModel.ViewModelEvent.LogInGoogleTasks,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onAccountsPermissionGranted for Google Tasks logs out and clears local tasks when authorized`() =
    runTest {
      every { googleTasksAuthManager.isAuthorized() } returns true
      // Re-collecting `state` re-triggers loadState(), which recomputes isGoogleTasksLoggedIn
      // from googleTasksAuthManager.isAuthorized() - a real auth manager would also flip to
      // unauthorized once its username is cleared, so make the mock behave the same way instead
      // of asserting against a stale, still-authorized stub.
      every { googleTasksAuthManager.removeUserName() } answers {
        every { googleTasksAuthManager.isAuthorized() } returns false
      }
      viewModel.onGoogleTasksClicked()

      viewModel.onAccountsPermissionGranted()

      verify { googleTasksAuthManager.removeUserName() }
      coVerify { googleTaskRepository.deleteAll() }
      coVerify { googleTaskListRepository.deleteAll() }
      assertFalse(viewModel.state.first().isGoogleTasksLoggedIn)
    }

  @Test
  fun `onDropboxLoginStateChanged updates state and sends analytics when logged in`() =
    runTest {
      viewModel.onDropboxLoginStateChanged(true)

      assertTrue(viewModel.state.first().isDropboxLoggedIn)
      verify { analyticsEventSender.send(any()) }
    }

  @Test
  fun `onDropboxLoginStateChanged updates state without analytics when logged out`() =
    runTest {
      viewModel.onDropboxLoginStateChanged(false)

      assertFalse(viewModel.state.first().isDropboxLoggedIn)
    }

  @Test
  fun `onGoogleTasksLoginStateChanged loads tasks and sends analytics when logged in`() =
    runTest {
      // A successful login means the auth manager would also report authorized afterwards -
      // re-collecting `state` re-triggers loadState(), which recomputes isGoogleTasksLoggedIn
      // from the auth manager, so keep the stub consistent with the just-completed login.
      every { googleTasksAuthManager.isAuthorized() } returns true

      viewModel.onGoogleTasksLoginStateChanged(true)

      assertTrue(viewModel.state.first().isGoogleTasksLoggedIn)
      coVerify { syncAllGoogleTaskListsUseCase() }
    }

  @Test
  fun `onGoogleTasksLoginStateChanged does not load tasks when logged out`() =
    runTest {
      viewModel.onGoogleTasksLoginStateChanged(false)

      assertFalse(viewModel.state.first().isGoogleTasksLoggedIn)
      coVerify(exactly = 0) { syncAllGoogleTaskListsUseCase() }
    }

  @Test
  fun `onGoogleDriveLoginStateChanged updates state and sends analytics when logged in`() =
    runTest {
      // Same reasoning as the Google Tasks login case above: keep the auth manager stub
      // consistent with the login that just happened, since re-collecting `state` recomputes
      // isGoogleDriveLoggedIn from it.
      every { googleDriveAuthManager.isAuthorized() } returns true

      viewModel.onGoogleDriveLoginStateChanged(true)

      assertTrue(viewModel.state.first().isGoogleDriveLoggedIn)
      verify { analyticsEventSender.send(any()) }
    }

  @Test
  fun `clearGoogleTasks deletes local google tasks and updates the widget`() =
    runTest {
      viewModel.clearGoogleTasks()

      coVerify { googleTaskRepository.deleteAll() }
      coVerify { googleTaskListRepository.deleteAll() }
      verify { appWidgetUpdater.updateScheduleWidget() }
      assertFalse(viewModel.state.first().isLoading)
    }

  @Test
  fun `loadGoogleTasks syncs and updates the widget`() =
    runTest {
      viewModel.loadGoogleTasks()

      coVerify { syncAllGoogleTaskListsUseCase() }
      verify { appWidgetUpdater.updateScheduleWidget() }
      assertFalse(viewModel.state.first().isLoading)
    }
}
