package com.github.naz013.sync

import java.io.File

interface FileCacheProvider {
  fun getRootCacheDir(): File
}
