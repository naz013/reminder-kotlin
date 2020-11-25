package com.elementary.tasks.core.arch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BindingFragment<B : ViewBinding> : Fragment() {

  protected lateinit var binding: B

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = inflate(inflater, container, savedInstanceState)
    return binding.root
  }

  protected fun string(@StringRes res: Int): String {
    return if (context != null && isAdded) {
      getString(res)
    } else {
      ""
    }
  }

  abstract fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): B
}