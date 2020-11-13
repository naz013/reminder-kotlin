package com.elementary.tasks.reminder.lists.adapter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.StringResPatterns
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.databinding.ListItemReminderBinding
import java.util.*

class ReminderHolder(
  parent: ViewGroup,
  hasHeader: Boolean,
  editable: Boolean,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseHolder<ListItemReminderBinding>(parent, R.layout.list_item_reminder) {

  val listHeader: TextView = binding.listHeader

  init {
    if (editable) {
      binding.itemCheck.show()
    } else {
      binding.itemCheck.hide()
    }
    if (!hasHeader) {
      binding.listHeader.hide()
    }
    binding.todoList.hide()
    binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
    binding.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }

    if (showMore) {
      binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
      binding.buttonMore.show()
    } else {
      binding.buttonMore.hide()
    }
  }

  fun setData(reminder: Reminder) {
    binding.taskText.text = reminder.summary
    loadDate(reminder)
    loadCheck(reminder)
    loadContact(reminder)
    loadRepeatLeft(reminder)
    loadGroup(reminder)
  }

  private fun loadGroup(reminder: Reminder) {
    val priority = ReminderUtils.getPriorityTitle(itemView.context, reminder.priority)
    val typeLabel = ReminderUtils.getTypeString(itemView.context, reminder.type)
    binding.reminderTypeGroup.text = "$typeLabel (${reminder.groupTitle}, $priority)"
  }

  private fun loadRepeatLeft(reminder: Reminder) {
    val visible = !(Reminder.isBase(reminder.type, Reminder.BY_LOCATION)
      || Reminder.isBase(reminder.type, Reminder.BY_OUT)
      || Reminder.isBase(reminder.type, Reminder.BY_PLACES))
    if (visible) {
      binding.reminderRepeatLeft.show()
      val context = binding.reminderRepeatLeft.context
      val spannableStringBuilder = SpannableStringBuilder()
      val repeatText = when {
        Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> String.format(context.getString(R.string.xM), reminder.repeatInterval.toString())
        Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> ReminderUtils.getRepeatString(context, prefs, reminder.weekdays)
        Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> context.getString(R.string.yearly)
        else -> IntervalUtil.getInterval(reminder.repeatInterval) { StringResPatterns.getIntervalPattern(context, it) }
      }
      var text = "!!!$repeatText"
      if (reminder.isActive && !reminder.isRemoved) {
        val remainingText = TimeCount.getRemaining(itemView.context, reminder.eventTime,
          reminder.delay, prefs.appLanguage)
        text += "\n"
        text += "!!!$remainingText"
        spannableStringBuilder.append(text)
        val stIndex = text.lastIndexOf("!!!")
        if (stIndex != -1) {
          spannableStringBuilder.setSpan(ImageSpan(context, R.drawable.ic_twotone_done_24px),
            stIndex, stIndex + 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
      } else {
        spannableStringBuilder.append(text)
      }
      spannableStringBuilder.setSpan(ImageSpan(context, R.drawable.ic_twotone_repeat_24px),
        0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
      binding.reminderRepeatLeft.text = spannableStringBuilder
    } else {
      binding.reminderRepeatLeft.hide()
    }


  }

  private fun loadDate(model: Reminder) {
    val is24 = prefs.is24HourFormat
    if (Reminder.isGpsType(model.type)) {
      val place = model.places[0]
      binding.taskDate.text = String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.latitude, place.longitude, model.places.size)
      return
    }
    binding.taskDate.text = TimeUtil.getRealDateTime(model.eventTime, model.delay, is24, prefs.appLanguage)
  }

  private fun loadCheck(item: Reminder?) {
    if (item == null || item.isRemoved) {
      binding.itemCheck.hide()
      return
    }
    binding.itemCheck.isChecked = item.isActive
  }

  private fun loadContact(model: Reminder) {
    val type = model.type
    val number = model.target
    if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
      binding.reminderPhone.show()
      binding.reminderPhone.text = number
    } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
      binding.reminderPhone.show()
      val name = if (Permissions.checkPermission(binding.reminderPhone.context, Permissions.READ_CONTACTS)) {
        Contacts.getNameFromNumber(number, binding.reminderPhone.context)
      } else {
        null
      }
      if (name == null) {
        binding.reminderPhone.text = number
      } else {
        binding.reminderPhone.text = "$name($number)"
      }
    } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
      val packageManager = binding.reminderPhone.context.packageManager
      var applicationInfo: ApplicationInfo? = null
      try {
        applicationInfo = packageManager.getApplicationInfo(number, 0)
      } catch (ignored: PackageManager.NameNotFoundException) {
      }

      val name = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
      binding.reminderPhone.show()
      binding.reminderPhone.text = "$name/$number"
    } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
      val name = if (Permissions.checkPermission(binding.reminderPhone.context, Permissions.READ_CONTACTS)) {
        Contacts.getNameFromMail(number, binding.reminderPhone.context)
      } else null
      binding.reminderPhone.show()
      if (name == null) {
        binding.reminderPhone.text = number
      } else {
        binding.reminderPhone.text = "$name($number)"
      }
    } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
      binding.reminderPhone.show()
      binding.reminderPhone.text = number
    } else {
      binding.reminderPhone.hide()
    }
  }
}
