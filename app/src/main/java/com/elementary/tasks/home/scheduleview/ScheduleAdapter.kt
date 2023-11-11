package com.elementary.tasks.home.scheduleview

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleBirthdayHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleGoogleViewHolderCommon
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleHeaderViewHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleNoteViewHolderCommon
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderAndGTaskViewHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderAndNoteViewHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolderCommon
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleShoppingAndGTaskViewHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleShoppingAndNoteViewHolder
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleShoppingViewHolder

class ScheduleAdapter(
  private val isDark: Boolean,
  private val onReminderClickListener: (position: Int, id: String) -> Unit,
  private val onBirthdayClickListener: (position: Int, id: String) -> Unit,
  private val onHeaderClickListener: (position: Int, time: HeaderTimeType) -> Unit,
  private val onNoteClickListener: (position: Int, id: String) -> Unit,
  private val onGoogleTaskClickListener: (position: Int, id: String) -> Unit
) : ListAdapter<ScheduleModel, RecyclerView.ViewHolder>(ScheduleModelDiffCallback()) {

  private val reminderCommon = ScheduleReminderViewHolderCommon()
  private val noteCommon = ScheduleNoteViewHolderCommon()
  private val googleCommon = ScheduleGoogleViewHolderCommon()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      ScheduleModelViewType.REMINDER.value -> ScheduleReminderViewHolder(
        parent = parent,
        common = reminderCommon
      ) { position -> onReminderClickListener(position, getItem(position).id) }

      ScheduleModelViewType.REMINDER_NOTE.value -> ScheduleReminderAndNoteViewHolder(
        parent = parent,
        common = reminderCommon,
        noteCommon = noteCommon,
        reminderClickListener = { position ->
          onReminderClickListener(position, getItem(position).id)
        },
        noteClickListener = { position ->
          getItemAs(position, ReminderAndNoteScheduleModel::class.java)?.also {
            onNoteClickListener(position, it.note.id)
          }
        }
      )

      ScheduleModelViewType.REMINDER_GTASK.value -> ScheduleReminderAndGTaskViewHolder(
        parent = parent,
        common = reminderCommon,
        googleCommon = googleCommon,
        reminderClickListener = { position ->
          onReminderClickListener(position, getItem(position).id)
        },
        taskClickListener = { position ->
          getItemAs(position, ReminderAndGoogleTaskScheduleModel::class.java)?.also {
            onGoogleTaskClickListener(position, it.googleTask.id)
          }
        }
      )

      ScheduleModelViewType.REMINDER_SHOPPING.value -> ScheduleShoppingViewHolder(
        parent = parent,
        isDark = isDark,
        common = reminderCommon
      ) { position -> onReminderClickListener(position, getItem(position).id) }

      ScheduleModelViewType.REMINDER_SHOPPING_NOTE.value -> ScheduleShoppingAndNoteViewHolder(
        parent = parent,
        isDark = isDark,
        common = reminderCommon,
        noteCommon = noteCommon,
        reminderClickListener = { position ->
          onReminderClickListener(position, getItem(position).id)
        },
        noteClickListener = { position ->
          getItemAs(position, ReminderAndNoteScheduleModel::class.java)?.also {
            onNoteClickListener(position, it.note.id)
          }
        }
      )

      ScheduleModelViewType.REMINDER_SHOPPING_GTASK.value -> ScheduleShoppingAndGTaskViewHolder(
        parent = parent,
        isDark = isDark,
        common = reminderCommon,
        googleCommon = googleCommon,
        reminderClickListener = { position ->
          onReminderClickListener(position, getItem(position).id)
        },
        taskClickListener = { position ->
          getItemAs(position, ReminderAndGoogleTaskScheduleModel::class.java)?.also {
            onGoogleTaskClickListener(position, it.googleTask.id)
          }
        }
      )

      ScheduleModelViewType.BIRTHDAY.value -> ScheduleBirthdayHolder(
        parent = parent
      ) { position -> onBirthdayClickListener(position, getItem(position).id) }

      else -> ScheduleHeaderViewHolder(
        parent = parent
      ) { position ->
        getItemAs(position, HeaderScheduleModel::class.java)?.also {
          onHeaderClickListener(position, it.headerTimeType)
        }
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is ScheduleBirthdayHolder -> {
        getItemAs(position, BirthdayScheduleModel::class.java)?.also {
          holder.setData(it.data)
        }
      }

      is ScheduleReminderViewHolder -> {
        getItemAs(position, ReminderScheduleModel::class.java)?.also {
          holder.setData(it.data as UiReminderListActive)
        }
      }

      is ScheduleReminderAndNoteViewHolder -> {
        getItemAs(position, ReminderAndNoteScheduleModel::class.java)?.also {
          holder.setData(it)
        }
      }

      is ScheduleReminderAndGTaskViewHolder -> {
        getItemAs(position, ReminderAndGoogleTaskScheduleModel::class.java)?.also {
          holder.setData(it)
        }
      }

      is ScheduleShoppingViewHolder -> {
        getItemAs(position, ReminderScheduleModel::class.java)?.also {
          holder.setData(it.data as UiReminderListActiveShop)
        }
      }

      is ScheduleShoppingAndNoteViewHolder -> {
        getItemAs(position, ReminderAndNoteScheduleModel::class.java)?.also { holder.setData(it) }
      }

      is ScheduleShoppingAndGTaskViewHolder -> {
        getItemAs(position, ReminderAndGoogleTaskScheduleModel::class.java)?.also {
          holder.setData(it)
        }
      }

      is ScheduleHeaderViewHolder -> {
        getItemAs(position, HeaderScheduleModel::class.java)?.also {
          holder.setData(it)
        }
      }
    }
  }

  override fun getItemViewType(position: Int) = getItem(position).viewType.value

  private fun <T : ScheduleModel> getItemAs(position: Int, clazz: Class<T>): T? {
    val item = getItem(position)
    return if (item.javaClass == clazz) {
      item as T
    } else {
      null
    }
  }
}
