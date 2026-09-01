package com.github.naz013.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appfunctions.birthday.BirthdayFunctionResult
import com.github.naz013.appfunctions.birthday.BirthdayIdParams
import com.github.naz013.appfunctions.birthday.CreateBirthdayParams
import com.github.naz013.appfunctions.birthday.CreateSimpleBirthdayUseCase
import com.github.naz013.appfunctions.birthday.DeleteBirthdayUseCase
import com.github.naz013.appfunctions.birthday.ListUpcomingBirthdaysParams
import com.github.naz013.appfunctions.birthday.ListUpcomingBirthdaysUseCase
import com.github.naz013.appfunctions.birthday.SearchBirthdaysParams
import com.github.naz013.appfunctions.birthday.SearchBirthdaysUseCase
import com.github.naz013.appfunctions.birthday.UpdateBirthdayParams
import com.github.naz013.appfunctions.birthday.UpdateBirthdayUseCase
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Exposes birthday-related capabilities of the app to Gemini and other on-device assistants via
 * the AppFunctions platform API. Only available in the PRO build - see [BuildInfo.isPro].
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "BirthdayAppFunctionService",
  appFunctionXmlFileName = "birthday_app_function_service",
)
abstract class BaseBirthdayAppFunctionService :
  AppFunctionService(),
  KoinComponent {
  private val buildInfo: BuildInfo by inject()
  private val dateTimeManager: DateTimeManager by inject()
  private val createSimpleBirthdayUseCase: CreateSimpleBirthdayUseCase by inject()
  private val listUpcomingBirthdaysUseCase: ListUpcomingBirthdaysUseCase by inject()
  private val updateBirthdayUseCase: UpdateBirthdayUseCase by inject()
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase by inject()
  private val searchBirthdaysUseCase: SearchBirthdaysUseCase by inject()
  private val analyticsEventSender: AnalyticsEventSender by inject()

  /**
   * Creates a new birthday.
   *
   * @param params The name and date of birth, and whether the birth year should be hidden.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun createBirthday(params: CreateBirthdayParams): BirthdayFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.name.isBlank()) {
        throw AppFunctionInvalidArgumentException("Name must not be blank")
      }

      val birthday =
        createSimpleBirthdayUseCase(
          name = params.name,
          date = params.date,
          ignoreYear = params.ignoreYear,
        )

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_CREATE_BIRTHDAY))

      BirthdayFunctionResult(id = birthday.uuId, name = birthday.name, date = params.date)
    }

  /**
   * Lists birthdays coming up within the next [ListUpcomingBirthdaysParams.withinDays] days,
   * soonest first.
   *
   * @param params How many days from now to look for upcoming birthdays in.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun listUpcomingBirthdays(params: ListUpcomingBirthdaysParams): List<BirthdayFunctionResult> =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.withinDays < 0) {
        throw AppFunctionInvalidArgumentException("withinDays must not be negative")
      }

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_LIST_BIRTHDAYS))

      listUpcomingBirthdaysUseCase(params.withinDays).mapNotNull { it.toFunctionResult() }
    }

  /**
   * Updates an existing birthday's name, date, and year-visibility.
   *
   * @param params The id of the birthday to update, plus its new name, date, and whether to hide the year.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun updateBirthday(params: UpdateBirthdayParams): BirthdayFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.name.isBlank()) {
        throw AppFunctionInvalidArgumentException("Name must not be blank")
      }

      val birthday =
        updateBirthdayUseCase(
          id = params.id,
          name = params.name,
          date = params.date,
          ignoreYear = params.ignoreYear,
        ) ?: throw AppFunctionElementNotFoundException("No birthday found with id = ${params.id}")

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_UPDATE_BIRTHDAY))

      BirthdayFunctionResult(id = birthday.uuId, name = birthday.name, date = params.date)
    }

  /**
   * Permanently deletes a birthday.
   *
   * @param params The id of the birthday to delete.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun deleteBirthday(params: BirthdayIdParams): BirthdayFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()

      val birthday =
        deleteBirthdayUseCase(params.id)
          ?: throw AppFunctionElementNotFoundException("No birthday found with id = ${params.id}")

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_DELETE_BIRTHDAY))

      birthday.toFunctionResult()
        ?: throw AppFunctionAppUnknownException(
          "Birthday with id = ${params.id} was deleted, but its stored date could not be parsed.",
        )
    }

  /**
   * Searches for birthdays by name.
   *
   * @param params The name text to search for.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun searchBirthdays(params: SearchBirthdaysParams): List<BirthdayFunctionResult> =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.query.isBlank()) {
        throw AppFunctionInvalidArgumentException("Query must not be blank")
      }

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_SEARCH_BIRTHDAYS))

      searchBirthdaysUseCase(params.query).mapNotNull { it.toFunctionResult() }
    }

  private fun requirePro() {
    if (!buildInfo.isPro) {
      throw AppFunctionPermissionRequiredException(
        "Birthday AppFunctions require the PRO version of the app.",
      )
    }
  }

  private fun Birthday.toFunctionResult(): BirthdayFunctionResult? {
    val birthDate = dateTimeManager.parseBirthdayDate(date) ?: return null
    return BirthdayFunctionResult(id = uuId, name = name, date = birthDate.toJavaTime())
  }
}
