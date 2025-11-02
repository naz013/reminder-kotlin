package com.github.naz013.sync.images

import com.github.naz013.logging.Logger
import com.github.naz013.sync.FileCacheProvider
import java.io.File
import java.io.InputStream

class CacheFileUseCase(
  private val fileCacheProvider: FileCacheProvider
) {

  operator fun invoke(
    inputStream: InputStream,
    folder: String,
    name: String
  ): String {
    val rootFolder = File(fileCacheProvider.getRootCacheDir(), folder)
    if (!rootFolder.exists()) {
      rootFolder.mkdirs()
    }
    val file = File(rootFolder, name)
    inputStream.use { input ->
      file.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    Logger.i(TAG, "Cached file at: ${file.absolutePath}")
    return file.absolutePath
  }

  companion object {
    private const val TAG = "CacheFileUseCase"
  }
}
