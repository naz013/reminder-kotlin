package com.elementary.tasks.reminder.lists.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ListItemParams
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.StringResPatterns
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.dp2px
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.views.TextDrawable
import com.elementary.tasks.databinding.ListItemReminderBinding
import java.util.*

class ReminderViewHolder(
  parent: ViewGroup,
  currentStateHolder: CurrentStateHolder,
  hasHeader: Boolean,
  editable: Boolean,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseViewHolder<ListItemReminderBinding>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {

  val listHeader: TextView = binding.listHeader

  init {
    binding.itemCheck.visibleGone(editable)
    binding.listHeader.visibleGone(hasHeader)
    binding.buttonMore.visibleGone(showMore)
    binding.todoList.hide()
    binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
    binding.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }
    binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
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
      binding.badgesView.show()
      binding.repeatBadge.show()

      val context = binding.itemCard.context
      val repeatText = when {
        Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> String.format(context.getString(R.string.xM), reminder.repeatInterval.toString())
        Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> ReminderUtils.getRepeatString(context, prefs, reminder.weekdays)
        Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> context.getString(R.string.yearly)
        else -> IntervalUtil.getInterval(reminder.repeatInterval) { StringResPatterns.getIntervalPattern(context, it) }
      }

      binding.repeatBadge.setImageDrawable(
        createBadge(context, repeatText, context.dp2px(ListItemParams.BADGE_WIDTH_DP))
      )
      (reminder.isActive && !reminder.isRemoved).also {
        binding.timeToBadge.visibleGone(it)
      }.takeIf { it }?.also {
        val remainingText = TimeCount.getRemaining(itemView.context, reminder.eventTime,
          reminder.delay, prefs.appLanguage)
        binding.timeToBadge.setImageDrawable(
          createBadge(context, remainingText, context.dp2px(ListItemParams.BADGE_WIDTH_INCREASED_DP))
        )
      }
    } else {
      binding.badgesView.hide()
    }
  }

  private fun createBadge(
    context: Context,
    text: String,
    width: Int = context.dp2px(ListItemParams.BADGE_WIDTH_DP),
    backgroundColor: Int = ThemeProvider.getSecondaryColor(context),
    textColor: Int = ThemeProvider.getOnSecondaryColor(context)
  ): TextDrawable {
    return TextDrawable.builder()
      .beginConfig()
      .textColor(textColor)
      .height(context.dp2px(ListItemParams.BADGE_HEIGHT_DP))
      .width(width)
      .toUpperCase()
      .bold()
      .endConfig()
      .buildRoundRect(text, backgroundColor, context.dp2px(ListItemParams.BADGE_CORNERS_DP)
      )
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
