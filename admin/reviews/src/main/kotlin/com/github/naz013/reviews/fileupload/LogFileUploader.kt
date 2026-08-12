package com.github.naz013.reviews.fileupload

import com.github.naz013.logging.Logger
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Handles uploading log files to Firebase Storage.
 * Files are compressed before upload to reduce size.
 *
 * @property firebaseStorage The Firebase Storage instance for file uploads
 */
internal class LogFileUploader(
  private val firebaseStorage: FirebaseStorage,
) {

  /**
   * Uploads a log file to Firebase Storage after compressing it.
   *
   * @param filePath The absolute path to the log file to upload
   * @return Result containing the download URL on success, or an error on failure
   */
  suspend fun upload(filePath: String): Result<String> {
    // Validate input
    if (filePath.isBlank()) {
      return Result.failure(IllegalArgumentException("File path cannot be empty"))
    }

    val logFile = File(filePath)
    if (!logFile.exists()) {
      return Result.failure(IllegalArgumentException("File does not exist: $filePath"))
    }

    if (!logFile.canRead()) {
      return Result.failure(IllegalArgumentException("Cannot read file: $filePath"))
    }

    return try {
      Logger.d(TAG, "Starting upload process for: $filePath")

      // Create zip file
      val zippedFile = zipLogFile(logFile)
      if (zippedFile == null) {
        return Result.failure(Exception("Failed to compress log file"))
      }

      try {
        // Upload to Firebase Storage
        val downloadUrl = uploadToFirebase(zippedFile)
        Logger.i(TAG, "Successfully uploaded log file: $downloadUrl")
        Result.success(downloadUrl)
      } finally {
        // Clean up temporary zip file
        if (zippedFile.exists()) {
          zippedFile.delete()
          Logger.d(TAG, "Cleaned up temporary zip file")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to upload log file", e)
      Result.failure(e)
    }
  }

  /**
   * Compresses the log file into a ZIP archive.
   *
   * @param logFile The log file to compress
   * @return The compressed file, or null if compression failed
   */
  private fun zipLogFile(logFile: File): File? {
    return try {
      val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      val zipFileName = "${logFile.nameWithoutExtension}_$timestamp.zip"
      val zipFile = File(logFile.parent, zipFileName)

      ZipOutputStream(FileOutputStream(zipFile)).use { zipOutputStream ->
        FileInputStream(logFile).use { fileInputStream ->
          val zipEntry = ZipEntry(logFile.name)
          zipOutputStream.putNextEntry(zipEntry)

          val buffer = ByteArray(BUFFER_SIZE)
          var length: Int
          while (fileInputStream.read(buffer).also { length = it } > 0) {
            zipOutputStream.write(buffer, 0, length)
          }

          zipOutputStream.closeEntry()
        }
      }

      Logger.d(TAG, "Successfully compressed log file: ${zipFile.absolutePath}")
      zipFile
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to compress log file", e)
      null
    }
  }

  /**
   * Uploads a file to Firebase Storage.
   *
   * @param file The file to upload
   * @return The download URL of the uploaded file
   * @throws Exception if upload fails
   */
  private suspend fun uploadToFirebase(file: File): String {
    return try {
      val timestamp = System.currentTimeMillis()
      val storagePath = "logs/${timestamp}_${file.name}"
      val storageRef = firebaseStorage.reference.child(storagePath)

      Logger.d(TAG, "Uploading to Firebase Storage: $storagePath")

      val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file))
      uploadTask.await()

      val downloadUrl = storageRef.downloadUrl.await()
      downloadUrl.toString()
    } catch (e: com.google.firebase.storage.StorageException) {
      // Firebase Storage specific errors
      Logger.e(TAG, "Firebase Storage error: ${e.errorCode}", e)
      when (e.errorCode) {
        com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED -> {
          throw Exception("Firebase Storage: Authentication required. Please sign in.")
        }
        com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED -> {
          throw Exception("Firebase Storage: Permission denied. Service account configuration required.")
        }
        com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED -> {
          throw Exception("Firebase Storage: Storage quota exceeded.")
        }
        else -> {
          throw Exception("Firebase Storage error: ${e.message}")
        }
      }
    } catch (e: com.google.firebase.FirebaseException) {
      // Firebase app registration or configuration errors
      Logger.e(TAG, "Firebase configuration error", e)
      when {
        e.message?.contains("App attestation failed", ignoreCase = true) == true ||
          e.message?.contains("attestation", ignoreCase = true) == true -> {
          throw Exception("Firebase App Check attestation failed. Please configure App Check properly.")
        }
        e.message?.contains("App not registered", ignoreCase = true) == true -> {
          throw Exception("Firebase app not registered. Please check Firebase Console configuration.")
        }
        e.message?.contains("API", ignoreCase = true) == true -> {
          throw Exception("Firebase API error. Please check project configuration.")
        }
        else -> {
          throw Exception("Firebase error: ${e.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to upload to Firebase Storage", e)
      throw e
    }
  }

  companion object {
    private const val TAG = "LogFileUploader"
    private const val BUFFER_SIZE = 8192
  }
}
