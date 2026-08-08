package com.elementary.tasks.reminder.build.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.PresetAction
import com.github.naz013.analytics.PresetUsed
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.files.DataType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManagePresetsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val uiPresetListAdapter: UiPresetListAdapter,
  private val recurPresetRepository: RecurPresetRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {
  val state: StateFlow<ManagePresetsState> field = MutableStateFlow(ManagePresetsState())

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      loadPresets()
    }
  }

  fun deletePreset(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      recurPresetRepository.delete(id)
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.RecurPresets,
        id = id,
        ids = null,
      )
      analyticsEventSender.send(PresetUsed(PresetAction.DELETE))
      loadPresets()
    }
  }

  private suspend fun loadPresets() {
    val presets = recurPresetRepository.getAll().map { uiPresetListAdapter.create(it) }
    withContext(dispatcherProvider.main()) {
      state.update { it.copy(presets = presets) }
    }
  }
}
