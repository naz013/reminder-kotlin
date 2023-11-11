package com.elementary.tasks.navigation.topfragment

import android.view.Menu
import android.view.MenuItem

interface FragmentMenuController {
  fun addMenu(
    menuRes: Int?,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)? = null
  )
}
