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
import com.github.naz013.appfunctions.note.CreateNoteParams
import com.github.naz013.appfunctions.note.CreateSimpleNoteUseCase
import com.github.naz013.appfunctions.note.NoteFunctionResult
import com.github.naz013.appfunctions.note.SearchNotesParams
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.usecase.notes.SearchNotesByTextUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Exposes note-related capabilities of the app to Gemini and other on-device assistants via the
 * AppFunctions platform API. Only available in the PRO build - see [BuildInfo.isPro].
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "NoteAppFunctionService",
  appFunctionXmlFileName = "note_app_function_service",
)
abstract class BaseNoteAppFunctionService :
  AppFunctionService(),
  KoinComponent {
  private val buildInfo: BuildInfo by inject()
  private val createSimpleNoteUseCase: CreateSimpleNoteUseCase by inject()
  private val searchNotesByTextUseCase: SearchNotesByTextUseCase by inject()
  private val analyticsEventSender: AnalyticsEventSender by inject()

  /**
   * Creates a new note.
   *
   * @param params The title and body text of the note.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun createNote(params: CreateNoteParams): NoteFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.title.isBlank() && params.content.isBlank()) {
        throw AppFunctionInvalidArgumentException("Title or content must not be blank")
      }

      val note = createSimpleNoteUseCase(title = params.title, content = params.content)

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_CREATE_NOTE))

      NoteFunctionResult(id = note.key, title = note.title, content = note.summary)
    }

  /**
   * Searches notes by title and body text.
   *
   * @param params The text to search for.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun searchNotes(params: SearchNotesParams): List<NoteFunctionResult> =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.query.isBlank()) {
        throw AppFunctionInvalidArgumentException("Query must not be blank")
      }

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_SEARCH_NOTES))

      searchNotesByTextUseCase(params.query)
        .mapNotNull { it.note }
        .map { NoteFunctionResult(id = it.key, title = it.title, content = it.summary) }
    }

  private fun requirePro() {
    if (!buildInfo.isPro) {
      throw AppFunctionPermissionRequiredException(
        "Note AppFunctions require the PRO version of the app.",
      )
    }
  }
}
