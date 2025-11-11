package com.elementary.tasks.settings.export

import com.elementary.tasks.settings.export.services.CloudServicesFragmentViewModel
import com.elementary.tasks.settings.export.work.BackupSettingsWorker
import com.elementary.tasks.settings.export.work.ObservableBackupWorker
import com.elementary.tasks.settings.export.work.ObservableEraseDataWorker
import com.elementary.tasks.settings.export.work.ObservableSyncWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val syncSettingsModule = module {
  factory { ObservableWorkerManager(get()) }

  worker { BackupSettingsWorker(get(), get(), get(), get()) }
  worker { ObservableBackupWorker(get(), get(), get(), get()) }
  worker { ObservableSyncWorker(get(), get(), get(), get()) }
  worker { ObservableEraseDataWorker(get(), get(), get(), get(), get()) }

  viewModel { CloudBackupSettingsViewModel(get(), get(), get()) }
  viewModel { CloudServicesFragmentViewModel(get(), get(), get(), get(), get()) }
}
