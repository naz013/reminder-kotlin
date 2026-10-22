package com.github.naz013.logic.reminder.chain

/**
 * Pure cycle check for "start after another reminder" links. A link is `reminderId -> its
 * predecessor id`; [linkOf] resolves the predecessor of any reminder, or null when it has none.
 */
class ReminderChainValidator {

  /** True when making [reminderId] wait for [predecessorId] would create a loop of any depth. */
  fun wouldCreateCycle(
    reminderId: String,
    predecessorId: String,
    linkOf: (String) -> String?,
  ): Boolean {
    val visited = mutableSetOf<String>()
    var current: String? = predecessorId
    while (current != null) {
      if (current == reminderId || !visited.add(current)) return true
      current = linkOf(current)
    }
    return false
  }
}
