package com.elementary.tasks.reminder.build.bi

import com.github.naz013.domain.reminder.BiType

sealed class BuilderItemError {
  data class RequiresAllConstraintError(
    val constraints: List<BiType>
  ) : BuilderItemError()

  data class RequiresAnyConstraintError(
    val constraints: List<BiType>
  ) : BuilderItemError()

  data class BlockedByConstraintError(
    val constraints: List<BiType>
  ) : BuilderItemError()

  data class MandatoryIfConstraintError(
    val constraints: List<BiType>
  ) : BuilderItemError()

  data class PermissionConstraintError(
    val permissions: List<String>
  ) : BuilderItemError()
}
