package com.elementary.tasks.reminder.build.logic

import android.content.Context
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints

class BuilderItemPermissionConstraintCalculator(
  private val context: Context
) {

  operator fun invoke(item: BuilderItemConstraints): List<String> {
    return item.permissions.map { it.value }
      .toHashSet()
      .filterNot { Permissions.checkPermission(context, it) }
  }
}
