package com.elementary.tasks.reminder.build.logic

import android.content.Context
import com.github.naz013.common.Permissions
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
