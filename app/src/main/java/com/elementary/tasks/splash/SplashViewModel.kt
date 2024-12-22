package com.elementary.tasks.splash

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.calendar.data.CalendarDataEngine
import com.elementary.tasks.core.data.repository.NoteImageMigration
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.utils.EnableThread
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.PresetInitProcessor
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import kotlinx.coroutines.launch

class SplashViewModel(
  googleTasksAuthManager: GoogleTasksAuthManager,
  private val prefs: Prefs,
  private val enableThread: EnableThread,
  private val dispatcherProvider: DispatcherProvider,
  private val notifier: Notifier,
  featureManager: FeatureManager,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val groupsUtil: GroupsUtil,
  private val noteImageMigration: NoteImageMigration,
  private val presetInitProcessor: PresetInitProcessor,
  private val calendarDataEngine: CalendarDataEngine
) : ViewModel(), DefaultLifecycleObserver {

  val isGoogleTasksEnabled = featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS) &&
    googleTasksAuthManager.isAuthorized()
  val openHome = mutableLiveDataOf<Boolean>()

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    calendarDataEngine.initEngine()
    viewModelScope.launch(dispatcherProvider.default()) {
      presetInitProcessor.run()
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    viewModelScope.launch(dispatcherProvider.default()) {
      initPrefs()
      checkIfAppUpdated()
      checkDb()
      withUIContext {
        if (prefs.isSbNotificationEnabled) {
          notifier.sendShowReminderPermanent()
        }
        openHome.postValue(prefs.hasPinCode)
      }
    }
  }

  private suspend fun checkDb() {
    runCatching {
      groupsUtil.initDefaultIfEmpty()
      if (!prefs.noteMigrationDone) {
        prefs.noteMigrationDone = true
        noteImageMigration.migrate()
      }
    }
  }

  private fun checkIfAppUpdated() {
    val versionName = packageManagerWrapper.getVersionName()
    if (!prefs.getVersion(versionName)) {
      prefs.saveVersionBoolean(versionName)
      enableThread.run()
    }
  }

  private fun initPrefs() {
    prefs.initPrefs()
    prefs.checkPrefs()
  }
}
