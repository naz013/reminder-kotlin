package com.elementary.tasks.core.arch

import androidx.viewbinding.ViewBinding

abstract class BindingActivity<B : ViewBinding> : ThemedActivity() {

  protected val binding: B by lazy { inflateBinding() }

  abstract fun inflateBinding(): B
}
