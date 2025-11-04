package com.example.cloudtestadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * ViewModel for cloud file testing and management.
 *
 * Handles authentication and file operations for Google Drive and Dropbox.
 */
class CloudTestViewModel(
  private val googleDriveApi: GoogleDriveApi,
  private val googleDriveAuthManager: GoogleDriveAuthManager,
  private val dropboxApi: DropboxApi,
  private val dropboxAuthManager: DropboxAuthManager
) : ViewModel() {

  private val _uiState = MutableStateFlow<CloudTestUiState>(CloudTestUiState.SelectSource)
  val uiState: StateFlow<CloudTestUiState> = _uiState.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private var currentSource: Source? = null
  private var currentApi: CloudFileApi? = null

  /**
   * Checks if the user is already authenticated for a cloud source.
   *
   * @param source The cloud source to check
   * @return True if authenticated, false otherwise
   */
  fun isAuthenticated(source: Source): Boolean {
    val isAuth = when (source) {
      Source.GoogleDrive -> googleDriveAuthManager.isAuthorized()
      Source.Dropbox -> dropboxAuthManager.isAuthorized()
    }
    Logger.d(TAG, "isAuthenticated for $source: $isAuth")
    return isAuth
  }

  /**
   * Selects a cloud source and proceeds to folder list if authenticated.
   *
   * @param source The cloud source to select
   */
  fun selectSource(source: Source) {
    Logger.i(TAG, "selectSource: $source")
    currentSource = source
    currentApi = when (source) {
      Source.GoogleDrive -> googleDriveApi
      Source.Dropbox -> dropboxApi
    }

    val isAuth = isAuthenticated(source)
    Logger.d(TAG, "Source $source authenticated: $isAuth")

    if (isAuth) {
      Logger.d(TAG, "Navigating to folder list")
      _uiState.value = CloudTestUiState.FolderList(getInternalDataTypes())
    } else {
      Logger.d(TAG, "Navigating to authentication screen")
      _uiState.value = CloudTestUiState.NeedAuth(source)
    }
  }

  /**
   * Completes authentication and navigates to folder list.
   */
  fun onAuthenticationComplete() {
    Logger.i(TAG, "onAuthenticationComplete called for source: $currentSource")

    if (currentSource != null) {
      val isAuth = isAuthenticated(currentSource!!)
      Logger.d(TAG, "Authentication status after completion: $isAuth")

      if (isAuth) {
        Logger.i(TAG, "Authentication successful, navigating to folder list")
        _uiState.value = CloudTestUiState.FolderList(getInternalDataTypes())
      } else {
        Logger.e(TAG, "Authentication failed - user not authorized")
        _errorMessage.value = "Authentication failed"
        _uiState.value = CloudTestUiState.SelectSource
      }
    } else {
      Logger.e(TAG, "Authentication failed - no current source")
      _errorMessage.value = "Authentication failed"
      _uiState.value = CloudTestUiState.SelectSource
    }
  }

  /**
   * Loads files for a specific data type.
   *
   * @param dataType The data type folder to load files from
   */
  fun loadFiles(dataType: CloudTestUiState.DataType) {
    Logger.i(TAG, "loadFiles for dataType: ${dataType.name}, extension: ${dataType.fileExtension}")

    val api = currentApi
    if (api == null) {
      Logger.e(TAG, "Cannot load files - no cloud service selected")
      _errorMessage.value = "No cloud service selected"
      return
    }

    Logger.d(TAG, "Using API for source: ${api.source}")

    viewModelScope.launch(Dispatchers.IO) {
      withContext(Dispatchers.Main) {
        _isLoading.value = true
        _errorMessage.value = null
      }
      try {
        Logger.d(TAG, "Finding files with extension: ${dataType.fileExtension}")
        val files = api.findFiles(dataType.fileExtension)
        Logger.i(TAG, "Found ${files.size} files for ${dataType.name}")
        withContext(Dispatchers.Main) {
          _uiState.value = CloudTestUiState.FileList(dataType, files)
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to load files for ${dataType.name}: ${e.message}", e)
        withContext(Dispatchers.Main) {
          _errorMessage.value = "Failed to load files: ${e.message}"
        }
      } finally {
        withContext(Dispatchers.Main) {
          _isLoading.value = false
        }
      }
    }
  }

  /**
   * Loads and previews the content of a specific file.
   *
   * @param dataType The data type of the file
   * @param cloudFile The file to preview
   */
  fun previewFile(dataType: CloudTestUiState.DataType, cloudFile: CloudFile) {
    Logger.i(TAG, "previewFile: ${cloudFile.name}, dataType: ${dataType.name}")

    val api = currentApi
    if (api == null) {
      Logger.e(TAG, "Cannot preview file - no cloud service selected")
      _errorMessage.value = "No cloud service selected"
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      withContext(Dispatchers.Main) {
        _isLoading.value = true
        _errorMessage.value = null
      }
      try {
        Logger.d(TAG, "Downloading file: ${cloudFile.name}")
        val stream = api.downloadFile(cloudFile)

        if (stream == null) {
          Logger.e(TAG, "Downloaded stream is null for file: ${cloudFile.name}")
          withContext(Dispatchers.Main) {
            _errorMessage.value = "Failed to download file"
          }
          return@launch
        }

        // Handle NoteImages as binary image data
        if (dataType.name == "NoteImages") {
          Logger.d(TAG, "Loading image data for ${cloudFile.name}")
          val imageBytes = stream.readBytes()
          Logger.i(TAG, "Image preview ready, size: ${imageBytes.size} bytes")
          withContext(Dispatchers.Main) {
            _uiState.value = CloudTestUiState.FilePreview(
              dataType = dataType,
              cloudFile = cloudFile,
              content = "",
              imageData = imageBytes
            )
          }
        } else {
          Logger.d(TAG, "Decoding file content for ${cloudFile.name}")
          val content = decodeFileContent(stream, dataType, cloudFile.name)

          Logger.i(TAG, "File preview ready, content length: ${content.length}")
          withContext(Dispatchers.Main) {
            _uiState.value = CloudTestUiState.FilePreview(
              dataType = dataType,
              cloudFile = cloudFile,
              content = content
            )
          }
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to preview file ${cloudFile.name}: ${e.message}", e)
        withContext(Dispatchers.Main) {
          _errorMessage.value = "Failed to preview file: ${e.message}"
        }
      } finally {
        withContext(Dispatchers.Main) {
          _isLoading.value = false
        }
      }
    }
  }

  /**
   * Decodes file content based on data type.
   *
   * @param stream The input stream containing the file data
   * @param dataType The data type to determine decoding method
   * @param fileName The file name for logging
   * @return Prettified JSON or XML string
   */
  private suspend fun decodeFileContent(
    stream: InputStream,
    dataType: CloudTestUiState.DataType,
    fileName: String
  ): String {
    return withContext(Dispatchers.IO) {
      try {
        when (dataType.name) {
          "Settings" -> {
            // Decode as SettingsModel and convert to XML
            val settings = parseSettingsModel(stream)
            Logger.d(TAG, "Decoded SettingsModel with ${settings.data.size} entries")
            convertSettingsToXml(settings)
          }
          else -> {
            // Decode as JSON
            val jsonContent = parseJsonContent(stream)
            Logger.d(TAG, "Decoded JSON content")
            prettifyJson(jsonContent)
          }
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to decode file content: ${e.message}", e)
        "Error decoding file: ${e.message}\n\nRaw content might be corrupted or in unexpected format."
      }
    }
  }

  /**
   * Parses SettingsModel from Base64-encoded stream.
   */
  private fun parseSettingsModel(stream: InputStream): com.github.naz013.sync.settings.SettingsModel {
    val base64Input = android.util.Base64InputStream(stream, android.util.Base64.DEFAULT)
    val objectInput = java.io.ObjectInputStream(base64Input)

    return objectInput.use { input ->
      val obj = input.readObject()

      if (obj !is Map<*, *>) {
        throw IllegalStateException("Expected Map but got ${obj?.javaClass?.name ?: "null"}")
      }

      @Suppress("UNCHECKED_CAST")
      val entries = obj as Map<String, *>
      com.github.naz013.sync.settings.SettingsModel(entries)
    }
  }

  /**
   * Converts SettingsModel to prettified XML format.
   */
  private fun convertSettingsToXml(settings: com.github.naz013.sync.settings.SettingsModel): String {
    val sb = StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    sb.append("<settings>\n")

    settings.data.entries.sortedBy { it.key }.forEach { (key, value) ->
      val valueStr = when (value) {
        null -> "null"
        is String -> value
        is Number -> value.toString()
        is Boolean -> value.toString()
        else -> value.toString()
      }
      val valueType = value?.javaClass?.simpleName ?: "null"

      sb.append("  <entry key=\"$key\" type=\"$valueType\">")
      sb.append(escapeXml(valueStr))
      sb.append("</entry>\n")
    }

    sb.append("</settings>")
    return sb.toString()
  }

  /**
   * Escapes XML special characters.
   */
  private fun escapeXml(text: String): String {
    return text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;")
  }

  /**
   * Parses JSON content from Base64-encoded stream.
   */
  private fun parseJsonContent(stream: InputStream): String {
    val base64Input = android.util.Base64InputStream(stream, android.util.Base64.DEFAULT)
    val bufferedReader = java.io.BufferedReader(java.io.InputStreamReader(base64Input))
    return bufferedReader.use { it.readText() }
  }

  /**
   * Prettifies JSON string with indentation.
   */
  private fun prettifyJson(json: String): String {
    return try {
      val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
      val jsonElement = com.google.gson.JsonParser.parseString(json)
      gson.toJson(jsonElement)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to prettify JSON: ${e.message}", e)
      json
    }
  }

  /**
   * Navigates back to the file list from preview.
   */
  fun backToFileList(dataType: CloudTestUiState.DataType) {
    Logger.d(TAG, "Navigating back to file list for ${dataType.name}")
    loadFiles(dataType)
  }

  /**
   * Navigates back to the folder list.
   */
  fun backToFolderList() {
    Logger.d(TAG, "Navigating back to folder list")
    _uiState.value = CloudTestUiState.FolderList(getInternalDataTypes())
  }

  /**
   * Navigates back to the source selection.
   */
  fun backToSourceSelection() {
    Logger.d(TAG, "Navigating back to source selection")
    currentSource = null
    currentApi = null
    _uiState.value = CloudTestUiState.SelectSource
  }

  /**
   * Deletes a specific file from cloud storage.
   *
   * @param cloudFile The file to delete
   * @param dataType The data type of the file
   */
  fun deleteFile(cloudFile: CloudFile, dataType: CloudTestUiState.DataType) {
    Logger.i(TAG, "deleteFile: ${cloudFile.name}, dataType: ${dataType.name}")

    val api = currentApi
    if (api == null) {
      Logger.e(TAG, "Cannot delete file - no cloud service selected")
      _errorMessage.value = "No cloud service selected"
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      withContext(Dispatchers.Main) {
        _isLoading.value = true
        _errorMessage.value = null
      }
      try {
        Logger.d(TAG, "Deleting file: ${cloudFile.name}")
        val success = api.deleteFile(cloudFile.name)

        if (success) {
          Logger.i(TAG, "File deleted successfully: ${cloudFile.name}")
          withContext(Dispatchers.Main) {
            _errorMessage.value = "File deleted successfully"
            // Reload the file list
            loadFiles(dataType)
          }
        } else {
          Logger.e(TAG, "Failed to delete file: ${cloudFile.name}")
          withContext(Dispatchers.Main) {
            _errorMessage.value = "Failed to delete file"
          }
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Error deleting file ${cloudFile.name}: ${e.message}", e)
        withContext(Dispatchers.Main) {
          _errorMessage.value = "Error deleting file: ${e.message}"
        }
      } finally {
        withContext(Dispatchers.Main) {
          _isLoading.value = false
        }
      }
    }
  }

  /**
   * Deletes all files in a specific folder (DataType).
   *
   * @param dataType The data type folder to clear
   */
  fun deleteAllFilesInFolder(dataType: CloudTestUiState.DataType) {
    Logger.i(TAG, "deleteAllFilesInFolder: ${dataType.name}")

    val api = currentApi
    if (api == null) {
      Logger.e(TAG, "Cannot delete files - no cloud service selected")
      _errorMessage.value = "No cloud service selected"
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      withContext(Dispatchers.Main) {
        _isLoading.value = true
        _errorMessage.value = null
      }
      try {
        Logger.d(TAG, "Finding all files with extension: ${dataType.fileExtension}")
        val files = api.findFiles(dataType.fileExtension)
        Logger.i(TAG, "Found ${files.size} files to delete in ${dataType.name}")

        var deletedCount = 0
        var failedCount = 0

        files.forEach { file ->
          try {
            Logger.d(TAG, "Deleting file: ${file.name}")
            val success = api.deleteFile(file.name)
            if (success) {
              deletedCount++
            } else {
              failedCount++
              Logger.w(TAG, "Failed to delete file: ${file.name}")
            }
          } catch (e: Exception) {
            failedCount++
            Logger.e(TAG, "Error deleting file ${file.name}: ${e.message}", e)
          }
        }

        Logger.i(TAG, "Deleted $deletedCount files, failed: $failedCount")
        withContext(Dispatchers.Main) {
          if (failedCount == 0) {
            _errorMessage.value = "Successfully deleted $deletedCount files"
          } else {
            _errorMessage.value = "Deleted $deletedCount files, $failedCount failed"
          }
          // Reload the file list
          loadFiles(dataType)
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Error deleting all files in ${dataType.name}: ${e.message}", e)
        withContext(Dispatchers.Main) {
          _errorMessage.value = "Error deleting files: ${e.message}"
        }
      } finally {
        withContext(Dispatchers.Main) {
          _isLoading.value = false
        }
      }
    }
  }

  /**
   * Clears all data from cloud storage.
   */
  fun clearAllData() {
    Logger.i(TAG, "clearAllData from $currentSource")

    val api = currentApi
    if (api == null) {
      Logger.e(TAG, "Cannot clear data - no cloud service selected")
      _errorMessage.value = "No cloud service selected"
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      withContext(Dispatchers.Main) {
        _isLoading.value = true
        _errorMessage.value = null
      }
      try {
        Logger.d(TAG, "Clearing all data from cloud storage")
        val success = api.removeAllData()

        if (success) {
          Logger.i(TAG, "All data cleared successfully")
          withContext(Dispatchers.Main) {
            _errorMessage.value = "All data cleared successfully"
            // Navigate back to folder list
            _uiState.value = CloudTestUiState.FolderList(getInternalDataTypes())
          }
        } else {
          Logger.e(TAG, "Failed to clear all data")
          withContext(Dispatchers.Main) {
            _errorMessage.value = "Failed to clear all data"
          }
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Error clearing all data: ${e.message}", e)
        withContext(Dispatchers.Main) {
          _errorMessage.value = "Error clearing data: ${e.message}"
        }
      } finally {
        withContext(Dispatchers.Main) {
          _isLoading.value = false
        }
      }
    }
  }

  /**
   * Clears the current error message.
   */
  fun clearError() {
    _errorMessage.value = null
  }

  /**
   * Logs out from the current cloud service.
   */
  fun logout() {
    Logger.i(TAG, "Logging out from $currentSource")

    when (currentSource) {
      Source.GoogleDrive -> {
        Logger.d(TAG, "Disconnecting Google Drive API")
        googleDriveApi.disconnect()
        googleDriveAuthManager.removeUserName()
      }
      Source.Dropbox -> {
        Logger.d(TAG, "Disconnecting Dropbox API")
        dropboxApi.disconnect()
        dropboxAuthManager.removeOAuth2Token()
      }
      null -> {
        Logger.w(TAG, "Logout called but no source selected")
      }
    }
    backToSourceSelection()
  }

  fun getInternalDataTypes(): List<CloudTestUiState.DataType> {
    return DataType.entries.map {
      CloudTestUiState.DataType(it.name, it.fileExtension)
    } + CloudTestUiState.DataType("NoteImages", ".nif")
  }

  companion object {
    private const val TAG = "CloudTestViewModel"
  }
}

/**
 * Represents the different UI states for the cloud test screen.
 */
sealed class CloudTestUiState {
  /**
   * Initial state for selecting a cloud source.
   */
  data object SelectSource : CloudTestUiState()

  /**
   * State indicating authentication is required.
   */
  data class NeedAuth(val source: Source) : CloudTestUiState()

  /**
   * State showing the list of available folders (DataTypes).
   */
  data class FolderList(val dataTypes: List<CloudTestUiState.DataType>) : CloudTestUiState()

  /**
   * State showing files in a specific folder.
   */
  data class FileList(val dataType: DataType, val files: List<CloudFile>) : CloudTestUiState()

  /**
   * State showing the preview of a file's content.
   */
  data class FilePreview(
    val dataType: DataType,
    val cloudFile: CloudFile,
    val content: String = "",
    val imageData: ByteArray? = null
  ) : CloudTestUiState() {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as FilePreview

      if (dataType != other.dataType) return false
      if (cloudFile != other.cloudFile) return false
      if (content != other.content) return false
      if (imageData != null) {
        if (other.imageData == null) return false
        if (!imageData.contentEquals(other.imageData)) return false
      } else if (other.imageData != null) return false

      return true
    }

    override fun hashCode(): Int {
      var result = dataType.hashCode()
      result = 31 * result + cloudFile.hashCode()
      result = 31 * result + content.hashCode()
      result = 31 * result + (imageData?.contentHashCode() ?: 0)
      return result
    }
  }

  data class DataType(val name: String, val fileExtension: String)
}
