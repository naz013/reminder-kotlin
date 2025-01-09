package com.elementary.tasks.navigation

import com.github.naz013.navigation.Destination

interface NavigationDispatcher<T : Destination> {
  fun dispatch(destination: T)
}
