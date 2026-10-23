package com.github.naz013.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Removes trailing entries that show the item that was just deleted, keeping at least one entry,
 * so the user lands on the list (or whatever sits below the preview/edit screens).
 */
fun MutableList<NavKey>.popDeleted(isSameItem: (NavKey) -> Boolean) {
  while (size > 1 && isSameItem(last())) {
    removeAt(lastIndex)
  }
}
