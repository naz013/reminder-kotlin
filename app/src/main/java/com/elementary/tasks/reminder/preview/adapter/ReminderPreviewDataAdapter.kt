package com.elementary.tasks.reminder.preview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.googletasks.list.GoogleTaskHolder
import com.elementary.tasks.notes.list.NoteViewHolder
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewAttachment
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewDataViewType
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewElement
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewGoogleCalendar
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewGoogleTask
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewMap
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewNote
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewStatus
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewSubTask

class ReminderPreviewDataAdapter(
  private val fragmentManager: FragmentManager,
  private val prefs: Prefs,
  private val onToggleClicked: (String) -> Unit,
  private val onMapClick: (View) -> Unit,
  private val subTaskRemoveClick: (String) -> Unit,
  private val subTaskCheckClick: (String) -> Unit,
  private val noteClick: (String) -> Unit,
  private val noteImageClick: (Int, UiNoteList) -> Unit,
  private val googleTaskClick: (String) -> Unit,
  private val googleCalendarClick: (Long) -> Unit,
  private val googleRemoveClick: (UiCalendarEventList) -> Unit
) :
  ListAdapter<UiReminderPreviewData, RecyclerView.ViewHolder>(UiReminderPreviewDataDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (UiReminderPreviewDataViewType.entries[viewType]) {
      UiReminderPreviewDataViewType.STATUS -> {
        ReminderStatusViewHolder(
          parent = parent,
          onToggleClicked = { onToggleClicked((getItem(it) as UiReminderPreviewStatus).id) }
        )
      }

      UiReminderPreviewDataViewType.TEXT_ELEMENT -> {
        ReminderElementViewHolder(parent)
      }

      UiReminderPreviewDataViewType.MAP -> {
        ReminderMapViewHolder(
          parent = parent,
          fragmentManager = fragmentManager,
          prefs = prefs,
          onMapClick = onMapClick
        )
      }

      UiReminderPreviewDataViewType.HEADER -> {
        ReminderHeaderViewHolder(parent)
      }

      UiReminderPreviewDataViewType.SUBTASK_ITEM -> {
        ReminderSubTaskViewHolder(
          parent = parent,
          removeClick = { subTaskRemoveClick((getItem(it) as UiReminderPreviewSubTask).id) },
          checkClick = { subTaskCheckClick((getItem(it) as UiReminderPreviewSubTask).id) }
        )
      }

      UiReminderPreviewDataViewType.ADS -> {
        ReminderAdsViewHolder(parent)
      }

      UiReminderPreviewDataViewType.NOTE -> {
        NoteViewHolder(
          parent = parent,
          listener = { _, position, _ ->
            getItem(position).let { it as? UiReminderPreviewNote }?.also {
              noteClick(it.note.id)
            }
          },
          imageClickListener = { _, position, imageId ->
            getItem(position).let { it as? UiReminderPreviewNote }?.also {
              noteImageClick(imageId, it.note)
            }
          },
          allowMore = false
        )
      }

      UiReminderPreviewDataViewType.GOOGLE_TASK -> {
        GoogleTaskHolder(
          parent = parent,
          listener = { _, position, _ ->
            getItem(position).let { it as? UiReminderPreviewGoogleTask }?.also {
              googleTaskClick(it.googleTask.id)
            }
          }
        )
      }

      UiReminderPreviewDataViewType.CALENDAR -> {
        GoogleEventViewHolder(
          parent = parent,
          listener = { _, position, action ->
            getItem(position).let { it as? UiReminderPreviewGoogleCalendar }?.also {
              if (action == ListActions.REMOVE) {
                googleRemoveClick(it.data)
              } else {
                googleCalendarClick(it.data.id)
              }
            }
          }
        )
      }

      UiReminderPreviewDataViewType.ATTACHMENT -> {
        ReminderAttachmentViewHolder(parent = parent)
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is ReminderHeaderViewHolder -> holder.bind(getItem(position) as UiReminderPreviewHeader)
      is ReminderElementViewHolder -> holder.bind(getItem(position) as UiReminderPreviewElement)
      is ReminderMapViewHolder -> holder.bind(getItem(position) as UiReminderPreviewMap)
      is ReminderSubTaskViewHolder -> holder.bind(getItem(position) as UiReminderPreviewSubTask)
      is ReminderStatusViewHolder -> holder.bind(getItem(position) as UiReminderPreviewStatus)
      is ReminderAttachmentViewHolder ->
        holder.bind(getItem(position) as UiReminderPreviewAttachment)
      is NoteViewHolder -> holder.setData((getItem(position) as UiReminderPreviewNote).note)
      is GoogleTaskHolder ->
        holder.bind((getItem(position) as UiReminderPreviewGoogleTask).googleTask)
      is GoogleEventViewHolder ->
        holder.bind((getItem(position) as UiReminderPreviewGoogleCalendar).data)
      is ReminderAdsViewHolder -> { }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return getItem(position).viewType.ordinal
  }
}
