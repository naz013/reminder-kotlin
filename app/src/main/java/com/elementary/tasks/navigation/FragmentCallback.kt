package com.elementary.tasks.navigation

interface FragmentCallback {

    fun onTitleChange(title: String)

    fun onScrollUpdate(y: Int)

    fun hideKeyboard()
}
