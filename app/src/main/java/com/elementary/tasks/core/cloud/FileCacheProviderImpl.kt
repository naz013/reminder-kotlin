package com.elementary.tasks.core.cloud

import android.content.Context
import com.github.naz013.sync.FileCacheProvider
import java.io.File

class FileCacheProviderImpl(
  private val context: Context,
) : FileCacheProvider {
  // Must match NoteImageRepositoryImpl.getImagesFolder() in feature:feature-note - same
  // directory name, so a note's images end up in one place regardless of source.
  override fun getNoteImagesRootDir(): File = context.getDir("note_images", Context.MODE_PRIVATE)
}
