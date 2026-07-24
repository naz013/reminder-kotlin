package com.elementary.tasks.core.apps

import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Deprecated("After S")
class SelectApplicationViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val packageManagerWrapper: PackageManagerWrapper,
) : ViewModel() {

  private val _state = MutableStateFlow(SelectApplicationState())
  val state: StateFlow<SelectApplicationState> = _state.asStateFlow()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var allApps: List<UiApplicationList> = emptyList()

  init {
    loadApps()
  }

  fun onSearchQueryChange(query: String) {
    _state.update { it.copy(searchQuery = query) }
    applyFilter(query)
  }

  fun onAppClick(app: UiApplicationList) {
    event.emit(ViewModelEvent.AppSelected(app.packageName))
  }

  private fun loadApps() {
    viewModelScope.launch(dispatcherProvider.default()) {
      allApps =
        packageManagerWrapper
          .getInstalledApplications()
          .map { info ->
            UiApplicationList(
              name = info.loadLabel(packageManagerWrapper.packageManager).toString(),
              packageName = info.packageName,
              icon = info.loadIcon(packageManagerWrapper.packageManager).toBitmap(),
            )
          }.sortedBy { it.name }

      applyFilter(_state.value.searchQuery)
    }
  }

  private fun applyFilter(query: String) {
    val filtered =
      if (query.isBlank()) {
        allApps
      } else {
        allApps.filter { it.name.contains(query, ignoreCase = true) }
      }
    _state.update {
      it.copy(
        listState = if (filtered.isEmpty()) AppListState.Empty else AppListState.Ready(filtered),
      )
    }
  }

  sealed interface ViewModelEvent {
    data class AppSelected(
      val packageName: String,
    ) : ViewModelEvent
  }
}
