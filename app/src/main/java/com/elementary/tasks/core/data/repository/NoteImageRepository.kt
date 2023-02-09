package com.elementary.tasks.core.data.repository

import android.content.Context
import com.elementary.tasks.core.data.models.ImageFile
import timber.log.Timber
import java.io.File
import java.io.InputStream

class NoteImageRepository(private val context: Context) {

  fun readBytes(filePath: String): ByteArray? {
    return File(filePath).takeIf { it.exists() }?.readBytes()
  }

  fun saveBytesToFile(fileName: String, byteArray: ByteArray?, folderName: String): String {
    if (byteArray == null) return ""
    return runCatching {
      val folder = getImageFolder(folderName)
      val file = File(folder, fileName).also {
        runCatching { it.createNewFile() }
      }
      file.writeBytes(byteArray)
      Timber.d("saveBytesToFile: saved $file")
      file.toString()
    }.getOrNull() ?: ""
  }

  fun moveImagesToFolder(files: List<ImageFile>, folderName: String): List<ImageFile> {
    val tmpFolder = getTmpFolder()
    val dstFolder = getImageFolder(folderName)
    val fileNames = files.map { it.fileName }
    tmpFolder.listFiles()?.filter { it.isFile && !it.isHidden }?.forEach { file ->
      if (fileNames.contains(file.name)) {
        val dstFile = File(dstFolder, file.name)
        Timber.d("moveImagesToFolder: name=${file.name}, dst=$dstFile")
        file.copyTo(dstFile, overwrite = true)
        files.firstOrNull { it.fileName == file.name }?.apply {
          filePath = dstFile.toString()
        }
      }
    }
    clearTemporaryFolder()
    return files
  }

  fun saveTemporaryImage(fileName: String, inputStream: InputStream): String {
    val tmpFile = createTemporaryFile(fileName)
    Timber.d("saveTemporaryImage: name=$fileName")
    tmpFile.copyInputStreamToFile(inputStream)
    return tmpFile.toString()
  }

  fun clearFolder(folderName: String) {
    Timber.d("clearFolder: $folderName")
    getImageFolder(folderName).deleteRecursively()
  }

  private fun clearTemporaryFolder() {
    Timber.d("clearTemporaryFolder: ")
    getTmpFolder().deleteRecursively()
  }

  private fun createTemporaryFile(fileName: String): File {
    return File(getTmpFolder(), fileName).also { it.createNewFile() }
  }

  fun getImageFolder(folderName: String): File {
    return File(getImagesFolder(), folderName).also {
      if (!it.exists()) {
        it.mkdirs()
      }
    }
  }

  private fun getTmpFolder(): File {
    return File(getImagesFolder(), "tmp").also {
      if (!it.exists()) {
        it.mkdirs()
      }
    }
  }

  private fun getImagesFolder(): File {
    return context.getDir("note_images", Context.MODE_PRIVATE)
  }

  private fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
      inputStream.copyTo(fileOut)
    }
  }
}
