package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil
import kotlinx.coroutines.withContext

class SyncDataWorker(
  private val syncManagers: SyncManagers,
  private val prefs: Prefs,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao,
  context: Context,
  workerParams: WorkerParameters,
  private val updatesHelper: UpdatesHelper,
  private val groupsUtil: GroupsUtil,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    if (prefs.autoSyncState == 0) {
      return Result.success()
    }
    val storage = CompositeStorage(syncManagers.storageManager)
    val syncFlags = prefs.autoSyncFlags
    withContext(dispatcherProvider.default()) {
      if (syncFlags.contains(FLAG_REMINDER)) {
        val groupRepository = syncManagers.repositoryManager.groupDataFlowRepository
        val groupConverter = syncManagers.converterManager.groupConverter
        BulkDataFlow(groupRepository, groupConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_GROUP, deleteFile = true)
        val list = reminderGroupDao.all()
        if (list.isEmpty()) {
          val defUiID = groupsUtil.initDefault()
          val items = reminderDao.getAll()
          for (item in items) {
            item.groupUuId = defUiID
          }
          reminderDao.insertAll(items)
        }
        BulkDataFlow(groupRepository, groupConverter, storage, completable = null).backup()

        val reminderRepository = syncManagers.repositoryManager.reminderDataFlowRepository
        val reminderConverter = syncManagers.converterManager.reminderConverter
        BulkDataFlow(
          reminderRepository,
          reminderConverter,
          storage,
          syncManagers.completableManager.reminderCompletable
        ).restore(IndexTypes.TYPE_REMINDER, deleteFile = true)
        BulkDataFlow(reminderRepository, reminderConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_NOTE)) {
        val noteRepository = syncManagers.repositoryManager.noteDataFlowRepository
        val noteConverter = syncManagers.converterManager.noteConverter
        BulkDataFlow(noteRepository, noteConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_NOTE, deleteFile = true)
        BulkDataFlow(noteRepository, noteConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_BIRTHDAY)) {
        val birthdayRepository = syncManagers.repositoryManager.birthdayDataFlowRepository
        val birthdayConverter = syncManagers.converterManager.birthdayConverter
        BulkDataFlow(birthdayRepository, birthdayConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_BIRTHDAY, deleteFile = true)
        BulkDataFlow(birthdayRepository, birthdayConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_PLACE)) {
        val placeRepository = syncManagers.repositoryManager.placeDataFlowRepository
        val placeConverter = syncManagers.converterManager.placeConverter
        BulkDataFlow(placeRepository, placeConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_PLACE, deleteFile = true)
        BulkDataFlow(placeRepository, placeConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_SETTINGS)) {
        val settingsRepository = syncManagers.repositoryManager.settingsDataFlowRepository
        val settingsConverter = syncManagers.converterManager.settingsConverter
        BulkDataFlow(settingsRepository, settingsConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_SETTINGS, deleteFile = true)
        BulkDataFlow(settingsRepository, settingsConverter, storage, completable = null).backup()
      }

      withUIContext {
        updatesHelper.updateWidgets()
        updatesHelper.updateNotesWidget()
      }
    }
    return Result.success()
  }

  companion object {
    private const val TAG = "SyncDataWorker"
    const val FLAG_REMINDER = "flag.reminder"
    const val FLAG_NOTE = "flag.note"
    const val FLAG_BIRTHDAY = "flag.birthday"
    const val FLAG_PLACE = "flag.place"
    const val FLAG_SETTINGS = "flag.settings"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
        .addTag(TAG)
        .setConstraints(Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED)
          .setRequiresBatteryNotLow(true)
          .build())
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}