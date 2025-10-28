package com.elementary.tasks.core.work

import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workModule = module {
  factory { SyncWorker(get(), get(), get()) }
  factory { BackupWorker(get(), get()) }
  single { ExportAllDataWorker(get()) }

  worker { BackupSettingsWorker(get(), get(), get()) }
}
