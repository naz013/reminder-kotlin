package com.elementary.tasks.home.scheduleview.viewholder

import android.widget.TextView
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList

class ScheduleGoogleViewHolderCommon {

  fun loadTitle(
    googleTask: UiGoogleTaskList,
    textView: TextView
  ) {
    textView.text = googleTask.text
  }

  fun loadNotes(
    googleTask: UiGoogleTaskList,
    textView: TextView
  ) {
    textView.text = googleTask.notes
  }
}
