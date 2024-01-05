package com.elementary.tasks.reminder.build.valuedialog.controller.core

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.reminder.build.BuilderItem

abstract class AbstractBindingValueController<T, V : ViewBinding>(
  builderItem: BuilderItem<T>
) : AbstractViewValueController<T>(builderItem) {

  protected lateinit var binding: V

  override fun createView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
    binding = bindView(LayoutInflater.from(parent.context), parent)
    return binding.root
  }

  abstract fun bindView(layoutInflater: LayoutInflater, parent: ViewGroup): V
}
