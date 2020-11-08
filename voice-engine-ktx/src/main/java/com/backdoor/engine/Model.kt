package com.backdoor.engine

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType

data class Model(
  val target: String? = null,
  val summary: String = "",
  val dateTime: String? = null,
  val repeatInterval: Long = 0,
  val type: ActionType? = null,
  val weekdays: List<Int> = listOf(),
  val action: Action = Action.NONE,
  val hasCalendar: Boolean = false
)