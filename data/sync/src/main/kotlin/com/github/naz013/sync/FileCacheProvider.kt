package com.github.naz013.sync

import java.io.File

interface FileCacheProvider {
  /**
   * Root directory for downloaded/migrated note images. Must resolve to the same directory
   * [com.github.naz013.logic.note.NoteImageRepository] uses for locally created note images, so
   * that a note's images always live in one place regardless of source - otherwise synced images
   * end up in an OS-reclaimable cache dir that note deletion never cleans up.
   */
  fun getNoteImagesRootDir(): File
}
