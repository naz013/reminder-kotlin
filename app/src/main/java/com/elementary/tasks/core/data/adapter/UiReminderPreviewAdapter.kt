package com.elementary.tasks.core.data.adapter

import android.media.RingtoneManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderPreview
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs
import java.io.File

class UiReminderPreviewAdapter(
  private val prefs: Prefs,
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val textProvider: TextProvider
) : UiAdapter<Reminder, UiReminderPreview> {

  override fun create(data: Reminder): UiReminderPreview {
    val type = UiReminderType(data.type)
    val title = if (type.isByDate() && data.allDay) {
      textProvider.getText(R.string.reminder_title_all_day)
    } else {
      uiReminderCommonAdapter.getTypeString(type)
    }
    return UiReminderPreview(
      id = data.uuId,
      group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
      noteId = data.noteId,
      type = type,
      actionTarget = uiReminderCommonAdapter.getTarget(data, type),
      summary = data.summary,
      isRunning = data.isActive && !data.isRemoved,
      attachmentFile = data.attachmentFile.takeIf { it.isNotEmpty() },
      windowType = uiReminderCommonAdapter.getWindowType(data.windowType),
      status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
      title = title,
      melodyName = getMelodyName(data.melodyPath),
      due = uiReminderCommonAdapter.getDue(data, type),
      shopList = data.shoppings,
      places = data.places.map { uiReminderPlaceAdapter.create(it) },
      allDay = data.allDay
    )
  }

  private fun getMelodyName(melodyPath: String): String? {
    var file: File? = null
    if (melodyPath.isNotEmpty()) {
      file = File(melodyPath)
    } else {
      val path = prefs.melodyFile
      if (path != "" && !Sound.isDefaultMelody(path)) {
        file = File(path)
      } else {
        val soundPath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)?.path
        if (soundPath != null) {
          file = File(soundPath)
        }
      }
    }
    return file?.name
  }
}
