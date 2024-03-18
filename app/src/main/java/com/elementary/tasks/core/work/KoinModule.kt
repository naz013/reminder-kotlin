package com.elementary.tasks.core.work

import com.elementary.tasks.core.work.operation.BirthdayOperationFactory
import com.elementary.tasks.core.work.operation.GroupOperationFactory
import com.elementary.tasks.core.work.operation.NoteOperationFactory
import com.elementary.tasks.core.work.operation.PlaceOperationFactory
import com.elementary.tasks.core.work.operation.ReminderOperationFactory
import com.elementary.tasks.core.work.operation.SettingsOperationFactory
import com.elementary.tasks.core.work.operation.SyncOperationFactory
import com.elementary.tasks.core.work.operation.SyncOperationsFactory
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workModule = module {
  factory { GroupOperationFactory(get(), get()) }
  factory { ReminderOperationFactory(get(), get(), get()) }
  factory { NoteOperationFactory(get(), get()) }
  factory { BirthdayOperationFactory(get(), get()) }
  factory { PlaceOperationFactory(get(), get()) }
  factory { SettingsOperationFactory(get(), get()) }

  factory { SyncOperationFactory(get(), get(), get(), get(), get(), get()) }

  factory { SyncOperationsFactory(get(), get()) }

  single { SyncWorker(get(), get(), get()) }
  single { BackupWorker(get(), get()) }
  single { ExportAllDataWorker(get()) }

  worker { BackupDataWorker(get(), get(), get(), get()) }
  worker { BackupSettingsWorker(get(), get(), get(), get()) }
  worker { DeleteFileWorker(get(), get(), get(), get()) }
  worker { LoadFileWorker(get(), get(), get(), get()) }
  worker { SyncDataWorker(get(), get(), get(), get(), get(), get(), get()) }
}
