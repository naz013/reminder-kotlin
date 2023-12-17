package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
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

  fun tintIcon(
    googleTask: UiGoogleTaskList,
    imageView: AppCompatImageView
  ) {
    val color = googleTask.taskListColor ?: Color.BLACK
    imageView.imageTintList = ColorStateList.valueOf(color)
  }
}
