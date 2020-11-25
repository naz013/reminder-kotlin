package com.elementary.tasks.core.arch

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.Prefs

abstract class BaseHolder<B : ViewBinding>(
  binding: B,
  protected val prefs: Prefs
) : HolderBinding<B>(binding)