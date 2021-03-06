package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil
import kotlinx.coroutines.Job

class SyncWorker(
  private val syncManagers: SyncManagers,
  private val context: Context,
  private val appDb: AppDb
) {

  private var mJob: Job? = null
  private var mLastMsg: String? = null
  var onEnd: (() -> Unit)? = null
  var progress: ((String) -> Unit)? = null
    set(value) {
      field = value
      val msg = mLastMsg ?: return
      if (mJob != null) {
        value?.invoke(msg)
      }
    }
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      value?.invoke(mJob != null)
    }

  fun sync() {
    mJob?.cancel()
    launchSync()
  }

  fun unsubscribe() {
    onEnd = null
    listener = null
    progress = null
  }

  private fun launchSync() {
    val storage = CompositeStorage(syncManagers.storageManager)
    mJob = launchIo {
      notifyMsg(context.getString(R.string.syncing_groups))
      val groupRepository = syncManagers.repositoryManager.groupRepository
      val groupConverter = syncManagers.converterManager.groupConverter
      BulkDataFlow(groupRepository, groupConverter, storage, completable = null)
        .restore(IndexTypes.TYPE_GROUP, deleteFile = true)
      val list = appDb.reminderGroupDao().all()
      if (list.isEmpty()) {
        val defUiID = GroupsUtil.initDefault(context, appDb)
        val items = appDb.reminderDao().all()
        for (item in items) {
          item.groupUuId = defUiID
        }
        appDb.reminderDao().insertAll(items)
      }
      BulkDataFlow(groupRepository, groupConverter, storage, completable = null).backup()

      notifyMsg(context.getString(R.string.syncing_reminders))
      val reminderRepository = syncManagers.repositoryManager.reminderRepository
      val reminderConverter = syncManagers.converterManager.reminderConverter
      BulkDataFlow(
        reminderRepository,
        reminderConverter,
        storage,
        syncManagers.completableManager.reminderCompletable
      ).restore(IndexTypes.TYPE_REMINDER, deleteFile = true)
      BulkDataFlow(reminderRepository, reminderConverter, storage, completable = null).backup()

      notifyMsg(context.getString(R.string.syncing_notes))
      val noteRepository = syncManagers.repositoryManager.noteRepository
      val noteConverter = syncManagers.converterManager.noteConverter
      BulkDataFlow(noteRepository, noteConverter, storage, completable = null)
        .restore(IndexTypes.TYPE_NOTE, deleteFile = true)
      BulkDataFlow(noteRepository, noteConverter, storage, completable = null).backup()

      notifyMsg(context.getString(R.string.syncing_birthdays))
      val birthdayRepository = syncManagers.repositoryManager.birthdayRepository
      val birthdayConverter = syncManagers.converterManager.birthdayConverter
      BulkDataFlow(birthdayRepository, birthdayConverter, storage, completable = null)
        .restore(IndexTypes.TYPE_BIRTHDAY, deleteFile = true)
      BulkDataFlow(birthdayRepository, birthdayConverter, storage, completable = null).backup()

      notifyMsg(context.getString(R.string.syncing_places))
      val placeRepository = syncManagers.repositoryManager.placeRepository
      val placeConverter = syncManagers.converterManager.placeConverter
      BulkDataFlow(placeRepository, placeConverter, storage, completable = null)
        .restore(IndexTypes.TYPE_PLACE, deleteFile = true)
      BulkDataFlow(placeRepository, placeConverter, storage, completable = null).backup()

      notifyMsg(context.getString(R.string.syncing_templates))
      val templateRepository = syncManagers.repositoryManager.templateRepository
      val templateConverter = syncManagers.converterManager.templateConverter
      BulkDataFlow(templateRepository, templateConverter, storage, completable = null)
        .restore(IndexTypes.TYPE_TEMPLATE, deleteFile = true)
      BulkDataFlow(templateRepository, templateConverter, storage, completable = null).backup()

      BulkDataFlow(
        syncManagers.repositoryManager.settingsRepository,
        syncManagers.converterManager.settingsConverter,
        storage,
        completable = null
      ).backup()

      withUIContext {
        UpdatesHelper.updateWidget(context)
        UpdatesHelper.updateNotesWidget(context)
        onEnd?.invoke()
      }
      mJob = null
    }
  }

  private suspend fun notifyMsg(msg: String) {
    mLastMsg = msg
    withUIContext { progress?.invoke(msg) }
  }
}