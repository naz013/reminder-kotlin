package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Base fragment for all navigation-enabled fragments in the app.
 *
 * This class provides core navigation functionality with safe navigation methods
 * that handle errors gracefully. Fragments extending this class should use the
 * navigation graph animations defined in XML or programmatically set NavOptions.
 *
 * Navigation animations are configured based on fragment hierarchy:
 *   use fade animations for lateral navigation
 * - In-depth navigation uses slide-from-right animations for hierarchical navigation
 * - Modal screens use slide-from-bottom animations
 *
 * @param B The ViewBinding type for this fragment
 */
abstract class BaseNavigationFragment<B : ViewBinding> : NavigationFragment() {
  protected lateinit var binding: B

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    binding = inflate(inflater, container, savedInstanceState)
    return binding.root
  }

  abstract fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): B
}
