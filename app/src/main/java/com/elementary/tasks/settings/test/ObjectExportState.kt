package com.elementary.tasks.settings.test

data class ObjectExportState(
  val objectType: ObjectExportType = ObjectExportType.Reminder,
  val items: List<ObjectExportItem> = emptyList(),
)

data class ObjectExportItem(
  val id: String,
  val title: String,
)

enum class ObjectExportType {
  Reminder,
  Birthday,
  Note,
  Place,
  Group,
}

sealed class ObjectExportEvent {
  data class RequestSaveLocation(val fileName: String, val itemId: String) : ObjectExportEvent()

  data object ObjectSaved : ObjectExportEvent()
}
