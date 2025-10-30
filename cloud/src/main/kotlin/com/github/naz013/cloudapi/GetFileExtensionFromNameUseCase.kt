package com.github.naz013.cloudapi

internal class GetFileExtensionFromNameUseCase {

  /**
   * Extracts the file extension from a given file name.
   *
   * @param fileName The name of the file
   * @return The file extension, or an empty string if none exists
   */
  operator fun invoke(fileName: String): String {
    val lastDotIndex = fileName.lastIndexOf('.')
    return if (lastDotIndex != -1 && lastDotIndex < fileName.length - 1) {
      fileName.substring(lastDotIndex + 1)
    } else {
      ""
    }
  }
}
