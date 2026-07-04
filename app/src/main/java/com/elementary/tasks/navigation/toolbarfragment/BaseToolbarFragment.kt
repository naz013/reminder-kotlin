package com.elementary.tasks.navigation.toolbarfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseToolbarFragment<B : ViewBinding> : ToolbarFragment() {
  protected lateinit var binding: B

  abstract fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): B

  final override fun onCreateContentView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle?,
  ): View {
    binding = inflate(inflater, container, savedInstanceState)
    return binding.root
  }
}
