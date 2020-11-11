package com.elementary.tasks.core.arch

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.Prefs
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
abstract class BaseHolder<B : ViewDataBinding>(parent: ViewGroup, @LayoutRes res: Int)
  : HolderBinding<B>(parent, res), KoinComponent {

  protected val prefs: Prefs by inject()
}