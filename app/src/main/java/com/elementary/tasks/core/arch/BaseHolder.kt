package com.elementary.tasks.core.arch

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.Prefs

abstract class BaseHolder<B : ViewDataBinding>(
  parent: ViewGroup,
  @LayoutRes res: Int,
  protected val prefs: Prefs
) : HolderBinding<B>(parent, res)