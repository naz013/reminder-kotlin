package com.elementary.tasks.settings.export

import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val syncSettingsModule = module {
  factory { ObservableWorkerManager(get()) }

  worker { BackupSettingsWorker(get(), get(), get(), get()) }
  worker { ObservableBackupWorker(get(), get(), get(), get()) }
  worker { ObservableSyncWorker(get(), get(), get(), get()) }
  worker { ObservableEraseDataWorker(get(), get(), get(), get(), get()) }
}
