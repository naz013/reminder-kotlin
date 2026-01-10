package com.github.naz013.ui.common.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BindingActivity<B : ViewBinding> : LightThemedActivity() {

  protected val binding: B by lazy { inflateBinding() }

  abstract fun inflateBinding(): B

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
  }
}
