package com.elementary.tasks.navigation

import com.elementary.tasks.navigation.fragments.BaseFragment

interface FragmentCallback {

    fun openFragment(fragment: BaseFragment<*>, tag: String)

    fun openFragment(fragment: BaseFragment<*>, tag: String, replace: Boolean)

    fun onTitleChange(title: String)

    fun onFragmentSelect(fragment: BaseFragment<*>)

    fun refreshMenu()

    fun onMenuSelect(menu: Int)

    fun onScrollUpdate(y: Int)

    fun hideKeyboard()
}
