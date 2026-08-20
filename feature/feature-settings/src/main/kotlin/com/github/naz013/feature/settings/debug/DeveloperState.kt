package com.github.naz013.feature.settings.debug

import com.github.naz013.reviews.AppSource

internal data class DeveloperState(
  val dialog: DeveloperChoiceDialog? = null,
  val clearAllTablesConfirmation: Boolean = false,
  val pendingRecurrenceTestMinutes: Int? = null,
)

internal enum class DeveloperDialogKind { BIRTHDAY, REMINDER, CLEAR_TABLE, RECURRENCE_TEST, RECURRENCE_TEST_TYPE }

internal data class DeveloperChoiceDialog(
  val kind: DeveloperDialogKind,
  val options: List<String>,
  val selectedIndex: Int,
)

internal sealed class DeveloperEvent {
  data object OpenObjectExport : DeveloperEvent()

  data class OpenReviewDialog(
    val appSource: AppSource,
  ) : DeveloperEvent()

  data object OpenProVersion : DeveloperEvent()

  data class OpenReminderAction(
    val reminderId: String,
  ) : DeveloperEvent()

  data class OpenBirthdayAction(
    val birthdayId: String,
  ) : DeveloperEvent()

  data object BannersReset : DeveloperEvent()

  data class ShowMessage(
    val message: String,
  ) : DeveloperEvent()
}
