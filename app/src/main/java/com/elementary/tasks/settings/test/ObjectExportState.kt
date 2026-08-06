package com.elementary.tasks.settings.test

data class ObjectExportState(
  val objectType: ObjectExportType = ObjectExportType.ReminderV2,
  val items: List<ObjectExportItem> = emptyList(),
)

data class ObjectExportItem(
  val id: String,
  val title: String,
)

enum class ObjectExportType {
  ReminderV2,
  ReminderV1,
  Birthday,
  NoteV2,
  NoteV1,
  Place,
  GroupV2,
  GroupV1,
}

sealed class ObjectExportEvent {
  data class RequestSaveLocation(
    val fileName: String,
    val itemId: String,
  ) : ObjectExportEvent()

  data object ObjectSaved : ObjectExportEvent()
}
