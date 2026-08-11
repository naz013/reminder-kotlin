package com.elementary.tasks.reminder.todo

import androidx.compose.runtime.Composable
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.koin.compose.koinInject

/**
 * Hands a fully-built [ReminderV2] from [TodoEditViewModel]'s Extend action across the Nav3
 * boundary into [com.elementary.tasks.reminder.build.BuildReminderViewModel], which can't receive
 * it directly since [com.elementary.tasks.reminder.build.BuildReminderNavKey] is `@Serializable`
 * and can only carry primitives. Same pattern as
 * [com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder].
 */
class TodoSeedHolder {
  var pendingSeed: ReminderV2? = null
}

@Composable
fun rememberTodoSeedHolder(): TodoSeedHolder = koinInject()
