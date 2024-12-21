package com.github.naz013.feature.common

import java.io.File
import java.io.InputStream

fun File.copyInputStreamToFile(inputStream: InputStream) {
  inputStream.use { input ->
    this.outputStream().use { fileOut ->
      input.copyTo(fileOut)
    }
  }
}
