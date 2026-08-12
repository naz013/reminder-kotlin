package com.github.naz013.feature.birthday.dialog

import com.github.naz013.feature.birthday.actions.BirthdayAction
import com.github.naz013.feature.birthday.actions.BirthdayActionCategory
import com.github.naz013.feature.birthday.actions.GetBirthdayActionsUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

/**
 * Use case to create the birthday action screen state.
 *
 * Generates the complete screen state including header, actions, and contact information
 * for displaying a birthday notification.
 */
class CreateBirthdayActionScreenStateUseCase(
  private val getBirthdayActionsUseCase: GetBirthdayActionsUseCase,
  private val textProvider: TextProvider,
  private val contactsReader: ContactsReader,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val dateTimeManager: DateTimeManager,
) {
  /**
   * Creates the birthday action screen state from a birthday entity.
   *
   * @param birthday The birthday entity to create state from
   * @return The complete screen state with header and actions
   * @throws IllegalStateException if no available actions are found
   */
  operator fun invoke(birthday: Birthday): BirthdayActionScreenState {
    val availableActions =
      getBirthdayActionsUseCase(
        birthday,
        SUPPORTED_ACTIONS.toSet(),
      )

    if (availableActions.isEmpty()) {
      throw IllegalStateException("No available actions for birthday ${birthday.uuId}")
    }

    val orderedActions =
      availableActions.sortedBy { action ->
        when (action.category) {
          BirthdayActionCategory.Action -> 0
          BirthdayActionCategory.Main -> 1
          BirthdayActionCategory.Secondary -> 2
        }
      }

    Logger.i(TAG, "Creating action screen state for birthday ${birthday.uuId} with actions: $orderedActions")

    return BirthdayActionScreenState(
      id = birthday.uuId,
      header = getHeader(birthday),
      mainAction =
        orderedActions.first().let {
          BirthdayActionScreenActionItem(
            action = it,
            text = textProvider.getString(it.titleRes),
            iconRes = it.iconRes,
          )
        },
      secondaryActions =
        orderedActions.drop(1).map {
          BirthdayActionScreenActionItem(
            action = it,
            text = textProvider.getString(it.titleRes),
            iconRes = it.iconRes,
          )
        },
    )
  }

  private fun getHeader(birthday: Birthday): BirthdayActionScreenHeader {
    val phoneNumber = birthday.number
    val contactId =
      if (phoneNumber.isNotEmpty()) {
        contactsReader.getIdFromNumber(phoneNumber)
      } else {
        0L
      }

    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date)
    val birthdayDateFormatted =
      birthdayDate?.let {
        dateTimeManager.formatBirthdayDateForUi(it, birthday.ignoreYear)
      } ?: ""

    val ageFormatted =
      if (birthday.ignoreYear) {
        null
      } else {
        modelDateTimeFormatter.getAgeFormatted(birthday.date).takeIf { it.isNotEmpty() }
      }

    return BirthdayActionScreenHeader(
      text = birthday.name,
      phoneNumber = phoneNumber,
      contactName =
        if (phoneNumber.isNotEmpty()) {
          contactsReader.getNameFromNumber(phoneNumber)
        } else {
          null
        },
      contactPhoto = contactsReader.getPhotoBitmap(contactId),
      birthdayDate = birthdayDateFormatted,
      age = ageFormatted,
    )
  }

  companion object {
    private const val TAG = "CreateBirthdayActionScreenStateUseCase"
    private val SUPPORTED_ACTIONS =
      listOf(
        BirthdayAction.Ok,
        BirthdayAction.MakeCall,
        BirthdayAction.SendSms,
      )
  }
}
