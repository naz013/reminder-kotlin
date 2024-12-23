package com.github.naz013.ui.common.view

import android.view.View
import androidx.annotation.IdRes

abstract class Binding(val view: View) {

  fun <ViewT : View> bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnSynchronized {
      view.findViewById(idRes)
    }
  }

  private fun <T> lazyUnSynchronized(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)
}
