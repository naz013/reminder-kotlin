package com.elementary.tasks.navigation.fragments

import androidx.navigation.NavController
import androidx.navigation.NavDirections

interface FragmentNavigationController {

  fun safeNavigation(function: () -> NavDirections)

  fun navigate(block: NavController.() -> Unit)
}
