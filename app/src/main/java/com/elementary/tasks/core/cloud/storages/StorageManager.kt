package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.DispatcherProvider

class StorageManager(
  val dropbox: Dropbox,
  val gDrive: GDrive,
  val prefs: Prefs,
  val dispatcherProvider: DispatcherProvider
) {
  val dropboxBackup = dropbox.isLinked
  val googleBackup = gDrive.isLogged
}
