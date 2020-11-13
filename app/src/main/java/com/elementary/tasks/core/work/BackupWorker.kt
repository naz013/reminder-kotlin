package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.BirthdayConverter
import com.elementary.tasks.core.cloud.converters.GroupConverter
import com.elementary.tasks.core.cloud.converters.NoteConverter
import com.elementary.tasks.core.cloud.converters.PlaceConverter
import com.elementary.tasks.core.cloud.converters.ReminderConverter
import com.elementary.tasks.core.cloud.converters.SettingsConverter
import com.elementary.tasks.core.cloud.converters.TemplateConverter
import com.elementary.tasks.core.cloud.repositories.BirthdayRepository
import com.elementary.tasks.core.cloud.repositories.GroupRepository
import com.elementary.tasks.core.cloud.repositories.NoteRepository
import com.elementary.tasks.core.cloud.repositories.PlaceRepository
import com.elementary.tasks.core.cloud.repositories.ReminderRepository
import com.elementary.tasks.core.cloud.repositories.SettingsRepository
import com.elementary.tasks.core.cloud.repositories.TemplateRepository
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job
import timber.log.Timber

object BackupWorker {

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
      Timber.d("BackupWorker: $mJob")
      value?.invoke(mJob != null)
    }

  fun backup(context: Context) {
    mJob?.cancel()
    launchSync(context)
  }

  fun unsubscribe() {
    onEnd = null
    listener = null
    progress = null
  }

  private fun launchSync(context: Context) {
    val storage = CompositeStorage(DataFlow.availableStorageList(context))

    mJob = launchIo {
      notifyMsg(context.getString(R.string.syncing_groups))
      BulkDataFlow(GroupRepository(), GroupConverter(), storage, null).backup()

      notifyMsg(context.getString(R.string.syncing_reminders))
      BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, ReminderCompletable()).backup()

      notifyMsg(context.getString(R.string.syncing_notes))
      BulkDataFlow(NoteRepository(), NoteConverter(), storage, null).backup()

      notifyMsg(context.getString(R.string.syncing_birthdays))
      BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null).backup()

      notifyMsg(context.getString(R.string.syncing_places))
      BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null).backup()

      notifyMsg(context.getString(R.string.syncing_templates))
      BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null).backup()
      BulkDataFlow(SettingsRepository(), SettingsConverter(), storage, null).backup()

      withUIContext {
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