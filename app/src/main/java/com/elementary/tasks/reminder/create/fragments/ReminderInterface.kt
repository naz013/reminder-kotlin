package com.elementary.tasks.reminder.create.fragments

import android.view.View
import com.github.naz013.domain.ReminderGroup
import com.elementary.tasks.reminder.create.ReminderStateViewModel

@Deprecated("Replaced by new Builder")
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
  fun setFullScreenMode(fullScreenEnabled: Boolean)
  fun updateScroll(y: Int)
  fun setFragment(typeFragment: TypeFragment<*>?)
}
