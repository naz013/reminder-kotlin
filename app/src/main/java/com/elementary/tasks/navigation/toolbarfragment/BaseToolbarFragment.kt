package com.elementary.tasks.navigation.toolbarfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.github.naz013.ui.common.view.applyTopInsets
import com.elementary.tasks.databinding.FragmentBaseToolbarBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

abstract class BaseToolbarFragment<B : ViewBinding> : BaseNavigationFragment<B>() {

  private lateinit var containerBinding: FragmentBaseToolbarBinding
  private var menuModifier: ((Menu) -> Unit)? = null

  abstract fun getTitle(): String

  @DrawableRes
  open fun getNavigationIcon(): Int {
    return R.drawable.ic_builder_arrow_left
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    containerBinding = FragmentBaseToolbarBinding.inflate(inflater, container, false)
    val subView = inflate(inflater, containerBinding.fragmentContentView, savedInstanceState)
    containerBinding.fragmentContentView.addView(subView.root)
    binding = subView
    return containerBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    containerBinding.appBar.applyTopInsets()
    containerBinding.toolbar.title = getTitle()
    containerBinding.toolbar.setNavigationIcon(getNavigationIcon())
    containerBinding.toolbar.setNavigationOnClickListener { moveBack() }
  }

  protected fun setTitle(title: String) {
    containerBinding.toolbar.title = title
  }

  protected fun invalidateOptionsMenu() {
    menuModifier?.invoke(containerBinding.toolbar.menu)
  }

  protected fun addMenu(
    menuRes: Int?,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)? = null
  ) {
    this.menuModifier = menuModifier
    if (menuRes != null) {
      containerBinding.toolbar.inflateMenu(menuRes)
    }
    menuModifier?.invoke(containerBinding.toolbar.menu)
    containerBinding.toolbar.setOnMenuItemClickListener {
      return@setOnMenuItemClickListener onMenuItemListener(it)
    }
  }
}
