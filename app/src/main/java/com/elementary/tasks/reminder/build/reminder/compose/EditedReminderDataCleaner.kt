package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.logging.Logger

class EditedReminderDataCleaner {

  operator fun invoke(
    reminder: Reminder,
    processedBuilderItems: ProcessedBuilderItems
  ) {
    if (reminder.noteId.isNotEmpty() && !processedBuilderItems.typeMap.containsKey(BiType.NOTE)) {
      Logger.d(TAG, "Note id is not empty, cleaning data")
      reminder.noteId = ""
    }

    if (
      reminder.exportToTasks &&
      !processedBuilderItems.typeMap.containsKey(BiType.GOOGLE_TASK_LIST)
    ) {
      Logger.d(TAG, "Export to tasks was enabled, disabling it")
      reminder.exportToTasks = false
    }

    if (
      reminder.exportToCalendar &&
      !processedBuilderItems.typeMap.containsKey(BiType.GOOGLE_CALENDAR)
    ) {
      Logger.d(TAG, "Export to calendar was enabled, disabling it")
      reminder.exportToCalendar = false
      reminder.calendarId = 0L
    }

    if (
      reminder.attachmentFile.isNotEmpty() &&
      !processedBuilderItems.typeMap.containsKey(BiType.ATTACHMENTS)
    ) {
      Logger.d(TAG, "Attachment file is not empty, cleaning data")
      reminder.attachmentFile = ""
    }

    if (
      reminder.attachmentFiles.isNotEmpty() &&
      !processedBuilderItems.typeMap.containsKey(BiType.ATTACHMENTS)
    ) {
      Logger.d(TAG, "Attachment files are not empty, cleaning data")
      reminder.attachmentFiles = emptyList()
    }

    if (
      reminder.target.isNotEmpty() &&
      (
        !processedBuilderItems.typeMap.containsKey(BiType.PHONE_CALL) &&
          !processedBuilderItems.typeMap.containsKey(BiType.SMS) &&
          !processedBuilderItems.typeMap.containsKey(BiType.EMAIL) &&
          !processedBuilderItems.typeMap.containsKey(BiType.APPLICATION) &&
          !processedBuilderItems.typeMap.containsKey(BiType.LINK)
        )
    ) {
      Logger.d(TAG, "Target is not empty, cleaning data")
      reminder.target = ""
    }

    if (
      reminder.subject.isNotEmpty() &&
      !processedBuilderItems.typeMap.containsKey(BiType.EMAIL)
    ) {
      Logger.d(TAG, "Subject is not empty, cleaning data")
      reminder.subject = ""
    }

    if (
      reminder.hasReminder &&
      (
        !processedBuilderItems.typeMap.containsKey(BiType.ARRIVING_COORDINATES) &&
          !processedBuilderItems.typeMap.containsKey(BiType.LEAVING_COORDINATES)
        )
    ) {
      Logger.d(TAG, "Reminder delay was enabled, disabling it")
      reminder.hasReminder = false
    }

    if (
      reminder.shoppings.isNotEmpty() &&
      !processedBuilderItems.typeMap.containsKey(BiType.SUB_TASKS)
    ) {
      Logger.d(TAG, "Shopping list is not empty, cleaning data")
      reminder.shoppings = emptyList()
    }
  }

  companion object {
    private const val TAG = "EditedReminderDataCleaner"
  }
}
