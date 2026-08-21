package com.github.naz013.feature.note.image

import android.content.Context
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.logging.Logger
import java.io.File
import java.io.InputStream

internal class NoteImageRepository(
  private val context: Context,
) {

  fun saveBytesToFile(
    fileName: String,
    byteArray: ByteArray?,
    folderName: String,
  ): String {
    if (byteArray == null) return ""
    return runCatching {
      val folder = getImageFolder(folderName)
      val file =
        File(folder, fileName).also {
          runCatching { it.createNewFile() }
        }
      file.writeBytes(byteArray)
      Logger.d(TAG, "Saved image file: $file")
      file.toString()
    }.getOrNull() ?: ""
  }

  fun moveImagesToFolder(
    files: List<ImageFile>,
    folderName: String,
  ): List<ImageFile> {
    val tmpFolder = getTmpFolder()
    val dstFolder = getImageFolder(folderName)
    val fileNames = files.map { it.fileName }
    tmpFolder.listFiles()?.filter { it.isFile && !it.isHidden }?.forEach { file ->
      if (fileNames.contains(file.name)) {
        val dstFile = File(dstFolder, file.name)
        Logger.d(TAG, "Moving file ${file.name} to $dstFile")
        file.copyTo(dstFile, overwrite = true)
        files.firstOrNull { it.fileName == file.name }?.apply {
          filePath = dstFile.toString()
        }
      }
    }
    clearTemporaryFolder()
    return files
  }

  fun copyImagesToFolder(
    images: List<ImageFile>,
    folderName: String,
  ): List<ImageFile> {
    val dstFolder = getImageFolder(folderName)
    return images.map { image ->
      val srcFile = File(image.filePath)
      if (!srcFile.exists()) return@map image
      val dstFile = File(dstFolder, image.fileName)
      runCatching { srcFile.copyTo(dstFile, overwrite = true) }
      image.copy(filePath = dstFile.toString())
    }
  }

  fun saveTemporaryImage(
    fileName: String,
    inputStream: InputStream,
  ): String {
    val tmpFile = createTemporaryFile(fileName)
    tmpFile.copyInputStreamToFile(inputStream)
    Logger.i(TAG, "Saved temporary image: $tmpFile")
    return tmpFile.toString()
  }

  fun clearFolder(folderName: String) {
    getImageFolder(folderName).deleteRecursively()
    Logger.i(TAG, "Cleared image folder: $folderName")
  }

  private fun clearTemporaryFolder() {
    getTmpFolder().deleteRecursively()
    Logger.i(TAG, "Cleared temporary image folder")
  }

  private fun createTemporaryFile(fileName: String): File = File(getTmpFolder(), fileName).also { it.createNewFile() }

  fun getImageFolder(folderName: String): File =
    File(getImagesFolder(), folderName).also {
      if (!it.exists()) {
        it.mkdirs()
      }
    }

  private fun getTmpFolder(): File =
    File(getImagesFolder(), "tmp").also {
      if (!it.exists()) {
        it.mkdirs()
      }
    }

  private fun getImagesFolder(): File = context.getDir("note_images", Context.MODE_PRIVATE)

  private fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
      inputStream.copyTo(fileOut)
    }
  }

  companion object {
    private const val TAG = "NoteImageRepository"
  }
}
