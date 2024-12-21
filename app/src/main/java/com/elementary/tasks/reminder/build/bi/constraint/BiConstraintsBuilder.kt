package com.elementary.tasks.reminder.build.bi.constraint

import com.elementary.tasks.reminder.build.bi.BiGroup
import com.github.naz013.domain.reminder.BiType

class BiConstraints(
  val constraints: List<BiConstraint<*>>
) {
  private constructor(builder: Builder) : this(builder.constraints)

  class Builder {
    val constraints = mutableListOf<BiConstraint<*>>()
    fun build() = BiConstraints(this)

    fun permission(vararg permissions: String) {
      permissions.toHashSet().map { PermissionConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun requiresAll(vararg biType: BiType) {
      biType.toHashSet().map { RequiresAllConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun requiresAll(vararg biGroup: BiGroup) {
      biGroup.toHashSet().map { RequiresAllConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun requiresAny(vararg biType: BiType) {
      biType.toHashSet().map { RequiresAnyOfConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun requiresAny(vararg biGroup: BiGroup) {
      biGroup.toHashSet().map { RequiresAnyOfConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun blockedBy(vararg biType: BiType) {
      biType.toHashSet().map { BlockedByConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun blockedBy(vararg biGroup: BiGroup) {
      biGroup.toHashSet().map { BlockedByConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun mandatoryIf(vararg biType: BiType) {
      biType.toHashSet().map { MandatoryIfConstraint(it) }.also {
        constraints.addAll(it)
      }
    }

    fun mandatoryIf(vararg biGroup: BiGroup) {
      biGroup.toHashSet().map { MandatoryIfConstraint(it) }.also {
        constraints.addAll(it)
      }
    }
  }
}

inline fun constraints(block: BiConstraints.Builder.() -> Unit): List<BiConstraint<*>> {
  return BiConstraints.Builder().apply(block).build().constraints
}
