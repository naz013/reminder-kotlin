package com.elementary.tasks.navigation

import com.elementary.tasks.navigation.fragments.BaseFragment

interface FragmentCallback {

    fun onTitleChange(title: String)

    fun onScrollUpdate(y: Int)

    fun hideKeyboard()

    fun provideInsets(): ScreenInsets

    fun setCurrentFragment(fragment: BaseFragment<*>)
}
