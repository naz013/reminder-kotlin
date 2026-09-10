package com.github.naz013.localbackup.transfer.compose

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.localbackup.ApplyBackupEnvelopeUseCase
import com.github.naz013.localbackup.ImportSummary
import com.github.naz013.localbackup.R
import com.github.naz013.localbackup.transfer.InvalidTransferPackageException
import com.github.naz013.localbackup.transfer.TransferPackageReader
import com.github.naz013.logging.Logger
import com.github.naz013.logic.note.NoteImageRepository
import com.github.naz013.logic.note.SaveNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TransferReceiverViewModel(
  private val uriString: String,
  private val dispatcherProvider: DispatcherProvider,
  private val context: Context,
  private val transferPackageReader: TransferPackageReader,
  private val applyBackupEnvelopeUseCase: ApplyBackupEnvelopeUseCase,
  private val noteImageRepository: NoteImageRepository,
  private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

  private val _state = MutableStateFlow(TransferReceiveState())
  val state = _state.asStateFlow()

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val status = runImport()
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(status = status) }
      }
    }
  }

  private suspend fun runImport(): TransferStatus {
    return try {
      val input = context.contentResolver.openInputStream(Uri.parse(uriString))
        ?: return TransferStatus.Error(R.string.backup_open_file_failed)
      val transferPackage = input.use { transferPackageReader.read(it) }

      val envelopeSummary = applyBackupEnvelopeUseCase(transferPackage.envelope)
      var notesImported = 0
      for (item in transferPackage.notes) {
        val images = item.images.map { (fileName, bytes) ->
          val path = noteImageRepository.saveBytesToFile(fileName, bytes, TMP_FOLDER)
          ImageFile(noteId = item.note.key, fileName = fileName, filePath = path)
        }
        saveNoteUseCase(NoteWithImages(item.note, images))
        notesImported++
      }

      Logger.i(TAG, "Transfer import complete: $notesImported notes, $envelopeSummary")
      TransferStatus.Success(envelopeSummary.totalCount() + notesImported)
    } catch (e: InvalidTransferPackageException) {
      Logger.e(TAG, "Invalid transfer package", e)
      TransferStatus.Error(R.string.transfer_invalid_package)
    } catch (e: Exception) {
      Logger.e(TAG, "Transfer import failed", e)
      TransferStatus.Error(R.string.transfer_failed)
    }
  }

  private fun ImportSummary.totalCount(): Int =
    remindersImported + groupsImported + birthdaysImported + placesImported + presetsImported +
      tagsImported + tagAssignmentsImported + routinesImported + routineExecutionsImported +
      workflowRulesImported + workflowTemplatesImported

  companion object {
    private const val TAG = "TransferReceiverViewModel"
    private const val TMP_FOLDER = "tmp"
  }
}
