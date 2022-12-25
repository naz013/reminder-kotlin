package com.elementary.tasks.core.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.utils.ui.Dialogues
import org.koin.android.ext.android.inject

abstract class BaseDialog : FragmentActivity() {

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val dialogues by inject<Dialogues>()
  protected val prefs = currentStateHolder.preferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
  }
}
