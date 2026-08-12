package com.github.naz013.feature.birthday.actions

import com.github.naz013.domain.Birthday
import com.github.naz013.logging.Logger

/**
 * Use case to determine available actions for a birthday.
 *
 * Returns a list of actions that can be performed based on the birthday's properties.
 */
class GetBirthdayActionsUseCase {
  /**
   * Gets the available actions for a birthday.
   *
   * @param birthday The birthday entity
   * @param supportedActions Set of actions that are supported by the caller
   * @return List of available birthday actions
   */
  operator fun invoke(
    birthday: Birthday,
    supportedActions: Set<BirthdayAction>,
  ): List<BirthdayAction> {
    val actions = mutableListOf<BirthdayAction>()

    // Main actions - always available
    if (supportedActions.contains(BirthdayAction.Ok)) {
      actions.add(BirthdayAction.Ok)
    }

    // Communication actions - only if phone number exists
    if (birthday.number.isNotEmpty()) {
      if (supportedActions.contains(BirthdayAction.MakeCall)) {
        actions.add(BirthdayAction.MakeCall)
      }
      if (supportedActions.contains(BirthdayAction.SendSms)) {
        actions.add(BirthdayAction.SendSms)
      }
    }

    // Secondary actions
    if (supportedActions.contains(BirthdayAction.Snooze)) {
      actions.add(BirthdayAction.Snooze)
    }
    if (supportedActions.contains(BirthdayAction.SnoozeCustom)) {
      actions.add(BirthdayAction.SnoozeCustom)
    }
    if (supportedActions.contains(BirthdayAction.Edit)) {
      actions.add(BirthdayAction.Edit)
    }
    if (supportedActions.contains(BirthdayAction.Delete)) {
      actions.add(BirthdayAction.Delete)
    }

    Logger.i(TAG, "Available actions for birthday ${birthday.uuId}: $actions")
    return actions
  }

  companion object {
    private const val TAG = "GetBirthdayActionsUseCase"
  }
}
