package com.github.naz013.localbackup

import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.localbackup.compose.LocalBackupMode
import com.github.naz013.localbackup.compose.LocalBackupViewModel
import com.github.naz013.localbackup.transfer.TransferPackageReader
import com.github.naz013.localbackup.transfer.TransferPackageWriter
import com.github.naz013.localbackup.transfer.TransferSender
import com.github.naz013.localbackup.transfer.compose.SendDataViewModel
import com.github.naz013.localbackup.transfer.compose.TransferReceiverViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val localBackupModule = module {
  factoryOf(::BackupArchiveWriter)
  factoryOf(::BackupArchiveReader)
  factoryOf(::BuildBackupEnvelopeUseCase)
  factoryOf(::ApplyBackupEnvelopeUseCase)
  factory {
    LocalBackupApiImpl(get(), get(), get(), get()) as LocalBackupApi
  }

  viewModel { (uriString: String, mode: LocalBackupMode) ->
    LocalBackupViewModel(uriString, mode, get(), get(), get())
  }

  factoryOf(::TransferPackageWriter)
  factoryOf(::TransferPackageReader)
  factoryOf(::TransferSender)
  viewModel { (uriString: String) ->
    TransferReceiverViewModel(uriString, get(), get(), get(), get(), get(), get())
  }
  viewModel { SendDataViewModel(get(), get(), get()) }
}
