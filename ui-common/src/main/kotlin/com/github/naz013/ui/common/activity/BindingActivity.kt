package com.github.naz013.ui.common.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding

abstract class BindingActivity<B : ViewBinding> : LightThemedActivity() {

  protected val binding: B by lazy { inflateBinding() }

  abstract fun inflateBinding(): B

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
  }

  protected fun drawBehindSystemBars(rootView: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
      val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(
        top = view.paddingTop + insets.top,
        bottom = view.paddingBottom + insets.bottom
      )
      WindowInsetsCompat.CONSUMED
    }
  }
}
