package com.github.naz013.localbackup.transfer

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.localbackup.BuildBackupEnvelopeUseCase
import com.github.naz013.logging.Logger
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.NoteRepository
import java.io.File

/** Held across the async gap between launching the sibling app and its `ActivityResult`
 * returning, by `SendDataViewModel` in this module's own `transfer.compose` package. */
internal data class PreparedTransfer(
  val intent: Intent,
  private val cacheFile: File,
  private val uri: Uri
) {
  fun cleanUp(context: Context) {
    context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    cacheFile.delete()
  }
}

/**
 * Builds the zip described by [TransferPackageWriter] and an explicit intent that launches the
 * sibling flavor's [TransferReceiverActivity] with read access to it, granted via this app's
 * existing `FileProvider` (`${applicationId}.provider`, already `grantUriPermissions="true"`) -
 * the same [UriUtil] helper `NoteIntentSender` already uses to share a note file to other apps.
 * There's no shared signing certificate between the flavors, so this URI grant plus the caller-
 * package check in [TransferReceiverActivity] is the real trust boundary, not a manifest permission.
 */
internal class TransferSender(
  private val context: Context,
  private val buildInfo: BuildInfo,
  private val buildBackupEnvelopeUseCase: BuildBackupEnvelopeUseCase,
  private val noteRepository: NoteRepository,
  private val transferPackageWriter: TransferPackageWriter
) {
  /** The sibling flavor's applicationId, or null if this build somehow isn't free/PRO. */
  fun counterpartPackageName(): String? = when (buildInfo.applicationId) {
    SystemInfo.PRO_PACKAGE_NAME -> SystemInfo.FREE_PACKAGE_NAME
    SystemInfo.FREE_PACKAGE_NAME -> SystemInfo.PRO_PACKAGE_NAME
    else -> null
  }

  suspend fun prepareTransfer(): PreparedTransfer? {
    val counterpartPackage = counterpartPackageName() ?: return null
    val envelope = buildBackupEnvelopeUseCase()
    val notes = noteRepository.getAll(isArchived = false) + noteRepository.getAll(isArchived = true)

    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, "transfer_${System.currentTimeMillis()}.zip")
    file.outputStream().use { output -> transferPackageWriter.write(output, envelope, notes) }

    val uri = UriUtil.getUri(context, file, buildInfo.applicationId)
    if (uri == null) {
      Logger.e(TAG, "Failed to build a content:// URI for the transfer package")
      file.delete()
      return null
    }

    val intent = Intent(TransferContract.ACTION_RECEIVE_TRANSFER).apply {
      setClassName(counterpartPackage, TransferContract.RECEIVER_ACTIVITY_CLASS_NAME)
      setDataAndType(uri, TransferContract.MIME_TYPE)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    Logger.i(TAG, "Prepared transfer package for $counterpartPackage: ${notes.size} notes")
    return PreparedTransfer(intent, file, uri)
  }

  companion object {
    private const val TAG = "TransferSender"
  }
}
