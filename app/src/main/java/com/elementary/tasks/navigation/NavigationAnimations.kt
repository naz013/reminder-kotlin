package com.elementary.tasks.navigation

import androidx.navigation.NavOptions
import com.elementary.tasks.R

/**
 * Navigation animation utilities for creating natural-feeling transitions.
 *
 * This object provides standardized navigation options for different types
 * of fragment transitions in the app.
 */
object NavigationAnimations {

  /**
   * Creates nav options for bottom navigation transitions.
   *
   * Uses cross-fade animation for smooth transitions between top-level
   * destinations (Home, Notes, Calendar, Tasks). This provides a natural
   * feel as these are lateral navigations, not hierarchical.
   *
   * @return NavOptions configured with fade animations
   */
  fun bottomNavOptions(): NavOptions {
    return NavOptions.Builder()
      .setEnterAnim(R.anim.fragment_fade_in)
      .setExitAnim(R.anim.fragment_fade_out)
      .setPopEnterAnim(R.anim.fragment_fade_in)
      .setPopExitAnim(R.anim.fragment_fade_out)
      .build()
  }

  /**
   * Creates nav options for in-depth navigation transitions.
   *
   * Uses slide-from-right animation with proper Material Design interpolators
   * for hierarchical navigation. This creates a natural forward/backward
   * navigation feel.
   *
   * @return NavOptions configured with slide animations
   */
  fun inDepthNavOptions(): NavOptions {
    return NavOptions.Builder()
      .setEnterAnim(R.anim.nav_default_enter_anim)
      .setExitAnim(R.anim.nav_default_exit_anim)
      .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
      .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
      .build()
  }

  /**
   * Creates nav options for modal-style navigation (bottom sheet style).
   *
   * Uses slide-from-bottom animation for dialogs and modal screens
   * that slide up from the bottom of the screen.
   *
   * @return NavOptions configured with bottom slide animations
   */
  fun modalNavOptions(): NavOptions {
    return NavOptions.Builder()
      .setEnterAnim(R.anim.fragment_slide_top)
      .setExitAnim(R.anim.fragment_wait)
      .setPopEnterAnim(R.anim.fragment_wait)
      .setPopExitAnim(R.anim.fragment_slide_bottom)
      .build()
  }
}

