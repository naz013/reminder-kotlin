package com.github.naz013.feature.reminder.build.bi.constraint

internal sealed class BiConstraint<T> {
  abstract val value: T
}

internal data class PermissionConstraint(
  override val value: String,
) : BiConstraint<String>()

internal data class RequiresAllConstraint<T>(
  override val value: T,
) : BiConstraint<T>()

internal data class RequiresAnyOfConstraint<T>(
  override val value: T,
) : BiConstraint<T>()

internal data class BlockedByConstraint<T>(
  override val value: T,
) : BiConstraint<T>()

internal data class MandatoryIfConstraint<T>(
  override val value: T,
) : BiConstraint<T>()
