package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding

abstract class BaseAnimatedFragment<B : ViewBinding> : BaseNavigationFragment<B>() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewCompat.setTranslationZ(view, 100f)
  }
}
