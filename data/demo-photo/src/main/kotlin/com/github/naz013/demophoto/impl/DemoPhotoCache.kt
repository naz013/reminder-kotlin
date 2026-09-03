package com.github.naz013.demophoto.impl

import android.content.Context
import com.github.naz013.demophoto.DemoPhoto
import java.io.File

/** Persists the one photo fetched for demo content so repeated seeding (e.g. re-pressing the
 * Developer Settings "Insert Demo Data" button) doesn't re-download it. */
internal class DemoPhotoCache(
  context: Context,
) {
  private val imageFile = File(context.filesDir, "demo_assets/wallpaper.jpg")
  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun read(): DemoPhoto? {
    if (!imageFile.exists()) return null
    val photographerName = prefs.getString(KEY_PHOTOGRAPHER_NAME, null) ?: return null
    val sourcePageUrl = prefs.getString(KEY_SOURCE_PAGE_URL, null) ?: return null
    return runCatching {
      DemoPhoto(bytes = imageFile.readBytes(), photographerName = photographerName, sourcePageUrl = sourcePageUrl)
    }.getOrNull()
  }

  fun write(photo: DemoPhoto) {
    imageFile.parentFile?.mkdirs()
    imageFile.writeBytes(photo.bytes)
    prefs.edit()
      .putString(KEY_PHOTOGRAPHER_NAME, photo.photographerName)
      .putString(KEY_SOURCE_PAGE_URL, photo.sourcePageUrl)
      .apply()
  }

  companion object {
    private const val PREFS_NAME = "demo_photo_cache"
    private const val KEY_PHOTOGRAPHER_NAME = "photographer_name"
    private const val KEY_SOURCE_PAGE_URL = "source_page_url"
  }
}
