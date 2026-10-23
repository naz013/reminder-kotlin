package com.github.naz013.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class BackStackExtensionsTest {

  private data class Key(val name: String) : NavKey

  private fun stack(vararg names: String): MutableList<NavKey> =
    names.map { Key(it) }.toMutableList<NavKey>()

  @Test
  fun `removes trailing entries of the deleted item`() {
    val stack = stack("list", "preview", "edit")

    stack.popDeleted { it is Key && (it.name == "preview" || it.name == "edit") }

    assertEquals(listOf<NavKey>(Key("list")), stack)
  }

  @Test
  fun `keeps entries when the top is not the deleted item`() {
    val stack = stack("list", "preview")

    stack.popDeleted { false }

    assertEquals(2, stack.size)
  }

  @Test
  fun `never empties the back stack`() {
    val stack = stack("preview", "edit")

    stack.popDeleted { true }

    assertEquals(listOf<NavKey>(Key("preview")), stack)
  }

  @Test
  fun `stops at the first entry that is not the deleted item`() {
    val stack = stack("preview", "other", "edit")

    stack.popDeleted { it is Key && it.name != "other" }

    assertEquals(listOf<NavKey>(Key("preview"), Key("other")), stack)
  }
}
