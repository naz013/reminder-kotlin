package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.github.naz013.cloudapi.CloudFileApi
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
    storage: CloudFileApi,
    syncOperationType: SyncOperationType
  ): Operation {
    return when (indexTypes) {
      IndexTypes.TYPE_GROUP -> groupOperationFactory(storage, syncOperationType)
      IndexTypes.TYPE_REMINDER -> reminderOperationFactory(storage, syncOperationType)
      IndexTypes.TYPE_NOTE -> noteOperationFactory(storage, syncOperationType)
      IndexTypes.TYPE_BIRTHDAY -> birthdayOperationFactory(storage, syncOperationType)
      IndexTypes.TYPE_PLACE -> placeOperationFactory(storage, syncOperationType)
      IndexTypes.TYPE_SETTINGS -> settingsOperationFactory(storage, syncOperationType)
    }
  }
}
