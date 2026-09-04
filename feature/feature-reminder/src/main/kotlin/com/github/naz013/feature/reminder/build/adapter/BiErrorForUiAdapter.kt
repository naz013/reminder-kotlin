package com.github.naz013.feature.reminder.build.adapter

import android.content.Context
import androidx.annotation.StringRes
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.bi.BuilderItemError
import com.github.naz013.common.Permissions
import com.github.naz013.domain.reminder.BiType

internal class BiErrorForUiAdapter(
  private val context: Context,
  private val biTypeForUiAdapter: BiTypeForUiAdapter,
) {
  fun getUiString(errors: List<BuilderItemError>): String {
    if (errors.isEmpty()) return ""
    val errorsStr = errors.joinToString("\n") { getErrorMessage(it) }
    return errorsStr
  }

  /**
   * Message for a builder item whose own value fails [com.elementary.tasks.reminder.build.bi
   * .BuilderModifier.isCorrect] with no attached constraint (e.g. a malformed email, or an empty
   * shopping list) - distinct from [getUiString], which only covers constraint violations and
   * returns an empty string when there are none, leaving the item's error row blank.
   */
  fun getInvalidValueMessage(biType: BiType): String =
    when (biType) {
      BiType.EMAIL -> context.getString(R.string.builder_invalid_email_value)
      BiType.LINK -> context.getString(R.string.builder_invalid_web_address_value)
      BiType.PHONE_CALL, BiType.SMS -> context.getString(R.string.builder_invalid_phone_number_value)
      BiType.SUB_TASKS -> context.getString(R.string.builder_shopping_list_empty_value)
      else -> context.getString(R.string.builder_invalid_value_generic)
    }

  private fun getErrorMessage(error: BuilderItemError): String =
    when (error) {
      is BuilderItemError.PermissionConstraintError -> getPermissionErrorMessage(error)
      is BuilderItemError.RequiresAllConstraintError -> getRequiresAllErrorMessage(error)
      is BuilderItemError.BlockedByConstraintError -> getBlockedByErrorMessage(error)
      is BuilderItemError.MandatoryIfConstraintError -> getMandatoryIfErrorMessage(error)
      is BuilderItemError.RequiresAnyConstraintError -> getRequiresAnyErrorMessage(error)
    }

  private fun getRequiresAllErrorMessage(error: BuilderItemError.RequiresAllConstraintError): String =
    getRequiresErrorMessage(error.constraints, R.string.builder_requires_all_x)

  private fun getRequiresAnyErrorMessage(error: BuilderItemError.RequiresAnyConstraintError): String =
    getRequiresErrorMessage(error.constraints, R.string.builder_requires_any_of_x)

  private fun getRequiresErrorMessage(
    types: List<BiType>,
    @StringRes multipleRes: Int,
  ): String {
    val typesStr = getTypesString(types)
    return if (types.size == 1) {
      context.getString(R.string.builder_requires_x, typesStr)
    } else {
      context.getString(multipleRes, typesStr)
    }
  }

  private fun getBlockedByErrorMessage(error: BuilderItemError.BlockedByConstraintError): String =
    context.getString(R.string.builder_blocked_by_x, getTypesString(error.constraints))

  private fun getMandatoryIfErrorMessage(error: BuilderItemError.MandatoryIfConstraintError): String =
    context.getString(R.string.builder_mandatory_if_x, getTypesString(error.constraints))

  private fun getPermissionErrorMessage(error: BuilderItemError.PermissionConstraintError): String {
    val permissions = error.permissions.joinToString(", ") { getPermissionName(it) }
    return context.getString(R.string.builder_requires_permissions_x, permissions)
  }

  private fun getTypesString(types: List<BiType>): String = types.joinToString(", ") { biTypeForUiAdapter.getUiString(it) }

  private fun getPermissionName(permission: String): String =
    when (permission) {
      Permissions.CALL_PHONE -> context.getString(R.string.call_phone)
      Permissions.CAMERA -> context.getString(R.string.builder_camera)
      Permissions.ACCESS_COARSE_LOCATION -> context.getString(R.string.coarse_location)
      Permissions.ACCESS_FINE_LOCATION -> context.getString(R.string.fine_location)
      Permissions.BACKGROUND_LOCATION -> context.getString(R.string.access_location_in_background)
      Permissions.WRITE_CALENDAR -> context.getString(R.string.write_calendar)
      Permissions.READ_CALENDAR -> context.getString(R.string.read_calendar)
      else -> ""
    }
}
