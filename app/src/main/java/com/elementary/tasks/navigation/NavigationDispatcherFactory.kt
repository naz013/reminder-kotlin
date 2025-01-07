package com.elementary.tasks.navigation

import android.content.Context
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.intent.IntentDataWriter

class NavigationDispatcherFactory(
  private val context: Context,
  private val intentDataWriter: IntentDataWriter
) {

  fun create(destination: Destination): NavigationDispatcher<Destination> {
    return when (destination) {
      is ActivityDestination -> {
        ActivityNavigationDispatcher(context) as NavigationDispatcher<Destination>
      }

      is DataDestination -> {
        DataNavigationDispatcher(context, intentDataWriter) as NavigationDispatcher<Destination>
      }

      else -> {
        ActivityNavigationDispatcher(context) as NavigationDispatcher<Destination>
      }
    }
  }
}
