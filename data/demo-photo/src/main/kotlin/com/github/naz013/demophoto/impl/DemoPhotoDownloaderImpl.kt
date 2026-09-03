package com.github.naz013.demophoto.impl

import com.github.naz013.demophoto.DemoPhoto
import com.github.naz013.demophoto.DemoPhotoDownloader
import com.github.naz013.logging.Logger
import kotlin.random.Random

internal class DemoPhotoDownloaderImpl(
  private val service: PicsumService,
  private val cache: DemoPhotoCache,
) : DemoPhotoDownloader {

  override suspend fun downloadRandomWallpaper(): DemoPhoto? {
    cache.read()?.let { return it }
    return runCatching {
      val candidates = service.list(page = Random.nextInt(1, MAX_LIST_PAGE), limit = LIST_PAGE_SIZE)
      val chosen = candidates.random()
      val bytes = service.downloadPhoto(chosen.id, PHOTO_WIDTH, PHOTO_HEIGHT).bytes()
      DemoPhoto(bytes = bytes, photographerName = chosen.author, sourcePageUrl = chosen.url)
        .also(cache::write)
    }.onFailure {
      Logger.w(TAG, "Failed to download demo wallpaper photo: ${it.message}")
    }.getOrNull()
  }

  companion object {
    private const val TAG = "DemoPhotoDownloader"
    private const val MAX_LIST_PAGE = 20
    private const val LIST_PAGE_SIZE = 30
    private const val PHOTO_WIDTH = 1080
    private const val PHOTO_HEIGHT = 1350
  }
}
