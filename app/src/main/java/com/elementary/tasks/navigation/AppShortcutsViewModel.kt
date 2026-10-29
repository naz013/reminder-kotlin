package com.elementary.tasks.navigation

import androidx.lifecycle.ViewModel
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.platform.BuildInfo
import com.github.naz013.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppShortcutsViewModel(
  private val routineRepository: RoutineRepository,
  private val routineConfig: RoutineConfig,
  private val buildInfo: BuildInfo,
  private val featureFlags: FeatureFlags,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

  private val builder = AppShortcutsBuilder()
  suspend fun isRoutineKnown(id: String): Boolean =
    withContext(dispatcherProvider.default()) {
      runCatching { routineRepository.getById(id) != null }.getOrDefault(false)
    }

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

  companion object {
    private const val TAG = "AppShortcutsViewModel"
  }
}
