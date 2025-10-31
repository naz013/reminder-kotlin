package com.github.nsy.reviewsadmin.cache

import android.content.Context
import com.github.naz013.logging.Logger
import java.io.File
import java.security.MessageDigest

/**
 * Cache manager for log files with automatic size management.
 * Caches extracted text content to avoid re-extraction overhead.
 *
 * @property context Application context for cache directory access
 * @property maxCacheSizeBytes Maximum cache size in bytes (default 100 MB)
 */
class LogFileCache(
  private val context: Context,
  private val maxCacheSizeBytes: Long = 100 * 1024 * 1024 // 100 MB
) {

  private val cacheDir: File by lazy {
    File(context.cacheDir, "log_files").apply {
      if (!exists()) {
        mkdirs()
      }
    }
  }

  /**
   * Gets cached extracted log content if available.
   *
   * @param url The URL of the log file
   * @return The cached extracted log content as String, or null if not cached
   */
  fun getExtractedLog(url: String): String? {
    return try {
      val cacheKey = generateCacheKey(url) + ".txt"
      val cacheFile = File(cacheDir, cacheKey)

      if (cacheFile.exists()) {
        // Update last access time
        cacheFile.setLastModified(System.currentTimeMillis())
        Logger.i(TAG, "Cache hit for extracted log: $url")
        cacheFile.readText()
      } else {
        Logger.i(TAG, "Cache miss for extracted log: $url")
        null
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Error reading extracted log from cache", e)
      null
    }
  }

  /**
   * Puts extracted log content into the cache.
   *
   * @param url The URL of the log file
   * @param extractedContent The extracted log content as String
   */
  fun putExtractedLog(url: String, extractedContent: String) {
    try {
      val contentBytes = extractedContent.toByteArray(Charsets.UTF_8)

      // Ensure cache size limit
      ensureCacheSize(contentBytes.size.toLong())

      val cacheKey = generateCacheKey(url) + ".txt"
      val cacheFile = File(cacheDir, cacheKey)

      cacheFile.writeText(extractedContent, Charsets.UTF_8)
      Logger.i(TAG, "Cached extracted log: $url (${contentBytes.size} bytes)")
    } catch (e: Exception) {
      Logger.e(TAG, "Error writing extracted log to cache", e)
    }
  }

  /**
   * Gets a cached raw zip file if available.
   *
   * @param url The URL of the log file
   * @return The cached file content as ByteArray, or null if not cached
   */
  fun get(url: String): ByteArray? {
    return try {
      val cacheKey = generateCacheKey(url) + ".zip"
      val cacheFile = File(cacheDir, cacheKey)

      if (cacheFile.exists()) {
        // Update last access time
        cacheFile.setLastModified(System.currentTimeMillis())
        Logger.i(TAG, "Cache hit for zip: $url")
        cacheFile.readBytes()
      } else {
        Logger.i(TAG, "Cache miss for zip: $url")
        null
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Error reading zip from cache", e)
      null
    }
  }

  /**
   * Puts a raw zip file into the cache.
   *
   * @param url The URL of the log file
   * @param data The file content as ByteArray
   */
  fun put(url: String, data: ByteArray) {
    try {
      // Ensure cache size limit
      ensureCacheSize(data.size.toLong())

      val cacheKey = generateCacheKey(url) + ".zip"
      val cacheFile = File(cacheDir, cacheKey)

      cacheFile.writeBytes(data)
      Logger.i(TAG, "Cached zip: $url (${data.size} bytes)")
    } catch (e: Exception) {
      Logger.e(TAG, "Error writing zip to cache", e)
    }
  }

  /**
   * Clears all cached files.
   */
  fun clear() {
    try {
      cacheDir.listFiles()?.forEach { it.delete() }
      Logger.i(TAG, "Cache cleared")
    } catch (e: Exception) {
      Logger.e(TAG, "Error clearing cache", e)
    }
  }

  /**
   * Gets the current cache size in bytes.
   *
   * @return Total size of all cached files in bytes
   */
  fun getCacheSize(): Long {
    return try {
      cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    } catch (e: Exception) {
      Logger.e(TAG, "Error calculating cache size", e)
      0L
    }
  }

  /**
   * Ensures the cache doesn't exceed the maximum size by removing oldest files.
   *
   * @param newFileSize Size of the file to be added
   */
  private fun ensureCacheSize(newFileSize: Long) {
    try {
      val currentSize = getCacheSize()
      val requiredSpace = currentSize + newFileSize

      if (requiredSpace <= maxCacheSizeBytes) {
        return // No need to clean up
      }

      // Sort files by last modified time (oldest first)
      val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return

      var freedSpace = 0L
      val spaceToFree = requiredSpace - maxCacheSizeBytes

      for (file in files) {
        if (freedSpace >= spaceToFree) {
          break
        }

        val fileSize = file.length()
        if (file.delete()) {
          freedSpace += fileSize
          Logger.i(TAG, "Removed old cache file: ${file.name} (${fileSize} bytes)")
        }
      }

      Logger.i(TAG, "Cache cleanup: freed $freedSpace bytes")
    } catch (e: Exception) {
      Logger.e(TAG, "Error during cache cleanup", e)
    }
  }

  /**
   * Generates a cache key from a URL using MD5 hash.
   *
   * @param url The URL to generate a key for
   * @return A safe filename based on the URL hash
   */
  private fun generateCacheKey(url: String): String {
    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(url.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
  }

  companion object {
    private const val TAG = "LogFileCache"
  }
}
