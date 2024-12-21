package com.elementary.tasks.reminder.build.selectordialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.livedata.toLiveData
import com.elementary.tasks.reminder.build.UiSelectorItem
import kotlinx.coroutines.launch

class SelectorDialogViewModel(
  private val selectorDialogDataHolder: SelectorDialogDataHolder,
  private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

  private val _showTab = mutableLiveDataOf<SelectorTab>()
  val showTab = _showTab.toSingleEvent()

  private val _tabs = mutableLiveDataOf<List<SelectorTab>>()
  val tabs = _tabs.toLiveData()

  private val _builderItems = mutableLiveDataOf<List<UiSelectorItem>>()
  val builderItems = _builderItems.toLiveData()

  private val _presetItems = mutableLiveDataOf<List<UiPresetList>>()
  val presetItems = _presetItems.toLiveData()

  private val _recurPresetItems = mutableLiveDataOf<List<UiPresetList>>()
  val recurPresetItems = _recurPresetItems.toLiveData()

  private var query: String? = null

  fun onTabSelected(position: Int) {
    val tabs = _tabs.value ?: emptyList()
    if (position < 0 || position >= tabs.size) {
      return
    }
    val newTab = tabs[position]
    _showTab.postValue(newTab)
    viewModelScope.launch(dispatcherProvider.default()) {
      updateSelectedList(newTab)
    }
  }

  fun onQueryChanged(query: String) {
    this.query = query
    viewModelScope.launch(dispatcherProvider.default()) {
      updateSelectedList(getSelectedTab())
    }
  }

  fun loadTabs() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val tabs = selectorDialogDataHolder.getTabs()
      if (tabs.size > 1) {
        _tabs.postValue(tabs)
      }

      _builderItems.postValue(selectorDialogDataHolder.selectorBuilderItems)
      _presetItems.postValue(selectorDialogDataHolder.presets)
      _recurPresetItems.postValue(selectorDialogDataHolder.recurPresets)

      _showTab.postValue(SelectorTab.BUILDER)
    }
  }

  private fun updateSelectedList(tab: SelectorTab) {
    when (tab) {
      SelectorTab.BUILDER -> {
        _builderItems.postValue(filteredBuilderItems())
      }

      SelectorTab.PRESETS -> {
        _presetItems.postValue(filteredPresets())
      }

      SelectorTab.RECUR_PRESETS -> {
        _recurPresetItems.postValue(filteredRecurPresets())
      }
    }
  }

  private fun getSelectedTab(): SelectorTab {
    return showTab.value ?: SelectorTab.BUILDER
  }

  private fun filteredBuilderItems(): List<UiSelectorItem> {
    val query = query?.lowercase() ?: ""
    if (query.isEmpty()) {
      return selectorDialogDataHolder.selectorBuilderItems
    }
    return selectorDialogDataHolder.selectorBuilderItems.filter {
      it.builderItem.title.lowercase().contains(query) ||
        it.builderItem.description?.lowercase()?.contains(query) == true
    }
  }

  private fun filteredPresets(): List<UiPresetList> {
    val query = query?.lowercase() ?: ""
    if (query.isEmpty()) {
      return selectorDialogDataHolder.presets
    }
    return selectorDialogDataHolder.presets.filter { it.name.lowercase().contains(query) }
  }

  private fun filteredRecurPresets(): List<UiPresetList> {
    val query = query?.lowercase() ?: ""
    if (query.isEmpty()) {
      return selectorDialogDataHolder.recurPresets
    }
    return selectorDialogDataHolder.recurPresets.filter { it.name.lowercase().contains(query) }
  }
}
