package com.github.naz013.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appfunctions.birthday.BirthdayFunctionResult
import com.github.naz013.appfunctions.birthday.CreateBirthdayParams
import com.github.naz013.appfunctions.birthday.CreateSimpleBirthdayUseCase
import com.github.naz013.appfunctions.birthday.ListUpcomingBirthdaysParams
import com.github.naz013.appfunctions.birthday.ListUpcomingBirthdaysUseCase
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.system.BuildInfo
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
