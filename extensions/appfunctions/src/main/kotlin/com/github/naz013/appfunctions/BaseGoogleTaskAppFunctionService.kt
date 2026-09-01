package com.github.naz013.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionNotSupportedException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appfunctions.googletask.CompleteGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.CreateGoogleTaskParams
import com.github.naz013.appfunctions.googletask.CreateGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.DeleteGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.GoogleTaskFunctionResult
import com.github.naz013.appfunctions.googletask.GoogleTaskIdParams
import com.github.naz013.appfunctions.googletask.ListGoogleTasksParams
import com.github.naz013.appfunctions.googletask.ListGoogleTasksUseCase
import com.github.naz013.appfunctions.googletask.SearchGoogleTasksParams
import com.github.naz013.appfunctions.googletask.SearchGoogleTasksUseCase
import com.github.naz013.appfunctions.googletask.UpdateGoogleTaskParams
import com.github.naz013.appfunctions.googletask.UpdateGoogleTaskUseCase
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Exposes Google Tasks capabilities of the app to Gemini and other on-device assistants via the
 * AppFunctions platform API. Only available in the PRO build - see [BuildInfo.isPro] - and only
 * once a Google account is linked - see [GoogleTasksAuthManager.isAuthorized].
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "GoogleTaskAppFunctionService",
  appFunctionXmlFileName = "google_task_app_function_service",
)
abstract class BaseGoogleTaskAppFunctionService :
  AppFunctionService(),
  KoinComponent {
  private val buildInfo: BuildInfo by inject()
  private val dateTimeManager: DateTimeManager by inject()
  private val googleTasksAuthManager: GoogleTasksAuthManager by inject()
  private val createGoogleTaskUseCase: CreateGoogleTaskUseCase by inject()
  private val listGoogleTasksUseCase: ListGoogleTasksUseCase by inject()
  private val completeGoogleTaskUseCase: CompleteGoogleTaskUseCase by inject()
  private val updateGoogleTaskUseCase: UpdateGoogleTaskUseCase by inject()
  private val deleteGoogleTaskUseCase: DeleteGoogleTaskUseCase by inject()
  private val searchGoogleTasksUseCase: SearchGoogleTasksUseCase by inject()
  private val analyticsEventSender: AnalyticsEventSender by inject()

  /**
   * Creates a new Google Task in the user's default task list.
   *
   * @param params The title, optional notes and optional due date/time for the task.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun createGoogleTask(params: CreateGoogleTaskParams): GoogleTaskFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      requireGoogleTasksLinked()
      if (params.title.isBlank()) {
        throw AppFunctionInvalidArgumentException("Title must not be blank")
      }

      val task =
        createGoogleTaskUseCase(title = params.title, notes = params.notes, dueDateTime = params.dueDateTime)
          ?: throw AppFunctionAppUnknownException(
            "Failed to create the Google Task - check your connection and try again.",
          )

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_CREATE_GOOGLE_TASK))

      task.toFunctionResult()
    }

  /**
   * Lists Google Tasks, excluding completed ones unless [ListGoogleTasksParams.includeCompleted]
   * is set.
   *
   * @param params Whether to include already-completed tasks.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun listGoogleTasks(params: ListGoogleTasksParams): List<GoogleTaskFunctionResult> =
    withContext(Dispatchers.IO) {
      requirePro()
      requireGoogleTasksLinked()

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_LIST_GOOGLE_TASKS))

      listGoogleTasksUseCase(params.includeCompleted).map { it.toFunctionResult() }
    }

  /**
   * Marks a Google Task as complete.
   *
   * @param params The id of the task to complete.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun completeGoogleTask(params: GoogleTaskIdParams): GoogleTaskFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      requireGoogleTasksLinked()

      val task =
        completeGoogleTaskUseCase(params.id)
          ?: throw AppFunctionElementNotFoundException(
            "No Google Task found with id = ${params.id}, or the update failed - check your connection and try again.",
          )

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_COMPLETE_GOOGLE_TASK))

      task.toFunctionResult()
    }

  /**
   * Updates an existing Google Task's title, notes, and due date/time.
   *
   * @param params The id of the task to update, plus its new title, optional notes, and optional due date/time.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun updateGoogleTask(params: UpdateGoogleTaskParams): GoogleTaskFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      requireGoogleTasksLinked()
      if (params.title.isBlank()) {
        throw AppFunctionInvalidArgumentException("Title must not be blank")
      }

      val task =
        updateGoogleTaskUseCase(
          id = params.id,
          title = params.title,
          notes = params.notes,
          dueDateTime = params.dueDateTime,
        ) ?: throw AppFunctionElementNotFoundException(
          "No Google Task found with id = ${params.id}, or the update failed - check your connection and try again.",
        )

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_UPDATE_GOOGLE_TASK))

      task.toFunctionResult()
    }

  /**
   * Permanently deletes a Google Task.
   *
   * @param params The id of the task to delete.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun deleteGoogleTask(params: GoogleTaskIdParams): GoogleTaskFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      requireGoogleTasksLinked()

      val task =
        deleteGoogleTaskUseCase(params.id)
          ?: throw AppFunctionElementNotFoundException(
            "No Google Task found with id = ${params.id}, or the delete failed - check your connection and try again.",
          )

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_DELETE_GOOGLE_TASK))

      task.toFunctionResult()
    }

  /**
   * Searches for Google Tasks by title or notes text, from the locally cached list.
   *
   * @param params The text to search for.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun searchGoogleTasks(params: SearchGoogleTasksParams): List<GoogleTaskFunctionResult> =
    withContext(Dispatchers.IO) {
      requirePro()
      requireGoogleTasksLinked()
      if (params.query.isBlank()) {
        throw AppFunctionInvalidArgumentException("Query must not be blank")
      }

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_SEARCH_GOOGLE_TASKS))

      searchGoogleTasksUseCase(params.query).map { it.toFunctionResult() }
    }

  private fun requirePro() {
    if (!buildInfo.isPro) {
      throw AppFunctionPermissionRequiredException(
        "Google Task AppFunctions require the PRO version of the app.",
      )
    }
  }

  private fun requireGoogleTasksLinked() {
    if (!googleTasksAuthManager.isAuthorized()) {
      throw AppFunctionNotSupportedException(
        "Google Tasks isn't linked. Open the app's Google Tasks screen to sign in first.",
      )
    }
  }

  private fun GoogleTask.toFunctionResult(): GoogleTaskFunctionResult =
    GoogleTaskFunctionResult(
      id = taskId,
      title = title,
      notes = notes.ifBlank { null },
      dueDateTime = if (dueDate != 0L) dateTimeManager.fromMillis(dueDate).toJavaTime() else null,
      isCompleted = isCompleted(),
    )
}
