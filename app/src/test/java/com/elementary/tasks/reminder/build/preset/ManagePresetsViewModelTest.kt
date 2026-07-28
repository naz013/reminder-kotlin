package com.elementary.tasks.reminder.build.preset

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.PresetAction
import com.github.naz013.analytics.PresetUsed
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.files.DataType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ManagePresetsViewModelTest : BaseTest() {
  private val uiPresetListAdapter = mockk<UiPresetListAdapter>()
  private val recurPresetRepository = mockk<RecurPresetRepository>()
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private lateinit var viewModel: ManagePresetsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // ManagePresetsViewModel's init{} eagerly loads presets on construction, and
    // mockDispatcherProvider() uses Dispatchers.Unconfined, so that runs synchronously here.
    coEvery { recurPresetRepository.getAll() } returns emptyList()
    every { uiPresetListAdapter.create(any()) } answers { uiPresetList(firstArg<RecurPreset>().id) }

    viewModel =
      ManagePresetsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        uiPresetListAdapter = uiPresetListAdapter,
        recurPresetRepository = recurPresetRepository,
        scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
        analyticsEventSender = analyticsEventSender,
      )
  }

  private fun recurPreset(id: String) =
    RecurPreset(
      id = id,
      name = "Preset $id",
      recurObject = "RRULE:FREQ=DAILY;COUNT=10",
      type = PresetType.RECUR,
      createdAt = LocalDateTime.now(),
      useCount = 0,
      description = null,
      isDefault = false,
      recurItemsToAdd = null,
      syncState = SyncState.Synced,
      version = 1L,
    )

  private fun uiPresetList(id: String) = UiPresetList(name = "Preset $id", id = id, description = "")

  @Test
  fun `loads presets into state on init`() {
    // Rebuild with non-empty presets since setUp() already ran init{} against an empty stub.
    val presets = listOf(recurPreset("1"), recurPreset("2"))
    coEvery { recurPresetRepository.getAll() } returns presets

    viewModel =
      ManagePresetsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        uiPresetListAdapter = uiPresetListAdapter,
        recurPresetRepository = recurPresetRepository,
        scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
        analyticsEventSender = analyticsEventSender,
      )

    assertEquals(listOf("1", "2"), viewModel.state.value.presets.map { it.id })
  }

  @Test
  fun `state is empty when there are no presets`() {
    assertEquals(emptyList<UiPresetList>(), viewModel.state.value.presets)
  }

  @Test
  fun `deletePreset deletes the preset, schedules background work, sends analytics and reloads`() =
    runTest {
      coEvery { recurPresetRepository.delete("1") } returns Unit
      coEvery { recurPresetRepository.getAll() } returns emptyList()

      viewModel.deletePreset("1")

      coVerify(exactly = 1) { recurPresetRepository.delete("1") }
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RecurPresets,
          id = "1",
          ids = null,
        )
      }
      coVerify(exactly = 1) { analyticsEventSender.send(PresetUsed(PresetAction.DELETE)) }
      coVerify(exactly = 2) { recurPresetRepository.getAll() }
    }
}
