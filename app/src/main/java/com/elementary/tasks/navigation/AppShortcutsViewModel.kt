package com.elementary.tasks.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.platform.BuildInfo
import com.github.naz013.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppShortcutsViewModel(
  private val routineRepository: RoutineRepository,
  private val routineConfig: RoutineConfig,
  private val buildInfo: BuildInfo,
  private val featureFlags: FeatureFlags,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

  private val builder = AppShortcutsBuilder()
  private val _knownRoutineIds = MutableStateFlow<Set<String>?>(null)
  val knownRoutineIds: StateFlow<Set<String>?> = _knownRoutineIds.asStateFlow()

  fun isRoutineKnown(id: String): Boolean = _knownRoutineIds.value?.contains(id) ?: true

  internal fun observeShortcuts(maxCount: Int): Flow<List<AppShortcutSpec>> =
    routineRepository
      .observeAll()
      .map { routines ->
        builder.build(
          isPro = buildInfo.isPro,
          isGoogleTasksEnabled =
            featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS) && googleTasksAuthManager.isAuthorized(),
          isRoutineEnabled = routineConfig.isEnabled,
          routines = routines,
          maxCount = maxCount,
        )
      }.catch { Logger.e(TAG, "Failed to observe shortcuts", it) }
      .flowOn(dispatcherProvider.default())

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      routineRepository
        .observeAll()
        .catch { Logger.e(TAG, "Failed to observe routines", it) }
        .collect { routines -> _knownRoutineIds.value = routines.map { it.id }.toSet() }
    }
  }

  companion object {
    private const val TAG = "AppShortcutsViewModel"
  }
}
