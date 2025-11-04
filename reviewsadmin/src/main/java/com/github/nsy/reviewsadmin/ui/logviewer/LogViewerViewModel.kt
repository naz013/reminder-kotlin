package com.github.nsy.reviewsadmin.ui.logviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.reviews.ReviewsApi
import com.github.nsy.reviewsadmin.cache.LogFileCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * ViewModel for managing log viewer state and operations.
 */
class LogViewerViewModel(
  private val reviewsApi: ReviewsApi,
  private val logFileCache: LogFileCache
) : ViewModel() {

  private val _uiState = MutableStateFlow<LogViewerUiState>(LogViewerUiState.Loading)
  val uiState: StateFlow<LogViewerUiState> = _uiState.asStateFlow()

  /**
   * Downloads and extracts log file from the given URL.
   * Uses cache to speed up re-opening of previously viewed files.
   *
   * @param url The URL of the log zip file
   */
  fun loadLogFile(url: String) {
    viewModelScope.launch {
      _uiState.value = LogViewerUiState.Loading

      try {
        // First, check if we have the extracted content cached
        val cachedExtractedLog = withContext(Dispatchers.IO) {
          logFileCache.getExtractedLog(url)
        }

        if (cachedExtractedLog != null) {
          // Use cached extracted content - no need to download or extract!
          _uiState.value = LogViewerUiState.Success(cachedExtractedLog)
          return@launch
        }

        // No cached extracted content, so we need to download and extract
        val result = withContext(Dispatchers.IO) {
          reviewsApi.downloadLogFile(url)
        }

        if (result.isFailure) {
          _uiState.value = LogViewerUiState.Error(
            "Failed to download log file: ${result.exceptionOrNull()?.message}"
          )
          return@launch
        }

        val zipBytes = result.getOrNull() ?: run {
          _uiState.value = LogViewerUiState.Error("Downloaded file is empty")
          return@launch
        }

        // Extract log content from zip
        val logContent = withContext(Dispatchers.IO) {
          extractLogFromZip(zipBytes)
        }

        if (logContent != null) {
          // Cache the extracted content for next time
          withContext(Dispatchers.IO) {
            logFileCache.putExtractedLog(url, logContent)
          }
          _uiState.value = LogViewerUiState.Success(logContent)
        } else {
          _uiState.value = LogViewerUiState.Error("Failed to extract log from zip file")
        }
      } catch (e: Exception) {
        _uiState.value = LogViewerUiState.Error("Error loading log file: ${e.message}")
      }
    }
  }

  /**
   * Extracts log content from a zip file.
   *
   * @param zipBytes The zip file bytes
   * @return The extracted log content as string, or null if extraction fails
   */
  private fun extractLogFromZip(zipBytes: ByteArray): String? {
    return try {
      val zipInputStream = ZipInputStream(ByteArrayInputStream(zipBytes))
      var entry = zipInputStream.nextEntry

      // Read all entries and concatenate them
      val logBuilder = StringBuilder()

      while (entry != null) {
        if (!entry.isDirectory) {
          val content = zipInputStream.readBytes().toString(Charsets.UTF_8)
          logBuilder.append("=== ${entry.name} ===\n\n")
          logBuilder.append(content)
          logBuilder.append("\n\n")
        }
        entry = zipInputStream.nextEntry
      }

      zipInputStream.close()
      logBuilder.toString().takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
      null
    }
  }
}

/**
 * UI state for the log viewer screen.
 */
sealed class LogViewerUiState {
  /**
   * Loading state while downloading and extracting log file.
   */
  data object Loading : LogViewerUiState()

  /**
   * Successfully loaded log content.
   *
   * @property logContent The extracted log content
   */
  data class Success(val logContent: String) : LogViewerUiState()

  /**
   * Error occurred during loading.
   *
   * @property message Error message to display
   */
  data class Error(val message: String) : LogViewerUiState()
}
