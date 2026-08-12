package com.github.naz013.reviews.logs

import com.github.naz013.common.ContextProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Use case for finding the most recent log file in the application's log directory.
 *
 * This class searches through the log directory (configured via logback.xml) and
 * identifies the most recently modified log file. The log files follow the pattern
 * defined in logback configuration: yyyy-MM-dd.i.log
 *
 * @property contextProvider Provides access to the application context
 */
internal class FindLatestLogsFileUseCase(
  private val contextProvider: ContextProvider
) {

  /**
   * Finds and returns the absolute path to the most recent log file.
   *
   * The method performs the following steps:
   * 1. Locates the log directory in the app's internal storage
   * 2. Lists all .log files in the directory
   * 3. Sorts files by last modified timestamp
   * 4. Returns the path to the most recent file
   *
   * @return The absolute path to the latest log file, or null if:
   *         - The log directory doesn't exist
   *         - No log files are found
   *         - An error occurs during file access
   */
  suspend operator fun invoke(): String? = withContext(Dispatchers.IO) {
    return@withContext try {
      val logDirectory = getLogDirectory()

      if (!logDirectory.exists()) {
        Logger.w(TAG, "Log directory does not exist: ${logDirectory.absolutePath}")
        return@withContext null
      }

      if (!logDirectory.isDirectory) {
        Logger.w(TAG, "Log path is not a directory: ${logDirectory.absolutePath}")
        return@withContext null
      }

      val logFiles = logDirectory.listFiles { file ->
        file.isFile && file.extension == "log"
      }

      if (logFiles.isNullOrEmpty()) {
        Logger.w(TAG, "No log files found in: ${logDirectory.absolutePath}")
        return@withContext null
      }

      Logger.d(TAG, "Found ${logFiles.size} log file(s) in directory")

      // Sort by last modified time (most recent first)
      val latestFile = logFiles.maxByOrNull { it.lastModified() }

      if (latestFile != null) {
        Logger.i(
          TAG,
          "Latest log file: ${latestFile.name}, " +
            "size: ${latestFile.length()} bytes, " +
            "modified: ${latestFile.lastModified()}"
        )
        latestFile.absolutePath
      } else {
        Logger.w(TAG, "Could not determine latest log file")
        null
      }
    } catch (e: SecurityException) {
      Logger.e(TAG, "Security exception accessing log directory", e)
      null
    } catch (e: Exception) {
      Logger.e(TAG, "Error finding latest log file", e)
      null
    }
  }

  /**
   * Retrieves the log directory path from the application's internal storage.
   *
   * The directory path is: <app_data_dir>/log
   * This matches the LOG_DIR property defined in logback.xml configuration.
   *
   * @return File object representing the log directory
   */
  private fun getLogDirectory(): File {
    val appContext = contextProvider.context
    val dataDir = appContext.filesDir
    return File(dataDir, LOG_DIRECTORY_NAME)
  }

  companion object {
    private const val TAG = "FindLatestLogsFileUseCase"
    private const val LOG_DIRECTORY_NAME = "log"
  }
}
