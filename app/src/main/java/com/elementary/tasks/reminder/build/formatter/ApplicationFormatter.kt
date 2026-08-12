package com.elementary.tasks.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.common.PackageManagerWrapper

class ApplicationFormatter(
  private val packageManagerWrapper: PackageManagerWrapper,
) : Formatter<String>() {
  override fun format(appId: String): String = packageManagerWrapper.getApplicationName(appId)
}
