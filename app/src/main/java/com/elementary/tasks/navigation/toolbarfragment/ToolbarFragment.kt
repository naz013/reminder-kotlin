package com.elementary.tasks.navigation.toolbarfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentBaseToolbarBinding
import com.elementary.tasks.navigation.fragments.NavigationFragment
import com.elementary.tasks.navigation.topfragment.FragmentMenuController
import com.github.naz013.ui.common.view.applyTopInsets

/**
 * Content-agnostic AppBar/Toolbar scaffold. Subclasses only need to provide the content view
 * (via [onCreateContentView]) that gets placed below the toolbar - it can be inflated from a
 * [androidx.viewbinding.ViewBinding] or built with Jetpack Compose.
 */
abstract class ToolbarFragment :
  NavigationFragment(),
  FragmentMenuController {
  private lateinit var containerBinding: FragmentBaseToolbarBinding
  private var menuModifier: ((Menu) -> Unit)? = null

  abstract fun getTitle(): String

  @DrawableRes
  open fun getNavigationIcon(): Int = R.drawable.ic_builder_arrow_left

  protected abstract fun onCreateContentView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle?,
  ): View

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    containerBinding = FragmentBaseToolbarBinding.inflate(inflater, container, false)
    val contentView =
      onCreateContentView(inflater, containerBinding.fragmentContentView, savedInstanceState)
    containerBinding.fragmentContentView.addView(contentView)
    return containerBinding.root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    containerBinding.appBar.applyTopInsets()
    containerBinding.toolbar.title = getTitle()
    containerBinding.toolbar.setNavigationIcon(getNavigationIcon())
    containerBinding.toolbar.setNavigationOnClickListener { moveBack() }
  }

  protected fun setTitle(title: String) {
    containerBinding.toolbar.title = title
  }

  @Deprecated("Use updateMenuItem instead")
  protected fun invalidateOptionsMenu() {
    menuModifier?.invoke(containerBinding.toolbar.menu)
  }

  override fun addMenu(
    menuRes: Int?,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)?,
  ) {
    this.menuModifier = menuModifier
    containerBinding.toolbar.menu.clear()
    if (menuRes != null) {
      containerBinding.toolbar.inflateMenu(menuRes)
    }
    menuModifier?.invoke(containerBinding.toolbar.menu)
    containerBinding.toolbar.setOnMenuItemClickListener {
      return@setOnMenuItemClickListener onMenuItemListener(it)
    }
  }

  override fun removeMenu() {
    containerBinding.toolbar.menu.clear()
    menuModifier = null
  }

  override fun updateMenuItem(
    itemId: Int,
    modifier: MenuItem.() -> Unit,
  ) {
    val menuItem = containerBinding.toolbar.menu.findItem(itemId) ?: return
    modifier(menuItem)
  }
}
