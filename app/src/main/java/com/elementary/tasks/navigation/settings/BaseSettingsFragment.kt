package com.elementary.tasks.navigation.settings

import androidx.databinding.ViewDataBinding
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import org.koin.android.ext.android.inject

abstract class BaseSettingsFragment<B : ViewDataBinding> : BaseNavigationFragment<B>() {

  protected val language: Language by inject()

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
