package com.elementary.tasks.reminder.build.preset

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.repository.RecurPresetRepository
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import kotlinx.coroutines.launch

class ManagePresetsViewModel(
  dispatcherProvider: DispatcherProvider,
  private val uiPresetListAdapter: UiPresetListAdapter,
  private val recurPresetRepository: RecurPresetRepository
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
      recurPresetRepository.deleteById(id)
      loadPresets()
    }
  }

  private fun loadPresets() {
    val presets = recurPresetRepository.getAll().map { uiPresetListAdapter.create(it) }
    _presets.postValue(presets)
  }
}
