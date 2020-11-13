package com.elementary.tasks.core.binding.dialogs

import android.view.View
import android.widget.CheckBox
import androidx.appcompat.widget.SwitchCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class DialogSelectExtraBinding(view: View) : Binding(view) {
  val extraSwitch: SwitchCompat by bindView(R.id.extraSwitch)
  val vibrationCheck: CheckBox by bindView(R.id.vibrationCheck)
  val voiceCheck: CheckBox by bindView(R.id.voiceCheck)
  val unlockCheck: CheckBox by bindView(R.id.unlockCheck)
  val repeatCheck: CheckBox by bindView(R.id.repeatCheck)
  val autoCheck: CheckBox by bindView(R.id.autoCheck)
}