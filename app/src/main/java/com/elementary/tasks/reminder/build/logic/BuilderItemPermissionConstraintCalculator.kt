package com.elementary.tasks.reminder.build.logic

import android.content.Context
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints
import com.github.naz013.common.Permissions

class BuilderItemPermissionConstraintCalculator(
  private val context: Context,
) {
  operator fun invoke(item: BuilderItemConstraints): List<String> =
    item.permissions
      .map { it.value }
      .toHashSet()
      .filterNot { Permissions.checkPermission(context, it) }
}
