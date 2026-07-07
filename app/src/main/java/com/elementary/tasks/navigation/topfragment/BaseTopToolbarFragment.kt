package com.elementary.tasks.navigation.topfragment

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment

abstract class BaseTopToolbarFragment<B : ViewBinding> :
  BaseToolbarFragment<B>(),
  FragmentMenuController {
  override fun getNavigationIcon(): Int = R.drawable.ic_builder_arrow_left
}
