package com.elementary.tasks.reminder.build.preset

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.sync.DataType
import kotlinx.coroutines.launch

class ManagePresetsViewModel(
  dispatcherProvider: DispatcherProvider,
  private val uiPresetListAdapter: UiPresetListAdapter,
  private val recurPresetRepository: RecurPresetRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _presets = mutableLiveDataOf<List<UiPresetList>>()
  val presets = _presets.toLiveData()

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
        ids = null
      )
      loadPresets()
    }
  }

  private suspend fun loadPresets() {
    val presets = recurPresetRepository.getAll().map { uiPresetListAdapter.create(it) }
    _presets.postValue(presets)
  }
}
