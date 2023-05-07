package com.elementary.tasks.core.os.data

import com.elementary.tasks.R
import com.elementary.tasks.core.os.Permissions

data class UiPermissionDialogData(
  val permission: String,
  val title: Int,
  val description: Int
) {

  companion object {
    val COARSE_LOCATION = UiPermissionDialogData(
      Permissions.ACCESS_COARSE_LOCATION,
      R.string.course_location,
      R.string.course_location_explanation
    )
    val FINE_LOCATION = UiPermissionDialogData(
      Permissions.ACCESS_FINE_LOCATION,
      R.string.fine_location,
      R.string.fine_location_explanation
    )

    val READ_CALENDAR = UiPermissionDialogData(
      Permissions.READ_CALENDAR,
      R.string.read_calendar,
      R.string.read_calendar_explanation
    )
    val WRITE_CALENDAR = UiPermissionDialogData(
      Permissions.WRITE_CALENDAR,
      R.string.write_calendar,
      R.string.write_calendar_explanation
    )

    val READ_EXTERNAL= UiPermissionDialogData(
      Permissions.READ_EXTERNAL,
      R.string.read_external_storage,
      R.string.read_external_storage_explanation
    )
    val WRITE_EXTERNAL = UiPermissionDialogData(
      Permissions.WRITE_EXTERNAL,
      R.string.write_external_storage,
      R.string.write_external_storage_explanation
    )

    val READ_CONTACTS = UiPermissionDialogData(
      Permissions.READ_CONTACTS,
      R.string.read_contacts,
      R.string.read_contacts_explanation
    )
    val GET_ACCOUNTS = UiPermissionDialogData(
      Permissions.GET_ACCOUNTS,
      R.string.get_accounts,
      R.string.get_accounts_explanation
    )
    val CALL_PHONE = UiPermissionDialogData(
      Permissions.CALL_PHONE,
      R.string.call_phone,
      R.string.call_phone_explanation
    )
    val RECORD_AUDIO = UiPermissionDialogData(
      Permissions.RECORD_AUDIO,
      R.string.record_audio,
      R.string.record_audio_explanation
    )
    val BACKGROUND_LOCATION = UiPermissionDialogData(
      Permissions.BACKGROUND_LOCATION,
      R.string.background_location,
      R.string.background_location_explanation
    )
    val FOREGROUND_SERVICE = UiPermissionDialogData(
      Permissions.FOREGROUND_SERVICE,
      R.string.foreground_service,
      R.string.foreground_service_explanation
    )
    val POST_NOTIFICATION = UiPermissionDialogData(
      Permissions.POST_NOTIFICATION,
      R.string.post_notification,
      R.string.post_notification_explanation
    )
  }
}
