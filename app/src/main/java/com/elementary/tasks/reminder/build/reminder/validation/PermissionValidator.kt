package com.elementary.tasks.reminder.build.reminder.validation

import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.bi.constraint.PermissionConstraint

class PermissionValidator(
  private val contextProvider: ContextProvider
) {

  operator fun invoke(items: List<BuilderItem<*>>): Result {
    val set = HashSet<String>()
    items.map { it.constraints.filterIsInstance<PermissionConstraint>() }
      .flatten()
      .map { it.value }
      .toHashSet()
      .forEach {
        if (!checkPermission(it)) {
          set.add(it)
        }
      }
    return if (set.isEmpty()) {
      Result.Success
    } else {
      Result.Failure(set.toList())
    }
  }

  sealed class Result {
    data object Success : Result()
    data class Failure(
      val permissions: List<String>
    ) : Result()
  }

  private fun checkPermission(permission: String): Boolean {
    return Permissions.checkPermission(contextProvider.context, permission)
  }
}
