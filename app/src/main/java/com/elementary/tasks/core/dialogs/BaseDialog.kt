package com.elementary.tasks.core.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import org.koin.android.ext.android.inject

abstract class BaseDialog : FragmentActivity() {

  protected val dialogues by inject<Dialogues>()
  protected val prefs by inject<Prefs>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
  }
}
