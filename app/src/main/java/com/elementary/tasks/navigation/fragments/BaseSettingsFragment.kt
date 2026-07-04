package com.elementary.tasks.navigation.fragments

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment

abstract class BaseSettingsFragment<B : ViewBinding> : BaseToolbarFragment<B>() {

  protected fun priorityList(): Array<String> = priorityListItems()
}

fun Fragment.priorityListItems(): Array<String> =
  arrayOf(
    getString(R.string.priority_lowest),
    getString(R.string.priority_low),
    getString(R.string.priority_normal),
    getString(R.string.priority_high),
    getString(R.string.priority_highest),
  )
