package com.github.naz013.logic.notificationaction.calendarevent

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleCalendarEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleCalendarEventActionProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val alertHandlerFactory: GoogleCalendarEventAlertHandlerFactory,
  private val cancelActionFactory: GoogleCalendarEventCancelActionFactory,
  private val googleCalendarEventRepository: GoogleCalendarEventRepository,
) {
  private val scope = CoroutineScope(dispatcherProvider.default())

  fun process(id: String) {
    Logger.i(TAG, "Going to process Google Calendar event: $id")
    scope.launch {
      val event = googleCalendarEventRepository.getById(id) ?: return@launch
      if (event.isDismissed) {
        Logger.i(TAG, "Google Calendar event was dismissed from the app, skipping: $id")
        return@launch
      }
      withContext(dispatcherProvider.main()) {
        alertHandlerFactory.create().handle(event)
      }
    }
  }

  fun cancel(id: String) {
    Logger.i(TAG, "Cancelling Google Calendar event notification: $id")
    scope.launch {
      val event = googleCalendarEventRepository.getById(id) ?: return@launch
      cancelActionFactory.createCancel().handle(event)
    }
  }

  companion object {
    private const val TAG = "GoogleCalendarEventActionProcessor"
  }
}
