package com.github.naz013.ui.common.menu

import android.view.Menu
import android.view.MenuItem

fun Menu.showOrHideItem(id: Int, visible: Boolean) {
  findItem(id)?.isVisible = visible
}

fun Menu.enableOrDisableItem(id: Int, enabled: Boolean) {
  findItem(id)?.isEnabled = enabled
}

fun Menu.modifyItem(id: Int, modifier: MenuItem.() -> Unit) {
  findItem(id)?.let { modifier.invoke(it) }
}
