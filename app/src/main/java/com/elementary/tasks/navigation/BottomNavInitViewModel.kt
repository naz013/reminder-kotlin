package com.elementary.tasks.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.elementary.tasks.core.data.repository.NoteImageMigration
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.ActivateAllActiveRemindersUseCase
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.PresetInitProcessor
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.workflow.WorkflowRulesUtil
import com.github.naz013.appwidgets.AppWidgetPreviewUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.migration.GroupV2BackfillUseCase
import com.github.naz013.repository.migration.ReminderV2BackfillUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BottomNavInitState {
  data object Loading : BottomNavInitState

  data class Ready(val requiresLogin: Boolean) : BottomNavInitState
}

class BottomNavInitViewModel(
  googleTasksAuthManager: GoogleTasksAuthManager,
  private val prefs: Prefs,
  private val activateAllActiveRemindersUseCase: ActivateAllActiveRemindersUseCase,
  private val dispatcherProvider: DispatcherProvider,
  private val notifier: Notifier,
  featureManager: FeatureManager,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val groupsUtil: GroupsUtil,
  private val noteImageMigration: NoteImageMigration,
  private val presetInitProcessor: PresetInitProcessor,
  private val appWidgetPreviewUpdater: AppWidgetPreviewUpdater,
  private val migrateExistingEventOccurrencesUseCase: MigrateExistingEventOccurrencesUseCase,
  private val groupV2BackfillUseCase: GroupV2BackfillUseCase,
  private val reminderV2BackfillUseCase: ReminderV2BackfillUseCase,
  private val workflowRulesUtil: WorkflowRulesUtil,
  private val jobScheduler: JobScheduler,
) : ViewModel() {
  val isGoogleTasksEnabled =
    featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS) &&
      googleTasksAuthManager.isAuthorized()

  private val _state = MutableStateFlow<BottomNavInitState>(BottomNavInitState.Loading)
  val state: StateFlow<BottomNavInitState> = _state.asStateFlow()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      presetInitProcessor.run()
      checkIfAppUpdated()
      checkDb()
      appWidgetPreviewUpdater.updateEventsWidgetPreview()
      if (!prefs.occurrenceMigrated) {
        prefs.occurrenceMigrated = true
        migrateExistingEventOccurrencesUseCase()
      }
      if (prefs.isSbNotificationEnabled) {
        notifier.sendShowReminderPermanent()
      }
      _state.value = BottomNavInitState.Ready(requiresLogin = prefs.hasPinCode)
    }
  }

  private suspend fun checkDb() {
    runCatching {
      groupsUtil.initDefaultIfEmpty()
      if (!prefs.noteMigrationDone) {
        prefs.noteMigrationDone = true
        noteImageMigration.migrate()
      }
      if (!prefs.groupV2BackfillDone) {
        groupV2BackfillUseCase()
        prefs.groupV2BackfillDone = true
      }
      if (!prefs.reminderV2BackfillDone) {
        reminderV2BackfillUseCase()
        prefs.reminderV2BackfillDone = true
      }
      workflowRulesUtil.initDefaultIfEmpty()
      if (!prefs.workflowRulesScheduled) {
        jobScheduler.scheduleWorkflowRulesCheck()
        prefs.workflowRulesScheduled = true
      }
      if (!prefs.workflowUnacknowledgedRulesScheduled) {
        jobScheduler.scheduleWorkflowUnacknowledgedCheck()
        prefs.workflowUnacknowledgedRulesScheduled = true
      }
    }
  }

  private fun checkIfAppUpdated() {
    val versionName = packageManagerWrapper.getVersionName()
    if (!prefs.getVersion(versionName)) {
      prefs.saveVersionBoolean(versionName)
      activateAllActiveRemindersUseCase.run()
    }
  }
}
