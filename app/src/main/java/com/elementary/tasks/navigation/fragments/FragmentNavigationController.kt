package com.elementary.tasks.navigation.fragments

import androidx.navigation.NavDirections

interface FragmentNavigationController {

  fun safeNavigation(function: () -> NavDirections)
}
