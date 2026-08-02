package com.github.naz013.localbackup

import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val localBackupModule = module {
  factoryOf(::BackupArchiveWriter)
  factoryOf(::BackupArchiveReader)
  factory { LocalBackupApiImpl(get(), get(), get(), get(), get(), get(), get()) as LocalBackupApi }
}
