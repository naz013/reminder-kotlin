package com.github.naz013.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appfunctions.note.CreateNoteParams
import com.github.naz013.appfunctions.note.CreateSimpleNoteUseCase
import com.github.naz013.appfunctions.note.DeleteNoteUseCase
import com.github.naz013.appfunctions.note.NoteFunctionResult
import com.github.naz013.appfunctions.note.NoteIdParams
import com.github.naz013.appfunctions.note.SearchNotesParams
import com.github.naz013.appfunctions.note.UpdateNoteParams
import com.github.naz013.appfunctions.note.UpdateNoteUseCase
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.domain.note.displayTitle
import com.github.naz013.repository.NoteRepository
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
  private val updateNoteUseCase: UpdateNoteUseCase by inject()
  private val deleteNoteUseCase: DeleteNoteUseCase by inject()
  private val noteRepository: NoteRepository by inject()
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

      NoteFunctionResult(id = note.key, title = note.content.displayTitle(), content = note.content.text)
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

      noteRepository.searchByText(params.query)
        .mapNotNull { it.note }
        .map { NoteFunctionResult(id = it.key, title = it.content.displayTitle(), content = it.content.text) }
    }

  /**
   * Updates an existing note's title and body text.
   *
   * @param params The id of the note to update, plus its new title and content.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun updateNote(params: UpdateNoteParams): NoteFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.title.isBlank() && params.content.isBlank()) {
        throw AppFunctionInvalidArgumentException("Title or content must not be blank")
      }

      val note =
        updateNoteUseCase(id = params.id, title = params.title, content = params.content)
          ?: throw AppFunctionElementNotFoundException("No note found with id = ${params.id}")

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_UPDATE_NOTE))

      NoteFunctionResult(id = note.key, title = note.content.displayTitle(), content = note.content.text)
    }

  /**
   * Permanently deletes a note.
   *
   * @param params The id of the note to delete.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun deleteNote(params: NoteIdParams): NoteFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()

      val note =
        deleteNoteUseCase(params.id)
          ?: throw AppFunctionElementNotFoundException("No note found with id = ${params.id}")

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_DELETE_NOTE))

      NoteFunctionResult(id = note.key, title = note.content.displayTitle(), content = note.content.text)
    }

  private fun requirePro() {
    if (!buildInfo.isPro) {
      throw AppFunctionPermissionRequiredException(
        "Note AppFunctions require the PRO version of the app.",
      )
    }
  }
}
