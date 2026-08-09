package com.elementary.tasks.settings.export

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.settings.export.work.ObservableBackupTask
import com.elementary.tasks.settings.export.work.ObservableEraseDataTask
import com.elementary.tasks.settings.export.work.ObservableSyncTask
import com.github.naz013.common.TextProvider
import com.github.naz013.logic.schedule.WorkerNetworkType
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.WorkScheduler
import com.github.naz013.workapi.WorkState
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CloudBackupSettingsViewModelTest : BaseTest() {
  private val cloudApiProvider = mockk<CloudApiProvider>()
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val prefs = mockk<Prefs>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val workScheduler = mockk<WorkScheduler>()

  private lateinit var viewModel: CloudBackupSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.autoBackupState } returns 0
    every { prefs.autoBackupState = any() } just Runs
    every { prefs.workerNetworkType } returns WorkerNetworkType.Any
    every { prefs.workerNetworkType = any() } just Runs
    every { cloudApiProvider.getAllowedCloudApis() } returns emptyList()

    viewModel =
      CloudBackupSettingsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        cloudApiProvider = cloudApiProvider,
        jobScheduler = jobScheduler,
        prefs = prefs,
        textProvider = textProvider,
        workScheduler = workScheduler,
      )
  }

  @Test
  fun `hasAnyCloudApi is false when no cloud apis are allowed`() =
    runTest {
      val state = viewModel.state.first()

      assertFalse(state.hasAnyCloudApi)
    }

  @Test
  fun `hasAnyCloudApi is true when at least one cloud api is allowed`() =
    runTest {
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockk())

      val vm =
        CloudBackupSettingsViewModel(
          mockDispatcherProvider(), cloudApiProvider, jobScheduler, prefs, textProvider, workScheduler,
        )

      assertTrue(vm.state.first().hasAnyCloudApi)
    }

  @Test
  fun `onAutoBackupIntervalClick shows the dialog with the current pref position`() =
    runTest {
      every { prefs.autoBackupState } returns 6

      viewModel.onAutoBackupIntervalClick()

      val dialog = viewModel.state.first().dialog as CloudBackupDialog.AutoBackupInterval
      assertEquals(2, dialog.selectedIndex)
      assertEquals(6, dialog.options.size)
    }

  @Test
  fun `onAutoBackupIntervalSelected saves the state and reschedules the auto backup job`() =
    runTest {
      viewModel.onAutoBackupIntervalSelected(3)

      verify { prefs.autoBackupState = 12 }
      verify { jobScheduler.scheduleAutoBackup() }
      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onNetworkTypeClick shows the dialog with the current network type`() =
    runTest {
      every { prefs.workerNetworkType } returns WorkerNetworkType.Wifi

      viewModel.onNetworkTypeClick()

      val dialog = viewModel.state.first().dialog as CloudBackupDialog.NetworkType
      assertEquals(WorkerNetworkType.Wifi.ordinal, dialog.selectedIndex)
    }

  @Test
  fun `onNetworkTypeSelected saves the selected network type`() =
    runTest {
      viewModel.onNetworkTypeSelected(WorkerNetworkType.Cellular.ordinal)

      verify { prefs.workerNetworkType = WorkerNetworkType.Cellular }
      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onEraseClick shows the erase confirmation dialog`() =
    runTest {
      viewModel.onEraseClick()

      assertEquals(CloudBackupDialog.EraseConfirm, viewModel.state.first().dialog)
    }

  @Test
  fun `onDialogDismiss clears the dialog`() =
    runTest {
      viewModel.onEraseClick()

      viewModel.onDialogDismiss()

      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onEraseConfirmed dismisses the dialog and enqueues the erase task`() =
    runTest {
      every {
        workScheduler.enqueueUnique(ObservableEraseDataTask.TASK_KEY, ExistingWorkPolicy.REPLACE, any())
      } returns "tag"
      every { workScheduler.observeUniqueWork(ObservableEraseDataTask.TASK_KEY) } returns flowOf(WorkState.Succeeded)
      viewModel.onEraseClick()

      viewModel.onEraseConfirmed()

      verify { workScheduler.enqueueUnique(ObservableEraseDataTask.TASK_KEY, ExistingWorkPolicy.REPLACE, any()) }
      assertNull(viewModel.state.first().dialog)
      assertFalse(viewModel.state.first().isInProgress)
    }

  @Test
  fun `onBackupNowClick marks progress while work is enqueued`() =
    runTest {
      every {
        workScheduler.enqueueUnique(ObservableBackupTask.TASK_KEY, ExistingWorkPolicy.REPLACE, any())
      } returns "tag"
      every { workScheduler.observeUniqueWork(ObservableBackupTask.TASK_KEY) } returns flowOf(WorkState.Enqueued)

      viewModel.onBackupNowClick()

      assertTrue(viewModel.state.first().isInProgress)
    }

  @Test
  fun `onSyncNowClick reflects the in-progress flag from a running work state`() =
    runTest {
      val progress = TaskData.builder().putBoolean(ObservableSyncTask.KEY_IS_IN_PROGRESS, true).build()
      every {
        workScheduler.enqueueUnique(ObservableSyncTask.TASK_KEY, ExistingWorkPolicy.REPLACE, any())
      } returns "tag"
      every {
        workScheduler.observeUniqueWork(ObservableSyncTask.TASK_KEY)
      } returns flowOf(WorkState.Running(progress))

      viewModel.onSyncNowClick()

      assertTrue(viewModel.state.first().isInProgress)
    }

  @Test
  fun `work state Blocked is ignored and does not change progress`() =
    runTest {
      every {
        workScheduler.enqueueUnique(ObservableBackupTask.TASK_KEY, ExistingWorkPolicy.REPLACE, any())
      } returns "tag"
      every { workScheduler.observeUniqueWork(ObservableBackupTask.TASK_KEY) } returns flowOf(WorkState.Blocked)

      viewModel.onBackupNowClick()

      // isInProgress was already flipped to true before the flow collection started, and the
      // Blocked branch returns without updating state, so it should remain true.
      assertTrue(viewModel.state.first().isInProgress)
    }
}
