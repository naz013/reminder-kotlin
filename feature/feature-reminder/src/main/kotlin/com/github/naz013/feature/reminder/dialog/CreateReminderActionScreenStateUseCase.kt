package com.github.naz013.feature.reminder.dialog

import com.github.naz013.feature.reminder.actions.ActionCategory
import com.github.naz013.feature.reminder.actions.GetReminderActionsUseCase
import com.github.naz013.feature.reminder.actions.ReminderAction
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ReminderAction as DomainReminderAction
import com.github.naz013.logging.Logger

internal class CreateReminderActionScreenStateUseCase(
  private val getReminderActionsUseCase: GetReminderActionsUseCase,
  private val textProvider: TextProvider,
  private val contactsReader: ContactsReader,
  private val packageManagerWrapper: PackageManagerWrapper,
) {
  suspend operator fun invoke(reminder: ReminderV2): ReminderActionScreenState {
    val availableActions =
      getReminderActionsUseCase(
        reminder,
        SUPPORTED_ACTIONS.toSet(),
      )
    if (availableActions.isEmpty()) {
      throw IllegalStateException("No available actions for reminder ${reminder.uuId}")
    }
    val orderedAction =
      availableActions.sortedBy { action ->
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
      mainAction =
        orderedAction.first().let {
          ReminderActionScreenActionItem(
            action = it,
            text = textProvider.getString(it.titleRes),
            iconRes = it.iconRes,
          )
        },
      secondaryActions =
        orderedAction.drop(1).map {
          ReminderActionScreenActionItem(
            action = it,
            text = textProvider.getString(it.titleRes),
            iconRes = it.iconRes,
          )
        },
    )
  }

  private fun getTodoList(reminder: ReminderV2): ReminderActionScreenTodoList? {
    val todos = reminder.shoppingItems
    return if (todos.isEmpty()) {
      null
    } else {
      val items =
        todos.map {
          ReminderActionScreenTodoItem(
            id = it.uuId,
            text = it.summary,
            isCompleted = it.isChecked,
          )
        }
      ReminderActionScreenTodoList(items = items)
    }
  }

  private fun getHeader(reminder: ReminderV2): ReminderActionScreenHeader {
    return when (val action = reminder.action) {
      is DomainReminderAction.Link -> {
        ReminderActionScreenHeader.OpenLink(
          text = reminder.summary,
          url = action.target,
        )
      }
      is DomainReminderAction.App -> {
        ReminderActionScreenHeader.OpenApplication(
          text = reminder.summary,
          appName = packageManagerWrapper.getApplicationName(action.target),
          appIcon = packageManagerWrapper.getAppInfo(action.target).loadIcon(packageManagerWrapper.packageManager),
        )
      }
      is DomainReminderAction.Email -> {
        val contactId = contactsReader.getIdFromMail(action.target)
        ReminderActionScreenHeader.SendEmail(
          text = reminder.summary,
          emailAddress = action.target,
          contactName = contactsReader.getNameFromMail(action.target),
          subject = action.subject,
          contactPhoto = contactsReader.getPhotoBitmap(contactId),
        )
      }
      is DomainReminderAction.Call -> {
        val contactId = contactsReader.getIdFromNumber(action.target)
        ReminderActionScreenHeader.MakeCall(
          text = reminder.summary,
          phoneNumber = action.target,
          contactName = contactsReader.getNameFromNumber(action.target),
          contactPhoto = contactsReader.getPhotoBitmap(contactId),
        )
      }
      is DomainReminderAction.Sms -> {
        val contactId = contactsReader.getIdFromNumber(action.target)
        ReminderActionScreenHeader.SendSms(
          text = reminder.summary,
          phoneNumber = action.target,
          contactName = contactsReader.getNameFromNumber(action.target),
          contactPhoto = contactsReader.getPhotoBitmap(contactId),
        )
      }
      DomainReminderAction.None, DomainReminderAction.Shopping -> {
        ReminderActionScreenHeader.SimpleWithSummary(text = reminder.summary)
      }
    }
  }

  companion object {
    private const val TAG = "CreateReminderActionScreenStateUseCase"
    private val SUPPORTED_ACTIONS =
      listOf(
        ReminderAction.Complete,
        ReminderAction.Snooze,
        ReminderAction.SnoozeCustom,
        ReminderAction.MakeCall,
        ReminderAction.SendSms,
        ReminderAction.SendEmail,
        ReminderAction.OpenApp,
        ReminderAction.OpenUrl,
        ReminderAction.ShowNotification,
      )
  }
}
