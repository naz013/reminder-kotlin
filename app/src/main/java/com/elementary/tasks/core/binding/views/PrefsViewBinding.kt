package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.elementary.tasks.R
import com.github.naz013.ui.common.view.Binding
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.materialswitch.MaterialSwitch

class PrefsViewBinding(view: View) : Binding(view) {
  val dividerTop: View by bindView(R.id.dividerTop)
  val dividerBottom: View by bindView(R.id.dividerBottom)
  val iconView: AppCompatImageView by bindView(R.id.iconView)
  val itemsContainer: View by bindView(R.id.itemsContainer)
  val viewContainer: View by bindView(R.id.viewContainer)
  val prefsPrimaryText: TextView by bindView(R.id.prefsPrimaryText)
  val prefsSecondaryText: TextView by bindView(R.id.prefsSecondaryText)
  val prefsCheck: MaterialCheckBox by bindView(R.id.prefsCheck)
  val prefsSwitch: MaterialSwitch by bindView(R.id.prefsSwitch)
  val prefsValue: TextView by bindView(R.id.prefsValue)
  val prefsView: AppCompatImageView by bindView(R.id.prefsView)
  val progressView: ProgressBar by bindView(R.id.progressViewPrefs)
}
