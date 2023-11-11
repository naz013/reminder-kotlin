package com.elementary.tasks.navigation.fragments

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import org.koin.android.ext.android.inject

abstract class BaseSettingsFragment<B : ViewBinding> : BaseToolbarFragment<B>() {

  protected val language by inject<Language>()

  protected fun priorityList(): Array<String> {
    return arrayOf(
      getString(R.string.priority_lowest),
      getString(R.string.priority_low),
      getString(R.string.priority_normal),
      getString(R.string.priority_high),
      getString(R.string.priority_highest)
    )
  }
}
