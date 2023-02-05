package com.elementary.tasks.navigation

import com.elementary.tasks.navigation.fragments.BaseFragment

interface FragmentCallback {
  fun onTitleChange(title: String)
  fun hideKeyboard()
  fun setCurrentFragment(fragment: BaseFragment<*>)
}
