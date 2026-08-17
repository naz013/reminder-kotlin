package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.io.CacheUtil
import com.github.naz013.feature.settings.troubleshooting.TroubleshootingCacheUtil
import java.io.File

class TroubleshootingCacheUtilImpl(
  private val cacheUtil: CacheUtil,
) : TroubleshootingCacheUtil {
  override fun cacheFile(file: File): File? = cacheUtil.cacheFile(file)
}
