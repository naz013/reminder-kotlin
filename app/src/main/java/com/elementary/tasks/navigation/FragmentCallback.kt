package com.elementary.tasks.navigation

import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

interface FragmentCallback {
  fun hideKeyboard()
  fun setCurrentFragment(fragment: BaseNavigationFragment<*>)
  fun onCreateFragment(fragment: BaseNavigationFragment<*>)
}
