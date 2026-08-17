package com.github.naz013.feature.settings.troubleshooting

import java.io.File

interface TroubleshootingCacheUtil {
  fun cacheFile(file: File): File?
}
