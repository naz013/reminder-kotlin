package com.elementary.tasks.core.arch

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

abstract class BaseHolder<B : ViewDataBinding>(parent: ViewGroup, @LayoutRes res: Int)
    : HolderBinding<B>(parent, res), KoinComponent {

    protected val prefs: Prefs by inject()

}