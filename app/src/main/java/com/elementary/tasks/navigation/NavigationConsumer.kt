package com.elementary.tasks.navigation

import com.github.naz013.navigation.Destination

interface NavigationConsumer {
  fun consume(destination: Destination)
}
