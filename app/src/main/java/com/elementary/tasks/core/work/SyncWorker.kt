package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.IoHelper
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil

object SyncWorker {

    fun sync(context: Context, ioHelper: IoHelper, progress: (String) -> Unit, onEnd: () -> Unit) {
        launchIo {
            withUIContext { progress. invoke(context.getString(R.string.syncing_groups))}
            ioHelper.restoreGroup(true)
            val list = AppDb.getAppDatabase(context).reminderGroupDao().all()
            if (list.isEmpty()) {
                val defUiID = GroupsUtil.initDefault(context)
                val items = AppDb.getAppDatabase(context).reminderDao().all()
                for (item in items) {
                    item.groupUuId = defUiID
                }
                AppDb.getAppDatabase(context).reminderDao().insertAll(items)
            }
            ioHelper.backupGroup()

            withUIContext { progress. invoke(context.getString(R.string.syncing_reminders))}
            ioHelper.restoreReminder(true)
            ioHelper.backupReminder()

            withUIContext { progress. invoke(context.getString(R.string.syncing_notes))}
            ioHelper.restoreNote(true)
            ioHelper.backupNote()

            withUIContext { progress. invoke(context.getString(R.string.syncing_birthdays))}
            ioHelper.restoreBirthday(true)
            ioHelper.backupBirthday()

            withUIContext { progress. invoke(context.getString(R.string.syncing_places))}
            ioHelper.restorePlaces(true)
            ioHelper.backupPlaces()

            withUIContext { progress. invoke(context.getString(R.string.syncing_templates))}
            ioHelper.restoreTemplates(true)
            ioHelper.backupTemplates()
            ioHelper.backupSettings()

            withUIContext {
                UpdatesHelper.updateWidget(context)
                UpdatesHelper.updateNotesWidget(context)
                onEnd.invoke()
            }
        }
    }
}