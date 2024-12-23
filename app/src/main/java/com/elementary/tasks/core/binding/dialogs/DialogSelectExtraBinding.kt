package com.elementary.tasks.core.binding.dialogs

import android.view.View
import android.widget.CheckBox
import androidx.appcompat.widget.SwitchCompat
import com.elementary.tasks.R
import com.github.naz013.ui.common.view.Binding

class DialogSelectExtraBinding(view: View) : Binding(view) {
  val extraSwitch: SwitchCompat by bindView(R.id.extraSwitch)
  val vibrationCheck: CheckBox by bindView(R.id.vibrationCheck)
  val voiceCheck: CheckBox by bindView(R.id.voiceCheck)
  val repeatCheck: CheckBox by bindView(R.id.repeatCheck)
}
