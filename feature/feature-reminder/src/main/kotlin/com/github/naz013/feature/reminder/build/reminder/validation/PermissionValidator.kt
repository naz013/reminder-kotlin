package com.github.naz013.feature.reminder.build.reminder.validation

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.bi.constraint.PermissionConstraint
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.Permissions

internal class PermissionValidator(
  private val contextProvider: ContextProvider,
) {
  operator fun invoke(items: List<BuilderItem<*>>): Result {
    val set = HashSet<String>()
    items
      .map { it.constraints.filterIsInstance<PermissionConstraint>() }
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
      val permissions: List<String>,
    ) : Result()
  }

  private fun checkPermission(permission: String): Boolean = Permissions.checkPermission(contextProvider.context, permission)
}
