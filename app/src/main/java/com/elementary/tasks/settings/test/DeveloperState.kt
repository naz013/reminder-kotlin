package com.elementary.tasks.settings.test

data class DeveloperState(
  val dialog: DeveloperChoiceDialog? = null,
)

enum class DeveloperDialogKind { BIRTHDAY, REMINDER }

data class DeveloperChoiceDialog(
  val kind: DeveloperDialogKind,
  val options: List<String>,
  val selectedIndex: Int,
)

sealed class DeveloperEvent {
  data object OpenObjectExport : DeveloperEvent()

  data object OpenReviewDialog : DeveloperEvent()

  data object OpenProVersion : DeveloperEvent()

  data class OpenReminderAction(val reminderId: String) : DeveloperEvent()

  data class OpenBirthdayAction(val birthdayId: String) : DeveloperEvent()
}
