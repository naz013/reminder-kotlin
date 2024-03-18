package com.elementary.tasks.core.cloud.storages

class StorageManager(
  private val dropbox: Dropbox,
  private val gDrive: GDrive
) {

  fun availableStorageList(): List<Storage> {
    return listOfNotNull(
      gDrive.takeIf { gDrive.isLogged },
      dropbox.takeIf { dropbox.isLinked }
    )
  }
}
