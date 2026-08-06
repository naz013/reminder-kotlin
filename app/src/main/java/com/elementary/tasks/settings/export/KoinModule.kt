package com.elementary.tasks.settings.export

import com.elementary.tasks.settings.export.services.CloudServicesViewModel
import com.elementary.tasks.settings.export.work.BackupSettingsTask
import com.elementary.tasks.settings.export.work.ObservableBackupTask
import com.elementary.tasks.settings.export.work.ObservableEraseDataTask
import com.elementary.tasks.settings.export.work.ObservableSyncTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val syncSettingsModule = module {
  factory<BackgroundTask>(named(BackupSettingsTask.TASK_KEY)) { BackupSettingsTask(get()) }
  factory<BackgroundTask>(named(ObservableBackupTask.TASK_KEY)) { ObservableBackupTask(get()) }
  factory<BackgroundTask>(named(ObservableSyncTask.TASK_KEY)) { ObservableSyncTask(get()) }
  factory<BackgroundTask>(named(ObservableEraseDataTask.TASK_KEY)) {
    ObservableEraseDataTask(
      get(),
      get()
    )
  }

  viewModelOf(::CloudBackupSettingsViewModel)
  viewModelOf(::CloudServicesViewModel)
}
