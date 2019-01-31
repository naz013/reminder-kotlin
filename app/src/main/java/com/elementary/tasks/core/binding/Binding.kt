package com.elementary.tasks.core.binding

import android.view.View
import androidx.annotation.IdRes
import com.elementary.tasks.core.utils.lazyUnSynchronized

abstract class Binding(val view: View) {

    fun <ViewT : View> bindView(@IdRes idRes: Int): Lazy<ViewT> {
        return lazyUnSynchronized {
            view.findViewById<ViewT>(idRes)
        }
    }
}