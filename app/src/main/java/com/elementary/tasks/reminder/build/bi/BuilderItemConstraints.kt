package com.elementary.tasks.reminder.build.bi

import com.elementary.tasks.reminder.build.bi.constraint.BiConstraint
import com.elementary.tasks.reminder.build.bi.constraint.BlockedByConstraint
import com.elementary.tasks.reminder.build.bi.constraint.MandatoryIfConstraint
import com.elementary.tasks.reminder.build.bi.constraint.PermissionConstraint
import com.elementary.tasks.reminder.build.bi.constraint.RequiresAllConstraint
import com.elementary.tasks.reminder.build.bi.constraint.RequiresAnyOfConstraint

data class BuilderItemConstraints(val constraints: List<BiConstraint<*>>) {

  val requiresAny: List<RequiresAnyOfConstraint<*>> by lazy {
    constraints.filterIsInstance<RequiresAnyOfConstraint<*>>()
  }

  val requiresAnyType: List<RequiresAnyOfConstraint<BiType>> by lazy {
    requiresAny.filter { it.value is BiType }
      .mapNotNull { it as? RequiresAnyOfConstraint<BiType> }
  }

  val requiresAnyGroup: List<RequiresAnyOfConstraint<BiGroup>> by lazy {
    requiresAny.filter { it.value is BiGroup }
      .mapNotNull { it as? RequiresAnyOfConstraint<BiGroup> }
  }

  val requiresAll: List<RequiresAllConstraint<*>> by lazy {
    constraints.filterIsInstance<RequiresAllConstraint<*>>()
  }

  val requiresAllType: List<RequiresAllConstraint<BiType>> by lazy {
    requiresAll.filter { it.value is BiType }
      .mapNotNull { it as? RequiresAllConstraint<BiType> }
  }

  val requiresAllGroup: List<RequiresAllConstraint<BiGroup>> by lazy {
    requiresAll.filter { it.value is BiGroup }
      .mapNotNull { it as? RequiresAllConstraint<BiGroup> }
  }

  val blockedBy: List<BlockedByConstraint<*>> by lazy {
    constraints.filterIsInstance<BlockedByConstraint<*>>()
  }

  val blockedByType: List<BlockedByConstraint<BiType>> by lazy {
    blockedBy.filter { it.value is BiType }
      .mapNotNull { it as? BlockedByConstraint<BiType> }
  }

  val blockedByGroup: List<BlockedByConstraint<BiGroup>> by lazy {
    blockedBy.filter { it.value is BiGroup }
      .mapNotNull { it as? BlockedByConstraint<BiGroup> }
  }

  val mandatoryIf: List<MandatoryIfConstraint<*>> by lazy {
    constraints.filterIsInstance<MandatoryIfConstraint<*>>()
  }

  val mandatoryIfType: List<MandatoryIfConstraint<BiType>> by lazy {
    mandatoryIf.filter { it.value is BiType }
      .mapNotNull { it as? MandatoryIfConstraint<BiType> }
  }

  val mandatoryIfGroup: List<MandatoryIfConstraint<BiGroup>> by lazy {
    mandatoryIf.filter { it.value is BiGroup }
      .mapNotNull { it as? MandatoryIfConstraint<BiGroup> }
  }

  val permissions: List<PermissionConstraint> = constraints.filterIsInstance<PermissionConstraint>()

  override fun toString(): String {
    return "BuilderItemConstraints(" +
      "constraints=$constraints, " +
      "requiresAll=$requiresAll, " +
      "requiresAllType=$requiresAllType, " +
      "requiresAllGroup=$requiresAllGroup" +
      ")"
  }
}
