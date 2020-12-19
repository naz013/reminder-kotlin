package com.elementary.tasks.core.arch

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BindingActivity<B : ViewBinding> : ThemedActivity() {

  protected val binding: B by lazy { inflateBinding() }

  abstract fun inflateBinding(): B

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
  }
}
