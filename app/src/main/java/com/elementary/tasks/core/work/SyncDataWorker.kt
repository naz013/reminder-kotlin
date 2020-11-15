package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil

class SyncDataWorker(
  private val syncManagers: SyncManagers,
  private val prefs: Prefs,
  private val appDb: AppDb,
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    if (prefs.autoSyncState == 0) {
      return Result.success()
    }
    val storage = CompositeStorage(syncManagers.storageManager)
    val syncFlags = prefs.autoSyncFlags
    launchDefault {
      if (syncFlags.contains(FLAG_REMINDER)) {
        val groupRepository = syncManagers.repositoryManager.groupRepository
        val groupConverter = syncManagers.converterManager.groupConverter
        BulkDataFlow(groupRepository, groupConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_GROUP, deleteFile = true)
        val list = appDb.reminderGroupDao().all()
        if (list.isEmpty()) {
          val defUiID = GroupsUtil.initDefault(applicationContext)
          val items = appDb.reminderDao().all()
          for (item in items) {
            item.groupUuId = defUiID
          }
          appDb.reminderDao().insertAll(items)
        }
        BulkDataFlow(groupRepository, groupConverter, storage, completable = null).backup()

        val reminderRepository = syncManagers.repositoryManager.reminderRepository
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
        val noteRepository = syncManagers.repositoryManager.noteRepository
        val noteConverter = syncManagers.converterManager.noteConverter
        BulkDataFlow(noteRepository, noteConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_NOTE, deleteFile = true)
        BulkDataFlow(noteRepository, noteConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_BIRTHDAY)) {
        val birthdayRepository = syncManagers.repositoryManager.birthdayRepository
        val birthdayConverter = syncManagers.converterManager.birthdayConverter
        BulkDataFlow(birthdayRepository, birthdayConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_BIRTHDAY, deleteFile = true)
        BulkDataFlow(birthdayRepository, birthdayConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_PLACE)) {
        val placeRepository = syncManagers.repositoryManager.placeRepository
        val placeConverter = syncManagers.converterManager.placeConverter
        BulkDataFlow(placeRepository, placeConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_PLACE, deleteFile = true)
        BulkDataFlow(placeRepository, placeConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_TEMPLATE)) {
        val templateRepository = syncManagers.repositoryManager.templateRepository
        val templateConverter = syncManagers.converterManager.templateConverter
        BulkDataFlow(templateRepository, templateConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_TEMPLATE, deleteFile = true)
        BulkDataFlow(templateRepository, templateConverter, storage, completable = null).backup()
      }

      if (syncFlags.contains(FLAG_SETTINGS)) {
        val settingsRepository = syncManagers.repositoryManager.settingsRepository
        val settingsConverter = syncManagers.converterManager.settingsConverter
        BulkDataFlow(settingsRepository, settingsConverter, storage, completable = null)
          .restore(IndexTypes.TYPE_SETTINGS, deleteFile = true)
        BulkDataFlow(settingsRepository, settingsConverter, storage, completable = null).backup()
      }

      withUIContext {
        UpdatesHelper.updateWidget(applicationContext)
        UpdatesHelper.updateNotesWidget(applicationContext)
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
    const val FLAG_TEMPLATE = "flag.template"
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