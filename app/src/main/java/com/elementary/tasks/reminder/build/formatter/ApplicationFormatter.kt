package com.elementary.tasks.reminder.build.formatter

import com.elementary.tasks.core.os.PackageManagerWrapper

class ApplicationFormatter(
  private val packageManagerWrapper: PackageManagerWrapper
) : Formatter<String>() {

  override fun format(appId: String): String {
    return packageManagerWrapper.getApplicationName(appId)
  }
}
