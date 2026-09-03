package com.github.naz013.logic.note

import com.github.naz013.domain.note.ImageFile
import java.io.File
import java.io.InputStream

interface NoteImageRepository {

  fun saveBytesToFile(
    fileName: String,
    byteArray: ByteArray?,
    folderName: String,
  ): String

  fun moveImagesToFolder(
    files: List<ImageFile>,
    folderName: String,
  ): List<ImageFile>

  fun copyImagesToFolder(
    images: List<ImageFile>,
    folderName: String,
  ): List<ImageFile>

  fun saveTemporaryImage(
    fileName: String,
    inputStream: InputStream,
  ): String

  fun clearFolder(folderName: String)

  fun getImageFolder(folderName: String): File
}
