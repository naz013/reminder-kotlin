package com.elementary.tasks.core.cloud

import android.content.Context
import com.github.naz013.sync.FileCacheProvider
import java.io.File

class FileCacheProviderImpl(
  private val context: Context
) : FileCacheProvider {

  override fun getRootCacheDir(): File {
    return context.externalCacheDir ?: context.cacheDir
  }
}
