package com.elementary.tasks.navigation.nav3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import org.koin.compose.koinInject

/**
 * Koin singleton letting any composable reach the app's single, shared outer Nav3 backstack (see
 * [AppNavGraph]) without needing to be handed it directly - e.g. a screen several NavEntries deep
 * that needs to push a destination belonging to a different feature's `XyzNavGraph.kt`.
 */
class AppNavBridge {
  private var outerBackStack: MutableList<NavKey>? = null

  fun attachOuterBackStack(backStack: MutableList<NavKey>) {
    outerBackStack = backStack
  }

  fun detachOuterBackStack(backStack: MutableList<NavKey>) {
    if (outerBackStack === backStack) outerBackStack = null
  }

  fun navigate(vararg keys: NavKey) {
    val stack = outerBackStack ?: return
    keys.forEach { stack.add(it) }
  }
}

@Composable
fun rememberAppNavBridge(): AppNavBridge = koinInject()
