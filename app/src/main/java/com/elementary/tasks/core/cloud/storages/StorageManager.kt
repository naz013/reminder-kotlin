package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.utils.Prefs

class StorageManager(
  val dropbox: Dropbox,
  val gDrive: GDrive,
  val localStorage: LocalStorage,
  val prefs: Prefs
) {
  val localBackup = prefs.localBackup
  val dropboxBackup = dropbox.isLinked
  val googleBackup = gDrive.isLogged
}