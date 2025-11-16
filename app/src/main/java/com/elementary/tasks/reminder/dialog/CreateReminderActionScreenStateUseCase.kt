package com.elementary.tasks.reminder.dialog

import com.elementary.tasks.reminder.actions.ActionCategory
import com.elementary.tasks.reminder.actions.GetReminderActionsUseCase
import com.elementary.tasks.reminder.actions.ReminderAction
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

class CreateReminderActionScreenStateUseCase(
  private val getReminderActionsUseCase: GetReminderActionsUseCase,
  private val textProvider: TextProvider,
  private val contactsReader: ContactsReader,
  private val packageManagerWrapper: PackageManagerWrapper,
) {

  suspend operator fun invoke(reminder: Reminder): ReminderActionScreenState {
    val availableActions = getReminderActionsUseCase(
      reminder,
      SUPPORTED_ACTIONS.toSet()
    )
    if (availableActions.isEmpty()) {
      throw IllegalStateException("No available actions for reminder ${reminder.uuId}")
    }
    val orderedAction = availableActions.sortedBy { action ->
      when (action.category) {
        ActionCategory.Action -> 0
        ActionCategory.Main -> 1
        ActionCategory.Secondary -> 2
      }
    }
    Logger.i(TAG, "Creating action screen state for reminder ${reminder.uuId} with actions: $orderedAction")
    return ReminderActionScreenState(
      id = reminder.uuId,
      header = getHeader(reminder),
      todoList = getTodoList(reminder),
      mainAction = orderedAction.first().let {
        ReminderActionScreenActionItem(
          action = it,
          text = textProvider.getString(it.titleRes),
          iconRes = it.iconRes
        )
      },
      secondaryActions = orderedAction.drop(1).map {
        ReminderActionScreenActionItem(
          action = it,
          text = textProvider.getString(it.titleRes),
          iconRes = it.iconRes
        )
      }
    )
  }

  private fun getTodoList(reminder: Reminder): ReminderActionScreenTodoList? {
    val todos = reminder.shoppings
    return if (todos.isEmpty()) {
      null
    } else {
      val items = todos.map {
        ReminderActionScreenTodoItem(
          id = it.uuId,
          text = it.summary,
          isCompleted = it.isChecked
        )
      }
      ReminderActionScreenTodoList(items = items)
    }
  }

  private fun getHeader(reminder: Reminder): ReminderActionScreenHeader {
    val type = reminder.readType()
    return when {
      type.hasLinkAction() -> {
        ReminderActionScreenHeader.OpenLink(
          text = reminder.summary,
          url = reminder.target
        )
      }
      type.hasApplicationAction() -> {
        ReminderActionScreenHeader.OpenApplication(
          text = reminder.summary,
          appName = packageManagerWrapper.getApplicationName(reminder.target),
          appIcon = packageManagerWrapper.getAppInfo(reminder.target).loadIcon(packageManagerWrapper.packageManager)
        )
      }
      type.hasEmailAction() -> {
        val contactId = contactsReader.getIdFromMail(reminder.target)
        ReminderActionScreenHeader.SendEmail(
          text = reminder.summary,
          emailAddress = reminder.target,
          contactName = contactsReader.getNameFromMail(reminder.target),
          subject = reminder.subject,
          contactPhoto = contactsReader.getPhotoBitmap(contactId)
        )
      }
      type.hasCallAction() -> {
        val contactId = contactsReader.getIdFromNumber(reminder.target)
        ReminderActionScreenHeader.MakeCall(
          text = reminder.summary,
          phoneNumber = reminder.target,
          contactName = contactsReader.getNameFromNumber(reminder.target),
          contactPhoto = contactsReader.getPhotoBitmap(contactId)
        )
      }
      type.hasSmsAction() -> {
        val contactId = contactsReader.getIdFromNumber(reminder.target)
        ReminderActionScreenHeader.SendSms(
          text = reminder.summary,
          phoneNumber = reminder.target,
          contactName = contactsReader.getNameFromNumber(reminder.target),
          contactPhoto = contactsReader.getPhotoBitmap(contactId)
        )
      }
      else -> {
        ReminderActionScreenHeader.SimpleWithSummary(text = reminder.summary)
      }
    }
  }

  companion object {
    private const val TAG = "CreateReminderActionScreenStateUseCase"
    private val SUPPORTED_ACTIONS = listOf(
      ReminderAction.Complete,
      ReminderAction.Snooze,
      ReminderAction.SnoozeCustom,
      ReminderAction.MakeCall,
      ReminderAction.SendSms,
      ReminderAction.SendEmail,
      ReminderAction.OpenApp,
      ReminderAction.OpenUrl,
      ReminderAction.ShowNotification
    )
  }
}
