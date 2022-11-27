package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.LifecycleOwner
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.view_models.DispatcherProvider

class NotesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  workManagerProvider: WorkManagerProvider
) : BaseNotesViewModel(
  appDb,
  prefs,
  calendarUtils,
  eventControlFactory,
  dispatcherProvider,
  workManagerProvider
) {
  val notes = appDb.notesDao().loadAll()

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    analyticsEventSender.send(FeatureUsedEvent(Feature.NOTE))
  }
}
