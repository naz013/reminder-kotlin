package com.elementary.tasks.navigation

import com.elementary.tasks.navigation.fragments.BaseFragment

interface FragmentCallback {

    @Deprecated("No used anymore")
    fun openFragment(fragment: BaseFragment<*>, tag: String)

    @Deprecated("No used anymore")
    fun openFragment(fragment: BaseFragment<*>, tag: String, replace: Boolean)

    fun onTitleChange(title: String)

    fun onScrollUpdate(y: Int)

    fun hideKeyboard()
}
