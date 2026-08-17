package com.github.naz013.feature.reminder.build.logic

import android.content.Context
import com.github.naz013.feature.reminder.build.bi.BuilderItemConstraints
import com.github.naz013.common.Permissions

internal class BuilderItemPermissionConstraintCalculator(
  private val context: Context,
) {
  operator fun invoke(item: BuilderItemConstraints): List<String> =
    item.permissions
      .map { it.value }
      .toHashSet()
      .filterNot { Permissions.checkPermission(context, it) }
}
