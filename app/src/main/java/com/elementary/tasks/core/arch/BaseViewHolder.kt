package com.elementary.tasks.core.arch

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.binding.HolderBinding

abstract class BaseViewHolder<B : ViewBinding>(
  binding: B,
  currentStateHolder: CurrentStateHolder
) : HolderBinding<B>(binding) {
  protected val theme = currentStateHolder.theme
  protected val prefs = currentStateHolder.preferences
}
