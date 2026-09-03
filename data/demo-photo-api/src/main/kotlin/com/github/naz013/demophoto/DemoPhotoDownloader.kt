package com.github.naz013.demophoto

/** Fetches a showcase photo for demo/first-install content. Implementations never throw -
 * a failed or offline fetch simply returns null so callers can degrade gracefully. */
interface DemoPhotoDownloader {
  suspend fun downloadRandomWallpaper(): DemoPhoto?
}
