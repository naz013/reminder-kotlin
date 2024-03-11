package com.elementary.tasks.reminder.build.bi.constraint

sealed class BiConstraint<T> {
  abstract val value: T
}

data class PermissionConstraint(
  override val value: String
) : BiConstraint<String>()

data class RequiresAllConstraint<T>(
  override val value: T
) : BiConstraint<T>()

data class RequiresAnyOfConstraint<T>(
  override val value: T
) : BiConstraint<T>()

data class BlockedByConstraint<T>(
  override val value: T
) : BiConstraint<T>()

data class MandatoryIfConstraint<T>(
  override val value: T
) : BiConstraint<T>()
