package com.elementary.tasks.settings.test

data class TestsSettingsState(
  val dialog: TestChoiceDialog? = null,
)

enum class TestDialogKind { BIRTHDAY, REMINDER }

data class TestChoiceDialog(
  val kind: TestDialogKind,
  val options: List<String>,
  val selectedIndex: Int,
)

sealed class TestsSettingsEvent {
  data object OpenObjectExport : TestsSettingsEvent()

  data object OpenDeveloperOptions : TestsSettingsEvent()

  data object OpenReviewDialog : TestsSettingsEvent()

  data class OpenReminderAction(val reminderId: String) : TestsSettingsEvent()

  data class OpenBirthdayAction(val birthdayId: String) : TestsSettingsEvent()
}
