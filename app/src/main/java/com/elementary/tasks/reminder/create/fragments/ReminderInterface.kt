package com.elementary.tasks.reminder.create.fragments

import android.view.View
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.reminder.create.ReminderStateViewModel

interface ReminderInterface {
  val defGroup: ReminderGroup?
  var canExportToTasks: Boolean
  var canExportToCalendar: Boolean
  val state: ReminderStateViewModel
  fun isTablet(): Boolean
  fun selectMelody()
  fun attachFile()
  fun selectGroup()
  fun showSnackbar(title: String)
  fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener)
  fun setFullScreenMode(b: Boolean)
  fun updateScroll(y: Int)
  fun setFragment(typeFragment: TypeFragment<*>?)
}
