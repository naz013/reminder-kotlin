package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class SyncOperationFactory(
  private val groupOperationFactory: GroupOperationFactory,
  private val reminderOperationFactory: ReminderOperationFactory,
  private val noteOperationFactory: NoteOperationFactory,
  private val birthdayOperationFactory: BirthdayOperationFactory,
  private val placeOperationFactory: PlaceOperationFactory,
  private val settingsOperationFactory: SettingsOperationFactory
) {

  fun create(
    indexTypes: IndexTypes,
    storage: Storage
  ): Operation {
    return when (indexTypes) {
      IndexTypes.TYPE_GROUP -> groupOperationFactory(storage)
      IndexTypes.TYPE_REMINDER -> reminderOperationFactory(storage)
      IndexTypes.TYPE_NOTE -> noteOperationFactory(storage)
      IndexTypes.TYPE_BIRTHDAY -> birthdayOperationFactory(storage)
      IndexTypes.TYPE_PLACE -> placeOperationFactory(storage)
      IndexTypes.TYPE_SETTINGS -> settingsOperationFactory(storage)
    }
  }
}
